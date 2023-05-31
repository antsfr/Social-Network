package com.socialnetwork.business.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.socialnetwork.business.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.script.ScriptEngine;
import java.time.LocalDateTime;
import java.util.Comparator;

@Entity
@Data
@NoArgsConstructor
public class Message implements Comparable<Message> {
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
    @ManyToOne
    @JoinColumn(name = "sender_id")
    @JsonIgnore
    private User sender;
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    @JsonIgnore
    User receiver;
    String text;
    private LocalDateTime date;

    public Message(User sender, User receiver, String text) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
    }

    @Override
    public int compareTo(Message o) {
        return -(this.getDate().compareTo(o.getDate()));
    }


    //    @ElementCollection
//    List<String> messages = new ArrayList<>();
}
