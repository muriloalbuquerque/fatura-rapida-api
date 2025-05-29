package com.faturarapida.service

import com.faturarapida.dto.CreateInvoiceRequest
import com.faturarapida.dto.InvoiceResponse
import com.faturarapida.exception.InvoiceGenerationException
import java.nio.file.Paths
import com.faturarapida.exception.InvoiceNotFoundException
import com.faturarapida.model.Invoice
import com.faturarapida.model.InvoiceStatus
import com.faturarapida.repository.InvoiceRepository
import com.faturarapida.service.storage.StorageService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

/**
 * Serviço responsável por gerenciar as operações relacionadas a faturas
 */
@Service
class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val pdfService: PdfService,
    private val storageService: StorageService
) {
    
    private val logger = LoggerFactory.getLogger(InvoiceService::class.java)

    /**
     * Cria uma nova fatura
     * @param request Dados para criação da fatura
     * @return Dados da fatura criada
     */
    @Transactional
    fun criarFatura(request: CreateInvoiceRequest): InvoiceResponse {
        logger.info("Criando nova fatura para o cliente: ${request.cliente}")
        
        try {
            // 1. Criar dados da fatura para o PDF
            val invoiceData = createInvoiceData(request)
            
            // 2. Gerar PDF
            val pdfBytes = pdfService.generateInvoicePdf(invoiceData)
            
            // 3. Salvar PDF no armazenamento
            val pdfFilename = "fatura_${invoiceData.invoiceNumber}.pdf"
            val pdfPath = storageService.store(pdfBytes, pdfFilename)
            
            // 4. Criar e salvar a entidade Invoice
            val invoice = Invoice(
                cliente = request.cliente,
                descricao = request.descricao,
                valor = request.valor,
                vencimento = request.vencimento,
                status = InvoiceStatus.EMITIDA,
                pdfUrl = pdfPath,
                documentoCliente = request.documentoCliente,
                enderecoCliente = request.enderecoCliente
            )
            
            val savedInvoice = invoiceRepository.save(invoice)
            logger.info("Fatura ${savedInvoice.id} criada com sucesso")
            
            return savedInvoice.toResponse()
            
        } catch (e: Exception) {
            val errorMsg = "Erro ao criar fatura para o cliente: ${request.cliente}"
            logger.error(errorMsg, e)
            throw InvoiceGenerationException(errorMsg, e)
        }
    }
    
    /**
     * Busca uma fatura pelo ID
     * @param id ID da fatura
     * @return Fatura encontrada
     * @throws InvoiceNotFoundException Se a fatura não for encontrada
     */
    @Transactional(readOnly = true)
    fun buscarPorId(id: Long): Invoice {
        logger.debug("Buscando fatura com ID: $id")
        return invoiceRepository.findById(id)
            .orElseThrow { 
                logger.warn("Fatura com ID $id não encontrada")
                InvoiceNotFoundException(id) 
            }
    }
    
    /**
     * Lista todas as faturas com paginação
     * @param pageable Configuração de paginação
     * @return Página de faturas
     */
    @Transactional(readOnly = true)
    fun listarTodas(pageable: Pageable): Page<Invoice> {
        logger.debug("Listando faturas - página ${pageable.pageNumber}, tamanho ${pageable.pageSize}")
        return invoiceRepository.findAll(pageable)
    }
    
    /**
     * Lista faturas por status
     * @param status Status das faturas a serem listadas
     * @param pageable Configuração de paginação
     * @return Página de faturas com o status especificado
     */
    @Transactional(readOnly = true)
    fun listarPorStatus(status: InvoiceStatus, pageable: Pageable): Page<Invoice> {
        logger.debug("Listando faturas com status: $status")
        return invoiceRepository.findByStatus(status, pageable)
    }
    
    /**
     * Atualiza o status de uma fatura
     * @param id ID da fatura
     * @param novoStatus Novo status
     * @return Fatura atualizada
     */
    @Transactional
    fun atualizarStatus(id: Long, novoStatus: InvoiceStatus): InvoiceResponse {
        logger.info("Atualizando status da fatura $id para $novoStatus")
        
        val invoice = buscarPorId(id)
        val updatedInvoice = invoice.atualizarStatus(novoStatus)
        
        val savedInvoice = invoiceRepository.save(updatedInvoice)
        logger.info("Status da fatura $id atualizado para $novoStatus")
        
        return savedInvoice.toResponse()
    }
    
    /**
     * Verifica e atualiza faturas vencidas
     * @return Número de faturas atualizadas
     */
    @Transactional
    fun verificarFaturasVencidas(): Int {
        logger.info("Verificando faturas vencidas")
        
        val hoje = LocalDate.now()
        val faturasVencidas = invoiceRepository.findByVencimentoBeforeAndStatusNot(
            hoje, 
            InvoiceStatus.PAGA
        )
        
        if (faturasVencidas.isNotEmpty()) {
            logger.info("Encontradas ${faturasVencidas.size} faturas vencidas")
            
            faturasVencidas.forEach { fatura ->
                if (fatura.status != InvoiceStatus.VENCIDA) {
                    fatura.atualizarStatus(InvoiceStatus.VENCIDA)
                    logger.debug("Fatura ${fatura.id} marcada como vencida")
                }
            }
            
            invoiceRepository.saveAll(faturasVencidas)
        }
        
        return faturasVencidas.size
    }
    
    /**
     * Obtém o PDF de uma fatura
     * @param id ID da fatura
     * @return Recurso do PDF
     */
    @Transactional(readOnly = true)
    fun obterPdf(id: Long): ByteArray {
        logger.debug("Solicitado PDF da fatura: $id")
        
        val invoice = buscarPorId(id)
        
        return try {
            storageService.loadAsBytes(Paths.get(invoice.pdfUrl).fileName.toString())
        } catch (e: Exception) {
            val errorMsg = "Erro ao carregar PDF da fatura: $id"
            logger.error(errorMsg, e)
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                errorMsg,
                e
            )
        }
    }
    
    private fun createInvoiceData(request: CreateInvoiceRequest): PdfService.InvoiceData {
        val items = listOf(
            PdfService.InvoiceItem(
                description = request.descricao,
                quantity = 1,
                unitPrice = request.valor.toDouble(),
                total = request.valor.toDouble()
            )
        )
        
        val subtotal = request.valor.toDouble()
        val tax = subtotal * 0.1 // 10% de imposto
        val total = subtotal + tax
        
        return PdfService.InvoiceData(
            invoiceNumber = generateInvoiceNumber(),
            issueDate = LocalDate.now(),
            dueDate = request.vencimento,
            clientName = request.cliente,
            clientDocument = request.documentoCliente ?: "Não informado",
            clientAddress = request.enderecoCliente ?: "Não informado",
            items = items,
            subtotal = subtotal,
            tax = tax,
            total = total
        )
    }
    
    private fun generateInvoiceNumber(): String {
        val date = java.time.LocalDate.now()
        val randomStr = (1000..9999).random().toString()
        return "${date.year}${String.format("%02d", date.monthValue)}$randomStr"
    }
    
    companion object {
        private const val INVOICE_NUMBER_PREFIX = "INV"
    }
}
