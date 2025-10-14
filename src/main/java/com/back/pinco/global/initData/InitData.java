package com.back.pinco.global.initData;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.post.entity.Post;
import com.back.pinco.domain.post.service.PostService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class InitData {
    @Autowired
    @Lazy
    private PinService pinService;
    private PostService postService;
    private InitData self;
    
    @Bean
    ApplicationRunner initData(){
        return args -> {
            self.work();
        };
    }
    @Transactional
    public void work() {
        if(pinService.count()>0){
            return;
        }

        Pin pin1 = pinService.write(37.5665,126.9780, LocalDateTime.now());
        Pin pin2 =pinService.write(40.7128,-74.0060, LocalDateTime.now());
        Pin pin3 =pinService.write(35.6895,139.6917, LocalDateTime.now());

        postService.write("content1-1", LocalDateTime.now(),LocalDateTime.now(), pin1);
        postService.write("content1-2", LocalDateTime.now(),LocalDateTime.now(), pin1);
        postService.write("content2-1", LocalDateTime.now(),LocalDateTime.now(), pin2);
        postService.write("content2-2", LocalDateTime.now(),LocalDateTime.now(), pin2);
        postService.write("content2-3", LocalDateTime.now(),LocalDateTime.now(), pin2);
    }

}
