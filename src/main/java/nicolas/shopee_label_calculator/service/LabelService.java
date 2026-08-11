package nicolas.shopee_label_calculator.service;

import com.sun.tools.jconsole.JConsoleContext;
import nicolas.shopee_label_calculator.dto.LabelResponse;
import nicolas.shopee_label_calculator.utils.SupplierPrices;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class LabelService {

    private final static Pattern DATE_REGEX = Pattern.compile("\\b\\d{2}/\\d{2}/\\d{4}\\b");

    private final static Pattern ID_REGEX = Pattern.compile("\\b\\d{6}(?=[A-Z0-9]{8}\\b)(?=[A-Z0-9]*[A-Z])[A-Z0-9]{8}\\b");

    private final static Pattern QUANTITY_REGEX = Pattern.compile("\\*(\\d+)");

    private final static Pattern SKUS_REGEX = Pattern.compile("\\d+\\.\\s*([A-Z][A-Z0-9-]*)");

    public List<LabelResponse> extractLabelInfos (List<String> texts) {
        List<LabelResponse> labelResponses = new ArrayList<>();

        try {
            for (String text : texts) {

                String date = matchGroup (DATE_REGEX, 0, text);
                List<String> ids = matchList (ID_REGEX, text, 0, true);
                String id = ids.isEmpty() ? null : ids.getLast();
                List<String> quantities = matchList(QUANTITY_REGEX, text, 0, false);
                List<String> skus = matchList (SKUS_REGEX, text, 1, false);

                List<Double> supplierPrices = new ArrayList<>();
                List<String> skusAndQuantities = new ArrayList<>();

                for (int i = 0; i < skus.size(); i++) {
                    skusAndQuantities.add (quantities.get (i).split("")[1] + "* " + skus.get(i));
                    supplierPrices.add(SupplierPrices.supplierPrices.get(skus.get (i)) * Integer.parseInt(quantities.get(i).replace("*", "")));
                }

                Double totalSupplierPrice = supplierPrices.stream().mapToDouble(Double::doubleValue).sum();

                labelResponses.add (new LabelResponse (id, date, skusAndQuantities, totalSupplierPrice));
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException ("O arquivo não é valido");
        }

        labelResponses.forEach (System.out::println);

        return labelResponses;
    }

    private String matchGroup (Pattern pattern, int group, String text) {
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(group);
        }

        return null;
    }

    private List<String> matchSKUs (Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);

        List<String> skus = new ArrayList<>();

        while (matcher.find()) {
            skus.add(matcher.group(1));
        }

        return skus;
    }

    private List<String> matchList (Pattern pattern, String text, int group, boolean id) {
        Matcher matcher = pattern.matcher (text);

        List<String> elements = new ArrayList<>();

        while (matcher.find()) {
            elements.add (matcher.group(group));
        }

        return elements;
    }
}
