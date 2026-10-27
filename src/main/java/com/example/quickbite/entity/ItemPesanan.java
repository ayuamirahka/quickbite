package com.example.quickbite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_pesanan")
public class ItemPesanan {

    @Id
    @Column(name = "id_item", length = 36, nullable = false)
    private String idItem;

    @Column(nullable = false)
    private int jumlah;

    @Column(nullable = false)
    private double subtotal;

    // Komposisi: item merupakan bagian dari pesanan
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pesanan", nullable = false)
    private Pesanan pesanan;

    // Agregasi: item merujuk ke menu yang tetap berdiri sendiri
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_menu", nullable = false)
    private Menu menu;

    // Constructor kosong untuk JPA
    protected ItemPesanan() {
    }

    // Constructor untuk membuat item pesanan
    public ItemPesanan(String idItem, int jumlah, Menu menu) {
    this.idItem = idItem;
    this.jumlah = jumlah;
    this.menu = menu;

    if (this.menu != null) {
        this.subtotal = this.menu.getHarga() * this.jumlah;
    }
}

    // Menghitung subtotal berdasarkan harga menu dan jumlah
    public double hitungSubtotal() {
        if (this.menu != null) {
            this.subtotal = this.menu.getHarga() * this.jumlah;
        }

        return this.subtotal;
    }

    // Menghubungkan item dengan pesanan induknya
    public void setPesanan(Pesanan pesanan) {
        this.pesanan = pesanan;
    }

    // Getter dan setter
    public String getIdItem() {
        return idItem;
    }

    public void setIdItem(String idItem) {
        this.idItem = idItem;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException(
                "Jumlah item harus lebih dari 0."
            );
        }

        this.jumlah = jumlah;
        hitungSubtotal();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public Pesanan getPesanan() {
        return pesanan;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
        hitungSubtotal();
    }
}