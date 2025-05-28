package com.faturarapida.service

import com.faturarapida.model.Invoice
import com.faturarapida.repository.InvoiceRepository
import org.springframework.stereotype.Service

@Service
class InvoiceService(
    private val invoiceRepository: InvoiceRepository
) {
    fun criarFatura(request: InvoiceRequest): Invoice {
        // 1. Gerar PDF com iText
        // 2. Salvar PDF em pasta temporária
        // 3. Criar entidade Invoice e salvar no banco
        // 4. Retornar entidade salva
    }
}
