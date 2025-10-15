package com.back.pinco.global.initData;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.post.service.PostService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@RequiredArgsConstructor
public class InitData {

    @Autowired
    @Lazy
    private InitData self;
    private final PinService pinService;
    private final PostService postService;

    
    @Bean
    ApplicationRunner baseInitData(){
        return args -> {
            self.work();
        };
    }
    @Transactional
    public void work() {
        if(postService.count()>0){
            return;
        }
        postService.write("content1", 37.5665,126.9780);
        postService.write("content2", 40.7128,-74.0060);
        postService.write("content3", 35.6895,139.6917);

    }

}
