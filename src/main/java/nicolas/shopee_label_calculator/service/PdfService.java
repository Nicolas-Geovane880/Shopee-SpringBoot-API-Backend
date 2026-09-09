package nicolas.shopee_label_calculator.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import nicolas.shopee_label_calculator.dto.LabelResponse;
import nicolas.shopee_label_calculator.dto.OrderDTO;
import nicolas.shopee_label_calculator.dto.ProductDTO;
import nicolas.shopee_label_calculator.dto.TaxRate;
import nicolas.shopee_label_calculator.utils.OrderStatus;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.color.ColorSpace;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final LabelService labelService;

    public List<LabelResponse> extractLabelText (List<MultipartFile> files) throws IOException {

        List<String> extractedTexts = new ArrayList<>();

        for (MultipartFile file : files) {
            try (PDDocument document = Loader.loadPDF(file.getBytes())){
                PDFTextStripper pdfTextStripper = new PDFTextStripper();

                for (int i = 1; i <= document.getNumberOfPages(); i++) {
                    pdfTextStripper.setStartPage (i);
                    pdfTextStripper.setEndPage (i);
                    extractedTexts.add (pdfTextStripper.getText(document));
                }
            }
        }

        return labelService.extractLabelInfos (extractedTexts);
    }

    public byte[] generateTablePDF (List<LabelResponse> labelResponses) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream ();

        Document document = new Document (PageSize.A4);

        PdfWriter.getInstance (document, outputStream);

        document.open ();

        Font fontTitle = new Font (Font.HELVETICA, 18, Font.BOLD);

        Paragraph paragraph = new Paragraph("Relátorio de pedidos", fontTitle);
        paragraph.setAlignment(Element.ALIGN_CENTER);

        document.add (paragraph);
        document.add (new Paragraph (" "));

        PdfPTable table = new PdfPTable (5);
        table.setWidthPercentage (100);

        table.setWidths (new float[]{25, 15, 30, 15, 15});

        table.addCell ("ID");
        table.addCell ("Data");
        table.addCell ("Produtos");
        table.addCell ("Fornecedor");
        table.addCell ("Total");

        for (int i = 0; i < labelResponses.size (); i++) {
            table.addCell (labelResponses.get(i).id());
            table.addCell (labelResponses.get (i).date());
            table.addCell (String.join (", ", labelResponses.get (i).skusAndQuantities()));
            table.addCell (String.format("R$%.2f", labelResponses.get (i).totalSupplierPrice()));

            if (i == 0) {
                table.addCell (String.format("R$%.2f", labelResponses.stream().mapToDouble(LabelResponse::totalSupplierPrice).sum()));
            } else {
                table.addCell (" ");
            }
        }

        document.add (table);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] generateMetricsTable (List<OrderDTO> orders) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream ();

        Document document = new Document (PageSize.A4);

        PdfWriter.getInstance (document, outputStream);

        document.open ();

        Font pageFont = new Font (Font.HELVETICA, 20, Font.BOLD);
        Font headerFont = new Font (Font.HELVETICA, 11, Font.NORMAL, new java.awt.Color(255, 255, 255));
        Font columnFont = new Font (Font.HELVETICA, 10, Font.NORMAL);
        Paragraph paragraph = new Paragraph("Relátorio de pedidos", pageFont);
        paragraph.setAlignment(Element.ALIGN_CENTER);

        document.add (paragraph);
        document.add (new Paragraph (" "));

        PdfPTable metricsTable = new PdfPTable (3);
        metricsTable.setWidthPercentage (70);
        metricsTable.setWidths (new float[]{33, 33, 33});

        double totalProfit = orders.stream().mapToDouble(OrderDTO::getProfit).sum();
        double totalRevenue = orders.stream().mapToDouble(OrderDTO::getRevenue).sum();
        double totalSupplierPrice = orders.stream().mapToDouble(OrderDTO::getSupplierPrice).sum();

        PdfPCell metricsHeader1 = new PdfPCell (new Phrase ("Faturamento total", headerFont));
        PdfPCell metricsHeader2 = new PdfPCell (new Phrase ("Fornecedor total", headerFont));
        PdfPCell metricsHeader3 = new PdfPCell (new Phrase ("Lucro total", headerFont));

        metricsHeader1.setBackgroundColor (new java.awt.Color(40, 40, 40));
        metricsHeader2.setBackgroundColor (new java.awt.Color(40, 40, 40));
        metricsHeader3.setBackgroundColor (new java.awt.Color(40, 40, 40));

        metricsHeader1.setHorizontalAlignment(Element.ALIGN_CENTER);
        metricsHeader2.setHorizontalAlignment(Element.ALIGN_CENTER);
        metricsHeader3.setHorizontalAlignment(Element.ALIGN_CENTER);

        metricsHeader1.setVerticalAlignment(Element.ALIGN_CENTER);
        metricsHeader2.setVerticalAlignment(Element.ALIGN_CENTER);
        metricsHeader3.setVerticalAlignment(Element.ALIGN_CENTER);

        metricsHeader1.setPadding(3);
        metricsHeader2.setPadding(3);
        metricsHeader3.setPadding(3);

        metricsTable.addCell (metricsHeader1);
        metricsTable.addCell (metricsHeader2);
        metricsTable.addCell (metricsHeader3);

        PdfPCell metricsRow1 = new PdfPCell (new Phrase (String.format ("R$%.2f", totalRevenue), columnFont));
        metricsRow1.setHorizontalAlignment (Element.ALIGN_CENTER);
        metricsRow1.setVerticalAlignment (Element.ALIGN_CENTER);

        PdfPCell metricsRow2 = new PdfPCell (new Phrase (String.format ("R$%.2f", totalSupplierPrice), columnFont));
        metricsRow2.setBackgroundColor (new java.awt.Color (253, 46, 71, 65));
        metricsRow2.setHorizontalAlignment (Element.ALIGN_CENTER);
        metricsRow2.setVerticalAlignment (Element.ALIGN_CENTER);

        PdfPCell metricsRow3 = new PdfPCell (new Phrase (String.format ("R$%.2f", totalProfit), columnFont));
        metricsRow3.setBackgroundColor (new java.awt.Color (92, 236, 33, 63));
        metricsRow3.setHorizontalAlignment (Element.ALIGN_CENTER);
        metricsRow3.setVerticalAlignment (Element.ALIGN_CENTER);

        metricsTable.addCell (metricsRow1);
        metricsTable.addCell (metricsRow2);
        metricsTable.addCell (metricsRow3);

        document.add (metricsTable);
        document.add (new Paragraph (" "));

        PdfPTable table = new PdfPTable (7);
        table.setWidthPercentage (110);
        table.setWidths (new float[]{18, 12, 13, 24, 10, 13, 10});

        PdfPCell header1 = new PdfPCell(new Phrase("ID", headerFont));
        PdfPCell header2 = new PdfPCell(new Phrase("Data", headerFont));
        PdfPCell header3 = new PdfPCell(new Phrase("Status",headerFont));
        PdfPCell header4 = new PdfPCell(new Phrase("Produtos",headerFont));
        PdfPCell header5 = new PdfPCell(new Phrase("Renda",headerFont));
        PdfPCell header6 = new PdfPCell(new Phrase("Fornecedor",headerFont));
        PdfPCell header7 = new PdfPCell(new Phrase("Lucro",headerFont));

        header1.setBackgroundColor (new java.awt.Color(40, 40, 40));
        header2.setBackgroundColor (new java.awt.Color(40, 40, 40));
        header3.setBackgroundColor (new java.awt.Color(40, 40, 40));
        header4.setBackgroundColor (new java.awt.Color(40, 40, 40));
        header5.setBackgroundColor (new java.awt.Color(40, 40, 40));
        header6.setBackgroundColor (new java.awt.Color(40, 40, 40));
        header7.setBackgroundColor (new java.awt.Color(40, 40, 40));

        header1.setHorizontalAlignment(Element.ALIGN_CENTER);
        header2.setHorizontalAlignment(Element.ALIGN_CENTER);
        header3.setHorizontalAlignment(Element.ALIGN_CENTER);
        header4.setHorizontalAlignment(Element.ALIGN_CENTER);
        header5.setHorizontalAlignment(Element.ALIGN_CENTER);
        header6.setHorizontalAlignment(Element.ALIGN_CENTER);
        header7.setHorizontalAlignment(Element.ALIGN_CENTER);

        header1.setVerticalAlignment(Element.ALIGN_CENTER);
        header2.setVerticalAlignment(Element.ALIGN_CENTER);
        header3.setVerticalAlignment(Element.ALIGN_CENTER);
        header4.setVerticalAlignment(Element.ALIGN_CENTER);
        header5.setVerticalAlignment(Element.ALIGN_CENTER);
        header6.setVerticalAlignment(Element.ALIGN_CENTER);
        header7.setVerticalAlignment(Element.ALIGN_CENTER);

        header1.setPadding(3);
        header2.setPadding(3);
        header3.setPadding(3);
        header4.setPadding(3);
        header5.setPadding(3);
        header6.setPadding(3);
        header7.setPadding(3);

        table.addCell (header1);
        table.addCell (header2);
        table.addCell (header3);
        table.addCell (header4);
        table.addCell (header5);
        table.addCell (header6);
        table.addCell (header7);

        for (OrderDTO order: orders) {
            table.addCell (new Phrase (order.getID(), columnFont));
            table.addCell (new Phrase (String.valueOf(order.getDate()), columnFont));
            table.addCell (new Phrase (OrderStatus.getOrderStatusStr (order.getStatus()), columnFont));
            table.addCell (new Phrase (String.join(", ", order.getProducts().stream()
                    .map(p -> p.getQuantity() + "* " + p.getSKU())
                    .toList()), columnFont));

            PdfPCell revenue = new PdfPCell (new Phrase (String.format("R$%.2f", order.getRevenue()), columnFont));

            PdfPCell supplierPrice = new PdfPCell (new Phrase (String.format("R$%.2f", order.getSupplierPrice()), columnFont));
            supplierPrice.setBackgroundColor (new java.awt.Color (253, 46, 71, 65));

            PdfPCell profit = new PdfPCell (new Phrase (String.format("R$%.2f", order.getProfit()), columnFont));
            profit.setBackgroundColor (new java.awt.Color (92, 236, 33, 63));

            table.addCell (revenue);
            table.addCell (supplierPrice);
            table.addCell (profit);
        }

        document.add (table);
        document.close ();

        return outputStream.toByteArray();
    }

    public Map<ProductDTO, TaxRate> getProductsAndTaxes (MultipartFile file) throws IOException {
        Workbook workbook = WorkbookFactory.create (file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Map<String, Integer> columns = mapColumnsIndex(sheet);

        Map<ProductDTO, TaxRate> productDTOTaxRateMap = new HashMap<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            String variation = "";

            String id = row.getCell(columns.get("ID do pedido")).getStringCellValue ();
            String productDate = row.getCell(columns.get("Hora do pagamento do pedido")).getStringCellValue();
            String sku = row.getCell(columns.get("Número de referência SKU")).getStringCellValue();

            if (sku.equals("CAPA ENCOSTO") || sku.equals("CERVICAL")) {
                variation = row.getCell (columns.get ("Nome da variação")).getStringCellValue();
            }
            double productSubtotal = Double.parseDouble (row.getCell(columns.get ("Subtotal do produto")).getStringCellValue());
            int quantity = Integer.parseInt (row.getCell(columns.get ("Quantidade")).getStringCellValue());
            double liquidComercialTax = Double.parseDouble (row.getCell (columns.get ("Taxa de comissão líquida")).getStringCellValue());
            double liquidServiceTax = Double.parseDouble (row.getCell (columns.get ("Taxa de serviço líquida")).getStringCellValue());
            double comercialTax = Double.parseDouble (row.getCell (columns.get("Ajuste por participação em ação comercial")).getStringCellValue());
            String status = row.getCell (columns.get ("Status do pedido")).getStringCellValue ();
            String refund = row.getCell (columns.get ("Status da Devolução / Reembolso")).getStringCellValue ();

            ProductDTO product = ProductDTO.builder()
                    .orderId(id)
                    .SKU(!variation.isEmpty() ? sku + " " + variation : sku)
                    .productSubtotal(productSubtotal)
                    .date(productDate.equals("-") ? LocalDate.now() : LocalDate.parse(productDate.split(" ")[0]))
                    .quantity(quantity)
                    .status(refund.isEmpty() ? OrderStatus.getOrderStatus(status) :
                            refund.equals ("Solicitação aprovada") ? OrderStatus.ACCEPTED_REFUND :
                            refund.equals ("Devolução em Andamento") ? OrderStatus.REFUND_IN_PROGRESS : OrderStatus.UNACCEPTED_REFUND)
                    .build();

            System.out.println(product.getOrderId() + " -> " + product.getStatus());

            TaxRate tax = TaxRate.builder()
                    .orderId(id)
                    .liquidServiceTax(liquidServiceTax)
                    .liquidComercialTax(liquidComercialTax)
                    .comercialAct(comercialTax)
                    .build();

            productDTOTaxRateMap.put (product, tax);
        }

        workbook.close ();

        return productDTOTaxRateMap;
    }

    public Map<String, Integer> mapColumnsIndex (Sheet sheet) {
        Map<String, Integer> index = new HashMap<>();

        Row headers = sheet.getRow (0);

        for (Cell header : headers) {
            String headerName = header.getStringCellValue().trim();
            int headerIndex = header.getColumnIndex();

            index.put (headerName, headerIndex);
        }

        return index;
    }

}
