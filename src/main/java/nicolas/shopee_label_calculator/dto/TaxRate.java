package nicolas.shopee_label_calculator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxRate {

    private String orderId;

    private double liquidComercialTax;

    private double liquidServiceTax;

    private double comercialAct;

    public double calculateTotalTax () {
        return this.comercialAct + this.liquidComercialTax + this.liquidServiceTax;
    }
}
