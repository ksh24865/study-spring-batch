package com.springboot.community.batch.jobs;

import com.springboot.community.batch.domain.User;
import com.springboot.community.batch.domain.enums.UserStatus;
import com.springboot.community.batch.jobs.readers.QueueItemReader;
import com.springboot.community.batch.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@Configuration
public class InactiveUserJobConfig {
    @Bean
    // Bean에 주입할 객체를 파라미터로 명시하면 오토와이어 쓰는 것과 같은 효과
    public Job inactiveUserJob(JobBuilderFactory jobBuilderFactory,
                               Step inactiveJobStep){
        return jobBuilderFactory.get("inactiveUserJob") // inactiveUserJob이름의 JobBuilder 생성
                .preventRestart() // Job의 재실행 막음
                .start(inactiveJobStep) // 인자로 받은 step을 젤 먼저 실행하도록 설정
                .build();
    }

    public Step inactiveJopStep(StepBuilderFactory stepBuilderFactory){
        return stepBuilderFactory.get("inactiveUserJob")
                .<User,User> chunk(10)
                .reader(inactiveUserReader())
                .processor(inactiveUserProcessor())
                .writer(inactiveUserWriter())
                .build();
    }

    private UserRepository userRepository;
    private QueueItemReader<User> inactiveUserReader() {
        List<User> oldUsers = userRepository.findByUpdatedDateBeforeAndStatusEquals(
                LocalDateTime.now().minusYears(1), UserStatus.ACTIVE);
        return new QueueItemReader<>(oldUsers);
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

    public ItemWriter<User> inactiveUserWriter(){
        // 앞서 설정한 청크 단위로 리스트 타입을 받음
        // users에는 10개의 휴면 회원 주어짐.
//        return User::setInactive;
        return ((List<? extends User> users) -> userRepository.saveAll(users));
    }
}

