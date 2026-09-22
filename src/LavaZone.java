import processing.core.PApplet;

/** A rectangular hazard zone (lava/ice-hole/acid depending on the map) that damages balls standing in it. */
public class LavaZone {
    float x, y, w, h;

    public LavaZone(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void checkDamage(Ball b) {
        if (b.x > x - w / 2 && b.x < x + w / 2 && b.y > y - h / 2 && b.y < y + h / 2) {
            b.takeDamage(0.3f);
        }
    }

    /** fillColor/strokeColor come from the current map's palette (GameConfig.HAZARD_FILL / HAZARD_LINE). */
    public void draw(PApplet p, int fillColor, int strokeColor) {
        p.pushMatrix();
        p.translate(x, y, -1);
        p.rectMode(PApplet.CENTER);
        p.fill(fillColor);
        p.stroke(strokeColor);
        p.strokeWeight(3);
        p.rect(0, 0, w, h, 15);
        p.popMatrix();
    }
}