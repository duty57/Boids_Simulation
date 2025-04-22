package models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "\"Simulation\"")
public class SimModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "\"user\"", nullable = false, length = 64)
    private String user;

    @Column(name = "simulation_date", nullable = false)
    private LocalDate simulationDate;

    @Column(name = "max_speed", nullable = false, precision = 8, scale = 3)
    private BigDecimal maxSpeed;

    @Column(name = "drag_force", nullable = false, precision = 8, scale = 3)
    private BigDecimal dragForce;

    @Column(name = "drag_radius", nullable = false, precision = 8, scale = 3)
    private BigDecimal dragRadius;

    @Column(name = "aligment_force", nullable = false, precision = 8, scale = 3)
    private BigDecimal aligmentForce;

    @Column(name = "cohesion_force", nullable = false, precision = 8, scale = 3)
    private BigDecimal cohesionForce;

    @Column(name = "separation_force", nullable = false, precision = 8, scale = 3)
    private BigDecimal separationForce;

    @Column(name = "boid_vision", nullable = false, precision = 8, scale = 3)
    private BigDecimal boidVision;

    @Column(name = "wind_direction", nullable = false, precision = 8, scale = 3)
    private BigDecimal windDirection;

    @Column(name = "wind_speed", nullable = false, precision = 8, scale = 3)
    private BigDecimal windSpeed;

    @Column(name = "clouds", nullable = false, precision = 8, scale = 3)
    private BigDecimal clouds;

    @Column(name = "sun_angle", nullable = false, precision = 8, scale = 3)
    private BigDecimal sunAngle;

    @Column(name = "temperature", nullable = false, precision = 8, scale = 3)
    private BigDecimal temperature;
}