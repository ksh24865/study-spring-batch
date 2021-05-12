package com.springboot.community.batch;

import com.springboot.community.batch.domain.Board;
import com.springboot.community.batch.domain.User;
import com.springboot.community.batch.domain.enums.BoardType;
import com.springboot.community.batch.repository.BoardRepository;
import com.springboot.community.batch.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class) //JUnit에 내장된 러너를  사용하는 대신 SpringRunner를 사용, 각 테스트 시 독립적인 APP CONTEXT 보장
@DataJpaTest // JPA테스트
public class JpaMappingTest {
    private final String boardTestTitle = "테스트";
    private final String email = "test@gmail.com";

    @Autowired
    UserRepository userRepository;

    @Autowired
    BoardRepository boardRepository;

    @Before // 테스트 실행 전 진행할 메서드
    public void init(){
        User user = userRepository.save(User.builder()
                .name("havi")
                .password("test")
                .email(email)
                .createdDate(LocalDateTime.now())
                .build());
        boardRepository.save(Board.builder()
                .title(boardTestTitle)
                .subTitle("서브타이틀")
                .content("콘텐츠")
                .boardType(BoardType.free)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .user(user).build());
    }

    @Test
    public void 생성_테스트(){
        User user = userRepository.findByEmail(email);
        assertThat(user.getName(), is("havi"));
        assertThat(user.getPassword(), is("test"));
        assertThat(user.getEmail(), is(email));

        Board board = boardRepository.findByUser(user);
        assertThat(board.getTitle(),is(boardTestTitle));
        assertThat(board.getSubTitle(), is("서브타이틀"));
        assertThat(board.getContent(),is("콘텐츠"));
        assertThat(board.getBoardType(), is(BoardType.free));
    }
}
