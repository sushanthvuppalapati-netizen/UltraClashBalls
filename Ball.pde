import processing.core.PApplet;
import java.util.ArrayList;

/** A player-controlled ball: movement, health, weapon swinging, and rendering. */
public class Ball {
    float x, y, z = 0;
    float speedX = 0, speedY = 0;
    float holdTimeX = 0, holdTimeY = 0;
    boolean keyUp, keyDown, keyLeft, keyRight;
    int activeColor;
    int accessoryType;
    int weaponType;
    int classType;

    float ballRotation = 0;
    float weaponSwingAngle = 0;
    float weaponSwingTimer = 0;
    float weaponSwingSpeed = 0.03f;
    float weaponLength = 40;

    float maxHealth;
    float health;
    float classHealthMult;
    float classSpeedMult;
    float classDamageMult;
    float weaponDamageMult;
    float weaponRotationSpeedMult;

    float speedMultiplier = 1.0f;
    float dmgMultiplier = 1.0f;
    boolean hasFlamethrower = false;

    int speedTimer = 0;
    int dmgTimer = 0;
    int flamethrowerTimer = 0;
    int minigunCooldown = 0;

    public Ball(PApplet p, float x, float y, int activeColor, int accessoryType, int weaponType, int classType) {
        this.x = x;
        this.y = y;
        this.activeColor = activeColor;
        this.accessoryType = accessoryType;
        this.weaponType = weaponType;
        this.classType = classType;
        this.weaponSwingTimer = p.random(100);

        // Apply class stats
        float[] stats = GameConfig.CLASS_STATS[classType];
        this.maxHealth = stats[0];
        this.health = stats[0];
        this.classSpeedMult = stats[1];
        this.classDamageMult = stats[2];

        // Apply weapon stats
        float[] wStats = GameConfig.WEAPON_STATS[weaponType];
        this.weaponDamageMult = wStats[0];
        this.weaponRotationSpeedMult = wStats[1];
    }


        health = PApplet.max(0, health - amount);


    public boolean canPickUp(int type) {
        if (type == 0) return health < maxHealth;
        if (type == 1) return speedTimer == 0;
        if (type == 2) return dmgTimer == 0;
        if (type == 3) return !hasFlamethrower;
        return true;
    }

    public void applyPowerUp(int type) {
        if (type == 0) {
            health = PApplet.min(maxHealth, health + 30);
        } else if (type == 1) {
            speedMultiplier = 1.5f;
            speedTimer = 300;
        } else if (type == 2) {
            dmgMultiplier = 2.0f;
            dmgTimer = 300;
        } else if (type == 3) {
            hasFlamethrower = true;
            weaponSwingSpeed = 0.15f;
            flamethrowerTimer = 300;
        }
    }

    /**
     * Advances the weapon swing and, for the Minigun, spawns bullets into the shared list.
     * If the Minigun has a clear line of sight to {@code opponent} (no tree in the way),
     * it locks on and fires straight at them instead of sweeping blindly.
     */
    public void updateWeapon(ArrayList<Bullet> bullets, Ball opponent, ArrayList<Tree> obstacles) {
        if (minigunCooldown > 0) minigunCooldown--;

        boolean autoAiming = (weaponType == 5) && hasLineOfSight(opponent.x, opponent.y, obstacles);

        if (autoAiming) {
            // Lock the barrel onto the opponent instead of sweeping.
            float targetAngle = PApplet.atan2(opponent.y - y, opponent.x - x);
            weaponSwingAngle = targetAngle - ballRotation;
            weaponSwingTimer += weaponSwingSpeed * weaponRotationSpeedMult; // keep barrel-spin animation alive
        } else {
            float rotSpeed = weaponSwingSpeed * weaponRotationSpeedMult;
            if (hasFlamethrower) {
                weaponSwingTimer += 0.15f * weaponRotationSpeedMult;
            } else {
                weaponSwingTimer += rotSpeed;
            }
            weaponSwingAngle = PApplet.sin(weaponSwingTimer) * PApplet.HALF_PI;
        }

        // Minigun firing
        if (weaponType == 5 && minigunCooldown <= 0) {
            float fireAngle = autoAiming
                    ? PApplet.atan2(opponent.y - y, opponent.x - x)
                    : ballRotation + weaponSwingAngle;
            float bulletX = x + PApplet.cos(fireAngle) * (GameConfig.RADIUS + 15);
            float bulletY = y + PApplet.sin(fireAngle) * (GameConfig.RADIUS + 15);
            float bulletSpeed = 8.0f;
            float bulletDmg = 2.0f * weaponDamageMult * dmgMultiplier * classDamageMult;
            bullets.add(new Bullet(bulletX, bulletY, PApplet.cos(fireAngle) * bulletSpeed, PApplet.sin(fireAngle) * bulletSpeed, bulletDmg));
            minigunCooldown = 4; // Fire every 4 frames
        }
    }

