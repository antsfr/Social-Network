package com.socialnetwork.business.friendship;

import com.socialnetwork.business.user.User;
import com.socialnetwork.persistence.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FriendshipService {

    private FriendshipRepository friendshipRepository;

    @Autowired
    FriendshipService(FriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }

    public void sendFriendshipRequest(User sender, User receiver) {
        friendshipRepository.save(new FriendshipRequest(sender, receiver, false));
    }

    public void acceptFriendshipRequest(User sender, User receiver) {
        FriendshipRequest friendshipRequest = friendshipRepository.findBySenderAndReceiver(sender, receiver);
        if (friendshipRequest == null)
            friendshipRequest = friendshipRepository.findBySenderAndReceiver(receiver, sender);
        friendshipRequest.setAccepted(true);
        friendshipRepository.save(friendshipRequest);
    }

    public void quitSubscription(User quitter, User subscription) {
        friendshipRepository.delete(friendshipRepository.findBySenderAndReceiver(quitter, subscription));
    }

    public void quitFriendship(User quitter, User friendToQuit) {
        FriendshipRequest friendshipRequest = friendshipRepository.findBySenderAndReceiver(friendToQuit, quitter);
        if (friendshipRequest == null) {
            friendshipRequest = friendshipRepository.findBySenderAndReceiver(quitter, friendToQuit);
            friendshipRequest.setReceiver(quitter);
            friendshipRequest.setSender(friendToQuit);
        }
        friendshipRequest.setAccepted(false);
        friendshipRepository.save(friendshipRequest);
    }
}
