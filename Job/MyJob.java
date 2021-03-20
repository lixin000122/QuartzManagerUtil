package com.utils.demo.Job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

/**
 * 定时任务类范例
 *
 * 必须继承Job类
 *
 * @version 1.0.0
 * @author lixin000122
 */
public class MyJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println(new Date() + ": 定时任务正常执行");
    }
}