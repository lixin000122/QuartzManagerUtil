import com.ftp.schedule.demo.quartz.listener.StdJobListener;
import com.ftp.schedule.demo.quartz.listener.StdTriggerListener;
import com.sun.istack.internal.NotNull;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.matchers.KeyMatcher;

import java.util.List;
import java.util.Set;

import static org.quartz.CronScheduleBuilder.cronSchedule;

/**
 * <p>调度管理器工具类
 *
 * <p>使用Quartz框架的工具类<br>
 * 需要引入quartz及c3p0依赖<br>
 * 使用quartz.properties进行配置
 *
 * @version 1.0.0
 */
public class QuartzManagerUtil {

    /**
     * 调度管理器实例使用的调度器
     */
    private static Scheduler scheduler;

    /*
     * 初始化实例时导入默认调度器
     */
    static {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取调度管理器实例使用的调度器
     *
     * @return Scheduler实例
     * @throws Exception    自定义异常
     */
    public Scheduler getScheduler() throws Exception {
        if (scheduler == null){
            try {
                this.setScheduler();
            } catch (SchedulerException e) {
                e.printStackTrace();
                throw new Exception("调度器初始化失败");
            }
        }
        return scheduler;
    }

    /**
     * 从标准调度器工厂类获取默认调度器
     *
     * @throws Exception 自定义异常
     */
    public void setScheduler() throws Exception {
        //从标准调度器工厂类获取默认调度器
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new Exception("获取默认调度器失败");
        }
    }

    /**
     * 创建JobDetail实例
     *
     * @param jobClass 任务实际运行的类
     * @param jobKey   任务JobKey
     * @return JobDetail   创建完成的JobDetail实例
     */
    public JobDetail createJob(Class<? extends Job> jobClass, JobKey jobKey) {
        //创建JobDetail实例
        JobDetail jobDetail;
        jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .build();

        return jobDetail;
    }

    /**
     * 创建JobDetail实例
     *
     * @param jobClass 任务实际运行的类
     * @param name     任务JobKey.name
     * @param group    任务JobKey.group
     * @return 创建完成的JobDetail实例
     */
    public JobDetail createJob(Class<? extends Job> jobClass, String name, String group) {
        //创建JobKey实例
        JobKey jobKey = new JobKey(name, group);
        //创建JobDetail实例
        return this.createJob(jobClass, jobKey);
    }

    /**
     * 创建Trigger实例
     *
     * @param triggerKey 触发器TriggerKey
     * @param cron       触发器cron表达式
     * @return 创建完成的Trigger实例
     */
    public Trigger createTrigger(TriggerKey triggerKey, String cron) {
        //创建TriggerBuilder实例
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startNow();//从调度器启动开始运行，防止出现恢复时多次重复调用Job实例的错误
        //导入cron表达式
        triggerBuilder.withSchedule(cronSchedule(cron));
        //创建Trigger实例
        return triggerBuilder.build();
    }

    /**
     * 创建Trigger实例
     *
     * @param name  触发器TriggerKey.name
     * @param group 触发器TriggerKey.group
     * @param cron  触发器cron表达式
     * @return 创建完成的Trigger实例
     */
    public Trigger createTrigger(String name, String group, String cron) {
        //创建TriggerKey
        TriggerKey triggerKey = new TriggerKey(name, group);
        //创建Trigger实例
        return this.createTrigger(triggerKey, cron);
    }

    /**
     * 获取已经注册的任务中JobKey与传入的JobKey相同的触发器列表
     *
     * @param jobKey 需要寻找的JobKey
     * @return List<? extends Trigger> 触发器列表
     * @throws Exception 自定义异常
     */
    public List<? extends Trigger> getTriggersByJobKey(JobKey jobKey) throws Exception {
        try {
            return scheduler.getTriggersOfJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new Exception("获取触发器列表失败");
        }
    }

    /**
     * 获取所有Job对象的Key
     *
     * @return Set<JobKey> 含有所有Job对象的集合
     * @throws Exception 自定义异常
     */
    public Set<JobKey> getAllJobs() throws Exception {
        Set<JobKey> jobKeySet;
        try {
            jobKeySet = scheduler.getJobKeys(GroupMatcher.anyGroup());
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new Exception("获取Job集合失败");
        }

        return jobKeySet;
    }

