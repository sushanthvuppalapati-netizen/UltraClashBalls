/* global p5, Ball, GameConfig */

class Tree {
    constructor(x, y, size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    resolveCollision(b) {
        const d = dist(this.x, this.y, b.x, b.y);
        const minDist = this.size + GameConfig.RADIUS;

        if (d < minDist && d > 0) {
            const nx = (b.x - this.x) / d;
            const ny = (b.y - this.y) / d;

            const overlap = minDist - d;
            b.x += nx * overlap;
            b.y += ny * overlap;

            b.speedX *= -0.5;
            b.speedY *= -0.5;
        }
    }

    /** obstacleColor comes from the current map's palette (GameConfig.OBSTACLE_COL). */
    draw(obstacleColor) {
        push();
        translate(this.x, this.y, 0);

        noStroke();
        fill(100, 60, 20);
        box(12, 12, 25);

        translate(0, 0, 15);
        fill(obstacleColor);
        sphere(this.size);

        pop();
    }
}