package com.pkozlowski.webstore.service.impl;

import com.pkozlowski.webstore.mapper.UserMapper;
import com.pkozlowski.webstore.model.User;
import com.pkozlowski.webstore.model.dto.UserDto;
import com.pkozlowski.webstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper mapper;
    @Mock
    private PasswordEncoder encoder;
    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setup() {
        this.userDto =  new UserDto("pawel@mail", "pass");
        this.user = new User("pawel@mail", "pass");
    }

    @Test
    public void shouldSaveUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(mapper.toEntity(any(UserDto.class))).thenReturn(user);
        userService.save(userDto);

        verify(userRepository, times(1)).findByEmail("pawel@mail");
        verify(mapper, times(1)).toEntity(userDto);
        verify(encoder, times(1)).encode("pass");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void save_should_throw_Exception_when_user_present_in_database() {
        User user = new User("pawel@mail", "pass");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        Exception thrown = assertThrows(RuntimeException.class,
                () -> userService.save(userDto));
        assertEquals("User with email: pawel@mail already exists", thrown.getMessage());
    }

    @Test
    public void save_should_throw_Exception_when_userDto_is_null() {
        Exception thrown = assertThrows(RuntimeException.class, () -> userService.save(null));
        assertEquals("User to save is null", thrown.getMessage());
    }

    @Test
    public void save_should_throw_Exception_when_email_is_blank() {
        UserDto userDto1 = new UserDto(" ", "passas");
        Exception thrown = assertThrows(RuntimeException.class, () -> userService.save(userDto1));
        assertEquals(String.format("Illegal argument:%s", userDto1), thrown.getMessage());
    }

    @Test
    public void save_should_throw_Exception_when_password_is_blank() {
        UserDto userDto1 = new UserDto("dwdwdw", " ");
        Exception thrown = assertThrows(RuntimeException.class, () -> userService.save(userDto1));
        assertEquals(String.format("Illegal argument:%s", userDto1), thrown.getMessage());
    }
}