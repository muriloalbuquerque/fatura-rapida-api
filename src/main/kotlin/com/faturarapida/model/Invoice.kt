package com.faturarapida.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime


@Entity
data class Invoice(
    @Id @GeneratedValue val id: Long? = null,
    val cliente: String,
    val descricao: String,
    val valor: BigDecimal,
    val vencimento: LocalDate,
    val status: String,
    val pdfUrl: String,
    val dataEmissao: LocalDateTime = LocalDateTime.now()
)

