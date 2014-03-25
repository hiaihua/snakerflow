/* Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snaker.engine.job;

import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.SnakerEngine;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.helper.StringHelper;
import org.snaker.engine.model.NodeModel;
import org.snaker.engine.model.ProcessModel;
import org.snaker.engine.model.TaskModel;

/**
 * 自动执行的job
 * @author yuqs
 * @since 1.4
 */
public class ExecutorJob extends AbstractJob {
	private static final Logger log = LoggerFactory.getLogger(ExecutorJob.class);
	
	/**
	 * 根据传递的id、参数列表，自动执行任务
	 */
	public void exec(String id, Map<String, Object> args) 
			throws JobExecutionException {
		String[] ids = id.split("-");
		if(ids.length != 3) {
			log.warn("id值不合法,执行操作被忽略.");
			return;
		}
		String processId = ids[0];
		String taskId = ids[2];
		Task task = engine.query().getTask(taskId);
		List<Task> tasks = engine.executeTask(taskId, SnakerEngine.AUTO, args);
		Process process = engine.process().getProcessById(processId);
		ProcessModel model = process.getModel();
		if(model != null && StringHelper.isNotEmpty(task.getTaskName())) {
			NodeModel node = model.getNode(task.getTaskName());
			if(node != null && node instanceof TaskModel) {
				JobCallback jobCallback = ((TaskModel)node).getJobCallback();
				callback(jobCallback, task, tasks);
			}
		}
	}
	
	/**
	 * 回调类执行
	 * @param jobCallback 回调类
	 * @param task 任务对象
	 */
	private void callback(JobCallback jobCallback, Task task, List<Task> tasks) {
		if(jobCallback == null) return;
		jobCallback.callback(task, tasks);
	}
}