    /** True if no tree obstacle sits between this ball and (targetX, targetY). */
    private boolean hasLineOfSight(float targetX, float targetY, ArrayList<Tree> obstacles) {
        for (Tree t : obstacles) {
            if (segmentIntersectsCircle(x, y, targetX, targetY, t.x, t.y, t.size)) {
                return false;
            }
        }
        return true;
    }

    /** Whether the line segment A->B passes within radius r of circle center C. */
    private static boolean segmentIntersectsCircle(float ax, float ay, float bx, float by, float cx, float cy, float r) {
        float dx = bx - ax;
        float dy = by - ay;
        float lenSq = dx * dx + dy * dy;
        float t = lenSq > 0 ? ((cx - ax) * dx + (cy - ay) * dy) / lenSq : 0;
        t = PApplet.constrain(t, 0, 1);
        float closestX = ax + t * dx;
        float closestY = ay + t * dy;
        float ddx = closestX - cx;
        float ddy = closestY - cy;
        return (ddx * ddx + ddy * ddy) <= r * r;
    }

    /** gravity/drag are passed in since they vary per map. */
    public void update(int width, int height, float gravity, float drag) {
        if (speedTimer > 0) {
            speedTimer--;
            if (speedTimer == 0) speedMultiplier = 1.0f;
        }
        if (dmgTimer > 0) {
            dmgTimer--;
            if (dmgTimer == 0) dmgMultiplier = 1.0f;
        }
        if (flamethrowerTimer > 0) {
            flamethrowerTimer--;
            if (flamethrowerTimer == 0) {
                hasFlamethrower = false;
                weaponSwingSpeed = 0.03f;
            }
        }

        float currentMaxSpeed = GameConfig.MAX_SPEED * speedMultiplier * classSpeedMult;

        if (keyLeft && keyRight) {
            speedX = 0;
            holdTimeX = 0;
        } else if (keyLeft) {
            holdTimeX++;
            speedX -= (GameConfig.BASE_ACCEL + (holdTimeX * GameConfig.ACCEL_RAMP_RATE)) * speedMultiplier;
        } else if (keyRight) {
            holdTimeX++;
            speedX += (GameConfig.BASE_ACCEL + (holdTimeX * GameConfig.ACCEL_RAMP_RATE)) * speedMultiplier;
        } else {
            holdTimeX = 0;
            speedX *= drag;
        }

        if (keyUp && keyDown) {
            speedY = 0;
            holdTimeY = 0;
        } else if (keyUp) {
            holdTimeY++;
            speedY -= (GameConfig.BASE_ACCEL + (holdTimeY * GameConfig.ACCEL_RAMP_RATE)) * speedMultiplier;
        } else if (keyDown) {
            holdTimeY++;
            speedY += (GameConfig.BASE_ACCEL + (holdTimeY * GameConfig.ACCEL_RAMP_RATE)) * speedMultiplier;
        } else {
            holdTimeY = 0;
            speedY += gravity;
            speedY *= drag;
        }

        speedX = PApplet.constrain(speedX, -currentMaxSpeed, currentMaxSpeed);
        speedY = PApplet.constrain(speedY, -currentMaxSpeed, currentMaxSpeed);

        x += speedX;
        y += speedY;

        ballRotation += speedX * 0.05f;

        float radius = GameConfig.RADIUS;
        float restitution = GameConfig.RESTITUTION;
        if (y > height - radius) { y = height - radius; speedY *= -restitution; }
        if (y < radius) { y = radius; speedY *= -restitution; }
        if (x > width - radius) { x = width - radius; speedX *= -restitution; }
        if (x < radius) { x = radius; speedX *= -restitution; }
    }

