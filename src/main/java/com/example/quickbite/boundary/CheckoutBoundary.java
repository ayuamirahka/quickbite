package com.example.quickbite.boundary;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quickbite.dto.request.BatalTransaksiRequest;
import com.example.quickbite.dto.request.CheckoutRequest;
import com.example.quickbite.dto.response.ApiResponse;
import com.example.quickbite.dto.response.CheckoutResponse;
import com.example.quickbite.dto.response.MenuResponse;
import com.example.quickbite.service.CheckoutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutBoundary {

    private final CheckoutService checkoutService;

    public CheckoutBoundary(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    // Memproses checkout dan pembayaran
    @GetMapping("/menus")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> daftarMenu() {

        List<MenuResponse> menus = checkoutService.daftarMenu();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Daftar menu berhasil diambil",
                        menus
                )
        );
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<CheckoutResponse>> prosesCheckout(
            @Valid @RequestBody CheckoutRequest request) {

        CheckoutResponse response =
                checkoutService.prosesCheckout(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Checkout dan pembayaran berhasil diproses",
                        response
                ));
    }

    // Membatalkan pesanan
    @PostMapping("/{idPesanan}/batal")
    public ResponseEntity<ApiResponse<Void>> batalkanTransaksi(
            @PathVariable String idPesanan,
            @Valid @RequestBody BatalTransaksiRequest request) {

        checkoutService.batalkanTransaksi(
                idPesanan,
                request.getAlasan()
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>success(
                        "Transaksi berhasil dibatalkan",
                        null
                )
        );
    }
}