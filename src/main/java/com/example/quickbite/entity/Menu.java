package com.example.quickbite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu")
public class Menu {

    @Id
    @Column(name = "id_menu", length = 36, nullable = false)
    private String idMenu;

    @Column(name = "nama_menu", nullable = false, length = 100)
    private String namaMenu;

    @Column(nullable = false)
    private double harga;

    @Column(nullable = false)
    private int stok;

    // Constructor kosong untuk JPA
    protected Menu() {
    }

    // Constructor untuk membuat objek Menu
    public Menu(String idMenu, String namaMenu, double harga, int stok) {
        this.idMenu = idMenu;
        this.namaMenu = namaMenu;
        this.harga = harga;
        this.stok = stok;
    }

    // Memeriksa apakah jumlah pesanan valid dan stok mencukupi
    public boolean validasiStok(int qty) {
        return qty > 0 && stok >= qty;
    }

    // Mengurangi stok jika jumlah pesanan valid
    public boolean kurangStok(int qty) {
        if (!validasiStok(qty)) {
            return false;
        }

        this.stok -= qty;
        return true;
    }

    // Getter dan setter
    public String getIdMenu() {
        return idMenu;
    }

    public void setIdMenu(String idMenu) {
        this.idMenu = idMenu;
    }

    public String getNamaMenu() {
        return namaMenu;
    }

    public void setNamaMenu(String namaMenu) {
        this.namaMenu = namaMenu;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        if (stok < 0) {
            throw new IllegalArgumentException(
                "Stok tidak boleh negatif."
            );
        }

        this.stok = stok;
    }
}