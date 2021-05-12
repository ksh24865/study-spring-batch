package com.springboot.community.batch.controller;

import com.springboot.community.batch.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("board") // URI경로를 board로 설정
public class BoardController {

    @Autowired
    BoardService boardService;

    @GetMapping({"", "/"}) // 복수 매핑 경로
    public String board(@RequestParam(value = "idx", defaultValue = "0") Long idx, Model model){
        // RequestParam idx파라미터 필수로 받음, 기본값 0
        model.addAttribute("board", boardService.findBoardByIdx(idx));
        return "board/form";
    }

    @GetMapping("/list")
    public String list(@PageableDefault Pageable pageable, Model model){
        // PageableDefault 어노테이션을 사용하여 페이징 처리에 대한 규약 정의한 pageable을 findBoardList에게 넘겨
        model.addAttribute("boardList", boardService.findBoardList(pageable));
        return "board/list";

    }


}
