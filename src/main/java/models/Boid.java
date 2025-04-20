package models;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

@Getter
@Setter
public class Boid {
    Vector4f position;
    Vector4f velocity;
    float angle;
    float padding[] = new float[3];

    public Boid(Vector4f position, Vector4f velocity, float angle) {
        this.position = position;
        this.velocity = velocity;
        this.angle = angle;
    }
}
