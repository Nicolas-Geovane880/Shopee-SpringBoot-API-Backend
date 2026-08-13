package nicolas.shopee_label_calculator.dto;

import lombok.*;
import nicolas.shopee_label_calculator.utils.OrderStatus;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {

    private String orderId;

    private String SKU;

    private int quantity;

    private double productSubtotal;

    private LocalDate date;

    private OrderStatus status;
}
