package com.socialnetwork.persistence;

import com.socialnetwork.business.friendship.FriendshipRequest;
import com.socialnetwork.business.user.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepository extends CrudRepository<FriendshipRequest, Long> {
    FriendshipRequest findBySenderAndReceiver(User sender, User receiver);
}
