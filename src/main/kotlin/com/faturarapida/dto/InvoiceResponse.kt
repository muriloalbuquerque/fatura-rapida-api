package com.faturarapida.dto

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * DTO que representa a resposta de uma fatura
 * @property id Identificador único da fatura
 * @property cliente Nome do cliente
 * @property descricao Descrição dos serviços/produtos
 * @property valor Valor total da fatura
 * @property vencimento Data de vencimento
 * @property status Status atual da fatura (EMITIDA, PAGA, VENCIDA, CANCELADA)
 * @property pdfUrl URL para download do PDF da fatura
 * @property dataEmissao Data e hora em que a fatura foi emitida
 */
data class InvoiceResponse(
    val id: Long?,
    val cliente: String,
    val descricao: String,
    val valor: BigDecimal,
    val vencimento: String,
    val status: String,
    val pdfUrl: String,
    val dataEmissao: LocalDateTime
) {
    companion object {
        fun fromEntity(invoice: com.faturarapida.model.Invoice): InvoiceResponse {
            return InvoiceResponse(
                id = invoice.id,
                cliente = invoice.cliente,
                descricao = invoice.descricao,
                valor = invoice.valor,
                vencimento = invoice.vencimento.toString(),
                status = invoice.status.toString(),
                pdfUrl = invoice.pdfUrl,
                dataEmissao = invoice.dataEmissao
            )
        }
    }
}
