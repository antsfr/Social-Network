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

    @Autowired
    PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post addPost(Post post) {
        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> getPostsByUser(User user) {
        return postRepository.findByAuthorOrderByDateDesc(user);
    }

    public List<Post> getPostsBySubscriptions(User subscriber, Integer pageNumber) {
        List<Post> posts = new ArrayList<>();
        Integer pageSize = 10;    //TODO: вынести в конфиг
        Integer postNumberToStart = 1 + (pageNumber - 1) * pageSize;
        posts.addAll(postRepository.findBySubscriptions(subscriber.getId(), postNumberToStart, pageSize));
        return posts;
    }

}
