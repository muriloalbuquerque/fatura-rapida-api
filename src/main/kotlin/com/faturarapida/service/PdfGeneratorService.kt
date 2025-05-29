package com.faturarapida.service

import org.springframework.stereotype.Service


@Service
class PdfGeneratorService(
    val item: String,
    val cliente: String,
    val fornecedor: String,
    val fatura: String,
)
{
    fun generatePdf(): String {
        return "PDF gerado com sucesso"
    }


}