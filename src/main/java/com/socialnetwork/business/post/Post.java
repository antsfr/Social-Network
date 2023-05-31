package com.socialnetwork.business.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.socialnetwork.business.message.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.socialnetwork.business.user.User;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Comparator;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Post implements Comparable<Post> {
    public interface View {}
    @JsonIgnore
    @Id
    @SequenceGenerator(
            name = "post_sequence",
            sequenceName = "post_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "post_sequence"
    )
    private Long id;
    @JsonView(Post.View.class)
    @NotBlank
    private String header;
    //@NotBlank
    @JsonView(Post.View.class)
    private String text;
//    @ElementCollection
//    //@NotEmpty
//    private List<Object> images;
    @JsonView(Post.View.class)
    private LocalDateTime date;
    //@JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonView(Post.View.class)
    private User author;

    @Override
    public int compareTo(Post o) {
        return -(this.getDate().compareTo(o.getDate()));
    }
}