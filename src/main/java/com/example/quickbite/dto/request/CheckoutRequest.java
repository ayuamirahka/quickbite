package com.example.quickbite.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class CheckoutRequest {

    @NotBlank(message = "ID Pelanggan wajib diisi")
    private String idPelanggan;

    @NotEmpty(message = "Keranjang belanja tidak boleh kosong")
    @Valid
    private List<CheckoutItemRequest> keranjangData;

    @NotBlank(message = "Metode pembayaran wajib diisi")
    private String metodePembayaran;

    @NotBlank(message = "Token kartu wajib diisi")
    private String tokenKartu;
}