package com.socialnetwork.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.socialnetwork.business.user.User;
import com.socialnetwork.business.user.UserService;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<Map<String, Long>> registerUser(@Valid @RequestBody User user) {
        if (userService.getUserByEmail(user.getEmail()).isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with this EMAIL is already registered");
        if (userService.getUserByUsername(user.getUsername()).isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with this USERNAME is already registered");

        return ResponseEntity.ok(Collections.singletonMap("id", userService.addUser(user)));
    }

    @PostMapping("{userId}/subscribe")
    @ResponseStatus(code = HttpStatus.OK)
    public void subscribe(@PathVariable("userId") Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        User userReceivingSubscription = getUserIfExists(userId);
        User userSubscribing = userService.getUserByUsername(userDetails.getUsername()).get();
        checkForSelfSubscription(userReceivingSubscription, userSubscribing);

        userReceivingSubscription.getSubscribers().add(userSubscribing.getId());
        userSubscribing.getSubscriptions().add(userReceivingSubscription.getId());

        userService.updateUser(userSubscribing);
        userService.updateUser(userReceivingSubscription);

    }

    @PostMapping("{userId}/unsubscribe")
    @ResponseStatus(code = HttpStatus.OK)
    public void unsubscribe(@PathVariable("userId") Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        User userReceivingSubscription = getUserIfExists(userId);
        User userSubscribing = userService.getUserByUsername(userDetails.getUsername()).get();
        checkForSelfSubscription(userReceivingSubscription, userSubscribing);

        userReceivingSubscription.getSubscribers().remove(userSubscribing.getId());
        userSubscribing.getSubscriptions().remove(userReceivingSubscription.getId());

        userService.updateUser(userSubscribing);
        userService.updateUser(userReceivingSubscription);
    }

    @GetMapping("{userId}/subscriptions")
    public void getSubscriptions(@PathVariable("userId") Long userId) {

    }

//    @PostMapping("{userId}/dialog")
//    @ResponseStatus(code = HttpStatus.OK)
//    public void unsubscribe(@PathVariable("userId") Long userId, @AuthenticationPrincipal UserDetails userDetails,
//                            String message) {
//        User receiver = getUserIfExists(userId);
//        User sender = userService.getUserByUsername(userDetails.getUsername()).get();
//        checkForFriendship(receiver, sender);
//    }


    private User getUserIfExists(Long userId) {
        return userService.getUserById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No such user"));
    }
    private void checkForSelfSubscription(User u1, User u2) {
        if (u1.getId().equals(u2.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Self-subscription is not supported");
    }

    private void checkForFriendship(User u1, User u2) {
        if (!(u1.getSubscriptions().contains(u2.getId()) && u2.getSubscriptions().contains(u1.getId())));
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can text only to your friends");
    }

}
