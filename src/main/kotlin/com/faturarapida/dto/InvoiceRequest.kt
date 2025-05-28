package com.faturarapida.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate

data class InvoiceRequest(
    @field:NotBlank val cliente: String,
    val descricao: String,
    @field:DecimalMin("0.01") val valor: BigDecimal,
    @field:NotNull val vencimento: LocalDate
)
