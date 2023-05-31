package com.socialnetwork.business.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.socialnetwork.business.user.User;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Post {
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
    @NotBlank
    private String header;
    //@NotBlank
    private String text;
//    @ElementCollection
//    //@NotEmpty
//    private List<Object> images;
    private LocalDateTime date;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;

}