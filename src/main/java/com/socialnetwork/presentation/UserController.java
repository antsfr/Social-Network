package com.socialnetwork.presentation;

import com.socialnetwork.business.friendship.FriendshipService;
import com.socialnetwork.config.security.jwt.JwtResponse;
import com.socialnetwork.business.user.LoginRequest;
import com.socialnetwork.config.security.jwt.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Validated
@RequestMapping("/api")
@Tag(name = "Users")
public class UserController {

    @Autowired
    private FriendshipService friendshipService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;


    @Operation(
            summary = "Зарегистрироваться",
            responses = {
                    @ApiResponse(
                            description = "Учетная запись создана",
                            responseCode = "201"
                    ),
                    @ApiResponse(
                            description = "Указанные данные невалидны / E-mail или username заняты",
                            responseCode = "400"
                    )
            }
    )
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<Map<String, Long>> registerUser(@Valid @RequestBody User user) {
        if (userService.getUserByEmail(user.getEmail()).isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with this EMAIL is already registered");
        if (userService.getUserByUsername(user.getUsername()).isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with this USERNAME is already registered");

        return new ResponseEntity<>(Collections.singletonMap("id", userService.addUser(user)), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Авторизоваться",
            responses = {
                    @ApiResponse(
                            description = "Вход выполнен",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Указанные данные неверны / невалидны",
                            responseCode = "400"
                    )
            }
    )
    @SecurityRequirements
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

    @Operation(
            summary = "Подписаться на пользователя",
            responses = {
                    @ApiResponse(
                            description = "Подписка выполнена",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Пользователь не найден",
                            responseCode = "404"
                    ),
                    @ApiResponse(
                            description = "Подписка на себя / Повторная подписка невозможны",
                            responseCode = "400"
                    )
            }
    )
    @PostMapping("user/{userId}/subscribe")
    @ResponseStatus(code = HttpStatus.OK)
    public void subscribe(@PathVariable("userId") Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        User receiver = getUserIfExists(userId);
        User subscriber = userService.getUserByUsername(userDetails.getUsername()).get();
        checkForSelfSubscription(subscriber, receiver);
        checkForRepetitiveSubscription(subscriber, receiver);

        if (existsRequestFromReceiver(subscriber, receiver)) {
            friendshipService.acceptFriendshipRequest(receiver, subscriber);
            return;
        }
        friendshipService.sendFriendshipRequest(subscriber, receiver);
    }

    @Operation(
            summary = "Отписаться от пользователя",
            responses = {
                    @ApiResponse(
                            description = "Отпиписка выполнена",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Пользователь не найден",
                            responseCode = "404"
                    ),
                    @ApiResponse(
                            description = "Отписаться от того, на кого подписки нет, невозможно",
                            responseCode = "400"
                    )
            }
    )
    @PostMapping("user/{userId}/unsubscribe")
    @ResponseStatus(code = HttpStatus.OK)
    public void unsubscribe(@PathVariable("userId") Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        User userToUnsubscribeFrom = getUserIfExists(userId);
        User subscriber = userService.getUserByUsername(userDetails.getUsername()).get();
        checkForSelfSubscription(subscriber, userToUnsubscribeFrom);
        checkIfNotSubscribed(subscriber, userToUnsubscribeFrom);

        if (areFriends(subscriber, userToUnsubscribeFrom)) {
            friendshipService.quitFriendship(subscriber, userToUnsubscribeFrom);
            return;
        }
        friendshipService.quitSubscription(subscriber, userToUnsubscribeFrom);
    }


    private User getUserIfExists(Long userId) {
        return userService.getUserById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such user"));
    }

    private void checkForSelfSubscription(User u1, User u2) {
        if (u1.getId().equals(u2.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Self-subscription is not supported");
    }

    private void checkForRepetitiveSubscription(User subscriber, User subscription) {
        if (subscriber.getSubscriptions().contains(subscription))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are already subscribed");
    }

    private void checkIfNotSubscribed(User subscriber, User subscription) {
        if (!subscriber.getSubscriptions().contains(subscription))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not subscribed");
    }

    private boolean existsRequestFromReceiver(User sender, User receiver) {
        return receiver.getSubscriptions().contains(sender);
    }

    private boolean areFriends(User u1, User u2) {
        return u2.getSubscriptions().contains(u1) && u1.getSubscriptions().contains(u2);
    }

}
