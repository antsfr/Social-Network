package com.socialnetwork.presentation;

import com.socialnetwork.business.message.Message;
import com.socialnetwork.business.message.MessageService;
import com.socialnetwork.business.user.User;
import com.socialnetwork.business.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MessageController {
    private final MessageService messageService;
    private final UserService userService;

    MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @PostMapping(value = {"{userId}/message", "dialog/{userId}"})
    @ResponseStatus(code = HttpStatus.OK)
    public void sendMessage(@PathVariable("userId") Long userId, @AuthenticationPrincipal UserDetails userDetails,
                            String text) {
        User receiver = getUserIfExists(userId);
        User sender = userService.getUserByUsername(userDetails.getUsername()).get();
        checkForFriendship(sender, receiver);
        //SELF-MESSAGING W/O CHECK
        Message  message = new Message(sender, receiver, text);
        messageService.saveMessage(message);
    }

    @GetMapping("dialog/{userId}")
    @ResponseStatus(code = HttpStatus.OK)
    public List<Message> getDialog(@PathVariable("userId") Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        User receiver = getUserIfExists(userId);
        User sender = userService.getUserByUsername(userDetails.getUsername()).get();
        return messageService.getDialogWithUser(sender, receiver);
    }


    private User getUserIfExists(Long userId) {
        return userService.getUserById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No such user"));
    }

    private void checkForFriendship(User u1, User u2) {
        if ((u1.getSubscriptions().contains(u2.getId()) && u2.getSubscriptions().contains(u1.getId())))
            return;
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can text only to your friends");
    }
}
