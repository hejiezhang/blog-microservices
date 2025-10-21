package com.example.userservice.controller;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration test for UserController using H2 (in-memory DB).
 * Uses RestAssured to simulate HTTP requests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository repo;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        repo.deleteAll();
    }

    @Test
    @Order(1)
    void createUser_shouldReturnCreatedUser() {
        User newUser = new User(null, "Alice", "alice@example.com");

        given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", equalTo("Alice"))
                .body("email", equalTo("alice@example.com"));
    }

    @Test
    @Order(2)
    void getAllUsers_shouldReturnList() {
        repo.saveAll(List.of(
                new User(null, "John", "john@example.com"),
                new User(null, "Jane", "jane@example.com")
        ));

        when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", equalTo("John"))
                .body("[1].name", equalTo("Jane"));
    }

    @Test
    @Order(3)
    void getUserById_shouldReturnUser() {
        User saved = repo.save(new User(null, "Charlie", "charlie@example.com"));

        when()
                .get("/users/{id}", saved.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(saved.getId().intValue()))
                .body("name", equalTo("Charlie"))
                .body("email", equalTo("charlie@example.com"));
    }

    @Test
    @Order(4)
    void updateUser_shouldModifyExisting() {
        User saved = repo.save(new User(null, "Old Name", "old@example.com"));
        User updated = new User(null, "New Name", "new@example.com");

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .put("/users/{id}", saved.getId())
                .then()
                .statusCode(200)
                .body("name", equalTo("New Name"))
                .body("email", equalTo("new@example.com"));
    }

    @Test
    @Order(5)
    void deleteUser_shouldRemoveIt() {
        User saved = repo.save(new User(null, "Delete Me", "delete@example.com"));

        when()
                .delete("/users/{id}", saved.getId())
                .then()
                .statusCode(204);

        Assertions.assertFalse(repo.existsById(saved.getId()));
    }

    @Test
    @Order(6)
    void getUserById_shouldReturnNotFound_whenNonExistent() {
        when()
                .get("/users/{id}", 999L)
                .then()
                .statusCode(404);
    }
}
