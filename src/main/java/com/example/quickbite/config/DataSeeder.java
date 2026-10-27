package com.example.quickbite.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.quickbite.entity.Menu;
import com.example.quickbite.entity.Pelanggan;
import com.example.quickbite.repository.MenuRepository;
import com.example.quickbite.repository.PelangganRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
            PelangganRepository pelangganRepository,
            MenuRepository menuRepository) {

        return args -> {

            // 1. Tambahkan pelanggan awal jika belum tersedia
            if (!pelangganRepository.existsById("CUST-001")) {
                Pelanggan pelanggan = new Pelanggan(
                    "CUST-001",
                    "Budi Santoso",
                    "budi@example.com",
                    "08123456789",
                    "Jl. Sudirman No. 45, Jakarta"
                );

                pelangganRepository.save(pelanggan);
                System.out.println("Data pelanggan berhasil ditambahkan.");
            }

            // 2. Tambahkan menu pertama jika belum tersedia
            if (!menuRepository.existsById("MENU-01")) {
                Menu kopi = new Menu(
                    "MENU-01",
                    "Kopi Susu Gula Aren",
                    18000.0,
                    20
                );

                menuRepository.save(kopi);
                System.out.println("Data menu kopi berhasil ditambahkan.");
            }

            // 3. Tambahkan menu kedua jika belum tersedia
            if (!menuRepository.existsById("MENU-02")) {
                Menu croissant = new Menu(
                    "MENU-02",
                    "Croissant Butter",
                    25000.0,
                    10
                );

                menuRepository.save(croissant);
                System.out.println("Data menu croissant berhasil ditambahkan.");
            }
        };
    }
}