package com.example.quickbite.service;

import java.util.List;

import com.example.quickbite.dto.request.CheckoutRequest;
import com.example.quickbite.dto.response.CheckoutResponse;
import com.example.quickbite.dto.response.MenuResponse;

public interface CheckoutService {

    List<MenuResponse> daftarMenu();

    CheckoutResponse prosesCheckout(CheckoutRequest request);

    void batalkanTransaksi(String idPesanan, String alasan);
}