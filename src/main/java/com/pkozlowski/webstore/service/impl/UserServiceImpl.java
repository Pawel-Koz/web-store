package com.pkozlowski.webstore.service.impl;

import com.pkozlowski.webstore.mapper.UserMapper;
import com.pkozlowski.webstore.model.Role;
import com.pkozlowski.webstore.model.User;
import com.pkozlowski.webstore.model.dto.UserDto;
import com.pkozlowski.webstore.repository.UserRepository;
import com.pkozlowski.webstore.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper mapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void save(UserDto userDto) {
        if (userDto == null) {
            throw new RuntimeException("User to save is null");
        }
        if ( userDto.getEmail().isBlank() || userDto.getPassword().isBlank()) {
            throw new RuntimeException(String.format("Illegal argument:%s", userDto));
        }
        Optional<User> user = userRepository.findByEmail(userDto.getEmail());
        if (user.isPresent()) {
            throw new RuntimeException(String.format("User with email: %s already exists", userDto.getEmail()));
        }
        User userToSave = mapper.toEntity(userDto);
        Role role = new Role("USER");
        userToSave.setRoles(Collections.singleton(role));
        userToSave.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(userToSave);
    }
}
