package com.example.aiservice.client;

import com.example.aiservice.dto.PostDto;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "post-service", url = "http://localhost:8082")
public interface PostClient {
  @GetMapping("/posts/{id}")
  PostDto getPostById(@PathVariable("id") Long id);

  @PostMapping("/posts")
  PostDto createPost(@RequestBody PostDto post);
}

