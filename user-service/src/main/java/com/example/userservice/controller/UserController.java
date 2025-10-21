package com.example.userservice.controller;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
  private final UserRepository repo;
  public UserController(UserRepository repo) { this.repo = repo; }

  @GetMapping
  public List<User> all() { return repo.findAll(); }

  @GetMapping("/{id}")
  public ResponseEntity<User> get(@PathVariable("id") Long id) {
    return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public User create(@RequestBody User user) { return repo.save(user); }

  @PutMapping("/{id}")
  public ResponseEntity<User> update(@PathVariable("id") Long id, @RequestBody User u) {
    return repo.findById(id).map(user -> {
      user.setName(u.getName());
      user.setEmail(u.getEmail());
      return ResponseEntity.ok(repo.save(user));
    }).orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
    if (!repo.existsById(id)) return ResponseEntity.notFound().build();
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
