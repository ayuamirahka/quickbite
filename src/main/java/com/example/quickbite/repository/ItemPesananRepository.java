package com.example.quickbite.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quickbite.entity.ItemPesanan;

public interface ItemPesananRepository
        extends JpaRepository<ItemPesanan, String> {
}