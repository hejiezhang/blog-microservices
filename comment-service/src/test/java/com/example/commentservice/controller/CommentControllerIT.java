package com.example.commentservice.controller;

import com.example.commentservice.model.Comment;
import com.example.commentservice.repository.CommentRepository;
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
 * Integration test for CommentController using H2 (in-memory DB).
 * Uses RestAssured to simulate HTTP requests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommentControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private CommentRepository repo;

    // Mock external Feign clients (PostClient and UserClient)
    @MockitoBean
    private com.example.commentservice.client.PostClient postClient;

    @MockitoBean
    private com.example.commentservice.client.UserClient userClient;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        repo.deleteAll();
    }

    @Test
    @Order(1)
    void createComment_shouldReturnCreatedComment() {
        Comment newComment = new Comment(null, 1L, 1L, "Nice post!");

        given()
                .contentType(ContentType.JSON)
                .body(newComment)
                .when()
                .post("/comments")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("content", equalTo("Nice post!"))
                .body("authorId", equalTo(1))
                .body("postId", equalTo(1));
    }

    @Test
    @Order(2)
    void getAllComments_shouldReturnList() {
        repo.saveAll(List.of(
                new Comment(null, 1L, 1L, "First comment"),
                new Comment(null, 2L, 1L, "Second comment")
        ));

        when()
                .get("/comments")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].content", equalTo("First comment"));
    }

    @Test
    @Order(3)
    void getCommentById_shouldReturnComment() {
        Comment saved = repo.save(new Comment(null, 1L, 1L, "Find me"));

        when()
                .get("/comments/{id}", saved.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(saved.getId().intValue()))
                .body("content", equalTo("Find me"));
    }

    @Test
    @Order(4)
    void updateComment_shouldModifyExisting() {
        Comment saved = repo.save(new Comment(null, 1L, 1L, "Old content"));
        Comment updated = new Comment(null, 2L, 2L, "Updated content");

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .put("/comments/{id}", saved.getId())
                .then()
                .statusCode(200)
                .body("content", equalTo("Updated content"))
                .body("authorId", equalTo(2));
    }

    @Test
    @Order(5)
    void deleteComment_shouldRemoveIt() {
        Comment saved = repo.save(new Comment(null, 1L, 1L, "Delete me"));

        when()
                .delete("/comments/{id}", saved.getId())
                .then()
                .statusCode(204);

        Assertions.assertFalse(repo.existsById(saved.getId()));
    }

    @Test
    @Order(6)
    void getCommentsByPost_shouldReturnPostAndComments() {
        repo.saveAll(List.of(
                new Comment(null, 100L, 1L, "Comment A"),
                new Comment(null, 100L, 2L, "Comment B")
        ));

        // Mock Feign client call to PostClient
        when(postClient.getPostById(100L))
                .thenReturn(new java.util.HashMap<>(java.util.Map.of("id", 100L, "title", "Mock Post")));

        when()
                .get("/comments/post/{postId}", 100L)
                .then()
                .statusCode(200)
                .body("comments.size()", is(2))
                .body("post.id", equalTo(100))
                .body("post.title", equalTo("Mock Post"));
    }
}
