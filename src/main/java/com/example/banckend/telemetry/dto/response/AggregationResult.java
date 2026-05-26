package com.example.banckend.telemetry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregationResult {
    private Double maxGas;
    private Double minGas;
    private Double avgGas;

    private Double maxCo;
    private Double minCo;
    private Double avgCo;

    private Double maxTemp;
    private Double minTemp;
    private Double avgTemp;

    private Double maxHumidity;
    private Double minHumidity;
    private Double avgHumidity;

    public AggregationResult(Object maxGas, Object minGas, Object avgGas,
                             Object maxCo, Object minCo, Object avgCo,
                             Object maxTemp, Object minTemp, Object avgTemp,
                             Object maxHumidity, Object minHumidity, Object avgHumidity) {
        this.maxGas = parseDouble(maxGas);
        this.minGas = parseDouble(minGas);
        this.avgGas = parseDouble(avgGas);

        this.maxCo = parseDouble(maxCo);
        this.minCo = parseDouble(minCo);
        this.avgCo = parseDouble(avgCo);

        this.maxTemp = parseDouble(maxTemp);
        this.minTemp = parseDouble(minTemp);
        this.avgTemp = parseDouble(avgTemp);

        this.maxHumidity = parseDouble(maxHumidity);
        this.minHumidity = parseDouble(minHumidity);
        this.avgHumidity = parseDouble(avgHumidity);
    }

    private Double parseDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return Double.parseDouble(obj.toString());
    }
}
