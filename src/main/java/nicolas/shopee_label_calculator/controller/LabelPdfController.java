package nicolas.shopee_label_calculator.controller;

import lombok.RequiredArgsConstructor;
import nicolas.shopee_label_calculator.dto.LabelResponse;
import nicolas.shopee_label_calculator.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping (value = "/api/v1/labels")
@RequiredArgsConstructor
public class LabelPdfController {

    private final PdfService pdfService;

    @PostMapping (value = "/")
    public ResponseEntity <byte[]> generateLabelTable (@RequestParam ("files") List<MultipartFile> files) throws Exception {
        List<LabelResponse> labelResponses = pdfService.extractLabelText(files);

        byte[] bytes = pdfService.generateTablePDF(labelResponses);

        return ResponseEntity.ok()
                .header (HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pedidos.pdf")
                .contentType (MediaType.APPLICATION_PDF)
                .body (bytes);
    }
}
