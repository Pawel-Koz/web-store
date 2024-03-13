package com.pkozlowski.webstore.rest;

import com.pkozlowski.webstore.model.dto.UserDto;
import com.pkozlowski.webstore.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService userService) {
        this.service = userService;
    }

    @PostMapping
    public ResponseEntity<String> save(@RequestBody UserDto userDto) {
        service.save(userDto);
        return new ResponseEntity<>("User saved to database", HttpStatus.OK);
    }
}
