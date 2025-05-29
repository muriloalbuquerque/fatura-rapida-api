package com.faturarapida.dto

data class InvoiceResponse(
    val id: Long?,
    val status: String,
    val pdfUrl: String
)