    public void draw(PApplet p, boolean paused, String playerName) {
        float radius = GameConfig.RADIUS;

        p.pushMatrix();
        p.translate(x, y, z);

        // Name Label & Health Bar
        if (playerName.length() > 0) {
            p.pushMatrix();
            p.translate(0, -radius - 22, 0);
            p.textAlign(PApplet.CENTER, PApplet.BOTTOM);
            p.textSize(12);
            p.fill(255);
            p.text(playerName, 0, -5);

            p.rectMode(PApplet.CENTER);
            p.noStroke();
            p.fill(80);
            float barWidth = 50;
            p.rect(0, 0, barWidth, 6);
            p.fill(0, 230, 100);
            float currentWidth = p.map(health, 0, maxHealth, 0, barWidth);
            p.rect(-(barWidth - currentWidth) / 2, 0, currentWidth, 6);
            p.popMatrix();
        }

        // Render Ball Body
        p.pushMatrix();
        p.rotateZ(ballRotation);
        if (paused) p.fill(255, 200, 0);
        else p.fill(activeColor);
        p.noStroke();
        p.strokeWeight(1);
        p.sphere(radius);

        // Render Accessories
        drawAccessory(p);

        p.popMatrix();

        // Render Weapon
        p.pushMatrix();
        p.rotateZ(ballRotation + weaponSwingAngle);

        if (hasFlamethrower) {
            p.stroke(100);
            p.strokeWeight(8);
            p.line(radius, 0, 0, radius + 20, 0, 0);

            p.noStroke();
            p.fill(255, p.random(100, 200), 0, 200);
            p.ellipse(radius + 35 + p.random(-5, 5), p.random(-4, 4), 25, 15);
            p.fill(255, 50, 0, 180);
            p.ellipse(radius + 50 + p.random(-5, 5), p.random(-8, 8), 35, 22);
        } else {
            drawCustomWeapon(p);
        }

        p.popMatrix();
        p.popMatrix();
    }

    private void drawAccessory(PApplet p) {
        float radius = GameConfig.RADIUS;

        p.pushMatrix();
        p.rectMode(PApplet.CENTER);

        if (accessoryType == 0) { // Crown
            p.translate(0, -radius - 8, 5);
            p.fill(255, 215, 0);
            p.stroke(180, 140, 0);
            p.strokeWeight(1);
            p.beginShape();
            p.vertex(-16, 8);
            p.vertex(-16, -10);
            p.vertex(-8, -2);
            p.vertex(0, -14);
            p.vertex(8, -2);
            p.vertex(16, -10);
            p.vertex(16, 8);
            p.endShape(PApplet.CLOSE);
        } else if (accessoryType == 1) { // Top Hat
            p.translate(0, -radius - 12, 5);
            p.fill(30);
            p.stroke(10);
            p.rect(0, 8, 38, 5, 2);
            p.rect(0, -4, 22, 20, 2);
            p.fill(200, 30, 30);
            p.rect(0, 4, 22, 4);
        } else if (accessoryType == 2) { // Cowboy Hat
            p.translate(0, -radius - 8, 5);
            p.fill(133, 94, 66);
            p.stroke(80, 50, 30);
            p.ellipse(0, 6, 48, 10);
            p.rect(0, -2, 22, 12, 4);
        } else if (accessoryType == 3) { // Glasses
            p.translate(0, -6, radius + 2);
            p.fill(20, 20, 20, 220);
            p.stroke(255);
            p.strokeWeight(1.5f);
            p.rect(-10, 0, 14, 10, 2);
            p.rect(10, 0, 14, 10, 2);
            p.line(-3, 0, 3, 0);
        } else if (accessoryType == 4) { // Horns
            p.translate(0, -radius + 2, 5);
            p.fill(220, 220, 200);
            p.stroke(150);
            p.triangle(-18, 5, -12, 5, -20, -18);
            p.triangle(18, 5, 12, 5, 20, -18);
        }
        p.popMatrix();
    }

