package nicolas.shopee_label_calculator.utils;

import java.util.Map;

public class SupplierPrices {

    private SupplierPrices () {}

    public static final Map<String, Double> supplierPrices = Map.ofEntries(
            Map.entry ("PILLOW-78-4", 24.0),
            Map.entry ("PILLOW-88-4", 26.0),
            Map.entry ("KIT02-20-ACUSTICO", 48.0),
            Map.entry ("KIT04-20-ACUSTICO", 96.0),
            Map.entry ("RAMPA", 38.0)
    );
}
