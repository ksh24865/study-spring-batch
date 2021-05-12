package com.springboot.community.batch.event;

import com.springboot.community.batch.domain.Board;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

@RepositoryEventHandler
public class BoardEventHandler {

    //Board생성 및 수정 이벤트 발생 시 작동하는 핸들러

    @HandleBeforeCreate
    public void beforeCreateBoard(Board board){
        board.setCreatedDateNow();
    }

    @HandleBeforeSave
    public void beforeSaveBoard(Board board){
        board.setUpdatedDateNow();
    }
}
