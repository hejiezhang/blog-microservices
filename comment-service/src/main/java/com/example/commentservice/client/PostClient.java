package com.example.commentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "post-service", url = "http://localhost:8082")
public interface PostClient {
  @GetMapping("/posts/{id}")
  Object getPostById(@PathVariable("id") Long id);
}
