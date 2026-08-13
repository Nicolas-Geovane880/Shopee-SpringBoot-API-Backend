package nicolas.shopee_label_calculator.dto;

import lombok.Builder;
import lombok.Getter;
import nicolas.shopee_label_calculator.utils.OrderStatus;
import nicolas.shopee_label_calculator.utils.SupplierPrices;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class OrderDTO {

    private String ID;

    private LocalDate date;

    private List<ProductDTO> products;

    private double orderSubtotal;

    private double supplierPrice = 0.0;

    private double revenue;

    private double profit;

    private TaxRate taxRate;

    private OrderStatus status;

    private void calculateOrderSubtotal () {
        this.orderSubtotal = this.products.stream ().mapToDouble (ProductDTO::getProductSubtotal).sum ();
    }

    public void calculateRevenue () {
        calculateOrderSubtotal ();

        double totalTax = taxRate.calculateTotalTax();

        this.revenue = this.orderSubtotal - totalTax;
    }

    private void calculateSupplierPrice () {
        this.products.forEach (product -> {
            this.supplierPrice += SupplierPrices.supplierPrices.get(product.getSKU ().toUpperCase ()) * product.getQuantity ();
        });
    }

    public void calculateProfit () {
        calculateSupplierPrice ();
        this.profit = this.revenue - this.supplierPrice;
    }
}
