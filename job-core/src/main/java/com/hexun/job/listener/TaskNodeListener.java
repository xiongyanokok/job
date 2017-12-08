package com.hexun.job.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;

/**
 * 任务节点下线监听
 * 用于报警，有些任务不知原因就下线了
 * 
 * <p>
 * &lt;bean id="taskListener" class="com.hexun.es.task.TaskNodeListener"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;property name="registryCenter" ref="registryCenter"/&gt;<br>
 * &lt;/bean&gt;
 * </p>
 * 
 * @author xiongyan
 * @date 2017年12月8日 下午12:34:38
 */
public class TaskNodeListener implements ApplicationListener<ContextRefreshedEvent> {

	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(TaskNodeListener.class);
	
	/**
	 * 注册中心
	 */
	private ZookeeperRegistryCenter registryCenter;
	
	/**
	 * 初始化
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		registryCenter.addCacheData("/");
		TreeCache cache = (TreeCache) registryCenter.getRawCache("/");
		cache.getListenable().addListener(new TreeCacheListener() {
			
			@Override
			public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
				if (event.getType().equals(TreeCacheEvent.Type.NODE_REMOVED)) {
					String path = event.getData().getPath(); 
					if (path.contains("/instances/")) {
						logger.error("任务【{}】下线通知", path);
					}
				}
			}
		});
	}

	public ZookeeperRegistryCenter getRegistryCenter() {
		return registryCenter;
	}

	public void setRegistryCenter(ZookeeperRegistryCenter registryCenter) {
		this.registryCenter = registryCenter;
	}

}
