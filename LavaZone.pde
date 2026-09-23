class LavaZone {
    float x, y, w, h;

    LavaZone(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    void checkDamage(Ball b) {
        if (b.x > x - w / 2 && b.x < x + w / 2 && b.y > y - h / 2 && b.y < y + h / 2) {
            b.takeDamage(0.3);
        }
    }

    void draw(int fillColor, int strokeColor) {
        pushMatrix();
        translate(x, y);
        rectMode(CENTER);
        fill(fillColor);
        stroke(strokeColor);
        strokeWeight(3);
        rect(0, 0, w, h, 15);
        popMatrix();
    }
}