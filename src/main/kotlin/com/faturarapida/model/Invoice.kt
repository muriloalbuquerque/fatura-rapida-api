package com.faturarapida.model

import com.faturarapida.dto.InvoiceResponse
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entidade que representa uma fatura no sistema
 */
@Entity
@Table(name = "faturas")
data class Invoice(
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 100)
    val cliente: String,
    
    @Column(nullable = false, length = 500)
    val descricao: String,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val valor: BigDecimal,
    
    @Column(nullable = false)
    val vencimento: LocalDate,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: InvoiceStatus = InvoiceStatus.EMITIDA,
    
    @Column(name = "pdf_url", nullable = false, length = 255)
    val pdfUrl: String,
    
    @Column(name = "data_emissao", nullable = false, updatable = false)
    val dataEmissao: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "data_atualizacao")
    val dataAtualizacao: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "documento_cliente", length = 20)
    val documentoCliente: String? = null,
    
    @Column(name = "endereco_cliente", length = 200)
    val enderecoCliente: String? = null
) {
    /**
     * Converte a entidade para DTO
     */
    fun toResponse(): InvoiceResponse = InvoiceResponse.fromEntity(this)
    
    /**
     * Atualiza o status da fatura
     */
    fun atualizarStatus(novoStatus: InvoiceStatus): Invoice {
        return this.copy(
            status = novoStatus,
            dataAtualizacao = LocalDateTime.now()
        )
    }
    
    /**
     * Verifica se a fatura está vencida
     */
    fun estaVencida(): Boolean {
        return LocalDate.now().isAfter(vencimento) && status != InvoiceStatus.PAGA
    }
}

