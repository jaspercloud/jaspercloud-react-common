package io.jaspercloud.react.http.client.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class SpringbootTest {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootTest.class, args);
    }

    @Autowired
    private ApplicationContext context;

    @GetMapping("/test1")
    public ResponseEntity<String> test1() {
        return ResponseEntity.ok("test");
    }

    @GetMapping("/test2")
    public ResponseEntity<byte[]> test2() {
        return ResponseEntity.ok("test".getBytes());
    }

    @GetMapping("/test3")
    public ResponseEntity<User> test3() {
        User user = new User();
        user.username = "test";
        user.password = "test";
        return ResponseEntity.ok(user);
    }

    public static class User {

        String username;
        String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
