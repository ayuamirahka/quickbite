package com.example.quickbite.boundary.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.quickbite.boundary.PaymentGatewayAPI;

@Component
public class PaymentGatewayAPIImpl implements PaymentGatewayAPI {

    @Value("${payment.gateway.endpoint-url}")
    private String endpointUrl;

    @Value("${payment.gateway.api-key:}")
    private String apiKey;

    @Override
    public String mintaTransaksiPembayaran(
            double total,
            String tokenKartu
    ) {
        if (total <= 0) {
            throw new IllegalArgumentException(
                "Total pembayaran harus lebih dari 0."
            );
        }

        if (tokenKartu == null || tokenKartu.isBlank()) {
            throw new IllegalArgumentException(
                "Token kartu tidak boleh kosong."
            );
        }

        System.out.println(
            "== [BOUNDARY LOG] Simulasi Gateway Pembayaran =="
        );
        System.out.println("Endpoint konfigurasi: " + endpointUrl);
        System.out.println("Total pembayaran: Rp" + total);

        // Token ini sengaja digunakan untuk menguji penolakan pembayaran.
        if ("invalid_card".equalsIgnoreCase(tokenKartu)) {
            throw new RuntimeException(
                "Penolakan dari Gateway Pembayaran: "
                + "saldo tidak cukup atau token kartu tidak sah."
            );
        }

        // Membuat referensi transaksi tiruan.
        return "PG-REF-" + UUID.randomUUID()
            .toString()
            .substring(0, 8)
            .toUpperCase();
    }
}