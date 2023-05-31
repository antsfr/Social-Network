package com.socialnetwork.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import com.socialnetwork.business.post.Post;
import com.socialnetwork.business.post.PostService;
import com.socialnetwork.business.user.User;
import com.socialnetwork.business.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @Autowired
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService= userService;
    }


    @PostMapping("/new")
    public ResponseEntity<Map<String, Long>> postPost(@Valid @RequestBody Post post,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        post.setDate(LocalDateTime.now());
        System.out.println(userDetails.getUsername());
        post.setAuthor(userService.getUserByUsername(userDetails.getUsername()).get());
        return ResponseEntity.ok(Collections.singletonMap("id", postService.addPost(post)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Post>> getPostsOfUser(@PathVariable("userId") Long id) {
        User user = userService.getUserById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such user"));
        List<Post> posts = postService.getPostsByUser(user);
        return ResponseEntity.ok().body(posts);
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable("postId") java.lang.Long id, @AuthenticationPrincipal UserDetails userDetails){
        Post post = getPostIfExists(id);
        User user = getUserIfExists(userDetails);
        checkIfUserIsAuthorOfPost(post, user);
        postService.deletePost(id);
    }

    @PutMapping("/{postId}")
    public void updatePost(@PathVariable("postId") java.lang.Long id, @AuthenticationPrincipal UserDetails userDetails,
                           @Valid @RequestBody Post newPost){
        Post oldPost = getPostIfExists(id);
        User user = getUserIfExists(userDetails);
        checkIfUserIsAuthorOfPost(oldPost, user);
        newPost.setId(oldPost.getId());
        newPost.setDate(LocalDateTime.now());
        newPost.setAuthor(user);
        postService.addPost(newPost);
    }

    @JsonView({Post.View.class})
    @GetMapping("/feed/{pageNumber}")
    public List<Post> getFeed(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer pageNumber) {
        User subscriber = getUserIfExists(userDetails);
        List<Post> feed = new ArrayList<>();
        feed.addAll(postService.getPostsBySubscriptions(subscriber, pageNumber));
        return feed;
    }


    private Post getPostIfExists(Long postId) {
        return postService.getPostById(postId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such post"));
    }
    private User getUserIfExists(UserDetails userDetails) {
        return userService.getUserByUsername(userDetails.getUsername()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such user"));
    }
    private void checkIfUserIsAuthorOfPost(Post post, User user) {
        boolean isAuthor = post.getAuthor().getId().equals(user.getId());
        if (!isAuthor)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not author");
    }
}
