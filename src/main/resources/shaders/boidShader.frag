#version 430

out vec4 fragColor;

in vec4 color;
uniform float temperature;
uniform float cloudiness;
uniform float sunAngle;

in vec3 Normal;
in vec3 FragPos;
void main() {
    float redShift = max(0, temperature - 0.6);
    float blueShift = max(0, 0.6 - temperature);

    vec3 lightColor = vec3(
    (165.0f + redShift * 75.0f - blueShift * 20.0f),
    119.0f + redShift * 50.0f - blueShift * 50.0f,
    (68.0f  + blueShift * 100.0f - redShift * 30.0f)
    ) / 255.0;

    vec3 lightPos = vec3(1000.0f * cos(radians(180 - sunAngle)), 0.0f, 1000.0f * sin(radians(180 - sunAngle)));

    float ambientStrength = 0.1f;
    vec3 ambient = ambientStrength * lightColor;

    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    float diffuseStrength = max(dot(norm, lightDir), 0.0f);
    vec3 diffuse = diffuseStrength * lightColor;

    float specularStrength = 0.9f;
    vec3 viewDir = normalize(-FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0f), 2);
    vec3 specular = specularStrength * spec * lightColor * (1.0f - cloudiness);

    vec3 result = ambient + diffuse + specular;
    fragColor = vec4(result, color.a);
}