package com.example.postservice.controller;

import com.example.postservice.model.Post;
import com.example.postservice.repository.PostRepository;
import com.example.postservice.client.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostController {
  private final PostRepository repo;
  private final UserClient userClient;

  @Autowired
  public PostController(PostRepository repo, UserClient userClient) { this.repo = repo; this.userClient = userClient; }

  @GetMapping
  public List<Post> all() { return repo.findAll(); }

  @GetMapping("/{id}")
  public ResponseEntity<?> get(@PathVariable("id") Long id) {
    return repo.findById(id).map(p -> ResponseEntity.ok(p)).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/with-author")
  public ResponseEntity<?> getWithAuthor(@PathVariable("id") Long id) {
    return repo.findById(id).map(p -> {
      Object author = null;
      try { author = userClient.getUserById(p.getAuthorId()); } catch (Exception e) { author = Map.of("error","unable to fetch user"); }
      return ResponseEntity.ok(Map.of("post", p, "author", author));
    }).orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public Post create(@RequestBody Post post) { return repo.save(post); }

  @PutMapping("/{id}")
  public ResponseEntity<Post> update(@PathVariable("id") Long id, @RequestBody Post p) {
    return repo.findById(id).map(post -> {
      post.setTitle(p.getTitle());
      post.setContent(p.getContent());
      post.setAuthorId(p.getAuthorId());
      return ResponseEntity.ok(repo.save(post));
    }).orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
    if (!repo.existsById(id)) return ResponseEntity.notFound().build();
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
