package models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SimulationCard {
    private Long id;
    private String title;
    private String vision;
    private String temperature;
    private String maxSpeed;

    public SimulationCard(Long id, LocalDate title, BigDecimal vision, BigDecimal temperature, BigDecimal maxSpeed) {
        this.id = id;
        this.title = String.valueOf(title);
        this.vision = String.format("%.2f", vision);
        this.temperature = String.format("%.2f", temperature);
        this.maxSpeed = String.format("%.2f", maxSpeed);
    }
}
