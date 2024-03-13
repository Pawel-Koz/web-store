package com.pkozlowski.webstore.mapper;

import com.pkozlowski.webstore.model.User;
import com.pkozlowski.webstore.model.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto userDto);
}
