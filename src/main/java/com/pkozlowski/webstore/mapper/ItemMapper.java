package com.pkozlowski.webstore.mapper;

import com.pkozlowski.webstore.model.CartItem;
import com.pkozlowski.webstore.model.CheckedItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "totalPrice", source = "price")
    CheckedItem toCheckedItem(CartItem cartItem);
}
