package nicolas.shopee_label_calculator.dto;

import java.util.List;

public record LabelResponse (
        String id,
        String date,
        List<String> skusAndQuantities,
        Double totalSupplierPrice
) {
    @Override
    public String toString() {
        return id + " | " + date + " | " + skusAndQuantities + " | " + totalSupplierPrice;
    }
}


