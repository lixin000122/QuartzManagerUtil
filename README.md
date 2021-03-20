QuartzManagerUtil
=================
## 项目简介
基于spring的Quartz定时任务调度管理器工具类。
## 环境配置
* JDK 1.8
* spring boot 2.3.7.RELEASE
* IntelliJ IDEA 2020.2.3 x64
* quartz 2.3.2
## 开始使用
* 项目需要导入quartz依赖，如果使用数据库持久化定时任务则还需要导入c3p0依赖。
* 在pom.xml中加入如下依赖。
```
    <dependency>
        <groupId>org.quartz-scheduler</groupId>
        <artifactId>quartz</artifactId>
        <version>2.3.2</version>
    </dependency>
    <dependency>
        <groupId>com.mchange</groupId>
        <artifactId>c3p0</artifactId>
        <version>0.9.5.5</version>
    </dependency>
```
* 注意: spring框架的spring-boot-starter-quartz依赖所使用的quartz框架可能出现无法正常读取资源文件夹下的quartz.properties配置文件，建议使用quartz依赖。
* 在需要使用的类中import导入本工具类。
```
import com.demo.util.SchedulerManagerUtil;

...

public static void test(){
    QuartzManagerUtil quartzManagerUtil = new QuartzManagerUtil();

    ...

    
    try {
        quartzManagerUtil.startScheduler();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```
* 实例化后即可直接使用。
## 实例方法
### 基本方法
* 获取调度管理器实例使用的调度器
```
quartzManagerUtil.getScheduler()

返回Scheduler对象
```
* 此调度器为实例初始化时从标准调度器工厂(StdSchedulerFactory)中获取的默认调度器(getDefaultScheduler())，可以使用
```
quartzManagerUtil.setScheduler()
```
方法重新从标准调度器工厂中获取默认调度器
* 启动调度器方法
```
quartzManagerUtil.startScheduler()
```
* 关闭调度器方法
```
quartzManagerUtil.shutdownScheduler()
```
* 清空调度器中任务方法
```
quartzManagerUtil.clearScheduler()
```
### 定时任务方法
* 创建JobDetail实例方法(含重载方法)
```
quartzManagerUtil.createJob(Class<? extends Job> jobClass, JobKey jobKey)

返回JobDetail对象
```
### 触发器方法
* 创建Trigger实例方法(含重载方法)
```
quartzManagerUtil.createTrigger(TriggerKey triggerKey, String cron)

返回Trigger对象
```
### 调度器方法
* 向调度器中注册任务方法(含重载方法)
```
quartzManagerUtil.registerTask(JobDetail jobDetail, Trigger trigger)
```
* 从调度器中删除任务方法(含重载方法)
```
quartzManagerUtil.deleteTask(TriggerKey triggerKey)
```
* 从调度器中删除使用同一Job实例的任务方法(含重载方法)
```
quartzManagerUtil.deleteJob(JobKey jobKey)
```
* 更新调度器中任务方法(含重载方法)
```
quartzManagerUtil.updateTask(JobDetail jobDetail, Trigger trigger)
```
## 简单实例
* 复制MyJob类到项目中
* 在主函数中写入以下代码
```
        //构建工具类实例
        QuartzManagerUtil quartzManagerUtil = new QuartzManagerUtil();
        //构建定时任务实例
        JobDetail jobDetail = quartzManagerUtil.createJob(MyJob.class, "name1", "group1");
        //构建触发器实例，定时为每十秒触发一次
        Trigger trigger = quartzManagerUtil.createTrigger("trname1", "trgroup1", "0/10 * * * * ?");
        //向调度器中注册任务
        try {
            quartzManagerUtil.registerTask(jobDetail, trigger);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //启动调度器
        try {
            quartzManagerUtil.startScheduler();
        } catch (Exception e) {
            e.printStackTrace();
        }
```
* 运行项目
* 得到如下结果
```
Sat Mar 20 14:42:00 CST 2021: 定时任务正常执行
Sat Mar 20 14:42:10 CST 2021: 定时任务正常执行
Sat Mar 20 14:42:20 CST 2021: 定时任务正常执行
...
```