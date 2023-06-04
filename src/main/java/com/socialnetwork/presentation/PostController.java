package com.socialnetwork.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import com.socialnetwork.business.post.Post;
import com.socialnetwork.business.post.PostService;
import com.socialnetwork.business.user.User;
import com.socialnetwork.business.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@Validated
@RequestMapping("/api")
@Tag(name = "Posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @Autowired
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService= userService;
    }


    @Operation(
            summary = "Создать новый пост",
            responses = {
                    @ApiResponse(
                            description = "Пост создан",
                            responseCode = "201"
                    ),
                    @ApiResponse(
                            description = "Пост невалиден",
                            responseCode = "400"
                    )
            }
    )
    @JsonView(Post.View.class)
    @PostMapping("/post/new")
    public ResponseEntity<Post> postPost(@Valid @RequestBody Post post,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        post.setDate(LocalDateTime.now());
        post.setAuthor(userService.getUserByUsername(userDetails.getUsername()).get());
        return new ResponseEntity<>(postService.addPost(post), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Получить все посты пользователя",
            responses = {
                    @ApiResponse(
                            description = "Посты получены",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Такой пользователь не найден",
                            responseCode = "404"
                    )
            }
    )
    @JsonView(Post.View.class)
    @GetMapping("/user/{userId}/wall")
    public ResponseEntity<List<Post>> getPostsOfUser(@PathVariable("userId") Long id) {
        User user = userService.getUserById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such user"));
        List<Post> posts = postService.getPostsByUser(user);
        return ResponseEntity.ok().body(posts);
    }

    @Operation(
            summary = "Получить пост",
            responses = {
                    @ApiResponse(
                            description = "Пост получен",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Такой пост не найден",
                            responseCode = "404"
                    )
            }
    )
    @JsonView(Post.View.class)
    @GetMapping("/post/{postId}")
    public Post getPost(@PathVariable("postId") Long id) {
        return getPostIfExists(id);
    }

    @Operation(
            summary = "Удалить пост",
            responses = {
                    @ApiResponse(
                            description = "Пост удален",
                            responseCode = "204"
                    ),
                    @ApiResponse(
                            description = "Такой пост не найден",
                            responseCode = "404"
                    ),
                    @ApiResponse(
                            description = "У пользователя нет прав на удаление",
                            responseCode = "403"
                    )
            }
    )
    @DeleteMapping("/post/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable("postId") java.lang.Long id, @AuthenticationPrincipal UserDetails userDetails){
        Post post = getPostIfExists(id);
        User user = getUser(userDetails);
        checkIfUserIsAuthorOfPost(post, user);
        postService.deletePost(id);
    }

    @Operation(
            summary = "Изменить пост",
            responses = {
                    @ApiResponse(
                            description = "Пост изменен",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Новый пост невалиден",
                            responseCode = "400"
                    ),
                    @ApiResponse(
                            description = "Такой пост не найден",
                            responseCode = "404"
                    ),
                    @ApiResponse(
                            description = "У пользователя нет прав на изменение",
                            responseCode = "403"
                    )
            }
    )
    @PutMapping("/post/{postId}")
    public ResponseEntity<Post> updatePost(@PathVariable("postId") Long id, @Valid @RequestBody Post newPost,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        Post oldPost = getPostIfExists(id);
        User user = getUser(userDetails);
        checkIfUserIsAuthorOfPost(oldPost, user);
        newPost.setId(oldPost.getId());
        newPost.setDate(LocalDateTime.now());
        newPost.setAuthor(user);
        return new ResponseEntity<>(postService.addPost(newPost), HttpStatus.OK);
    }

    @Operation(
            summary = "Открыть свою ленту на указанной странице",
            responses = {
                    @ApiResponse(
                            description = "Лента возвращена",
                            responseCode = "200"
                    )
            }
    )
    @JsonView({Post.View.class})
    @GetMapping("/feed/{pageNumber}")
    public List<Post> getFeed(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer pageNumber) {
        User user = getUser(userDetails);
        List<Post> feed = new ArrayList<>();
        feed.addAll(postService.getPostsBySubscriptions(user, pageNumber));
        return feed;
    }


    private Post getPostIfExists(Long postId) {
        return postService.getPostById(postId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such post"));
    }
    private User getUser(UserDetails userDetails) {
        return userService.getUserByUsername(userDetails.getUsername()).get();
    }
    private void checkIfUserIsAuthorOfPost(Post post, User user) {
        boolean isAuthor = post.getAuthor().getId().equals(user.getId());
        if (!isAuthor)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not author");
    }
}
