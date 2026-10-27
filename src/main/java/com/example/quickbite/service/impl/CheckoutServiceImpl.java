package com.example.quickbite.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quickbite.boundary.PaymentGatewayAPI;
import com.example.quickbite.dto.request.CheckoutItemRequest;
import com.example.quickbite.dto.request.CheckoutRequest;
import com.example.quickbite.dto.response.CheckoutResponse;
import com.example.quickbite.dto.response.MenuResponse;
import com.example.quickbite.entity.ItemPesanan;
import com.example.quickbite.entity.Menu;
import com.example.quickbite.entity.Pelanggan;
import com.example.quickbite.entity.Pembayaran;
import com.example.quickbite.entity.Pesanan;
import com.example.quickbite.entity.StatusPesanan;
import com.example.quickbite.exception.BusinessValidationException;
import com.example.quickbite.exception.ResourceNotFoundException;
import com.example.quickbite.repository.MenuRepository;
import com.example.quickbite.repository.PelangganRepository;
import com.example.quickbite.repository.PesananRepository;
import com.example.quickbite.service.CheckoutService;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private final PelangganRepository pelangganRepository;
    private final MenuRepository menuRepository;
    private final PesananRepository pesananRepository;
    private final PaymentGatewayAPI paymentGatewayAPI;

    public CheckoutServiceImpl(
            PelangganRepository pelangganRepository,
            MenuRepository menuRepository,
            PesananRepository pesananRepository,
            PaymentGatewayAPI paymentGatewayAPI
    ) {
        this.pelangganRepository = pelangganRepository;
        this.menuRepository = menuRepository;
        this.pesananRepository = pesananRepository;
        this.paymentGatewayAPI = paymentGatewayAPI;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResponse> daftarMenu() {
        return menuRepository.findAll()
                .stream()
                .map(menu -> MenuResponse.builder()
                        .idMenu(menu.getIdMenu())
                        .namaMenu(menu.getNamaMenu())
                        .harga(menu.getHarga())
                        .stok(menu.getStok())
                        .build())
                .toList();
    }
    
    @Override
    @Transactional
    public CheckoutResponse prosesCheckout(CheckoutRequest request) {

        // 1. Validasi data keranjang
        if (request == null
                || request.getKeranjangData() == null
                || request.getKeranjangData().isEmpty()) {
            throw new BusinessValidationException(
                "Keranjang belanja tidak boleh kosong."
            );
        }

        // 2. Cari pelanggan berdasarkan ID
        Pelanggan pelanggan = pelangganRepository
                .findById(request.getIdPelanggan())
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Pelanggan dengan ID "
                        + request.getIdPelanggan()
                        + " tidak ditemukan."
                    )
                );

        // 3. Bentuk pesanan baru
        Pesanan pesanan = new Pesanan(
            "ORD-" + UUID.randomUUID()
                    .toString().substring(0, 8).toUpperCase(),
            LocalDateTime.now(),
            pelanggan
        );

        // 4. Validasi menu, stok, dan susun item pesanan
        for (CheckoutItemRequest itemRequest
                : request.getKeranjangData()) {

            if (itemRequest == null
                    || itemRequest.getIdMenu() == null
                    || itemRequest.getJumlah() == null
                    || itemRequest.getJumlah() <= 0) {
                throw new BusinessValidationException(
                    "Data menu dan jumlah pesanan tidak valid."
                );
            }

            Menu menu = menuRepository
                    .findByIdForUpdate(itemRequest.getIdMenu())
                    .orElseThrow(() ->
                        new ResourceNotFoundException(
                            "Menu dengan ID "
                            + itemRequest.getIdMenu()
                            + " tidak ditemukan."
                        )
                    );

            int jumlah = itemRequest.getJumlah();

            if (!menu.validasiStok(jumlah)) {
                throw new BusinessValidationException(
                    "Stok tidak mencukupi untuk menu: "
                    + menu.getNamaMenu()
                    + " (Sisa: " + menu.getStok() + ")"
                );
            }

            // Kurangi stok dalam transaksi database
            if (!menu.kurangStok(jumlah)) {
                throw new BusinessValidationException(
                    "Gagal mengurangi stok menu: "
                    + menu.getNamaMenu()
                );
            }

            menuRepository.save(menu);

            // Buat item dan hubungkan ke pesanan
            ItemPesanan item = new ItemPesanan(
                "ITEM-" + UUID.randomUUID()
                        .toString().substring(0, 8).toUpperCase(),
                jumlah,
                menu
            );

            pesanan.tambahItem(item);
        }

        // 5. Hitung total biaya pesanan
        double total = pesanan.hitungTotal();

        if (total <= 0) {
            throw new BusinessValidationException(
                "Total pesanan harus lebih dari 0."
            );
        }

        // 6. Minta gateway memproses pembayaran simulasi
        String kodeReferensi;

        try {
            kodeReferensi = paymentGatewayAPI
                    .mintaTransaksiPembayaran(
                        total,
                        request.getTokenKartu()
                    );
        } catch (RuntimeException ex) {
            throw new BusinessValidationException(
                "Pembayaran gagal diproses: "
                + ex.getMessage()
            );
        }

        // 7. Buat entitas pembayaran
        Pembayaran pembayaran = new Pembayaran(
            "PAY-" + UUID.randomUUID()
                    .toString().substring(0, 8).toUpperCase(),
            pesanan,
            total,
            request.getMetodePembayaran(),
            "SUCCESS",
            kodeReferensi
        );

        // 8. Verifikasi hasil simulasi pembayaran
        if (!pembayaran.prosesVerifikasi()) {
            throw new BusinessValidationException(
                "Verifikasi pembayaran gagal."
            );
        }

        // 9. Hubungkan pembayaran dan perbarui status pesanan
        pesanan.setPembayaran(pembayaran);
        pesanan.updateStatus(StatusPesanan.DIPROSES);

        // 10. Simpan pesanan beserta item dan pembayaran
        Pesanan savedPesanan = pesananRepository.save(pesanan);

        // 11. Bentuk respons checkout
        return CheckoutResponse.builder()
                .idPesanan(savedPesanan.getIdPesanan())
                .idPembayaran(pembayaran.getIdPembayaran())
                .totalBiaya(savedPesanan.getTotalBiaya())
                .statusPesanan(savedPesanan.getStatus())
                .statusPembayaran(pembayaran.getStatusBayar())
                .kodeReferensi(pembayaran.getKodeReferensi())
                .tanggal(savedPesanan.getTanggal())
                .build();
    }

    @Override
    @Transactional
    public void batalkanTransaksi(
            String idPesanan,
            String alasan
    ) {

        // 1. Validasi alasan pembatalan
        if (alasan == null || alasan.isBlank()) {
            throw new BusinessValidationException(
                "Alasan pembatalan harus disertakan."
            );
        }

        // 2. Cari pesanan
        Pesanan pesanan = pesananRepository
                .findById(idPesanan)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Pesanan dengan ID "
                        + idPesanan
                        + " tidak ditemukan."
                    )
                );

        // 3. Cegah pembatalan berulang
        if (pesanan.getStatus() == StatusPesanan.BATAL) {
            throw new BusinessValidationException(
                "Pesanan sudah dibatalkan sebelumnya."
            );
        }

        // 4. Pesanan yang sudah dikirim atau selesai tidak dapat dibatalkan
        if (pesanan.getStatus() == StatusPesanan.DIKIRIM
                || pesanan.getStatus() == StatusPesanan.SELESAI) {
            throw new BusinessValidationException(
                "Pesanan sudah dikirim atau selesai "
                + "dan tidak dapat dibatalkan."
            );
        }

        // 5. Kembalikan stok dari seluruh item pesanan
        for (ItemPesanan item : pesanan.getDaftarItem()) {

            Menu menu = item.getMenu();

            menu.setStok(menu.getStok() + item.getJumlah());

            menuRepository.save(menu);
        }

        // 6. Ubah status pesanan menjadi BATAL
        pesanan.updateStatus(StatusPesanan.BATAL);

        pesananRepository.save(pesanan);

        System.out.println(
            "Pesanan " + idPesanan
            + " dibatalkan. Alasan: " + alasan
        );
    }
}