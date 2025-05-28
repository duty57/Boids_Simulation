package models;

import clients.WeatherAPIClient;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

import org.apache.commons.lang3.ObjectUtils;

@Getter
@Setter
public class Simulation {
    private static Simulation instance;

    private int shaderProgram;
    private int renderProgram;
    private int[] ssbo = new int[1];
    private int vao;

    private int NUMBER_OF_BOIDS;
    private float maxSpeed;
    private float dragForce;
    private float alignmentForce;
    private float cohesionForce;
    private float separationForce;
    private float vision;
    private float dragRadius;

    private float temperature; //celsius
    private float windSpeed;
    private float windDirection;
    private float cloudiness;
    private float sunAngle;

    private Boid[] boids;
    private int moveTowardsMouse = 0;
    private int moveAwayFromMouse = 0;
    private float[] mousePosition = new float[2];

    private Simulation() {
        Properties prop = new Properties();
        InputStream input;
        input = Simulation.class.getClassLoader().getResourceAsStream("config/simulation.properties");

        try {
            prop.load(input);
            NUMBER_OF_BOIDS = Integer.parseInt(prop.getProperty("simulation.numberOfBoids"));
            maxSpeed = Float.parseFloat(prop.getProperty("boids.maxSpeed"));
            dragForce = Float.parseFloat(prop.getProperty("boids.dragForce"));
            alignmentForce = Float.parseFloat(prop.getProperty("boids.alignmentForce"));
            cohesionForce = Float.parseFloat(prop.getProperty("boids.cohesionForce"));
            separationForce = Float.parseFloat(prop.getProperty("boids.separationForce"));
            vision = Float.parseFloat(prop.getProperty("boids.vision"));
            dragRadius = Float.parseFloat(prop.getProperty("boids.dragRadius"));
            temperature = Float.parseFloat(prop.getProperty("boids.temperature"));
            windSpeed = Float.parseFloat(prop.getProperty("boids.windSpeed"));
            windDirection = Float.parseFloat(prop.getProperty("boids.windDirection"));
            cloudiness = Float.parseFloat(prop.getProperty("boids.cloudiness"));
            sunAngle = Float.parseFloat(prop.getProperty("boids.sunAngle"));
            boids = new Boid[NUMBER_OF_BOIDS];
        } catch (Exception e) {
            System.out.println("Error loading shader program");
        }

    }

    public static Simulation getInstance() {
        if (instance == null) {
            instance = new Simulation();
        }
        return instance;
    }


    public void update(SimModel model) {
        if (model == null) {
            return;
        }

        this.maxSpeed = ObjectUtils.defaultIfNull(model.getMaxSpeed(), BigDecimal.ZERO).floatValue();
        this.alignmentForce = ObjectUtils.defaultIfNull(model.getAligmentForce(), BigDecimal.ZERO).floatValue();
        this.cohesionForce = ObjectUtils.defaultIfNull(model.getCohesionForce(), BigDecimal.ZERO).floatValue();
        this.separationForce = ObjectUtils.defaultIfNull(model.getSeparationForce(), BigDecimal.ZERO).floatValue();
        this.vision = ObjectUtils.defaultIfNull(model.getBoidVision(), BigDecimal.ZERO).floatValue();
        this.dragForce = ObjectUtils.defaultIfNull(model.getDragForce(), BigDecimal.ZERO).floatValue();
        this.dragRadius = ObjectUtils.defaultIfNull(model.getDragRadius(), BigDecimal.ZERO).floatValue();
        this.temperature = ObjectUtils.defaultIfNull(model.getTemperature(), BigDecimal.ZERO).floatValue();
        this.windSpeed = ObjectUtils.defaultIfNull(model.getWindSpeed(), BigDecimal.ZERO).floatValue();
        this.windDirection = ObjectUtils.defaultIfNull(model.getWindDirection(), BigDecimal.ZERO).floatValue();
        this.sunAngle = ObjectUtils.defaultIfNull(model.getSunAngle(), BigDecimal.ZERO).floatValue();
        this.cloudiness = ObjectUtils.defaultIfNull(model.getClouds(), BigDecimal.ZERO).floatValue();
    }

    public void getCityData(WeatherAPIClient.CityData cityData) {
        if (cityData == null) {
            return;
        }

        this.vision = ObjectUtils.defaultIfNull(cityData.getVisibility(), 0.0f);
        this.temperature = ObjectUtils.defaultIfNull(cityData.getTemperature(), 0.0f);
        this.windSpeed = ObjectUtils.defaultIfNull(cityData.getWindSpeed(), 0.0f);
        this.windDirection = ObjectUtils.defaultIfNull(cityData.getWindDeg(), 0.0f);
        this.sunAngle = ObjectUtils.defaultIfNull(cityData.getSunAngle(), 0.0f);
        this.cloudiness = ObjectUtils.defaultIfNull(cityData.getClouds(), 0.0f);
    }

    public int getNumberOfBoids() {
        return this.NUMBER_OF_BOIDS;
    }
}