    /**
     * 获取所有Trigger对象的Key
     *
     * @return Set<TriggerKey> 含有所有Trigger对象的集合
     * @throws Exception 自定义异常
     */
    public Set<TriggerKey> getAllTriggers() throws Exception {
        Set<TriggerKey> triggerKeySet;
        try {
            triggerKeySet = scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new Exception("获取Trigger集合失败");
        }

        return triggerKeySet;
    }

    /**
     * 向调度器中注册任务
     *
     * @param jobDetail 任务jobDetail实例
     * @param trigger   触发器trigger实例
     * @throws Exception 自定义异常
     */
    public void registerTask(JobDetail jobDetail, Trigger trigger) throws Exception {
        //获取调度管理器实例使用的调度器
        Scheduler scheduler = this.getScheduler();
        //获取需要注册的任务的JobKey
        JobKey jobKey = jobDetail.getKey();
        //检查该任务是否已经注册(避免重启时出现数据库表的键重复的错误)

        List<? extends Trigger> triggers;
        try {
            triggers = this.getTriggersByJobKey(jobKey);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("获取调度器中任务失败");
        }
        boolean flag_already_register = false;
        if (triggers.size() > 0) {
            for (Trigger tg : triggers) {
                if (tg.equals(trigger)) {
                    flag_already_register = true;
                    break;
                }
            }
        }
        /*
         * 如果该任务(包括使用相同JobDetail但使用不同Trigger的情况)未被注册，那么向调度器中注册此任务
         * 否则抛出异常
         */
        if (flag_already_register) {
            try {
                System.out.println("已存在同名任务，正在尝试重启");
                System.out.println("任务组:" + jobKey.getGroup() + "任务名:" + jobKey.getName());
                System.out.println("触发组:" + trigger.getKey().getGroup() + "触发名:" + trigger.getKey().getName());
                scheduler.resumeJob(jobKey);
            } catch (SchedulerException e) {
                e.printStackTrace();
                throw new Exception("任务已被注册，并且重启该任务时失败");
            }
        } else {
            //向调度器中注册此任务
            try {
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException e) {
                e.printStackTrace();
                throw new Exception("任务注册失败");
            }
        }
    }

    /**
     * 向调度器中注册任务
     *
     * @param jobDetail     任务jobDetail实例
     * @param triggerKey    触发器triggerKey
     * @param cron          触发表达式
     * @throws Exception    自定义异常
     */
    public void registerTask(JobDetail jobDetail, TriggerKey triggerKey, String cron) throws Exception {
        Trigger trigger=this.createTrigger(triggerKey, cron);
        this.registerTask(jobDetail, trigger);
    }

    /**
     * 向调度器中注册任务
     *
     * @param jobDetail     任务jobDetail实例
     * @param triggerName   触发器triggerName
     * @param triggerGroup  触发器triggerGroup
     * @param cron          触发表达式
     * @throws Exception    自定义异常
     */
    public void registerTask(JobDetail jobDetail, String triggerName, String triggerGroup, String cron) throws Exception {
        Trigger trigger = this.createTrigger(triggerName, triggerGroup, cron);
        this.registerTask(jobDetail, trigger);
    }

    /**
     * 向调度器中注册任务
     *
     * @param jobClass      任务实际运行的类
     * @param jobKey        任务JobKey
     * @param trigger       触发器Trigger
     * @throws Exception    自定义异常
     */
    public void registerTask(Class<? extends Job> jobClass, JobKey jobKey, Trigger trigger) throws Exception{
        JobDetail jobDetail = this.createJob(jobClass, jobKey);
        this.registerTask(jobDetail, trigger);
    }

    /**
     * 向调度器中注册任务
     *
     * @param jobClass      任务实际运行的类
     * @param jobKey        任务JobKey
     * @param triggerKey    触发器triggerKey
     * @param cron          触发表达式
     * @throws Exception    自定义异常
     */
    public void registerTask(Class<? extends Job> jobClass, JobKey jobKey, TriggerKey triggerKey, String cron) throws Exception {
        JobDetail jobDetail = this.createJob(jobClass, jobKey);
        Trigger trigger=this.createTrigger(triggerKey, cron);
        this.registerTask(jobDetail, trigger);
    }

