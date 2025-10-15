package com.back.pinco.domain.post.controller;

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
    private PostRepository postRepository;

    @Test
    @DisplayName("게시글 조회 - 단건 - 조회 성공")
    void t1_1() throws Exception {
        Long pinId = 13L;

        Post post = postRepository.findByPinId(pinId).get();
        PostDto postDto = new PostDto(post);
        ResultActions resultActions = mvc
                .perform(
                        get("/api/posts/%s".formatted(pinId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("getPostByPinId"))
                .andExpect(status().isOk());

        resultActions
                .andExpect(jsonPath("$.data.id").value(postDto.id()))
                .andExpect(jsonPath("$.data.content").value(postDto.content()))
                .andExpect(jsonPath("$.data.createAt").value(
                        matchesPattern(postDto.createAt().toString().replaceAll("0+$", "") + ".*")))
                .andExpect(jsonPath("$.data.modifiedAt").value(
                        matchesPattern(postDto.modifiedAt().toString().replaceAll("0+$", "") + ".*")))
        ;
    }

    @Test
    @DisplayName("게시글 조회 - 단건 - 조회 실패")
    void t1_2() throws Exception {
        Long postId = 10L;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/posts/%s".formatted(postId))
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

        ResultActions resultActions = mvc
                .perform(
                        get("/api/posts")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("getAllPost"))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("게시글 조회 - 전체 - 조건 있을 때 (page=1&size=2&modifiedAt,asc)")
    void t2_2() throws Exception {

        Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "modifiedAt"));
        Page<Post> posts = postRepository.findAll(pageable);

        ResultActions resultActions = mvc
                .perform(
                        get("/api/posts?page=%d&size=%d&%s".formatted(1, 2, "modifiedAt,asc"))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("getAllPost"))
                .andExpect(status().isOk());


    }

    @Test
    @DisplayName("게시글 조회 - 전체 - 조건 있을 때 (page=10&size=20&modifiedAt,asc) - 실패 (빈 리스트 리턴)")
    void t2_3() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/posts?page=%d&size=%d&%s".formatted(10, 20, "modifiedAt,asc"))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("getAllPost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0))
        ;

    }

    @Test
    @DisplayName("게시글 생성 - 성공")
    void t3_1() throws Exception {
        String content = "new content!";
        double latitude = 37.5670;
        double longitude = 126.9785;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/posts")

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "%s",
                                            "latitude" : "%f",
                                            "longitude" : "%f"
                                        }
                                        """.formatted(content, latitude, longitude))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("writePost"))
                .andExpect(status().isOk());

        resultActions
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.content").value(content))
                .andExpect(jsonPath("$.data.createAt").isNotEmpty())
                .andExpect(jsonPath("$.data.modifiedAt").isNotEmpty());

    }

    @Test
    @DisplayName("게시글 생성 - 실패 (양식 오류)")
    void t3_2() throws Exception {
        String content = null;
        double latitude = -100;
        double longitude = 200;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "%s",
                                            "latitude" : "%f",
                                            "longitude" : "%f"
                                        }
                                        """.formatted(content, latitude, longitude))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("writePost"))
                .andExpect(status().is(400));

    }

    @Test
    @DisplayName("게시글 삭제 - 성공")
    void t4_1() throws Exception {
        Long postId = 1L;

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/posts/%s".formatted(postId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("deletePostByPinId"))
                .andExpect(jsonPath("$.errorCode").value(200))
                .andExpect(jsonPath("$.data.stateCode").value(404)); //여기서 상태 플러그 바뀐 거 검증

    }

    @Test
    @DisplayName("게시글 수정 - 성공")
    void t5_1() throws Exception {
        Long postId =7L;
        String content = "changed contents";
        ResultActions resultActions = mvc
                .perform(
                        put("/api/posts/%s".formatted(postId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "%s"
                                        }
                                        """.formatted(content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("updatePostByPinId"))
                .andExpect(status().isOk());

        resultActions
                .andExpect(jsonPath("$.data.content").value(content));

    }

    @Test
    @DisplayName("게시글 수정 - 실패")
    void t5_2() throws Exception {
        Long postId = (long) Integer.MAX_VALUE;
        String content = "changed contents";

        ResultActions resultActions = mvc
                .perform(
                        put("/api/posts/%s".formatted(postId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "%s"
                                        }
                                        """.formatted(content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("updatePostByPinId"))
                .andExpect(status().is(404));



    }

}
