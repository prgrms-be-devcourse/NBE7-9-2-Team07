package com.back.pinco.domain.post.controller;

import com.back.pinco.domain.pin.controller.PinController;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.post.dto.PostDto;
import com.back.pinco.domain.post.entity.Post;
import com.back.pinco.domain.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PostControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private PinRepository pinRepository;
    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("게시글 조회 - 단건 - 조회 성공")
    void t1_1() throws Exception {
        long pinId = 1;
        Pin pin = pinRepository.findById(pinId).get();
        List<Post> posts = postRepository.findByPin(pin).get();
        List<PostDto> postDtos = posts.stream().map(PostDto::new).toList();
        ResultActions resultActions = mvc
                .perform(
                        get("/api/posts/%s".formatted(pinId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("getPostByPinId"))
                .andExpect(status().isOk());

        for(PostDto postDto : postDtos){
            resultActions
                    .andExpect(jsonPath("$.id").value(postDto.id()))
                    .andExpect(jsonPath("$.content").value(postDto.content()))
                    .andExpect(jsonPath("$.createAt").value(
                            matchesPattern(postDto.createAt().toString().replaceAll("0+$", "") + ".*")))
                    .andExpect(jsonPath("$.modifiedAt").value(
                            matchesPattern(postDto.modifiedAt().toString().replaceAll("0+$", "") + ".*")))
                    .andExpect(jsonPath("$.pin.id").value(postDto.pin().getId()))
                    .andExpect(jsonPath("$.pin.latitude").value(postDto.pin().getLatitude()))
                    .andExpect(jsonPath("$.pin.longitude").value(postDto.pin().getLongitude()))
                    .andExpect(jsonPath("$.pin.createDate").value(
                            matchesPattern(postDto.pin().getCreateAt().toString().replaceAll("0+$", "") + ".*")))
            ;
        }
    }

    @Test
    @DisplayName("게시글 조회 - 단건 - 조회 실패")
    void t1_2() throws Exception {
        long pinId = 10;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/posts/%s".formatted(pinId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("getPostByPinId"))
                .andExpect(jsonPath("$.errorCode").value(404));
    }

    @Test
    @DisplayName("게시글 조회 - 전체 - 조건 없을 때 (기본 값으로 조회)")
    void t2_1() throws Exception {

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findAll(pageable);
        List<PostDto> postDtos = posts.stream().map(PostDto::new).toList();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/posts")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("getAllPost"))
                .andExpect(status().isOk());

        for(PostDto postDto : postDtos){
            resultActions
                    .andExpect(jsonPath("$.id").value(postDto.id()))
                    .andExpect(jsonPath("$.content").value(postDto.content()))
                    .andExpect(jsonPath("$.createAt").value(
                            matchesPattern(postDto.createAt().toString().replaceAll("0+$", "") + ".*")))
                    .andExpect(jsonPath("$.modifiedAt").value(
                            matchesPattern(postDto.modifiedAt().toString().replaceAll("0+$", "") + ".*")))
                    .andExpect(jsonPath("$.pin.id").value(postDto.pin().getId()))
                    .andExpect(jsonPath("$.pin.latitude").value(postDto.pin().getLatitude()))
                    .andExpect(jsonPath("$.pin.longitude").value(postDto.pin().getLongitude()))
                    .andExpect(jsonPath("$.pin.createDate").value(
                            matchesPattern(postDto.pin().getCreateAt().toString().replaceAll("0+$", "") + ".*")))
            ;
        }
    }

    @Test
    @DisplayName("게시글 조회 - 전체 - 조건 있을 때 (page=1&size=2&modifiedAt,asc)")
    void t2_2() throws Exception {

        Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "modifiedAt"));
        Page<Post> posts = postRepository.findAll(pageable);
        List<PostDto> postDtos = posts.stream().map(PostDto::new).toList();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/posts?page=%d&size=%d&%s".formatted(1,2,"modifiedAt,asc"))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("getAllPost"))
                .andExpect(status().isOk());

        for(PostDto postDto : postDtos){
            resultActions
                    .andExpect(jsonPath("$.id").value(postDto.id()))
                    .andExpect(jsonPath("$.content").value(postDto.content()))
                    .andExpect(jsonPath("$.createAt").value(
                            matchesPattern(postDto.createAt().toString().replaceAll("0+$", "") + ".*")))
                    .andExpect(jsonPath("$.modifiedAt").value(
                            matchesPattern(postDto.modifiedAt().toString().replaceAll("0+$", "") + ".*")))
                    .andExpect(jsonPath("$.pin.id").value(postDto.pin().getId()))
                    .andExpect(jsonPath("$.pin.latitude").value(postDto.pin().getLatitude()))
                    .andExpect(jsonPath("$.pin.longitude").value(postDto.pin().getLongitude()))
                    .andExpect(jsonPath("$.pin.createDate").value(
                            matchesPattern(postDto.pin().getCreateAt().toString().replaceAll("0+$", "") + ".*")))
            ;
        }
    }

    @Test
    @DisplayName("게시글 조회 - 전체 - 조건 있을 때 (page=10&size=20&modifiedAt,asc) - 실패 (빈 리스트 리턴)")
    void t2_3() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/posts?page=%d&size=%d&%s".formatted(10,20,"modifiedAt,asc"))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("getAllPost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0))
        ;

    }



}
