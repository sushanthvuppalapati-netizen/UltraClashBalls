import processing.core.PApplet;

/** A pickup: heal (0), speed boost (1), damage boost (2), or flamethrower (3). */
public class PowerUp {
    float x, y;
    int type;
    float size = 24;
    float pulse = 0;

    public PowerUp(float x, float y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void update() {
        pulse += 0.05f;
    }

    public boolean checkCollision(Ball b) {
        if (PApplet.dist(x, y, b.x, b.y) < GameConfig.RADIUS + size / 2) {
            if (b.canPickUp(type)) {
                b.applyPowerUp(type);
                return true;
            }
        }
        return false;
    }

    public void draw(PApplet p) {
        p.pushMatrix();
        p.translate(x, y, 0);

        float scaleFactor = 1.0f + p.sin(pulse) * 0.15f;
        p.scale(scaleFactor);

        if (type == 0) {
            p.fill(0, 230, 80);
            p.stroke(255);
            p.strokeWeight(2);
            p.rectMode(PApplet.CENTER);
            p.rect(0, 0, 8, 26, 3);
            p.rect(0, 0, 26, 8, 3);
        } else if (type == 1) {
            p.fill(255, 220, 0);
            p.stroke(0);
            p.strokeWeight(1.5f);
            p.beginShape();
            p.vertex(-10, -5);
            p.vertex(-3, -5);
            p.vertex(0, 2);
            p.vertex(10, 2);
            p.bezierVertex(14, 2, 14, 8, 10, 8);
            p.vertex(-10, 8);
            p.endShape(PApplet.CLOSE);
            p.fill(200, 170, 0);
            p.rectMode(PApplet.CORNER);
            p.rect(-10, 8, 20, 3);
        } else if (type == 2) {
            p.fill(230, 40, 40);
            p.stroke(0);
            p.strokeWeight(1.5f);
            p.rectMode(PApplet.CENTER);
            p.rect(0, 2, 16, 14, 4);
            p.ellipse(-6, -5, 5, 6);
            p.ellipse(-2, -6, 5, 6);
            p.ellipse(2, -6, 5, 6);
            p.ellipse(6, -5, 5, 6);
            p.ellipse(8, 3, 6, 8);
        } else if (type == 3) {
            p.stroke(255, 120, 0);
            p.strokeWeight(1);
            p.fill(255, 120, 0);
            p.beginShape();
            p.vertex(0, -14);
            p.bezierVertex(6, -6, 12, 0, 10, 8);
            p.bezierVertex(8, 14, -8, 14, -10, 8);
            p.bezierVertex(-12, 0, -6, -6, 0, -14);
            p.endShape(PApplet.CLOSE);
            p.fill(255, 230, 0);
            p.noStroke();
            p.beginShape();
            p.vertex(0, -6);
            p.bezierVertex(3, -2, 6, 2, 5, 6);
            p.bezierVertex(4, 9, -4, 9, -5, 6);
            p.bezierVertex(-6, 2, -3, -2, 0, -6);
            p.endShape(PApplet.CLOSE);
        }

        p.popMatrix();
    }
}