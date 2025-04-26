package models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Wind {

    private int vao;
    private int vbo;
    private int renderProgram;

    private float[] vertices = {
            -1.1f, 0.0f, 0.1f,
            -0.9f, 0.0f, 0.1f,

            -1.1f, -0.1f, 0.1f,
            -0.9f, -0.1f, 0.1f,

            -1.1f, 0.1f, 0.1f,
            -0.9f, 0.1f, 0.1f,

            -1.1f, -0.2f, 0.1f,
            -0.9f, -0.2f, 0.1f,

            -1.1f, 0.2f, 0.1f,
            -0.9f, 0.2f, 0.1f,
    };

    float deltaTime = 0.0f;

    public Wind(){}

}
