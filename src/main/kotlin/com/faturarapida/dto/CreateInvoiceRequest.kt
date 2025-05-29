package com.faturarapida.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate

data class CreateInvoiceRequest(
    @field:NotBlank(message = "O nome do cliente é obrigatório")
    @field:Size(max = 100, message = "O nome do cliente deve ter no máximo {max} caracteres")
    val cliente: String,
    
    @field:NotBlank(message = "A descrição é obrigatória")
    @field:Size(max = 500, message = "A descrição deve ter no máximo {max} caracteres")
    val descricao: String,
    
    @field:NotNull(message = "O valor é obrigatório")
    @field:DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    val valor: BigDecimal,
    
    @field:NotNull(message = "A data de vencimento é obrigatória")
    val vencimento: LocalDate,
    
    @field:Size(max = 20, message = "O documento deve ter no máximo {max} caracteres")
    val documentoCliente: String? = null,
    
    @field:Size(max = 200, message = "O endereço deve ter no máximo {max} caracteres")
    val enderecoCliente: String? = null
)
