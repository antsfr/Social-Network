package com.socialnetwork.business.message;

import com.socialnetwork.business.user.User;
import com.socialnetwork.persistence.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message sendMessage(Message message) {
        message.setDate(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<Message> getDialogWithUser(User sender, User receiver) {
        List<Message> dialog = new ArrayList<>();
        dialog.addAll(messageRepository.findBySenderAndReceiverOrderByDateDesc(sender, receiver));
        dialog.addAll(messageRepository.findBySenderAndReceiverOrderByDateDesc(receiver, sender));
        Collections.sort(dialog);
        return dialog;
    }
}
