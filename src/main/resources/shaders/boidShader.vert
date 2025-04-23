#version 430

layout(location = 0) in vec2 position;// Triangle vertices input

struct Boid {
    vec4 position;
    vec4 velocity;
    float angle;
};

layout(std430, binding = 0) buffer boidBuffer {
    Boid boids[];
};


out vec4 color;

void main() {
    Boid boid = boids[gl_InstanceID];

    // Rotate boid vertex
    float c = cos(boid.angle);
    float s = sin(boid.angle);
    mat2 rotation = mat2(c, -s, s, c);
    vec2 rotatedPos = rotation * position;

    // Transform to world space
    vec2 finalPos = rotatedPos + boid.position.xy;
    gl_Position = vec4(finalPos, 0.0, 1.0);

    color = vec4(0.3 + length(boid.velocity.xy));
}
