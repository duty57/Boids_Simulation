import clients.WeatherAPIClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherAPIClientTest {

    @Test
    public void testCityData() {
        WeatherAPIClient client = WeatherAPIClient.getClient();
        WeatherAPIClient.CityData cityData = client.getCityData("London");
        assertNotNull(cityData);

        WeatherAPIClient.CityData cityData2 = client.getCityData("Rome");
        assertNotNull(cityData2);

        WeatherAPIClient.CityData cityData3 = client.getCityData("Mumbai");
        assertNotNull(cityData3);

        WeatherAPIClient.CityData cityData4 = client.getCityData("Ejij");
        assertNull(cityData4);
    }
}
