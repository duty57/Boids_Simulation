package models;

import clients.WeatherAPIClient;
import lombok.Getter;
import lombok.Setter;
import java.io.InputStream;
import java.util.Properties;

@Getter
@Setter
public class Simulation {

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

    public Simulation() {
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
        }catch (Exception e){
            System.out.println("Error loading shader program");
        }

    }

    public void update(SimModel model) {
        this.maxSpeed = model.getMaxSpeed().floatValue();
        this.alignmentForce = model.getAligmentForce().floatValue();
        this.cohesionForce = model.getCohesionForce().floatValue();
        this.separationForce = model.getSeparationForce().floatValue();
        this.vision = model.getBoidVision().floatValue();
        this.dragForce = model.getDragForce().floatValue();
        this.dragRadius = model.getDragRadius().floatValue();
        this.temperature = model.getTemperature().floatValue();
        this.windSpeed = model.getWindSpeed().floatValue();
        this.windDirection = model.getWindDirection().floatValue();
        this.sunAngle = model.getSunAngle().floatValue();
        this.cloudiness = model.getClouds().floatValue();
    }

    public void getCityData(WeatherAPIClient.CityData cityData) {
        this.vision = cityData.getVisibility();
        this.temperature = cityData.getTemperature();
        this.windSpeed = cityData.getWindSpeed();
        this.windDirection = cityData.getWindDeg();
        this.sunAngle = cityData.getSunAngle();
        this.cloudiness = cityData.getClouds();
    }

    public int getNumberOfBoids() {
        return this.NUMBER_OF_BOIDS;
    }
}
