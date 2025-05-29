package com.faturarapida.service

import com.faturarapida.dto.InvoiceRequest
import com.faturarapida.dto.InvoiceResponse
import com.faturarapida.exception.InvoiceGenerationException
import com.faturarapida.model.Invoice
import com.faturarapida.repository.InvoiceRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.util.*

@Service
class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val pdfService: PdfService
) {

    @Value("\${app.invoice.directory:invoices}")
    private lateinit var invoiceDirectory: String

    @Transactional
    fun criarFatura(request: InvoiceRequest): InvoiceResponse {
        try {
            // Criar dados da fatura para o PDF
            val invoiceData = createInvoiceData(request)
            
            // Gerar PDF
            val pdfBytes = pdfService.generateInvoicePdf(invoiceData)
            
            // Criar diretório se não existir
            val directoryPath = Paths.get(invoiceDirectory)
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath)
            }
            
            // Gerar nome único para o arquivo
            val fileName = "fatura_${UUID.randomUUID()}.pdf"
            val filePath = directoryPath.resolve(fileName)
            
            // Salvar PDF
            Files.write(filePath, pdfBytes)
            
            // Criar e salvar a entidade Invoice
            val invoice = Invoice(
                cliente = request.cliente,
                descricao = request.descricao,
                valor = request.valor,
                vencimento = request.vencimento,
                status = "EMITIDA",
                pdfUrl = filePath.toString()
            )
            
            val savedInvoice = invoiceRepository.save(invoice)
            
            return InvoiceResponse(
                id = savedInvoice.id,
                status = savedInvoice.status,
                pdfUrl = savedInvoice.pdfUrl
            )
            
        } catch (e: IOException) {
            throw InvoiceGenerationException("Erro ao gerar ou salvar o PDF da fatura", e)
        } catch (e: Exception) {
            throw InvoiceGenerationException("Erro inesperado ao processar a fatura", e)
        }
    }
    
    private fun createInvoiceData(request: InvoiceRequest): PdfService.InvoiceData {
        // Aqui você pode adicionar lógica para calcular itens, subtotal, impostos, etc.
        // Este é um exemplo básico
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
            invoiceNumber = UUID.randomUUID().toString().substring(0, 8).uppercase(),
            issueDate = LocalDate.now(),
            dueDate = request.vencimento,
            clientName = request.cliente,
            clientDocument = "CPF/CNPJ não informado",
            clientAddress = "Endereço não informado",
            items = items,
            subtotal = subtotal,
            tax = tax,
            total = total
        )
    }
    
    fun listarTodas(): List<Invoice> = invoiceRepository.findAll()
    
    fun buscarPorId(id: Long): Invoice = 
        invoiceRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Fatura não encontrada") }
    
    fun obterPdf(id: Long): Resource {
        val invoice = buscarPorId(id)
        return try {
            val path = Paths.get(invoice.pdfUrl)
            val resource = ByteArrayResource(Files.readAllBytes(path))
            resource
        } catch (e: IOException) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao ler o arquivo da fatura",
                e
            )
        }
    }
}
