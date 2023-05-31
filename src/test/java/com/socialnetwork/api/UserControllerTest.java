//package com.socialnetwork.api;
//
//import com.socialnetwork.SocialNetworkApplication;
//import com.socialnetwork.business.user.User;
//import com.socialnetwork.business.user.UserService;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.skyscreamer.jsonassert.JSONAssert;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.web.server.LocalServerPort;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//
//@AutoConfigureMockMvc
//@SpringBootTest(classes = SocialNetworkApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class UserControllerTest {
//    @LocalServerPort
//    private int port;
//
//    TestRestTemplate restTemplate = new TestRestTemplate();
//    HttpHeaders headers = new HttpHeaders();
//    private String createURLWithPort(String uri) {
//        return "http://localhost:" + port + uri;
//    }
//
//    @Autowired
//    private MockMvc mockMvc;
//    @MockBean
//    UserService userService;
//
//    @Test
//    public void NAMEME() {
//        User user = new User();
//        user.setEmail("user1@gmail.com");
//        user.setUsername("user1");
//        user.setPassword("324234123");
//
//        HttpEntity<User> userHttpEntity = new HttpEntity<User>(user, headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(
//                createURLWithPort("/api/register/"),
//                HttpMethod.POST, userHttpEntity, String.class);
//
//        Assertions.assertEquals(400, response.getStatusCodeValue());
//
//
//    }
