package com.example.quickbite.dto.response;

import java.time.LocalDateTime;

import com.example.quickbite.entity.StatusPesanan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponse {

    private String idPesanan;
    private String idPembayaran;
    private double totalBiaya;
    private StatusPesanan statusPesanan;
    private String statusPembayaran;
    private String kodeReferensi;
    private LocalDateTime tanggal;
}