package com.pkozlowski.webstore.service.impl;

import com.pkozlowski.webstore.model.Item;
import com.pkozlowski.webstore.repository.ItemRepository;
import com.pkozlowski.webstore.service.ItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;

    public ItemServiceImpl(ItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Item> findAll() {
        return repository.findAll();
    }
}
