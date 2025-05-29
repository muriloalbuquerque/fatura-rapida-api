package com.faturarapida.service

import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.swing.text.Document

@Service
class PdfService {

    @Value("\${app.invoice.directory:invoices}")
    private lateinit var invoiceDirectory: String

    data class InvoiceData(
        val invoiceNumber: String,
        val issueDate: LocalDate,
        val dueDate: LocalDate,
        val clientName: String,
        val clientDocument: String,
        val clientAddress: String,
        val items: List<InvoiceItem>,
        val subtotal: Double,
        val tax: Double,
        val total: Double
    )

    data class InvoiceItem(
        val description: String,
        val quantity: Int,
        val unitPrice: Double,
        val total: Double
    )

    fun generateInvoicePdf(invoiceData: InvoiceData): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        // Configuração da página
        document.setMargins(50f, 36f, 36f, 36f)


        // Cabeçalho
        addHeader(document, invoiceData)
        
        // Dados da empresa e do cliente
        addCompanyAndClientInfo(document, invoiceData)
        
        // Itens da fatura
        addInvoiceItems(document, invoiceData)
        
        // Total e rodapé
        addTotalAndFooter(document, invoiceData)
        
        document.close()
        return outputStream.toByteArray()
    }

    private fun addHeader(document: Document, data: InvoiceData) {
        val title = Paragraph("FATURA")
            .setFontSize(20f)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20f)
        document.add(title)

        val headerTable = Table(2)
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(20f)


        headerTable.addCell(createCell("Número: ${data.invoiceNumber}", TextAlignment.LEFT, false))
        headerTable.addCell(createCell("Data: ${formatDate(data.issueDate)}", TextAlignment.RIGHT, false))
        headerTable.addCell(createCell("Vencimento: ${formatDate(data.dueDate)}", TextAlignment.LEFT, false))
        document.add(headerTable)
    }
    
    private fun addCompanyAndClientInfo(document: Document, data: InvoiceData) {
        val infoTable = Table(2)
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(30f)

        val companyInfo = """
            Empresa XPTO LTDA
            CNPJ: 12.345.678/0001-90
            Rua das Empresas, 123
            Bairro Centro
            São Paulo/SP - CEP: 01001-000
            Telefone: (11) 1234-5678
            contato@empresa.com.br
        """.trimIndent()

        val clientInfo = """
            ${data.clientName}
            ${data.clientDocument}
            ${data.clientAddress}
        """.trimIndent()

        infoTable.addCell(createCell("EMITENTE\n$companyInfo", TextAlignment.LEFT, false))
        infoTable.addCell(createCell("DESTINATÁRIO\n$clientInfo", TextAlignment.LEFT, false))
        document.add(infoTable)
    }
    
    private fun addInvoiceItems(document: Document, data: InvoiceData) {
        val itemTable = Table(floatArrayOf(3f, 1f, 1f, 1f))
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(20f)

        itemTable.addHeaderCell(createCell("Descrição", TextAlignment.LEFT, true))
        itemTable.addHeaderCell(createCell("Quantidade", TextAlignment.RIGHT, true))
        itemTable.addHeaderCell(createCell("Valor Unit.", TextAlignment.RIGHT, true))
        itemTable.addHeaderCell(createCell("Total", TextAlignment.RIGHT, true))

        data.items.forEach { item ->
            itemTable.addCell(createCell(item.description, TextAlignment.LEFT, false))
            itemTable.addCell(createCell(item.quantity.toString(), TextAlignment.RIGHT, false))
            itemTable.addCell(createCell(formatCurrency(item.unitPrice), TextAlignment.RIGHT, false))
            itemTable.addCell(createCell(formatCurrency(item.total), TextAlignment.RIGHT, false))
        }

        document.add(itemTable)
    }
    
    private fun addTotalAndFooter(document: Document, data: InvoiceData) {
        val totalTable = Table(2)
            .setWidth(UnitValue.createPercentValue(40f))
            .setHorizontalAlignment(com.itextpdf.layout.property.HorizontalAlignment.RIGHT)
            .setMarginBottom(30f)

        totalTable.addCell(createCell("Subtotal:", TextAlignment.RIGHT, true))
        totalTable.addCell(createCell(formatCurrency(data.subtotal), TextAlignment.RIGHT, false))
        
        totalTable.addCell(createCell("Impostos (10%):", TextAlignment.RIGHT, true))
        totalTable.addCell(createCell(formatCurrency(data.tax), TextAlignment.RIGHT, false))
        
        totalTable.addCell(createCell("TOTAL:", TextAlignment.RIGHT, true))
        totalTable.addCell(createCell(formatCurrency(data.total), TextAlignment.RIGHT, true))
        
        document.add(totalTable)
        
        val footer = Paragraph("Obrigado por escolher nossos serviços!")
            .setTextAlignment(TextAlignment.CENTER)
            .setItalic()
            .setFontColor(ColorConstants.GRAY)
        document.add(footer)
    }

    private fun createCell(text: String, alignment: TextAlignment, isHeader: Boolean): Cell {
        val cell = Cell().add(Paragraph(text).setTextAlignment(alignment))
        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY)
            cell.setBold()
        }
        cell.setPadding(8f)
        cell.setBorder(Border.NO_BORDER)
        return cell
    }

    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    private fun formatCurrency(value: Double): String {
        return String.format("R$ %,.2f", value).replace(".", "X").replace(",", ".").replace("X", ",")
    }
}
