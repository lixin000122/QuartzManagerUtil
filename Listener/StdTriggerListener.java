package com.utils.demo.Listener;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.TriggerListener;

import java.util.Date;

/**
 * 触发器监听类范例
 *
 * 必须继承TriggerListener类
 *
 * @version 1.0.0
 * @author lixin000122
 */
public class StdTriggerListener implements TriggerListener {

    /**
     * 获取监听器名
     *
     * @return 监听器名
     */
    @Override
    public String getName() {
        return "StdTriggerListener";
    }

    /**
     * 监听对象被触发时触发
     *
     * @param trigger   触发器
     * @param jobExecutionContext   任务上下文
     */
    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext jobExecutionContext) {

        System.out.println(new Date() + ": " + trigger.getKey().toString() + " 被触发了");
    }

    /**
     * 任务资源出现异常时触发
     *
     * @param trigger   触发器
     * @param jobExecutionContext   任务上下文
     * @return  boolean vetoedExecution
     */
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jobExecutionContext) {
        boolean vetoedExecution = false;
        System.out.println(new Date() + ": " + trigger.getKey().toString() + "当前Job的相关资源准备是否出现问题: " + vetoedExecution);
        return vetoedExecution;
    }

    /**
     * 任务错过触发时间时触发
     *
     * @param trigger   触发器
     */
    @Override
    public void triggerMisfired(Trigger trigger) {
        System.out.println(new Date() + ": " + trigger.getKey().toString() + "错过了触发时间");
    }

    /**
     *  触发执行完成时触发
     *
     * @param trigger   触发器
     * @param jobExecutionContext   任务上下文
     * @param completedExecutionInstruction 完成情况
     */
    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext jobExecutionContext, Trigger.CompletedExecutionInstruction completedExecutionInstruction) {
        System.out.println(new Date() + ": " + trigger.getKey().toString() + "触发执行完成");
    }
}
