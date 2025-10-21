package com.example.commentservice.controller;

import com.example.commentservice.model.Comment;
import com.example.commentservice.repository.CommentRepository;
import com.example.commentservice.client.PostClient;
import com.example.commentservice.client.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {
  private static final Logger log = LoggerFactory.getLogger(CommentController.class);

  private final CommentRepository repo;
  private final PostClient postClient;
  private final UserClient userClient;

  public CommentController(CommentRepository repo, PostClient postClient, UserClient userClient) {
    this.repo = repo; this.postClient = postClient; this.userClient = userClient;
  }

  @GetMapping
  public List<Comment> all() { return repo.findAll(); }

  @GetMapping("/{id}")
  public ResponseEntity<Comment> get(@PathVariable("id") Long id) {
    return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/post/{postId}")
  public ResponseEntity<?> byPost(@PathVariable("postId") Long postId) {

    log.info("Fetching comments for postId={}", postId);

    List<Comment> comments = repo.findByPostId(postId);
    Object post = null;
    try { post = postClient.getPostById(postId); } catch (Exception e) { post = Map.of("error","unable to fetch post"); }
    return ResponseEntity.ok(Map.of("post", post, "comments", comments));
  }

  @PostMapping
  public Comment create(@RequestBody Comment c) { return repo.save(c); }

  @PutMapping("/{id}")
  public ResponseEntity<Comment> update(@PathVariable("id") Long id, @RequestBody Comment c) {
    return repo.findById(id).map(comment -> {
      comment.setContent(c.getContent());
      comment.setAuthorId(c.getAuthorId());
      comment.setPostId(c.getPostId());
      return ResponseEntity.ok(repo.save(comment));
    }).orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
    if (!repo.existsById(id)) return ResponseEntity.notFound().build();
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
