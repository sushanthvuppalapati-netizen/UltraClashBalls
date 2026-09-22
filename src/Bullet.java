import processing.core.PApplet;

/** Simple projectile used by the Minigun weapon. */
public class Bullet {
    float x, y;
    float speedX, speedY;
    float damage;
    float size = 8;

    public Bullet(float x, float y, float speedX, float speedY, float damage) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.damage = damage;
    }

    public void update() {
        x += speedX;
        y += speedY;
    }

    public boolean isOffScreen(int width, int height) {
        return x < -20 || x > width + 20 || y < -20 || y > height + 20;
    }

    public boolean checkHit(Ball b) {
        return PApplet.dist(x, y, b.x, b.y) < GameConfig.RADIUS + size / 2;
    }

    public void draw(PApplet p) {
        p.pushMatrix();
        p.translate(x, y, 0);
        p.noStroke();
        p.fill(255, 220, 40);
        p.sphere(size / 2f);
        p.popMatrix();
    }
}