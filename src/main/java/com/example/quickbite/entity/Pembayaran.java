package com.example.quickbite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pembayaran")
public class Pembayaran {

    @Id
    @Column(name = "id_pembayaran", length = 36, nullable = false)
    private String idPembayaran;

    @Column(name = "jumlah_bayar", nullable = false)
    private double jumlahBayar;

    @Column(nullable = false, length = 50)
    private String metode;

    @Column(name = "status_bayar", nullable = false, length = 50)
    private String statusBayar;

    @Column(name = "kode_referensi", length = 100)
    private String kodeReferensi;

    // Satu pembayaran terkait dengan satu pesanan
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_pesanan",
        nullable = false,
        unique = true
    )
    private Pesanan pesanan;

    // Constructor kosong untuk JPA
    protected Pembayaran() {
    }

    // Constructor untuk membuat objek Pembayaran
    public Pembayaran(
            String idPembayaran,
            Pesanan pesanan,
            double jumlahBayar,
            String metode,
            String statusBayar,
            String kodeReferensi
    ) {
        this.idPembayaran = idPembayaran;
        this.pesanan = pesanan;
        this.jumlahBayar = jumlahBayar;
        this.metode = metode;
        this.statusBayar = statusBayar;
        this.kodeReferensi = kodeReferensi;
    }

    // Memeriksa keberhasilan pembayaran
    public boolean prosesVerifikasi() {
        return "SUCCESS".equalsIgnoreCase(this.statusBayar)
            && this.kodeReferensi != null
            && !this.kodeReferensi.isEmpty();
    }

    // Getter dan setter
    public String getIdPembayaran() {
        return idPembayaran;
    }

    public void setIdPembayaran(String idPembayaran) {
        this.idPembayaran = idPembayaran;
    }

    public double getJumlahBayar() {
        return jumlahBayar;
    }

    public void setJumlahBayar(double jumlahBayar) {
        this.jumlahBayar = jumlahBayar;
    }

    public String getMetode() {
        return metode;
    }

    public void setMetode(String metode) {
        this.metode = metode;
    }

    public String getStatusBayar() {
        return statusBayar;
    }

    public void setStatusBayar(String statusBayar) {
        this.statusBayar = statusBayar;
    }

    public String getKodeReferensi() {
        return kodeReferensi;
    }

    public void setKodeReferensi(String kodeReferensi) {
        this.kodeReferensi = kodeReferensi;
    }

    public Pesanan getPesanan() {
        return pesanan;
    }

    public void setPesanan(Pesanan pesanan) {
        this.pesanan = pesanan;
    }
}