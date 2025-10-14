package com.back.pinco.domain.post.service;


import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.post.entity.Post;
import com.back.pinco.domain.post.repository.PostRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@NoArgsConstructor
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public long count() {
        return postRepository.count();
    }

    public Post write(String content, LocalDateTime createDate, LocalDateTime modifyDate, Pin pin) {
        Post post = new Post(content, createDate, modifyDate, pin);
        return post;
    }

    public Optional<Post> findById(long id) {
        return postRepository.findById(id);
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public void deleteById(long id) {
        Post post = postRepository.findById(id).get();
        postRepository.delete(post);
    }
}
