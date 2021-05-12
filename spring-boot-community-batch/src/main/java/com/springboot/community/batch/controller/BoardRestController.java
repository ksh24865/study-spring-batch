package com.springboot.community.batch.controller;

import com.springboot.community.batch.domain.Board;
import com.springboot.community.batch.repository.BoardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.PagedModel;

import org.springframework.hateoas.PagedModel.PageMetadata;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping("/api/boards")
public class BoardRestController {
    private final BoardRepository boardRepository;

    //의존성 주입
    public BoardRestController(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    //반환값은 JSON 타입
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBoards(@PageableDefault Pageable pageable){
        Page<Board> boards = boardRepository.findAll(pageable);

        //현재 페이지 수, 총 게시판 수, 한 페이지의 게시판수 등 페이징 처리에 관한 리소스를 만드는
        //PagedResources 객체를 생성하기 위해 PagedResources 생성자의 파라미터로 사용되는 PageMetadata객체 생성
        //PageMetadata는 전체 페이지 수, 현재 페이지 번호, 총 게시판 수로 구성됨.
        PageMetadata pageMetadata = new PageMetadata(pageable.getPageSize(),
                boards.getNumber(), boards.getTotalElements());

        //PagedResources 객체 생성, 생성시 해태오스 적용되며 페이징값 생선된 REST형의 데이터 만들어줌
        PagedModel<Board> resources = new PagedModel<>(boards.getContent(),pageMetadata);

        //PagedResources 객체 생성 시 따로 링크를 설정하지 않았다면, 링크 추가 가능
        //Board 마다 상세 정보를 불러올 수 있는 링크 추가
        resources.add(linkTo(methodOn(BoardRestController.class).
                getBoards(pageable)).withSelfRel());
        System.out.println("!!!!GET!!!!");
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    public ResponseEntity<?> postBoard(@RequestBody Board board){
        board.setCreatedDateNow();
        boardRepository.save(board);
        System.out.println("!!!!POST!!!!"+board.getTitle());
        return new ResponseEntity<>("{}", HttpStatus.CREATED);
    }

    @PutMapping("{idx}")
    public ResponseEntity<?> putBoard(@PathVariable("idx")Long idx, @RequestBody Board board) {
        Board persistBoard = boardRepository.getOne(idx);
        persistBoard.update(board);
        boardRepository.save(persistBoard);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    @DeleteMapping("{idx}")
    public ResponseEntity<?> deleteBoard(@PathVariable("idx")Long idx){
        boardRepository.deleteById(idx);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
}
