package com.socialnetwork.persistence;

import com.socialnetwork.business.post.Post;
import com.socialnetwork.business.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Long> {
    List<Post> findByAuthorOrderByDateDesc(User user);
    String GET_POSTS_BY_SUBSCRIPTIONS = "SELECT * FROM post WHERE user_id IN " +
            "(SELECT subscriptions FROM user_subscriptions WHERE user_id = :subscriberId) " +
            "ORDER BY date DESC " +
            "OFFSET :postNumberToStart LIMIT :pageSize";
    @Query(value = GET_POSTS_BY_SUBSCRIPTIONS, nativeQuery = true)
    List<Post> findBySubscriptions(@Param("subscriberId") Long subscriberId, Integer postNumberToStart, Integer pageSize);
}
