package nicolas.shopee_label_calculator.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
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

        Font headerFont = new Font (Font.HELVETICA, 18, Font.BOLD);
        Font columnFont = new Font (Font.HELVETICA, 10, Font.NORMAL);
        Paragraph paragraph = new Paragraph("Relátorio de pedidos", headerFont);
        paragraph.setAlignment(Element.ALIGN_CENTER);

        document.add (paragraph);
        document.add (new Paragraph (" "));

        PdfPTable metricsTable = new PdfPTable (3);
        metricsTable.setWidthPercentage (50);
        metricsTable.setWidths (new float[]{33, 33, 33});

        double totalProfit = orders.stream().mapToDouble(OrderDTO::getProfit).sum();
        double totalRevenue = orders.stream().mapToDouble(OrderDTO::getRevenue).sum();
        double totalSupplierPrice = orders.stream().mapToDouble(OrderDTO::getSupplierPrice).sum();

        metricsTable.addCell ("Faturamento total");
        metricsTable.addCell ("Fornecedor total");
        metricsTable.addCell ("Lucro total");

        metricsTable.addCell (String.format ("R$%.2f", totalRevenue));
        metricsTable.addCell (String.format ("R$%.2f", totalSupplierPrice));
        metricsTable.addCell (String.format ("R$%.2f", totalProfit));

        document.add (metricsTable);
        document.add (new Paragraph (" "));

        PdfPTable table = new PdfPTable (6);
        table.setWidthPercentage (100);
        table.setWidths (new float[]{20, 12, 30, 12, 13, 11});

        table.addCell ("ID");
        table.addCell ("Data");
        table.addCell ("Produtos");
        table.addCell ("Renda");
        table.addCell ("Fornecedor");
        table.addCell ("Lucro");

        for (OrderDTO order: orders) {
            table.addCell (new Phrase (order.getID(), columnFont));
            table.addCell (new Phrase (String.valueOf(order.getDate()), columnFont));
            table.addCell (new Phrase (String.join(", ", order.getProducts().stream()
                    .map(p -> p.getQuantity() + "* " + p.getSKU())
                    .toList()), columnFont));
            table.addCell (new Phrase (String.format("R$%.2f", order.getRevenue()), columnFont));
            table.addCell (new Phrase (String.format("R$%.2f", order.getSupplierPrice()), columnFont));
            table.addCell (new Phrase (String.format("R$%.2f", order.getProfit()), columnFont));
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
                            refund.equals ("Solicitação aprovada") ? OrderStatus.ACCEPTED_REFUND : OrderStatus.UNACCEPTED_REFUND)
                    .build();

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
