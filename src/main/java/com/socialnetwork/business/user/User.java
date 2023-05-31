package com.socialnetwork.business.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.socialnetwork.business.post.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    @Column(name = "user_id")
    private Long id;
    @JsonView(Post.View.class)
    //@Pattern(regexp = "[a-zA-Z1-9_]{4,16}")
    private String username;
    //@Pattern(regexp = ".+@.+\\..+.")
    private String email;
    //@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$")
    private String password;

//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(name="subscription",
//            joinColumns={@JoinColumn(name="user_id")},
//            inverseJoinColumns={@JoinColumn(name="anotherUser_id")})
    //@JsonIgnore
    @ElementCollection
    private Set<Long> subscriptions = new HashSet<>();

//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(name="subscription",
//            joinColumns={@JoinColumn(name="anotherUser_id")},
//            inverseJoinColumns={@JoinColumn(name="user_id")})
    //@JsonIgnore
    @ElementCollection
    private Set<Long> subscribers = new HashSet<>();
//    @ElementCollection
//    private Set<Long> friends = new HashSet<>();
//    @OneToMany
//    private Set<FriendshipRequest> friendshipRequests = new HashSet<>();
}
