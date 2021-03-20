package com.utils.demo.Listener;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;

import java.util.Date;

/**
 * 任务监听类范例
 *
 * 必须继承JobListener类
 *
 * @version 1.0.0
 * @author lixin000122
 */
public class StdJobListener implements JobListener {

    /**
     * 获取监听器名
     *
     * @return 监听器名
     */
    @Override
    public String getName() {
        return "StdJobListener";
    }

    /**
     * 监听对象即将执行时触发
     *
     * @param jobExecutionContext   任务上下文
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
        System.out.println(new Date() + ": " + jobKey.toString() + "即将执行");
    }

    /**
     * 监听对象执行失败时触发
     *
     * @param jobExecutionContext   任务上下文
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
        System.out.println(new Date() + ": " + jobKey.toString() + "执行失败");
    }

    /**
     * 监听对象执行完毕时触发
     *
     * @param jobExecutionContext   任务上下文
     * @param e 继承SchedulerException的异常
     */
    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
        System.out.println(new Date() + ": " + jobKey.toString() + "执行完毕");
    }
}