    /**
     * 向调度器中注册任务
     *
     * @param jobClass      任务实际运行的类
     * @param jobKey        任务JobKey
     * @param triggerName   触发器triggerName
     * @param triggerGroup  触发器triggerGroup
     * @param cron          触发表达式
     * @throws Exception    自定义异常
     */
    public void registerTask(Class<? extends Job> jobClass, JobKey jobKey, String triggerName, String triggerGroup, String cron) throws Exception {
        JobDetail jobDetail = this.createJob(jobClass, jobKey);
        Trigger trigger=this.createTrigger(triggerName,triggerGroup, cron);
        this.registerTask(jobDetail, trigger);
    }

    /**
     * 向调度器中注册任务
     *
     * @param jobClass      任务实际运行的类
     * @param jobName       任务JobKey.name
     * @param jobGroup      任务JobKey.group
     * @param trigger       触发器Trigger
     * @throws Exception    自定义异常
     */
    public void registerTask(Class<? extends Job> jobClass, String jobName, String jobGroup, Trigger trigger) throws Exception {
        JobDetail jobDetail = this.createJob(jobClass, jobName, jobGroup);
        this.registerTask(jobDetail, trigger);
    }

    /**
     * 向调度器中注册任务
     *
     * @param jobClass      任务实际运行的类
     * @param jobName       任务JobKey.name
     * @param jobGroup      任务JobKey.group
     * @param triggerKey    触发器triggerKey
     * @param cron          触发表达式
     * @throws Exception    自定义异常
     */
    public void registerTask(Class<? extends Job> jobClass, String jobName, String jobGroup, TriggerKey triggerKey, String cron) throws Exception {
        JobDetail jobDetail = this.createJob(jobClass, jobName, jobGroup);
        Trigger trigger=this.createTrigger(triggerKey, cron);
        this.registerTask(jobDetail, trigger);
    }

    /**
     * 向调度器中注册任务
     *
     * @param jobClass      任务实际运行的类
     * @param jobName       任务JobKey.name
     * @param jobGroup      任务JobKey.group
     * @param triggerName   触发器triggerName
     * @param triggerGroup  触发器triggerGroup
     * @param cron          触发表达式
     * @throws Exception    自定义异常
     */
    public void registerTask(Class<? extends Job> jobClass, String jobName, String jobGroup, String triggerName, String triggerGroup, String cron) throws Exception {
        JobDetail jobDetail = this.createJob(jobClass, jobName, jobGroup);
        Trigger trigger = this.createTrigger(triggerName, triggerGroup, cron);
        this.registerTask(jobDetail, trigger);
    }

    /**
     * 从调度器中删除任务
     *
     * @param triggerKey 触发器TriggerKey
     * @throws Exception 自定义异常
     */
    public void deleteTask(TriggerKey triggerKey) throws Exception {
        try {
            scheduler.unscheduleJob(triggerKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new Exception("删除任务失败");
        }
    }

    /**
     * 从调度器中删除任务
     *
     * @param trigger 触发器TriggerKey
     * @throws Exception 自定义异常
     */
    public void deleteTask(Trigger trigger) throws Exception {
        this.deleteTask(trigger.getKey());
    }

    /**
     * 从调度器中删除任务
     *
     * @param triggerName   触发器TriggerKey.name
     * @param triggerGroup  触发器TriggerKey.group
     * @throws Exception    自定义异常
     */
    public void deleteTask(String triggerName, String triggerGroup) throws Exception {
        TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroup);
        this.deleteTask(triggerKey);
    }

    /**
     * 从调度器中删除使用同一Job实例的任务
     *
     * @param jobKey 需要删除的JobKey
     * @throws Exception 自定义异常
     */
    public void deleteJob(JobKey jobKey) throws Exception {
        List<? extends Trigger> triggers = this.getTriggersByJobKey(jobKey);
        for (Trigger tg : triggers) {
            this.deleteTask(tg);
        }
    }

