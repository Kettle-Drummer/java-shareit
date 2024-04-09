package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.error.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Component
public class ItemRepository {
    private final Map<Long, Item> storageItem = new HashMap<>();
    private Long idGen = 1L;

    private Long idGen() {
    return idGen++;
}

    public Item create(Item item) {
        item.setId(idGen());
        storageItem.put(item.getId(), item);
        return item;
    }


    public Item update(Item item) {
        if (!storageItem.containsKey(item.getId())) {
            throw new EntityNotFoundException(String.format("Обновление невозможно %s не сущесвует", item));
        }
        storageItem.put(item.getId(), item);
        return item;
    }


    public Item getById(Long id) {
        if (storageItem.containsKey(id)) {
            return storageItem.get(id);
        }
        throw new EntityNotFoundException("Нет вещи с таким id");
    }

    public Collection<ItemDto> getByUser(Long id) {
        List<ItemDto> list = new ArrayList<>();
        for (Item item : storageItem.values()) {
            if (Objects.equals(item.getOwner().getId(), id)) {
                list.add(ItemMapper.toItemDto(item));
            }
        }
        return list;
    }


    public void delete(Item item) {
        if (storageItem.containsKey(item.getId())) {
            storageItem.remove(item.getId());
            return;
        }
        throw new EntityNotFoundException(String.format("Удаление невозможно %s не сущесвует", item));
    }

    public List<ItemDto> getBySearch(String textQuery) {
        List<ItemDto> items = new ArrayList<>();
        for (Item item : storageItem.values()) {
            if ((item.getName().toLowerCase().contains(textQuery)
                    || item.getDescription().toLowerCase().contains(textQuery))
                    && item.getAvailable()) {
                items.add(ItemMapper.toItemDto(item));
            }
        }
        return items;
    }
}


