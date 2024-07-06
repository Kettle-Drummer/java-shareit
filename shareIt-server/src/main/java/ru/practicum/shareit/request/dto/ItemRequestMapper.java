package ru.practicum.shareit.request.dto;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface ItemRequestMapper {

    ItemRequestMapper INSTANCE = Mappers.getMapper(ItemRequestMapper.class);

    ItemRequest toItemRequest(ItemRequestDto dto);

    @AfterMapping
    default void setCreatedTime(@MappingTarget ItemRequest target) {
        target.setCreated(LocalDateTime.now());
    }

    @Mappings({
            @Mapping(target = "requesterId", source = "source.requester.id"),
            @Mapping(target = "items", ignore = true)
    })
    ItemRequestDto toItemRequestDto(ItemRequest source);

    List<ItemRequestDto> toItemRequestDtoList(List<ItemRequest> source);
}
