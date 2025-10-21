package com.example.postservice.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", url = "http://localhost:8081")
public interface UserClient {
  @GetMapping("/users/{id}")
  Object getUserById(@PathVariable("id") Long id);
}
