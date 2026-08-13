package nicolas.shopee_label_calculator.service;

import nicolas.shopee_label_calculator.dto.OrderDTO;
import nicolas.shopee_label_calculator.dto.ProductDTO;
import nicolas.shopee_label_calculator.dto.TaxRate;
import nicolas.shopee_label_calculator.utils.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {

    public List<OrderDTO> createOrders (Map<ProductDTO, TaxRate> productsAndTaxes) {
        Set<String> IDs = removeDuplicatedId(productsAndTaxes);

        List<OrderDTO> orders = new ArrayList<>();

        for (String ID : IDs) {
            List<ProductDTO> productsById = productsAndTaxes.keySet().stream()
                    .filter(p -> p.getOrderId().equals(ID))
                    .toList();

            TaxRate taxRate = productsAndTaxes.values().stream()
                    .filter(t -> t.getOrderId().equals(ID))
                    .findFirst()
                    .orElse(new TaxRate());

            OrderDTO order = OrderDTO.builder()
                    .ID(ID)
                    .date(productsById.getFirst().getDate())
                    .products(productsById)
                    .status(productsById.getFirst().getStatus())
                    .taxRate(taxRate)
                    .build();

            order.calculateRevenue();
            order.calculateProfit();

            orders.add (order);
        }

        return orders;
    }

    public Set<String> removeDuplicatedId (Map<ProductDTO, TaxRate> productsAndTaxes) {
        return productsAndTaxes.keySet().stream()
                .map(ProductDTO::getOrderId)
                .collect(Collectors.toSet());
    }

    public List<OrderDTO> filterOrders (List<OrderDTO> orderDTOS) {
        return orderDTOS.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED &&
                                       o.getStatus() != OrderStatus.NOT_PAID &&
                                        o.getStatus() != OrderStatus.ACCEPTED_REFUND).toList();
    }
}
