/* global p5, Ball */

class LavaZone {
    constructor(x, y, w, h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    checkDamage(b) {
        if (b.x > this.x - this.w / 2 && b.x < this.x + this.w / 2 &&
            b.y > this.y - this.h / 2 && b.y < this.y + this.h / 2) {
            b.takeDamage(0.3);
        }
    }

    /** fillColor/strokeColor come from the current map's palette (GameConfig.HAZARD_FILL / HAZARD_LINE). */
    draw(fillColor, strokeColor) {
        push();
        translate(this.x, this.y, -1);
        rectMode(CENTER);
        fill(fillColor);
        stroke(strokeColor);
        strokeWeight(3);
        rect(0, 0, this.w, this.h, 15);
        pop();
    }
}