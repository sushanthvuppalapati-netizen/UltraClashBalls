class Bullet {
    float x, y;
    float speedX, speedY;
    float damage;
    float size = 8;

    Bullet(float x, float y, float speedX, float speedY, float damage) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.damage = damage;
    }

    void update() {
        x += speedX;
        y += speedY;
    }

    boolean isOffScreen(int width, int height) {
        return x < -20 || x > width + 20 || y < -20 || y > height + 20;
    }

    boolean checkHit(Ball b) {
        return dist(x, y, b.x, b.y) < GameConfig.RADIUS + size / 2;
    }

    void draw() {
        noStroke();
        fill(255, 220, 40);
        ellipse(x, y, size, size);
    }
}