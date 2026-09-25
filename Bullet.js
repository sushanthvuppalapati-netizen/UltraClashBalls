/* global p5, Ball, GameConfig */

class Bullet {
    constructor(x, y, speedX, speedY, damage) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.damage = damage;
        this.size = 8;
    }

    update() {
        this.x += this.speedX;
        this.y += this.speedY;
    }

    isOffScreen(width, height) {
        return this.x < -20 || this.x > width + 20 || this.y < -20 || this.y > height + 20;
    }

    checkHit(b) {
        return dist(this.x, this.y, b.x, b.y) < GameConfig.RADIUS + this.size / 2;
    }

    draw() {
        push();
        translate(this.x, this.y, 0);
        noStroke();
        fill(255, 220, 40);
        sphere(this.size / 2);
        pop();
    }
}