package com.example.quickbite.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pelanggan")
public class Pelanggan {

    @Id
    @Column(name = "id_pelanggan", length = 36, nullable = false)
    private String idPelanggan;

    @Column(nullable = false, length = 100)
    private String nama;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "nomor_telepon", length = 20)
    private String nomorTelepon;

    @Column(columnDefinition = "TEXT")
    private String alamat;

    @OneToMany(
        mappedBy = "pelanggan",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    private List<Pesanan> daftarPesanan = new ArrayList<>();

    // Constructor kosong untuk JPA
    protected Pelanggan() {
    }

    // Constructor untuk membuat objek Pelanggan
    public Pelanggan(
            String idPelanggan,
            String nama,
            String email,
            String nomorTelepon,
            String alamat
    ) {
        this.idPelanggan = idPelanggan;
        this.nama = nama;
        this.email = email;
        this.nomorTelepon = nomorTelepon;
        this.alamat = alamat;
    }

    // Menampilkan informasi pelanggan
    public String getInfoPelanggan() {
        return String.format(
            "%s (%s) - %s",
            this.nama,
            this.email,
            this.nomorTelepon
        );
    }

    // Mengubah alamat pelanggan
    public void ubahAlamat(String alamatBaru) {
        if (alamatBaru == null || alamatBaru.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Alamat baru tidak boleh kosong."
            );
        }

        this.alamat = alamatBaru;
    }

    // Getter dan setter
    public String getIdPelanggan() {
        return idPelanggan;
    }

    public void setIdPelanggan(String idPelanggan) {
        this.idPelanggan = idPelanggan;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    public void setNomorTelepon(String nomorTelepon) {
        this.nomorTelepon = nomorTelepon;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public List<Pesanan> getDaftarPesanan() {
        return daftarPesanan;
    }

    public void setDaftarPesanan(List<Pesanan> daftarPesanan) {
        this.daftarPesanan = daftarPesanan;
    }
}