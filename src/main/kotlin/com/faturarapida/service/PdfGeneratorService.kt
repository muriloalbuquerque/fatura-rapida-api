package com.faturarapida.service

import org.springframework.stereotype.Service

@Service
class PdfGeneratorService {
    fun generatePdf(): String {
        return "PDF gerado com sucesso"
    }
}
