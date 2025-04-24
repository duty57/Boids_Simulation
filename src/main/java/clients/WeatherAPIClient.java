package clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import util.SimulationDataUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class WeatherAPIClient {
    private final HttpClient client = HttpClient.newHttpClient();

    private static class CityCoordinates {
        float lat;
        float lon;
    }

    @Getter
    @Setter
    public static class CityData {
        float temperature;// in kelvins
        float visibility;// max value 10000
        float windDeg;// meteorological
        float windSpeed;// m/s
        float clouds;// cloudiness %
        float sunAngle; // 0 - sunrise, 180 - sunset
    }

    private CityCoordinates getCityCoordinates(String city) {
        Dotenv dotenv = Dotenv.load();
        String weatherApiKey = dotenv.get("WEATHER_API_KEY");
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://api.openweathermap.org/geo/1.0/direct?q=" + city + "&appid=" + weatherApiKey)).build();
        try{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.body().equals("[]")) { return null; }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            CityCoordinates cityCoordinates = new CityCoordinates();
            cityCoordinates.lat = (float) root.get(0).get("lat").asDouble();
            cityCoordinates.lon = (float) root.get(0).get("lon").asDouble();
            System.out.println(cityCoordinates.lat + " " + cityCoordinates.lon);
            return cityCoordinates;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CityData getCityData(String city) {
        if (StringUtils.isBlank(city)) return null;
        Dotenv dotenv = Dotenv.load();
        String weatherApiKey = dotenv.get("WEATHER_API_KEY");

        CityCoordinates cityCoordinates = getCityCoordinates(city);
        if (cityCoordinates == null) return null;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.openweathermap.org/data/2.5/weather?lat=" + cityCoordinates.lat + "&lon=" + cityCoordinates.lon + "&appid=" + weatherApiKey)).build();
        try{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            CityData cityData = new CityData();
            cityData.temperature = SimulationDataUtil.toCelsius(root.get("main").get("temp").asDouble());
            cityData.visibility = (float) root.get("visibility").asDouble() / 10000.0f;
            cityData.windDeg = (float) root.get("wind").get("deg").asDouble();
            cityData.windSpeed = (float) root.get("wind").get("speed").asDouble() / 100.0f;
            cityData.clouds = (float) root.get("clouds").get("all").asDouble() / 100.0f;
            cityData.sunAngle = SimulationDataUtil.calculatesunAngle(root.get("sys").get("sunrise").asDouble(), root.get("sys").get("sunset").asDouble(), root.get("timezone").asDouble());
            System.out.println("sunAngle: " + cityData.sunAngle);

            return cityData;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
}
