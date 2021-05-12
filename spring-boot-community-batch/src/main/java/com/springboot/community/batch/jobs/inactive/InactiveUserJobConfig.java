package com.springboot.community.batch.jobs.inactive;

import com.springboot.community.batch.domain.User;
import com.springboot.community.batch.domain.enums.UserStatus;
import com.springboot.community.batch.jobs.readers.QueueItemReader;
import com.springboot.community.batch.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.validator.SpringValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@AllArgsConstructor
@Configuration
public class InactiveUserJobConfig {

    private final static int CHUNK_SIZE = 15;
    private final EntityManagerFactory entityManagerFactory;


    @Bean
    // Bean에 주입할 객체를 파라미터로 명시하면 오토와이어 쓰는 것과 같은 효과
    public Job inactiveUserJob(JobBuilderFactory jobBuilderFactory,
                               Step inactiveJobStep){
        return jobBuilderFactory.get("inactiveUserJob") // inactiveUserJob이름의 JobBuilder 생성
                .preventRestart() // Job의 재실행 막음
                .start(inactiveJobStep) // 인자로 받은 step을 젤 먼저 실행하도록 설정
                .build();
    }
    @Bean
    public Step inactiveJopStep(StepBuilderFactory stepBuilderFactory,
                                ListItemReader<User> inactiveUserReader){
        return stepBuilderFactory.get("inactiveUserJob")
                .<User,User> chunk(CHUNK_SIZE)
                .reader(inactiveUserReader)
                .processor(inactiveUserProcessor())
                .writer(inactiveUserWriter())
                .build();
    }

    private UserRepository userRepository;
    private ListItemReader<User> inactiveUserReader() {
        List<User> oldUsers = userRepository.findByUpdatedDateBeforeAndStatusEquals(
                LocalDateTime.now().minusYears(1), UserStatus.ACTIVE);
        return new ListItemReader<>(oldUsers);
    }

    @Bean(destroyMethod = "")
    @StepScope
    public JpaPagingItemReader<User> inactiveUserJpaReader(){
        JpaPagingItemReader<User> jpaPagingItemReader = new JpaPagingItemReader<>(){
            @Override
            public int getPage(){
                return 0;
            }
        };

        //jpaPagingItemReader를 사용하려면 쿼리를 직접 작성해 실행하는 법 뿐
        jpaPagingItemReader.setQueryString(
                "select u from User as u where u.updatedDate < :updatedDate and u.status = :status");
        Map<String,Object> map = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        map.put("updatedDate", now.minusYears(1));
        map.put("status", UserStatus.ACTIVE);

        jpaPagingItemReader.setParameterValues(map);
        jpaPagingItemReader.setEntityManagerFactory(entityManagerFactory);
        jpaPagingItemReader.setPageSize(CHUNK_SIZE);
        return jpaPagingItemReader;

    }
    @Bean
    @StepScope
    public JpaPagingItemReader<User> inactiveUserReader(@Value("#{jobParameters[nowDate]}")Date nowDate,
                                                        UserRepository userRepository) {
         LocalDateTime now = LocalDateTime.ofInstant(nowDate.toInstant(), ZoneId.systemDefault());
         List<User> inactiveUsers =
                 userRepository.findByUpdatedDateBeforeAndStatusEquals(
                         now.minusYears(1), UserStatus.ACTIVE);
         return new ListItemReader<>(inactiveUsers);



    }


    //reader에서 읽은 User를 휴면상태로 전환하는 processor메서드 추가
    public ItemProcessor<User,User> inactiveUserProcessor(){
        return new ItemProcessor<User, User>() {
            @Override
            public User process(User user) throws Exception {
                return user.setInactive();
            }
        };
    }

    public JpaItemWriter<User> inactiveUserWriter() {

        // 앞서 설정한 청크 단위로 리스트 타입을 받음
        // users에는 10개의 휴면 회원 주어짐.
        JpaItemWriter<User> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;

    }
}

