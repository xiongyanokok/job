package com.hexun.job.zookeeper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.codehaus.jackson.type.TypeReference;

import com.dangdang.ddframe.job.lite.lifecycle.api.JobOperateAPI;
import com.dangdang.ddframe.job.lite.lifecycle.internal.operate.JobOperateAPIImpl;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Optional;
import com.hexun.job.pojo.SerialTask;
import com.hexun.job.utils.JacksonUtils;

/**
 * zookeeper 事件监听
 * 
 * @author xiongyan
 * @date 2017年4月21日 上午11:15:53
 */
public class ZookeeperEventListener implements TreeCacheListener {
	
	/**
	 * 注册中心
	 */
	private ZookeeperRegistryCenter registryCenter;
	
	/**
	 * 自己的注册中心
	 */
	private ZookeeperRegistryCenter zookeeperRegistryCenter;
	
	/**
	 * 构造方法
	 * 
	 * @param registryCenter
	 * @param nodePath
	 */
	public ZookeeperEventListener(ZookeeperRegistryCenter registryCenter, ZookeeperRegistryCenter zookeeperRegistryCenter) {
		this.registryCenter = registryCenter;
		this.zookeeperRegistryCenter = zookeeperRegistryCenter;
	}

	/**
	 * 事件
	 * 
	 * @param client
	 * @param event
	 */
	@Override
	public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
		if (event.getType().equals(TreeCacheEvent.Type.NODE_ADDED) || event.getType().equals(TreeCacheEvent.Type.NODE_UPDATED)) {
			String jobName = new String(event.getData().getData());
			String path = event.getData().getPath();
			if (StringUtils.isEmpty(jobName) || StringUtils.isEmpty(path)) {
				return;
			}
			
			// 获取串行任务数据集合
			Map<String, SerialTask> serialTaskMap = getSerialTaskMap(path);
			if (null == serialTaskMap || serialTaskMap.isEmpty()) {
				return;
			}
			
			// 获取下一个任务
			jobName = getNextJobName(serialTaskMap, jobName);
			if (StringUtils.isEmpty(jobName)) {
				return;
			}
			
			// 创建JobOperateAPI对象
			JobOperateAPI api = new JobOperateAPIImpl(registryCenter);
			// 立即触发任务
			api.trigger(Optional.of(jobName), Optional.<String>absent());
		}
	}
	
	/**
	 * 获取串行任务数据集合
	 * 
	 * @param path
	 * @return
	 */
	private Map<String, SerialTask> getSerialTaskMap(String path) {
		// 获取串行任务节点数据
		String serialData = zookeeperRegistryCenter.get(path.replace("notice", "config"));
		if (StringUtils.isEmpty(serialData)) {
			return null;
		}
		// Jackson反序列化
		List<SerialTask> serialTasks = JacksonUtils.deserialize(serialData, new TypeReference<List<SerialTask>>() {});
		if (null == serialTasks || serialTasks.isEmpty()) {
			return null;
		}
		Map<String, SerialTask> serialTaskMap = new HashMap<>();
		String key = null;
		for (SerialTask serialTask : serialTasks) {
			if (null == key) {
				key = serialTask.getNamespace() + "_" + serialTask.getTaskName();
			} else {
				serialTaskMap.put(key, serialTask);
				key = serialTask.getNamespace() + "_" + serialTask.getTaskName();
			}
		}
		return serialTaskMap;
	}
	
	/**
	 * 获取下一个任务
	 * 
	 * @param serialTaskMap
	 * @param jobName
	 * @return
	 */
	private String getNextJobName(Map<String, SerialTask> serialTaskMap, String jobName) {
		// 命名空间+任务名 确定唯一要执行的任务
		String key = registryCenter.getClient().getNamespace() + "_" + jobName;
		SerialTask serialTask = serialTaskMap.get(key);
		if (null == serialTask) {
			return null;
		}
		if (!serialTask.getTaskOpen()) {
			return getNextJobName(serialTaskMap, serialTask.getTaskName());
		} else {
			return serialTask.getTaskName();
		}
	}
	
}
