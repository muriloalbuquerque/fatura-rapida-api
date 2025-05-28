# 🧾 Fatura Rápida

O **Fatura Rápida** é um microSaaS que automatiza a criação e o envio de faturas em PDF por e-mail, oferecendo uma solução simples e eficiente para freelancers, autônomos e pequenos negócios.

---

## 🚀 Tecnologias Utilizadas

- **Kotlin + Spring Boot** — Backend robusto e moderno
- **PostgreSQL** — Banco de dados relacional
- **iText** — Biblioteca para geração de PDF
- **SendGrid** — Serviço de envio de e-mails
- **Railway** — Plataforma de deploy simplificada

---

## 📦 Funcionalidades (MVP)

- [x] Estrutura base do projeto
- [ ] Criar fatura com dados do cliente
- [ ] Gerar arquivo PDF automaticamente
- [ ] Enviar fatura por e-mail
- [ ] Consultar faturas emitidas via API

---

## 📋 Tarefas Backend (Dia 2)

- [ ] Implementar geração de PDF com iText
  - Layout com logo no topo e tabela de dados
  - Arquivo salvo com UUID único
- [ ] Criar `InvoiceService` com método `criarFatura(request: InvoiceRequest)`
  - Validar dados
  - Gerar e salvar PDF
  - Persistir `Invoice` no banco
- [ ] Integrar envio de e-mail com SendGrid
  - PDF em anexo
  - Usar `EmailService` separado
- [ ] Criar endpoint `GET /api/invoices`
  - Listar faturas com dados básicos

---

## 👨‍💻 Time

- Backend: [Murilo Albuquerque](https://github.com/muriloalbuquerque)
- Frontend: *[Nome do colaborador]* (adicionar quando disponível)

---

## 📄 Licença

Projeto com fins educacionais e comerciais. Licença a definir.
