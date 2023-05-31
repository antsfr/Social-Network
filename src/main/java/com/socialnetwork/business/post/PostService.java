package com.socialnetwork.business.post;

import com.socialnetwork.business.user.User;
import com.socialnetwork.persistence.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final List<Post> posts = new ArrayList<>();

    @Autowired
    PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Long addPost(Post post) {
        return postRepository.save(post).getId();
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> getPostsByUser(User user) {;
        return postRepository.findByAuthorOrderByDateDesc(user);
    }

    public List<Post> getAllPosts() {
        posts.clear();
        postRepository.findAll().forEach(posts::add);
        return posts;
    }
}
