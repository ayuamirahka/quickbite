package com.example.quickbite;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.quickbite.boundary.CheckoutBoundary;
import com.example.quickbite.dto.request.CheckoutRequest;
import com.example.quickbite.dto.response.CheckoutResponse;
import com.example.quickbite.entity.StatusPesanan;
import com.example.quickbite.exception.BusinessValidationException;
import com.example.quickbite.exception.GlobalExceptionHandler;
import com.example.quickbite.exception.ResourceNotFoundException;
import com.example.quickbite.service.CheckoutService;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private CheckoutService checkoutService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CheckoutBoundary boundary =
                new CheckoutBoundary(checkoutService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(boundary)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void checkoutBerhasilMengembalikan201() throws Exception {
        CheckoutResponse response = CheckoutResponse.builder()
                .idPesanan("ORD-TEST1234")
                .idPembayaran("PAY-TEST1234")
                .totalBiaya(18000.0)
                .statusPesanan(StatusPesanan.DIPROSES)
                .statusPembayaran("SUCCESS")
                .kodeReferensi("PG-REF-TEST1234")
                .tanggal(LocalDateTime.of(2026, 10, 27, 12, 0))
                .build();

        when(checkoutService.prosesCheckout(
                any(CheckoutRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "idPelanggan": "CUST-001",
                      "keranjangData": [
                        {
                          "idMenu": "MENU-01",
                          "jumlah": 1
                        }
                      ],
                      "metodePembayaran": "Kartu",
                      "tokenKartu": "valid_card"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.idPesanan")
                        .value("ORD-TEST1234"))
                .andExpect(jsonPath("$.data.totalBiaya")
                        .value(18000.0))
                .andExpect(jsonPath("$.data.statusPesanan")
                        .value("DIPROSES"));

        verify(checkoutService)
                .prosesCheckout(any(CheckoutRequest.class));
    }

    @Test
    void checkoutStokTidakCukupMengembalikan400()
            throws Exception {

        when(checkoutService.prosesCheckout(
                any(CheckoutRequest.class)))
                .thenThrow(new BusinessValidationException(
                        "Stok tidak mencukupi"));

        mockMvc.perform(post("/api/v1/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "idPelanggan": "CUST-001",
                      "keranjangData": [
                        {
                          "idMenu": "MENU-01",
                          "jumlah": 100
                        }
                      ],
                      "metodePembayaran": "Kartu",
                      "tokenKartu": "valid_card"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Stok tidak mencukupi"));
    }

    @Test
    void pelangganTidakDitemukanMengembalikan404()
            throws Exception {

        when(checkoutService.prosesCheckout(
                any(CheckoutRequest.class)))
                .thenThrow(new ResourceNotFoundException(
                        "Pelanggan tidak ditemukan"));

        mockMvc.perform(post("/api/v1/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "idPelanggan": "CUST-TIDAK-ADA",
                      "keranjangData": [
                        {
                          "idMenu": "MENU-01",
                          "jumlah": 1
                        }
                      ],
                      "metodePembayaran": "Kartu",
                      "tokenKartu": "valid_card"
                    }
                    """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Pelanggan tidak ditemukan"));
    }
}