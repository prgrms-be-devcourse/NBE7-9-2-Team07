package com.back.pinco.domain.post.service;


import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.post.entity.Post;
import com.back.pinco.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public long count() {
        return postRepository.count();
    }

    public Post write(String content, Pin pin) {
        Post post = new Post(content,pin);
        return post;
    }

    public Optional<Post> findById(long id) {
        return postRepository.findById(id);
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public void deleteById(long id) {
        Post post = postRepository.findById(id).get();
        postRepository.delete(post);
    }

    public Optional<List<Post>> findByPin(Pin pin) {
        return postRepository.findByPin(pin);
    }
}
