#version 430

out vec4 fragColor;

in vec4 color;
uniform float temperature;
uniform float cloudiness;
uniform float sunAngle;

in vec3 Normal;
in vec3 FragPos;

void main() {

    vec3 lightColor = vec3(
        mix(76.0f, 248.0f, temperature),
        mix(165.0f, 92.0f, temperature),
        mix(255.0f, 56.0f, temperature)
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