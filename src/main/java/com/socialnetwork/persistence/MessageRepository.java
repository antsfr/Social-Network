package com.socialnetwork.persistence;

import com.socialnetwork.business.message.Message;
import com.socialnetwork.business.user.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Long> {
    List<Message> findBySenderAndReceiverOrderByDateDesc(User sender, User receiver);
}
