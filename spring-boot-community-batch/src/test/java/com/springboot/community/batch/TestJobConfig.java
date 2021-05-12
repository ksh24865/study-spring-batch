package com.springboot.community.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 스프링부트 배치 스타터에 미리 정의된 설정 실행
// JobBuilder, StepBuilder, JobRepository, JonLauncher 자동주입
@EnableBatchProcessing
@Configuration
public class TestJobConfig {

    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() {
        return new JobLauncherTestUtils();
    }
}
