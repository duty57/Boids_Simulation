#version 430

out vec4 fragColor;

in vec4 color;
uniform float temperature;
uniform float cloudiness;
uniform float sunAngle;

void main() {
    //    // Color based on velocity
    float redShift = max(0, temperature - 0.6);
    float blueShift = max(0, 0.6 - temperature);

    vec3 lightColor = vec3(
    (205 + redShift * 75.0f - blueShift * 20.0f),
    (119.0f + (230 - 119) * (sin(radians(sunAngle))) - cloudiness),
    (68.0f + (175 - 68) * (sin(radians(sunAngle)) - cloudiness) + blueShift * 80.0f - redShift * 30.0f)
    ) / 255.0;
    //remake cloudiness
    float ambientStrength = 0.4 + 0.2 * (sin(radians(sunAngle)) - cloudiness);
    float diffuseStrength = 0.6 + 0.4 * (sin(radians(sunAngle)) - cloudiness);
    float specularStrength = 0.1 + 0.9 * (sin(radians(sunAngle)) - cloudiness);

    vec3 ambient = ambientStrength * lightColor;
    vec3 diffuse = diffuseStrength * lightColor;
    vec3 specular = specularStrength * sin(radians(sunAngle)) * lightColor;

    vec3 result = ambient + diffuse + specular;
    fragColor = vec4(result, color.a);// Use result instead of lightColor
}