    private void drawCustomWeapon(PApplet p) {
        float radius = GameConfig.RADIUS;

        if (weaponType == 0) { // Battleaxe
            p.stroke(100, 60, 25);
            p.strokeWeight(6);
            p.line(radius, 0, 0, radius + weaponLength, 0, 0);

            p.pushMatrix();
            p.translate(radius + weaponLength - 8, 0, 0);
            if (dmgTimer > 0) p.fill(255, 80, 80);
            else p.fill(170, 175, 180);
            p.stroke(220);
            p.strokeWeight(1.5f);
            p.beginShape();
            p.vertex(0, -5);
            p.bezierVertex(12, -22, 18, -18, 16, 0);
            p.bezierVertex(18, 18, 12, 22, 0, 5);
            p.vertex(-3, 0);
            p.endShape(PApplet.CLOSE);
            p.popMatrix();

        } else if (weaponType == 1) { // Broadsword
            p.stroke(120, 125, 130);
            p.strokeWeight(6);
            p.line(radius + 12, -10, 0, radius + 12, 10, 0);

            p.pushMatrix();
            p.translate(radius + 12, 0, 0);
            if (dmgTimer > 0) p.fill(255, 80, 80);
            else p.fill(200, 205, 215);
            p.stroke(140);
            p.strokeWeight(1);
            p.beginShape();
            p.vertex(0, -3.5f);
            p.vertex(weaponLength - 15, -3);
            p.vertex(weaponLength - 5, 0);
            p.vertex(weaponLength - 15, 3);
            p.vertex(0, 3.5f);
            p.endShape(PApplet.CLOSE);
            p.popMatrix();

        } else if (weaponType == 2) { // Spear
            p.stroke(110, 70, 30);
            p.strokeWeight(4);
            p.line(radius, 0, 0, radius + weaponLength + 10, 0, 0);

            p.pushMatrix();
            p.translate(radius + weaponLength + 10, 0, 0);
            if (dmgTimer > 0) p.fill(255, 80, 80);
            else p.fill(220);
            p.stroke(160);
            p.strokeWeight(1);
            p.triangle(0, -5, 15, 0, 0, 5);
            p.popMatrix();

        } else if (weaponType == 3) { // Katana
            p.stroke(40);
            p.strokeWeight(4);
            p.line(radius, 0, 0, radius + 10, 0, 0);

            p.pushMatrix();
            p.translate(radius + 10, 0, 0);
            if (dmgTimer > 0) p.fill(255, 80, 80);
            else p.fill(230, 235, 240);
            p.stroke(180);
            p.strokeWeight(1);
            p.beginShape();
            p.vertex(0, -2);
            p.vertex(weaponLength, -4);
            p.vertex(weaponLength + 5, 0);
            p.vertex(weaponLength, 1);
            p.vertex(0, 2);
            p.endShape(PApplet.CLOSE);
            p.popMatrix();

        } else if (weaponType == 4) { // Warhammer
            p.stroke(90, 50, 20);
            p.strokeWeight(6);
            p.line(radius, 0, 0, radius + weaponLength, 0, 0);

            p.pushMatrix();
            p.translate(radius + weaponLength - 5, 0, 0);
            if (dmgTimer > 0) p.fill(255, 80, 80);
            else p.fill(130, 135, 140);
            p.stroke(80);
            p.strokeWeight(1.5f);
            p.rectMode(PApplet.CENTER);
            p.rect(0, 0, 18, 28, 3);
            p.popMatrix();

        } else if (weaponType == 5) { // Minigun
            p.stroke(100, 100, 100);
            p.strokeWeight(5);
            p.line(radius, 0, 0, radius + 18, 0, 0);

            p.pushMatrix();
            p.translate(radius + 18, 0, 0);
            p.fill(80);
            p.stroke(120);
            p.strokeWeight(1);
            p.box(8, 20, 8);

            // Rotating barrel (drawn as a box since PApplet has no built-in cylinder())
            p.pushMatrix();
            p.rotateY(weaponSwingTimer * 0.5f);
            p.fill(150);
            p.box(6, 15, 6);
            p.popMatrix();
            p.popMatrix();
        }
    }
}