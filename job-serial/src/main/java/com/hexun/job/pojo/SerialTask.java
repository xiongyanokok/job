package com.hexun.job.pojo;

import java.io.Serializable;

/**
 * 串行任务实体类
 * 
 * @author xiongyan
 * @date 2017年5月16日 下午3:47:47
 */
public class SerialTask implements Comparable<SerialTask>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 所属命名空间
	 */
	private String namespace;

	/**
	 * 任务执行顺序
	 */
	private Integer taskIndex;
	
	/**
	 * 任务名称
	 */
	private String taskName;
	
	/**
	 * 任务是否打开
	 */
	private Boolean taskOpen;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public Integer getTaskIndex() {
		return taskIndex;
	}

	public void setTaskIndex(Integer taskIndex) {
		this.taskIndex = taskIndex;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Boolean getTaskOpen() {
		return taskOpen;
	}

	public void setTaskOpen(Boolean taskOpen) {
		this.taskOpen = taskOpen;
	}

	@Override
	public int compareTo(SerialTask serialTask) {
		return this.getTaskIndex().compareTo(serialTask.getTaskIndex());
	}

}
