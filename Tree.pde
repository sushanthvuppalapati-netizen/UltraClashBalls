import processing.core.PApplet;

/** A solid obstacle that balls bounce off of. */
public class Tree {
    float x, y, size;

    public Tree(float x, float y, float size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public void resolveCollision(Ball b) {
        float d = PApplet.dist(x, y, b.x, b.y);
        float minDist = size + GameConfig.RADIUS;

        if (d < minDist && d > 0) {
            float nx = (b.x - x) / d;
            float ny = (b.y - y) / d;

            float overlap = minDist - d;
            b.x += nx * overlap;
            b.y += ny * overlap;

            b.speedX *= -0.5f;
            b.speedY *= -0.5f;
        }
    }

    /** obstacleColor comes from the current map's palette (GameConfig.OBSTACLE_COL). */
    public void draw(PApplet p, int obstacleColor) {
        p.pushMatrix();
        p.translate(x, y, 0);

        p.noStroke();
        p.fill(100, 60, 20);
        p.box(12, 12, 25);

        p.translate(0, 0, 15);
        p.fill(obstacleColor);
        p.sphere(size);

        p.popMatrix();
    }
}