    /**
     * 从调度器中删除使用同一Job实例的任务
     *
     * @param jobDetail 需要删除的JobDetail
     * @throws SchedulerException 调度器异常
     */
    public void deleteJob(JobDetail jobDetail) throws Exception {
        this.deleteJob(jobDetail.getKey());
    }

    /**
     * 从调度器中删除使用同一Job实例的任务
     *
     * @throws SchedulerException 调度器异常
     */
    public void deleteJob(String name, String group) throws Exception {
        JobKey jobKey = new JobKey(name, group);
        this.deleteJob(jobKey);
    }

    /**
     * 更新调度器中任务
     *
     * @param jobDetail jobDetail实例
     * @param trigger   trigger实例
     * @throws Exception    自定义异常
     */
    public  void updateTask(JobDetail jobDetail, Trigger trigger) throws Exception {
        try {
            this.deleteTask(trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new Exception("删除调度器中任务失败，可能是调度器中没有此任务");
        }
        try{
            this.registerTask(jobDetail, trigger);
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("更新任务失败");
        }

    }


    /**
     * 更新调度器中任务
     *
     * @param jobDetail     任务jobDetail实例
     * @param triggerKey    触发器triggerKey
     * @param cron          触发表达式
     * @throws Exception    自定义异常
     */
    public  void updateTask(JobDetail jobDetail, TriggerKey triggerKey, String cron) throws Exception {
        Trigger trigger = this.createTrigger(triggerKey, cron);
        this.updateTask(jobDetail, trigger);
    }

    /**
     * 更新调度器中任务
     *
     * @param jobDetail     任务jobDetail实例
     * @param triggerName   触发器triggerName
     * @param triggerGroup  触发器triggerGroup
     * @param cron          触发表达式
     * @throws Exception    自定义异常
     */
    public  void updateTask(JobDetail jobDetail, String triggerName, String triggerGroup, String cron) throws Exception {
        Trigger trigger = this.createTrigger(triggerName, triggerGroup, cron);
        this.updateTask(jobDetail, trigger);
    }

    /**
     * 更新调度器中任务
     *
     * @param jobClass      任务实际运行的类
     * @param jobKey        任务JobKey
     * @param trigger       触发器Trigger
     * @throws Exception    自定义异常
     */
    public  void updateTask(Class<? extends Job> jobClass, JobKey jobKey, Trigger trigger) throws Exception {
        JobDetail jobDetail = this.createJob(jobClass, jobKey);
        this.updateTask(jobDetail, trigger);
    }

    /**
     * 更新调度器中任务
     *
     * @param jobClass      任务实际运行的类
     * @param jobKey        任务JobKey
     * @param triggerKey    触发器triggerKey
     * @param cron          触发表达式
     * @throws Exception    自定义异常
     */
    public  void updateTask(Class<? extends Job> jobClass, JobKey jobKey, TriggerKey triggerKey, String cron) throws Exception {
        JobDetail jobDetail = this.createJob(jobClass, jobKey);
        Trigger trigger = this.createTrigger(triggerKey, cron);
        this.updateTask(jobDetail, trigger);
    }

    /**
     * 更新调度器中任务
     *
     * @param jobClass      任务实际运行的类
     * @param jobKey        任务JobKey
     * @param triggerName   触发器triggerName
     * @param triggerGroup  触发器triggerGroup
     * @param cron          触发表达式
     * @throws Exception    自定义异常
     */
    public  void updateTask(Class<? extends Job> jobClass, JobKey jobKey, String triggerName, String triggerGroup, String cron) throws Exception {
        JobDetail jobDetail = this.createJob(jobClass, jobKey);
        Trigger trigger = this.createTrigger(triggerName, triggerGroup, cron);
        this.updateTask(jobDetail, trigger);
    }

    /**
     * 更新调度器中任务
     *
     * @param jobClass      任务实际运行的类
     * @param jobName       任务JobKey.name
     * @param jobGroup      任务JobKey.group
     * @param trigger       触发器Trigger
     * @throws Exception    自定义异常
     */
    public  void updateTask(Class<? extends Job> jobClass, String jobName, String jobGroup, Trigger trigger) throws Exception {
        JobDetail jobDetail = this.createJob(jobClass, jobName, jobGroup);
        this.updateTask(jobDetail, trigger);
    }

    /**
     * 更新调度器中任务
     *
     * @param jobClass      任务实际运行的类
     * @param jobName       任务JobKey.name
     * @param jobGroup      任务JobKey.group
     * @param triggerKey    触发器triggerKey
     * @param cron          触发表达式
     * @throws Exception    自定义异常
     */
    public  void updateTask(Class<? extends Job> jobClass, String jobName, String jobGroup, TriggerKey triggerKey, String cron) throws Exception {
        JobDetail jobDetail = this.createJob(jobClass, jobName, jobGroup);
        Trigger trigger = this.createTrigger(triggerKey, cron);
        this.updateTask(jobDetail, trigger);
    }

    /**
     * 更新调度器中任务
     *
     * @param jobClass      任务实际运行的类
     * @param jobName       任务JobKey.name
     * @param jobGroup      任务JobKey.group
     * @param triggerName   触发器triggerName
     * @param triggerGroup  触发器triggerGroup
     * @param cron          触发表达式
     * @throws Exception    自定义异常
     */
    public  void updateTask(Class<? extends Job> jobClass, String jobName, String jobGroup, String triggerName, String triggerGroup, String cron) throws Exception {
        JobDetail jobDetail = this.createJob(jobClass, jobName, jobGroup);
        Trigger trigger = this.createTrigger(triggerName, triggerGroup, cron);
        this.updateTask(jobDetail, trigger);
    }

    /**
     * 向调度器中注册Job监听器
     *
     * @param jobKey JobKey
     * @throws Exception 自定义异常
     */
    public void registerStdJobDetailListener(JobKey jobKey) throws Exception {
        Matcher<JobKey> jobKeyMatcher = KeyMatcher.keyEquals(jobKey);
        try {
            scheduler.getListenerManager().addJobListener(new StdJobListener(), jobKeyMatcher);
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new Exception("注册监听器失败");
        }
    }

    /**
     * 向调度器中注册Job监听器
     *
     * @param name  JobKey.name
     * @param group JobKey.group
     * @throws Exception 自定义异常
     */
    public void registerStdJobDetailListener(String name, String group) throws Exception {
        JobKey jobKey = new JobKey(name, group);
        this.registerStdJobDetailListener(jobKey);
    }

    /**
     * 向调度器中注册Trigger监听器
     *
     * @param triggerKey TriggerKey
     * @throws Exception 自定义异常
     */
    public void registerStdTriggerListener(TriggerKey triggerKey) throws Exception {
        Matcher<TriggerKey> triggerKeyMatcher = KeyMatcher.keyEquals(triggerKey);
        try {
            scheduler.getListenerManager().addTriggerListener(new StdTriggerListener(), triggerKeyMatcher);
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new Exception("注册监听器失败");
        }
    }

    /**
     * 向调度器中注册Trigger监听器
     *
     * @param name  TriggerKey.name
     * @param group TriggerKey.group
     * @throws Exception 自定义异常
     */
    public void registerStdTriggerListener(String name, String group) throws Exception {
        TriggerKey triggerKey = new TriggerKey(name, group);
        this.registerStdTriggerListener(triggerKey);
    }

    /**
     * 启动调度器
     *
     * @throws Exception 自定义异常
     */
    public void startScheduler() throws Exception {
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new Exception("调度器启动失败");
        }
    }

    /**
     * 关闭调度器
     *
     * @throws Exception 自定义异常
     */
    public void shutdownScheduler() throws Exception {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new Exception("调度器关闭出现异常");
        }
    }

    /**
     * 清空调度器中任务
     *
     * @throws Exception   自定义异常
     */
    public void clearScheduler() throws Exception {
        Set<TriggerKey> triggerKeySet;
        triggerKeySet = this.getAllTriggers();
        for (TriggerKey tgk : triggerKeySet) {
            this.deleteTask(tgk);
        }
    }


}
