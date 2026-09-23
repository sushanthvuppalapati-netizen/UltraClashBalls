class Ball {
    float x, y;
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
    float weaponSwingSpeed = 0.03;
    float weaponLength = 40;

    float maxHealth;
    float health;
    float classHealthMult;
    float classSpeedMult;
    float classDamageMult;
    float weaponDamageMult;
    float weaponRotationSpeedMult;

    float speedMultiplier = 1.0;
    float dmgMultiplier = 1.0;
    boolean hasFlamethrower = false;

    int speedTimer = 0;
    int dmgTimer = 0;
    int flamethrowerTimer = 0;
    int minigunCooldown = 0;

    Ball(float x, float y, int activeColor, int accessoryType, int weaponType, int classType) {
        this.x = x;
        this.y = y;
        this.activeColor = activeColor;
        this.accessoryType = accessoryType;
        this.weaponType = weaponType;
        this.classType = classType;
        this.weaponSwingTimer = random(100);

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

    void takeDamage(float amount) {
        health = max(0, health - amount);
    }

    boolean canPickUp(int type) {
        if (type == 0) return health < maxHealth;
        if (type == 1) return speedTimer == 0;
        if (type == 2) return dmgTimer == 0;
        if (type == 3) return !hasFlamethrower;
        return true;
    }

    void applyPowerUp(int type) {
        if (type == 0) {
            health = min(maxHealth, health + 30);
        } else if (type == 1) {
            speedMultiplier = 1.5;
            speedTimer = 300;
        } else if (type == 2) {
            dmgMultiplier = 2.0;
            dmgTimer = 300;
        } else if (type == 3) {
            hasFlamethrower = true;
            weaponSwingSpeed = 0.15;
            flamethrowerTimer = 300;
        }
    }

    void updateWeapon(ArrayList<Bullet> bullets, Ball opponent, ArrayList<Tree> obstacles) {
        if (minigunCooldown > 0) minigunCooldown--;

        boolean autoAiming = (weaponType == 5) && hasLineOfSight(opponent.x, opponent.y, obstacles);

        if (autoAiming) {
            float targetAngle = atan2(opponent.y - y, opponent.x - x);
            weaponSwingAngle = targetAngle - ballRotation;
            weaponSwingTimer += weaponSwingSpeed * weaponRotationSpeedMult;
        } else {
            float rotSpeed = weaponSwingSpeed * weaponRotationSpeedMult;
            if (hasFlamethrower) {
                weaponSwingTimer += 0.15 * weaponRotationSpeedMult;
            } else {
                weaponSwingTimer += rotSpeed;
            }
            weaponSwingAngle = sin(weaponSwingTimer) * HALF_PI;
        }

        if (weaponType == 5 && minigunCooldown <= 0) {
            float fireAngle = autoAiming
                    ? atan2(opponent.y - y, opponent.x - x)
                    : ballRotation + weaponSwingAngle;
            float bulletX = x + cos(fireAngle) * (GameConfig.RADIUS + 15);
            float bulletY = y + sin(fireAngle) * (GameConfig.RADIUS + 15);
            float bulletSpeed = 8.0;
            float bulletDmg = 2.0 * weaponDamageMult * dmgMultiplier * classDamageMult;
            bullets.add(new Bullet(bulletX, bulletY, cos(fireAngle) * bulletSpeed, sin(fireAngle) * bulletSpeed, bulletDmg));
            minigunCooldown = 4;
        }
    }

    boolean hasLineOfSight(float targetX, float targetY, ArrayList<Tree> obstacles) {
        for (int i = 0; i < obstacles.size(); i++) {
            Tree t = obstacles.get(i);
            if (segmentIntersectsCircle(x, y, targetX, targetY, t.x, t.y, t.size)) {
                return false;
            }
        }
        return true;
    }

    boolean segmentIntersectsCircle(float ax, float ay, float bx, float by, float cx, float cy, float r) {
        float dx = bx - ax;
        float dy = by - ay;
        float lenSq = dx * dx + dy * dy;
        float t = lenSq > 0 ? ((cx - ax) * dx + (cy - ay) * dy) / lenSq : 0;
        t = constrain(t, 0, 1);
        float closestX = ax + t * dx;
        float closestY = ay + t * dy;
        float ddx = closestX - cx;
        float ddy = closestY - cy;
        return (ddx * ddx + ddy * ddy) <= r * r;
    }

    void update(int width, int height, float gravity, float drag) {
        if (speedTimer > 0) {
            speedTimer--;
            if (speedTimer == 0) speedMultiplier = 1.0;
        }
        if (dmgTimer > 0) {
            dmgTimer--;
            if (dmgTimer == 0) dmgMultiplier = 1.0;
        }
        if (flamethrowerTimer > 0) {
            flamethrowerTimer--;
            if (flamethrowerTimer == 0) {
                hasFlamethrower = false;
                weaponSwingSpeed = 0.03;
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

        speedX = constrain(speedX, -currentMaxSpeed, currentMaxSpeed);
        speedY = constrain(speedY, -currentMaxSpeed, currentMaxSpeed);

        x += speedX;
        y += speedY;

        ballRotation += speedX * 0.05;

        float radius = GameConfig.RADIUS;
        float restitution = GameConfig.RESTITUTION;
        if (y > height - radius) { y = height - radius; speedY *= -restitution; }
        if (y < radius) { y = radius; speedY *= -restitution; }
        if (x > width - radius) { x = width - radius; speedX *= -restitution; }
        if (x < radius) { x = radius; speedX *= -restitution; }
    }

    void draw(boolean paused, String playerName) {
        float radius = GameConfig.RADIUS;

        pushMatrix();
        translate(x, y);

        // Name Label & Health Bar
        if (playerName.length() > 0) {
            pushMatrix();
            translate(0, -radius - 22);
            textAlign(CENTER, BOTTOM);
            textSize(12);
            fill(255);
            text(playerName, 0, -5);

            rectMode(CENTER);
            noStroke();
            fill(80);
            float barWidth = 50;
            rect(0, 0, barWidth, 6);
            fill(0, 230, 100);
            float currentWidth = map(health, 0, maxHealth, 0, barWidth);
            rect(-(barWidth - currentWidth) / 2, 0, currentWidth, 6);
            popMatrix();
        }

        // Render Ball Body (2D Circle)
        pushMatrix();
        rotate(ballRotation);
        if (paused) fill(255, 200, 0);
        else fill(activeColor);
        noStroke();
        strokeWeight(1);
        ellipse(0, 0, radius * 2, radius * 2);

        // Render Accessories
        drawAccessory();

        popMatrix();

        // Render Weapon
        pushMatrix();
        rotate(ballRotation + weaponSwingAngle);

        if (hasFlamethrower) {
            stroke(100);
            strokeWeight(8);
            line(radius, 0, radius + 20, 0);

            noStroke();
            fill(255, random(100, 200), 0, 200);
            ellipse(radius + 35 + random(-5, 5), random(-4, 4), 25, 15);
            fill(255, 50, 0, 180);
            ellipse(radius + 50 + random(-5, 5), random(-8, 8), 35, 22);
        } else {
            drawCustomWeapon();
        }

        popMatrix();
        popMatrix();
    }

    void drawAccessory() {
        float radius = GameConfig.RADIUS;

        pushMatrix();
        rectMode(CENTER);

        if (accessoryType == 0) { // Crown
            translate(0, -radius - 8);
            fill(255, 215, 0);
            stroke(180, 140, 0);
            strokeWeight(1);
            beginShape();
            vertex(-16, 8);
            vertex(-16, -10);
            vertex(-8, -2);
            vertex(0, -14);
            vertex(8, -2);
            vertex(16, -10);
            vertex(16, 8);
            endShape(CLOSE);
        } else if (accessoryType == 1) { // Top Hat
            translate(0, -radius - 12);
            fill(30);
            stroke(10);
            rect(0, 8, 38, 5, 2);
            rect(0, -4, 22, 20, 2);
            fill(200, 30, 30);
            rect(0, 4, 22, 4);
        } else if (accessoryType == 2) { // Cowboy Hat
            translate(0, -radius - 8);
            fill(133, 94, 66);
            stroke(80, 50, 30);
            ellipse(0, 6, 48, 10);
            rect(0, -2, 22, 12, 4);
        } else if (accessoryType == 3) { // Glasses
            translate(0, -6);
            fill(20, 20, 20, 220);
            stroke(255);
            strokeWeight(1.5);
            rect(-10, 0, 14, 10, 2);
            rect(10, 0, 14, 10, 2);
            line(-3, 0, 3, 0);
        } else if (accessoryType == 4) { // Horns
            translate(0, -radius + 2);
            fill(220, 220, 200);
            stroke(150);
            triangle(-18, 5, -12, 5, -20, -18);
            triangle(18, 5, 12, 5, 20, -18);
        }
        popMatrix();
    }

    void drawCustomWeapon() {
        float radius = GameConfig.RADIUS;

        if (weaponType == 0) { // Battleaxe
            stroke(100, 60, 25);
            strokeWeight(6);
            line(radius, 0, radius + weaponLength, 0);

            pushMatrix();
            translate(radius + weaponLength - 8, 0);
            if (dmgTimer > 0) fill(255, 80, 80);
            else fill(170, 175, 180);
            stroke(220);
            strokeWeight(1.5);
            beginShape();
            vertex(0, -5);
            bezierVertex(12, -22, 18, -18, 16, 0);
            bezierVertex(18, 18, 12, 22, 0, 5);
            vertex(-3, 0);
            endShape(CLOSE);
            popMatrix();

        } else if (weaponType == 1) { // Broadsword
            stroke(120, 125, 130);
            strokeWeight(6);
            line(radius + 12, -10, radius + 12, 10);

            pushMatrix();
            translate(radius + 12, 0);
            if (dmgTimer > 0) fill(255, 80, 80);
            else fill(200, 205, 215);
            stroke(140);
            strokeWeight(1);
            beginShape();
            vertex(0, -3.5);
            vertex(weaponLength - 15, -3);
            vertex(weaponLength - 5, 0);
            vertex(weaponLength - 15, 3);
            vertex(0, 3.5);
            endShape(CLOSE);
            popMatrix();

        } else if (weaponType == 2) { // Spear
            stroke(110, 70, 30);
            strokeWeight(4);
            line(radius, 0, radius + weaponLength + 10, 0);

            pushMatrix();
            translate(radius + weaponLength + 10, 0);
            if (dmgTimer > 0) fill(255, 80, 80);
            else fill(220);
            stroke(160);
            strokeWeight(1);
            triangle(0, -5, 15, 0, 0, 5);
            popMatrix();

        } else if (weaponType == 3) { // Katana
            stroke(40);
            strokeWeight(4);
            line(radius, 0, radius + 10, 0);

            pushMatrix();
            translate(radius + 10, 0);
            if (dmgTimer > 0) fill(255, 80, 80);
            else fill(230, 235, 240);
            stroke(180);
            strokeWeight(1);
            beginShape();
            vertex(0, -2);
            vertex(weaponLength, -4);
            vertex(weaponLength + 5, 0);
            vertex(weaponLength, 1);
            vertex(0, 2);
            endShape(CLOSE);
            popMatrix();

        } else if (weaponType == 4) { // Warhammer
            stroke(90, 50, 20);
            strokeWeight(6);
            line(radius, 0, radius + weaponLength, 0);

            pushMatrix();
            translate(radius + weaponLength - 5, 0);
            if (dmgTimer > 0) fill(255, 80, 80);
            else fill(130, 135, 140);
            stroke(80);
            strokeWeight(1.5);
            rectMode(CENTER);
            rect(0, 0, 18, 28, 3);
            popMatrix();

        } else if (weaponType == 5) { // Minigun
            stroke(100, 100, 100);
            strokeWeight(5);
            line(radius, 0, radius + 18, 0);

            pushMatrix();
            translate(radius + 18, 0);
            fill(80);
            stroke(120);
            strokeWeight(1);
            rectMode(CENTER);
            rect(0, 0, 8, 20);

            pushMatrix();
            rotate(weaponSwingTimer * 0.5);
            fill(150);
            rect(0, 0, 6, 15);
            popMatrix();
            popMatrix();
        }
    }
}