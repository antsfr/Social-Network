package com.socialnetwork.presentation;

import com.socialnetwork.security.jwt.JwtResponse;
import com.socialnetwork.business.user.LoginRequest;
import com.socialnetwork.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;


    @PostMapping("/register")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<Map<String, Long>> registerUser(@Valid @RequestBody User user) {
        if (userService.getUserByEmail(user.getEmail()).isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with this EMAIL is already registered");
        if (userService.getUserByUsername(user.getUsername()).isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with this USERNAME is already registered");

        return ResponseEntity.ok(Collections.singletonMap("id", userService.addUser(user)));
    }

    @PostMapping("/login")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginForm) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginForm.getUsername(), loginForm.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwt(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.getUserByUsername(userDetails.getUsername()).get();
            return ResponseEntity.ok(new JwtResponse(jwt, user.getId(), user.getUsername()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("{userId}/subscribe")
    @ResponseStatus(code = HttpStatus.OK)
    public void subscribe(@PathVariable("userId") Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        User userReceivingSubscription = getUserIfExists(userId);
        User userSubscribing = userService.getUserByUsername(userDetails.getUsername()).get();
        checkForSelfSubscription(userSubscribing, userReceivingSubscription);
        checkIfAlreadySubscribed(userSubscribing, userReceivingSubscription);

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
        checkForSelfSubscription(userSubscribing, userReceivingSubscription);
        checkIfNotSubscribed(userSubscribing, userReceivingSubscription);

        userReceivingSubscription.getSubscribers().remove(userSubscribing.getId());
        userSubscribing.getSubscriptions().remove(userReceivingSubscription.getId());

        userService.updateUser(userSubscribing);
        userService.updateUser(userReceivingSubscription);
    }

//    @GetMapping("{userId}/subscriptions")
//    public void getSubscriptions(@PathVariable("userId") Long userId) {
//
//    }

    private User getUserIfExists(Long userId) {
        return userService.getUserById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No such user"));
    }
    private void checkForSelfSubscription(User u1, User u2) {
        if (u1.getId().equals(u2.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Self-subscription is not supported");
    }
    private void checkIfAlreadySubscribed(User subscriber, User subscription) {
        if (subscriber.getSubscriptions().contains(subscription.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are already subscribed");
    }
    private void checkIfNotSubscribed(User subscriber, User subscription) {
        if (!subscriber.getSubscriptions().contains(subscription.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not subscribed");
    }

}
