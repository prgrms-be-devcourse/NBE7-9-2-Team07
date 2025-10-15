package com.back.pinco.domain.post.service;


import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.post.dto.PostDto;
import com.back.pinco.domain.post.entity.Post;
import com.back.pinco.domain.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PinService pinService;

    public long count() {
        return postRepository.count();
    }

    public Post write(String content, Pin pin) {
        Post post = new Post(content,pin);
        return postRepository.save(post);
    }

    @Transactional
    public Post write(String content, double latitude, double longitude) {
        Pin pin = pinService.write(latitude,longitude);
        Post post = new Post(content,pin);
        return postRepository.save(post);
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

    public void deleteByPinId(Pin pin) {
        postRepository.deleteByPin(pin);
    }

    @Transactional
    public PostDto modifyPost(Long postId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(NoSuchElementException::new);
        post.update(content);
        return new PostDto(post);
    }
}
