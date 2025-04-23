package util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.commons.lang3.math.NumberUtils;

public class SimulationDataUtil {

    public static float toCelsius(double celsius) {
        return (float) (celsius - 273.15f);
    }


    public static float calculatesunAngle(double sunrise, double sunset, double timeZoneOffset) {
        LocalDateTime sunriseTime = LocalDateTime.ofInstant(Instant.ofEpochSecond((long) sunrise), ZoneId.systemDefault());
        LocalDateTime sunsetTime = LocalDateTime.ofInstant(Instant.ofEpochSecond((long) sunset), ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();

        float sunriseF = (float) sunriseTime.getHour() + (float) sunriseTime.getMinute() / 60;
        float sunsetF = (float) sunsetTime.getHour() + (float) sunsetTime.getMinute() / 60;
        float nowF = (float) ((float) now.getHour() + (float) now.getMinute() / 60 - timeZoneOffset / 3600);

        System.out.println("sunriseF: " + sunriseF);
        System.out.println("sunsetF: " + sunsetF);
        System.out.println("nowF: " + nowF);

        if (nowF - sunriseF < 0 || sunsetF - sunriseF < 0) {
            return 0.0f;
        }

        float angle = (nowF - sunriseF) / (sunsetF - sunriseF) * 180.0f;
        return NumberUtils.min(NumberUtils.max(angle, 0.0f), 180.0f);
    }

}
