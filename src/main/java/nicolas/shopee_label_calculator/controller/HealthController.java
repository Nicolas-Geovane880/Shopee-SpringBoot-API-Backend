package nicolas.shopee_label_calculator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping (value = "/")
    public String isOk () {
        return "OK";
    }
}
