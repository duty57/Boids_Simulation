#version 430

layout(location = 0) in vec3 position;

uniform float windSpeed;
uniform mat4 transformationMatrix;
out vec4 color;

void main() {

    gl_Position = transformationMatrix * vec4(position, 1.0f);
    color = vec4(1.0f, 1.0f, 1.0f, windSpeed * 0.8f);

}