package com.khs.chat.main;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

/*
 * @Configuration : bean 객체 등록.
 * @EnableAsync : 비동기 프로세서 사용선언.
 */
@Configuration
@EnableAsync
public class ServerAsyncConfig implements AsyncConfigurer {

    private static int TASK_CORE_POOL_SIZE = 3;                   //기본 Thread 수
    private static int TASK_MAX_POOL_SIZE = 6;                    //최대 Thread 수
    private static int TASK_QUEUE_CAPACITY = 0;                   //QUEUE 수
    private final String EXECUTOR_BEAN_NAME = "EXECUTOR_SERVER";  //Thread Bean Name

    @Resource(name = "EXECUTOR_SERVER")
    private ThreadPoolTaskExecutor executor1;

    @Bean(name = EXECUTOR_BEAN_NAME)
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(TASK_CORE_POOL_SIZE);
        executor.setMaxPoolSize(TASK_MAX_POOL_SIZE);
        executor.setQueueCapacity(TASK_QUEUE_CAPACITY);
        executor.setBeanName(EXECUTOR_BEAN_NAME);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.initialize();
        return executor;
    }

    /*
     * Thread Process 도중 에러 발생시
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    /*
     * task 생성전에 pool이 찼는지를 체크
     */
    public int checkSampleTaskExecute() {
        boolean result = true;
        System.out.println("활성 Task 수 :::: " + executor1.getActiveCount());
        if (executor1.getActiveCount() >= (TASK_MAX_POOL_SIZE + TASK_QUEUE_CAPACITY)) {
            result = false;
        }
        return executor1.getActiveCount();
    }
}
