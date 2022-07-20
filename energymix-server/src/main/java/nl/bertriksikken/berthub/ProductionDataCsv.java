package nl.bertriksikken.berthub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Iterables;

public class ProductionDataCsv {
    
    private final List<ProductionData> productionData = new ArrayList<>();

    private ProductionDataCsv(List<ProductionData> datas) {
        productionData.addAll(datas);
    }

    public static ProductionDataCsv parse(CsvMapper csvMapper, String csvData) throws IOException {
        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();

        Iterator<Map<String, String>> iterator = csvMapper.readerFor(Map.class).with(csvSchema).readValues(csvData);
        List<ProductionData> datas = new ArrayList<>();
        while (iterator.hasNext()) {
            Map<String, String> line = iterator.next(); 
            ProductionData data = ProductionData.parse(line);
            if (data.isValid()) {
                datas.add(data);
            }
        }
        return new ProductionDataCsv(datas);
    }
    
    public ProductionData getLatest() {
        return Iterables.getLast(productionData, null);
    }
    
    public boolean hasData() {
        return !productionData.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s", productionData);
    }
    

}
