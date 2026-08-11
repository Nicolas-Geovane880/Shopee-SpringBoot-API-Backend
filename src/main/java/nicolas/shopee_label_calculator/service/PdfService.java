package nicolas.shopee_label_calculator.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import nicolas.shopee_label_calculator.dto.LabelResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public byte[] generateTablePDF (List<LabelResponse> labelResponses) throws Exception {
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
}
