package com.socialnetwork.presentation;

import com.socialnetwork.business.message.Message;
import com.socialnetwork.business.message.MessageService;
import com.socialnetwork.business.user.User;
import com.socialnetwork.business.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api")
@Tag(name = "Messages")
public class MessageController {
    private final MessageService messageService;
    private final UserService userService;

    MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @Operation(
            summary = "Отправить сообщение пользователю",
            responses = {
                    @ApiResponse(
                            description = "Сообщение отправлено",
                            responseCode = "201"
                    ),
                    @ApiResponse(
                            description = "Сообщение пустое",
                            responseCode = "400"
                    ),
                    @ApiResponse(
                            description = "Получатель не найден",
                            responseCode = "404"
                    ),
                    @ApiResponse(
                            description = "Отправлять сообщения можно только друзьям",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping(value = {"{userId}/message", "dialog/{userId}"})
    public ResponseEntity<Message> sendMessage(@PathVariable("userId") Long userId, @NotBlank String text,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        User receiver = getUserIfExists(userId);
        User sender = userService.getUserByUsername(userDetails.getUsername()).get();
        checkForFriendship(sender, receiver);
        Message  message = new Message(sender, receiver, text);
        return new ResponseEntity<>(messageService.sendMessage(message), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Открыть диалог с пользователем",
            responses = {
                    @ApiResponse(
                            description = "Диалог получен",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Пользователь не найден",
                            responseCode = "404"
                    )
            }
    )
    @GetMapping("dialog/{userId}")
    @ResponseStatus(code = HttpStatus.OK)
    public List<Message> getDialog(@PathVariable("userId") Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        User receiver = getUserIfExists(userId);
        User sender = userService.getUserByUsername(userDetails.getUsername()).get();
        return messageService.getDialogWithUser(sender, receiver);
    }


    private User getUserIfExists(Long userId) {
        return userService.getUserById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such user"));
    }

    private void checkForFriendship(User u1, User u2) {
        if ((u1.getSubscriptions().contains(u2) && u2.getSubscriptions().contains(u1)))
            return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can text only to your friends");
    }
}
