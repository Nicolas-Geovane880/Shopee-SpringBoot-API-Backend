package nicolas.shopee_label_calculator.controller;

import lombok.RequiredArgsConstructor;
import nicolas.shopee_label_calculator.dto.OrderDTO;
import nicolas.shopee_label_calculator.dto.ProductDTO;
import nicolas.shopee_label_calculator.dto.TaxRate;
import nicolas.shopee_label_calculator.service.OrderService;
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
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PdfService pdfService;

    private final OrderService service;

//    @PostMapping(value = "/")
//    public Set<ProductDTO> getMap (@RequestParam ("file") MultipartFile file) throws IOException {
//        return pdfService.getProductsAndTaxes (file).keySet();
//    }

    @PostMapping(value = "/")
    public ResponseEntity<byte[]> getOrders (@RequestParam ("file") MultipartFile file) throws IOException {
        Map<ProductDTO, TaxRate> productsAndTaxes = pdfService.getProductsAndTaxes(file);
        List<OrderDTO> orders = service.createOrders(productsAndTaxes);
        return ResponseEntity.ok()
                .header (HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pedidos.pdf")
                .contentType (MediaType.APPLICATION_PDF)
                .body (pdfService.generateMetricsTable(service.filterOrders(orders)));
    }
}


