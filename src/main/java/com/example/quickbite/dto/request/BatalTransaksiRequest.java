package com.example.quickbite.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatalTransaksiRequest {

    @NotBlank(message = "Alasan pembatalan harus disertakan")
    private String alasan;
}