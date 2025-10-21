package com.example.postservice.controller;

import com.example.postservice.model.Post;
import com.example.postservice.repository.PostRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for PostController using H2 (in-memory DB).
 * Uses RestAssured to simulate HTTP requests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private PostRepository repo;

    // Mock external Feign client
    @MockitoBean
    private com.example.postservice.client.UserClient userClient;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        repo.deleteAll();
    }

    @Test
    @Order(1)
    void createPost_shouldReturnCreatedPost() {
        Post newPost = new Post(null, "My first post", "Hello world!", 1L);

        given()
                .contentType(ContentType.JSON)
                .body(newPost)
                .when()
                .post("/posts")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("title", equalTo("My first post"))
                .body("content", equalTo("Hello world!"))
                .body("authorId", equalTo(1));
    }

    @Test
    @Order(2)
    void getAllPosts_shouldReturnList() {
        repo.saveAll(List.of(
                new Post(null, "First", "Content A", 1L),
                new Post(null, "Second", "Content B", 2L)
        ));

        when()
                .get("/posts")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].title", equalTo("First"));
    }

    @Test
    @Order(3)
    void getPostById_shouldReturnPost() {
        Post saved = repo.save(new Post(null, "Find me", "Here I am", 1L));

        when()
                .get("/posts/{id}", saved.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(saved.getId().intValue()))
                .body("title", equalTo("Find me"));
    }

    @Test
    @Order(4)
    void updatePost_shouldModifyExisting() {
        Post saved = repo.save(new Post(null, "Old title", "Old content", 1L));
        Post updated = new Post(null, "New title", "Updated content", 2L);

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .put("/posts/{id}", saved.getId())
                .then()
                .statusCode(200)
                .body("title", equalTo("New title"))
                .body("authorId", equalTo(2));
    }

    @Test
    @Order(5)
    void deletePost_shouldRemoveIt() {
        Post saved = repo.save(new Post(null, "Delete me", "Will be gone", 1L));

        when()
                .delete("/posts/{id}", saved.getId())
                .then()
                .statusCode(204);

        Assertions.assertFalse(repo.existsById(saved.getId()));
    }

    @Test
    @Order(6)
    void getPostWithAuthor_shouldReturnPostAndMockedAuthor() {
        Post saved = repo.save(new Post(null, "Post with author", "Author details", 42L));

        // Mock Feign client call to UserClient
        when(userClient.getUserById(42L))
                .thenReturn(new java.util.HashMap<>(java.util.Map.of("id", 42L, "name", "Mock User")));

        when()
                .get("/posts/{id}/with-author", saved.getId())
                .then()
                .statusCode(200)
                .body("post.id", equalTo(saved.getId().intValue()))
                .body("post.title", equalTo("Post with author"))
                .body("author.id", equalTo(42))
                .body("author.name", equalTo("Mock User"));
    }
}
