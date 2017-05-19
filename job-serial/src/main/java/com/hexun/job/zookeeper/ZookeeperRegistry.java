package com.hexun.job.zookeeper;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;

/**
 * zookeeper 注册中心
 * 
 * @author xiongyan
 * @date 2017年4月21日 上午11:21:03
 */
public class ZookeeperRegistry implements InitializingBean, DisposableBean, ApplicationListener<ContextRefreshedEvent> {

	/**
	 * 连接Zookeeper服务器的列表
	 * 包括IP地址和端口号
	 * 多个地址用逗号分隔
	 * 如: host1:2181,host2:2181
	 */
	private String serverLists;

	/**
	 * 命名空间
	 */
	private String namespace = "job/serial";

	/**
	 * 要执行任务的注册中心
	 */
	private ZookeeperRegistryCenter registryCenter;

	/**
	 * 自己的注册中心
	 */
	private ZookeeperRegistryCenter zookeeperRegistryCenter;

	/**
	 * 初始化
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(serverLists, namespace);
		zookeeperRegistryCenter = new ZookeeperRegistryCenter(zkConfig);
		zookeeperRegistryCenter.init();
	}

	/**
	 * 当spring容器初始化完成后执行该方法
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// 监听notice节点下所有子节点的变化
		zookeeperRegistryCenter.addCacheData("/notice");
		TreeCache cache = (TreeCache) zookeeperRegistryCenter.getRawCache("/notice");
		cache.getListenable().addListener(new ZookeeperEventListener(registryCenter, zookeeperRegistryCenter));
	}
	
	/**
	 * 销毁
	 */
	@Override
	public void destroy() throws Exception {
		zookeeperRegistryCenter.close();
	}

	/**
	 * 任务完成通知
	 * 
	 * @param name
	 * @param value
	 */
	public void complete(String name, String value) {
		if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
			zookeeperRegistryCenter.persist("/notice/" + name, value);
		}
	}

	public String getServerLists() {
		return serverLists;
	}

	public void setServerLists(String serverLists) {
		this.serverLists = serverLists;
	}

	public ZookeeperRegistryCenter getRegistryCenter() {
		return registryCenter;
	}

	public void setRegistryCenter(ZookeeperRegistryCenter registryCenter) {
		this.registryCenter = registryCenter;
	}

}
