package com.example.quickbite.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.quickbite.entity.Menu;

public interface MenuRepository extends JpaRepository<Menu, String> {

    @Query(
        value = "SELECT * FROM menu WHERE id_menu = :id FOR UPDATE",
        nativeQuery = true
    )
    Optional<Menu> findByIdForUpdate(@Param("id") String id);
}