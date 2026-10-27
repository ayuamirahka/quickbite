package com.example.quickbite.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quickbite.entity.Pembayaran;

public interface PembayaranRepository
        extends JpaRepository<Pembayaran, String> {
}