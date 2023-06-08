package com.socialnetwork.business.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.socialnetwork.business.friendship.FriendshipRequest;
import com.socialnetwork.business.post.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "users")
public class User {
    @JsonIgnore
    @Id
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    private Long id;
    @JsonView(Post.View.class)
    @Pattern(regexp = "[a-zA-Z1-9_]{4,24}")
    private String username;
    @Pattern(regexp = ".+@.+\\..+")
    private String email;
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$")
    private String password;

    @OneToMany(mappedBy = "sender")
    Set<FriendshipRequest> friendshipRequestsSent;
    @OneToMany(mappedBy = "receiver")
    Set<FriendshipRequest> friendshipRequestsReceived;

    public Set<User> getFriends() {
        Set<User> friends = friendshipRequestsReceived.stream()
                        .filter(FriendshipRequest::isAccepted)
                        .map(FriendshipRequest::getSender)
                        .collect(Collectors.toSet());
        friends.addAll(friendshipRequestsSent.stream()
                .filter(FriendshipRequest::isAccepted)
                .map(FriendshipRequest::getReceiver)
                .collect(Collectors.toSet()));
        return friends;
    }

    public Set<User> getSubscribers() {
        Set<User> subscribers = friendshipRequestsReceived.stream()
                .map(FriendshipRequest::getReceiver)
                .collect(Collectors.toSet());
        subscribers.addAll(friendshipRequestsSent.stream()
                .filter(FriendshipRequest::isAccepted)
                .map(FriendshipRequest::getSender)
                .collect(Collectors.toSet()));
        return subscribers;
    }

    public Set<User> getSubscriptions() {
        Set<User> subscriptions = friendshipRequestsSent.stream()
                .map(FriendshipRequest::getReceiver)
                .collect(Collectors.toSet());
        subscriptions.addAll(friendshipRequestsReceived.stream()
                .filter(FriendshipRequest::isAccepted)
                .map(FriendshipRequest::getSender)
                .collect(Collectors.toSet()));
        return subscriptions;
    }

}
