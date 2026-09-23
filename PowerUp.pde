class PowerUp {
    float x, y;
    int type;
    float size = 24;
    float pulse = 0;

    PowerUp(float x, float y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    void update() {
        pulse += 0.05;
    }

    boolean checkCollision(Ball b) {
        if (dist(x, y, b.x, b.y) < GameConfig.RADIUS + size / 2) {
            if (b.canPickUp(type)) {
                b.applyPowerUp(type);
                return true;
            }
        }
        return false;
    }

    void draw() {
        pushMatrix();
        translate(x, y);

        float scaleFactor = 1.0 + sin(pulse) * 0.15;
        scale(scaleFactor);

        if (type == 0) { // Heal
            fill(0, 230, 80);
            stroke(255);
            strokeWeight(2);
            rectMode(CENTER);
            rect(0, 0, 8, 26, 3);
            rect(0, 0, 26, 8, 3);
        } else if (type == 1) { // Speed Boost
            fill(255, 220, 0);
            stroke(0);
            strokeWeight(1.5);
            beginShape();
            vertex(-10, -5);
            vertex(-3, -5);
            vertex(0, 2);
            vertex(10, 2);
            bezierVertex(14, 2, 14, 8, 10, 8);
            vertex(-10, 8);
            endShape(CLOSE);
            fill(200, 170, 0);
            rectMode(CORNER);
            rect(-10, 8, 20, 3);
        } else if (type == 2) { // Damage Boost
            fill(230, 40, 40);
            stroke(0);
            strokeWeight(1.5);
            rectMode(CENTER);
            rect(0, 2, 16, 14, 4);
            ellipse(-6, -5, 5, 6);
            ellipse(-2, -6, 5, 6);
            ellipse(2, -6, 5, 6);
            ellipse(6, -5, 5, 6);
            ellipse(8, 3, 6, 8);
        } else if (type == 3) { // Flamethrower
            stroke(255, 120, 0);
            strokeWeight(1);
            fill(255, 120, 0);
            beginShape();
            vertex(0, -14);
            bezierVertex(6, -6, 12, 0, 10, 8);
            bezierVertex(8, 14, -8, 14, -10, 8);
            bezierVertex(-12, 0, -6, -6, 0, -14);
            endShape(CLOSE);
            fill(255, 230, 0);
            noStroke();
            beginShape();
            vertex(0, -6);
            bezierVertex(3, -2, 6, 2, 5, 6);
            bezierVertex(4, 9, -4, 9, -5, 6);
            bezierVertex(-6, 2, -3, -2, 0, -6);
            endShape(CLOSE);
        }

        popMatrix();
    }
}