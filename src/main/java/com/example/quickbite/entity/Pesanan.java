package com.example.quickbite.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pesanan")
public class Pesanan {

    @Id
    @Column(name = "id_pesanan", length = 36, nullable = false)
    private String idPesanan;

    @Column(nullable = false)
    private LocalDateTime tanggal;

    @Column(name = "total_biaya", nullable = false)
    private double totalBiaya;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPesanan status = StatusPesanan.MENUNGGU_BAYAR;

    // Relasi: banyak pesanan dapat dimiliki satu pelanggan
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pelanggan", nullable = false)
    private Pelanggan pelanggan;

    // Komposisi: item pesanan mengikuti siklus hidup pesanan
    @OneToMany(
        mappedBy = "pesanan",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ItemPesanan> daftarItem = new ArrayList<>();

    // Satu pesanan memiliki maksimal satu pembayaran
    @OneToOne(
        mappedBy = "pesanan",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Pembayaran pembayaran;

    // Constructor kosong untuk JPA
    protected Pesanan() {
    }

    // Constructor untuk membuat pesanan baru
    public Pesanan(
            String idPesanan,
            LocalDateTime tanggal,
            Pelanggan pelanggan
    ) {
        this.idPesanan = idPesanan;
        this.tanggal = tanggal;
        this.pelanggan = pelanggan;
        this.status = StatusPesanan.MENUNGGU_BAYAR;
        this.totalBiaya = 0.0;
    }

    // Menambahkan item ke dalam pesanan
    public void tambahItem(ItemPesanan item) {
        if (item != null) {
            item.setPesanan(this);
            daftarItem.add(item);
            item.hitungSubtotal();
            hitungTotal();
        }
    }

    // Menghitung total seluruh item
    public double hitungTotal() {
        this.totalBiaya = daftarItem.stream()
            .mapToDouble(ItemPesanan::hitungSubtotal)
            .sum();

        return this.totalBiaya;
    }

    // Mengubah status pesanan
    public void updateStatus(StatusPesanan statusBaru) {
        if (statusBaru == null) {
            throw new IllegalArgumentException(
                "Status pesanan tidak boleh kosong."
            );
        }

        this.status = statusBaru;
    }

    // Menghubungkan pembayaran dengan pesanan
    public void setPembayaran(Pembayaran pembayaran) {
        this.pembayaran = pembayaran;

        if (pembayaran != null
                && pembayaran.getPesanan() != this) {
            pembayaran.setPesanan(this);
        }
    }

    // Getter dan setter
    public String getIdPesanan() {
        return idPesanan;
    }

    public void setIdPesanan(String idPesanan) {
        this.idPesanan = idPesanan;
    }

    public LocalDateTime getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDateTime tanggal) {
        this.tanggal = tanggal;
    }

    public double getTotalBiaya() {
        return totalBiaya;
    }

    public void setTotalBiaya(double totalBiaya) {
        this.totalBiaya = totalBiaya;
    }

    public StatusPesanan getStatus() {
        return status;
    }

    public void setStatus(StatusPesanan status) {
        this.status = status;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public void setPelanggan(Pelanggan pelanggan) {
        this.pelanggan = pelanggan;
    }

    public List<ItemPesanan> getDaftarItem() {
        return daftarItem;
    }

    public void setDaftarItem(List<ItemPesanan> daftarItem) {
        this.daftarItem = daftarItem != null
            ? daftarItem
            : new ArrayList<>();

        for (ItemPesanan item : this.daftarItem) {
            item.setPesanan(this);
        }

        hitungTotal();
    }

    public Pembayaran getPembayaran() {
        return pembayaran;
    }
}