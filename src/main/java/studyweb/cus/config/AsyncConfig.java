package studyweb.cus.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {
    @Value("${thread.core-thread-pool-size}")
    private int corePoolSize;

    @Value("${thread.max-thread-pool-size}")
    private int maxPoolSize;

    @Value("${thread.thread-queue-capacity}")
    private int queueCapacity;

    private String UpdateProgressThread = "CUS-Progress";

    @Bean(name = "uploadExecutor")
    public Executor uploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(UpdateProgressThread);
        executor.initialize();
        return executor;
    }
}
