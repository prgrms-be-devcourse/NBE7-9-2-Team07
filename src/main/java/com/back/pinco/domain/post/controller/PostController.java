package com.back.pinco.domain.post.controller;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.post.dto.PostDto;
import com.back.pinco.domain.post.entity.Post;
import com.back.pinco.domain.post.service.PostService;
import com.back.pinco.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private PinService pinService;


    @GetMapping("/{pinId}")
    public RsData<List<PostDto>> getPostByPinId(
            @PathVariable Long pinId
    ) {
        Pin pin = pinService.findById(pinId).get();

        List<Post> posts = postService.findByPin(pin).get();

        List<PostDto> postDtos = posts.stream().map(PostDto::new).toList();

        return new RsData<List<PostDto>>(
                "200",
                "성공적으로 처리되었습니다",
                postDtos
        );
    }

    @GetMapping
    public RsData<List<PostDto>> getAllPost(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Post> posts = postService.findAll(pageable);

        List<PostDto> postDtos = posts.stream().map(PostDto::new).toList();

        return new RsData<List<PostDto>>(
                "200",
                "성공적으로 처리되었습니다",
                postDtos
        );
    }

    record PostReqBody(
            @NotNull
            @Min(-90)
            @Max(90)
            @RequestParam double latitude,

            @NotNull
            @Min(-180)
            @Max(180)
            @RequestParam double longitude,
            @NotBlank
            String content
    ) {
    }

    @PostMapping
    public RsData<PostDto> writePost(
            @RequestBody @Valid PostReqBody postReqBody
    ) {
        Pin pin = pinService.write(postReqBody.latitude,postReqBody.longitude);
        Post post = postService.write(postReqBody.content, pin);

        PostDto postDto = new PostDto(post);

        return new RsData<PostDto>(
                "200",
                "성공적으로 처리되었습니다",
                postDto
        );
    }



}
