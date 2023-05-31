package com.socialnetwork.persistence;

import com.socialnetwork.business.post.Post;
import com.socialnetwork.business.user.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Long> {
    List<Post> findByAuthorOrderByDateDesc(User user);
}
