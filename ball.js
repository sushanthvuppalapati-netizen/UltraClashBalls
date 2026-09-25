/* global p5, GameConfig, Bullet, Tree */

class Ball {
    constructor(x, y, activeColor, accessoryType, weaponType, characterIndex) {
        this.x = x;
        this.y = y;
        this.z = 0;
        this.speedX = 0;
        this.speedY = 0;
        this.holdTimeX = 0;
        this.holdTimeY = 0;
        this.keyUp = false;
        this.keyDown = false;
        this.keyLeft = false;
        this.keyRight = false;
        this.activeColor = activeColor;
        this.accessoryType = accessoryType;
        this.weaponType = weaponType;
        this.characterIndex = characterIndex;

        this.ballRotation = 0;
        this.weaponSwingAngle = 0;
        this.weaponSwingTimer = random(100);
        this.weaponSwingSpeed = 0.03;
        this.weaponLength = 40;

        // Apply character class stats
        const c = GameConfig.CHARACTERS[characterIndex];
        this.maxHealth = c ? c.health : 100;
        this.health = this.maxHealth;
        this.classSpeedMult = c ? c.speedMult : 1.0;
        this.classDamageMult = c ? c.damageMult : 1.0;

        // Apply weapon stats
        const wStats = (GameConfig.WEAPON_STATS && GameConfig.WEAPON_STATS[weaponType])
            ? GameConfig.WEAPON_STATS[weaponType]
            : [1.0, 1.0];
        this.weaponDamageMult = wStats[0];
        this.weaponRotationSpeedMult = wStats[1];

        // Godly weapon exceptions
        if (weaponType === GameConfig.WEAPON_GODKILLER || weaponType === GameConfig.WEAPON_PLUTON) {
            this.weaponLength = (weaponType === GameConfig.WEAPON_PLUTON) ? 75 : 55;
            this.maxHealth += 5000;
            this.health += 5000;
        } else if (weaponType === GameConfig.WEAPON_STICK) {
            this.weaponLength = 22;
        }

        this.speedMultiplier = 1.0;
        this.dmgMultiplier = 1.0;
        this.hasFlamethrower = false;

        this.speedTimer = 0;
        this.dmgTimer = 0;
        this.flamethrowerTimer = 0;
        this.minigunCooldown = 0;
    }

    takeDamage(amount) {
        this.health = max(0, this.health - amount);
    }

    canPickUp(type) {
        if (type === 0) return this.health < this.maxHealth;
        if (type === 1) return this.speedTimer === 0;
        if (type === 2) return this.dmgTimer === 0;
        if (type === 3) return !this.hasFlamethrower;
        return true;
    }

    applyPowerUp(type) {
        if (type === 0) {
            this.health = min(this.maxHealth, this.health + 30);
        } else if (type === 1) {
            this.speedMultiplier = 1.5;
            this.speedTimer = 300;
        } else if (type === 2) {
            this.dmgMultiplier = 2.0;
            this.dmgTimer = 300;
        } else if (type === 3) {
            this.hasFlamethrower = true;
            this.weaponSwingSpeed = 0.15;
            this.flamethrowerTimer = 300;
        }
    }

    updateWeapon(bullets, opponent, obstacles) {
        if (this.minigunCooldown > 0) this.minigunCooldown--;

        const autoAiming = (this.weaponType === 5) && this.hasLineOfSight(opponent.x, opponent.y, obstacles);

        if (autoAiming) {
            const targetAngle = atan2(opponent.y - this.y, opponent.x - this.x);
            this.weaponSwingAngle = targetAngle - this.ballRotation;
            this.weaponSwingTimer += this.weaponSwingSpeed * this.weaponRotationSpeedMult;
        } else {
            const rotSpeed = this.weaponSwingSpeed * this.weaponRotationSpeedMult;
            if (this.hasFlamethrower) {
                this.weaponSwingTimer += 0.15 * this.weaponRotationSpeedMult;
            } else {
                this.weaponSwingTimer += rotSpeed;
            }
            this.weaponSwingAngle = sin(this.weaponSwingTimer) * HALF_PI;
        }

        if (this.weaponType === 5 && this.minigunCooldown <= 0) {
            const fireAngle = autoAiming
                ? atan2(opponent.y - this.y, opponent.x - this.x)
                : this.ballRotation + this.weaponSwingAngle;
            const bulletX = this.x + cos(fireAngle) * (GameConfig.RADIUS + 15);
            const bulletY = this.y + sin(fireAngle) * (GameConfig.RADIUS + 15);
            const bulletSpeed = 8.0;
            const bulletDmg = 2.0 * this.weaponDamageMult * this.dmgMultiplier * this.classDamageMult;
            bullets.push(new Bullet(bulletX, bulletY, cos(fireAngle) * bulletSpeed, sin(fireAngle) * bulletSpeed, bulletDmg));
            this.minigunCooldown = 4;
        }
    }

    hasLineOfSight(targetX, targetY, obstacles) {
        if (!obstacles) return true;
        for (let t of obstacles) {
            if (Ball.segmentIntersectsCircle(this.x, this.y, targetX, targetY, t.x, t.y, t.size)) {
                return false;
            }
        }
        return true;
    }

    static segmentIntersectsCircle(ax, ay, bx, by, cx, cy, r) {
        const dx = bx - ax;
        const dy = by - ay;
        const lenSq = dx * dx + dy * dy;
        let t = lenSq > 0 ? ((cx - ax) * dx + (cy - ay) * dy) / lenSq : 0;
        t = constrain(t, 0, 1);
        const closestX = ax + t * dx;
        const closestY = ay + t * dy;
        const ddx = closestX - cx;
        const ddy = closestY - cy;
        return (ddx * ddx + ddy * ddy) <= r * r;
    }

    update(width, height, gravity, drag, wind) {
        if (this.speedTimer > 0) {
            this.speedTimer--;
            if (this.speedTimer === 0) this.speedMultiplier = 1.0;
        }
        if (this.dmgTimer > 0) {
            this.dmgTimer--;
            if (this.dmgTimer === 0) this.dmgMultiplier = 1.0;
        }
        if (this.flamethrowerTimer > 0) {
            this.flamethrowerTimer--;
            if (this.flamethrowerTimer === 0) {
                this.hasFlamethrower = false;
                this.weaponSwingSpeed = 0.03;
            }
        }

        const currentMaxSpeed = GameConfig.MAX_SPEED * this.speedMultiplier * this.classSpeedMult;

        if (this.keyLeft && this.keyRight) {
            this.speedX = 0;
            this.holdTimeX = 0;
        } else if (this.keyLeft) {
            this.holdTimeX++;
            this.speedX -= (GameConfig.BASE_ACCEL + (this.holdTimeX * GameConfig.ACCEL_RAMP_RATE)) * this.speedMultiplier;
        } else if (this.keyRight) {
            this.holdTimeX++;
            this.speedX += (GameConfig.BASE_ACCEL + (this.holdTimeX * GameConfig.ACCEL_RAMP_RATE)) * this.speedMultiplier;
        } else {
            this.holdTimeX = 0;
            this.speedX += wind;
            this.speedX *= drag;
        }

        if (this.keyUp && this.keyDown) {
            this.speedY = 0;
            this.holdTimeY = 0;
        } else if (this.keyUp) {
            this.holdTimeY++;
            this.speedY -= (GameConfig.BASE_ACCEL + (this.holdTimeY * GameConfig.ACCEL_RAMP_RATE)) * this.speedMultiplier;
        } else if (this.keyDown) {
            this.holdTimeY++;
            this.speedY += (GameConfig.BASE_ACCEL + (this.holdTimeY * GameConfig.ACCEL_RAMP_RATE)) * this.speedMultiplier;
        } else {
            this.holdTimeY = 0;
            this.speedY += gravity;
            this.speedY *= drag;
        }

        this.speedX = constrain(this.speedX, -currentMaxSpeed, currentMaxSpeed);
        this.speedY = constrain(this.speedY, -currentMaxSpeed, currentMaxSpeed);

        this.x += this.speedX;
        this.y += this.speedY;

        this.ballRotation += this.speedX * 0.05;

        const radius = GameConfig.RADIUS;
        const restitution = GameConfig.RESTITUTION;
        if (this.y > height - radius) { this.y = height - radius; this.speedY *= -restitution; }
        if (this.y < radius) { this.y = radius; this.speedY *= -restitution; }
        if (this.x > width - radius) { this.x = width - radius; this.speedX *= -restitution; }
        if (this.x < radius) { this.x = radius; this.speedX *= -restitution; }
    }

    draw(paused, playerName) {
        const radius = GameConfig.RADIUS;

        push();
        translate(this.x, this.y, this.z);

        // Name Label & Health Bar
        if (playerName && playerName.length > 0) {
            push();
            translate(0, -radius - 22, 0);
            textAlign(CENTER, BOTTOM);
            textSize(12);
            fill(255);
            text(playerName, 0, -5);

            rectMode(CENTER);
            noStroke();
            fill(80);
            const barWidth = 50;
            rect(0, 0, barWidth, 6);
            fill(0, 230, 100);
            const currentWidth = map(this.health, 0, this.maxHealth, 0, barWidth);
            rect(-(barWidth - currentWidth) / 2, 0, currentWidth, 6);
            pop();
        }

        // Render Ball Body
        push();
        rotateZ(this.ballRotation);
        if (paused) fill(255, 200, 0);
        else fill(this.activeColor);
        stroke(0, 100);
        strokeWeight(1);
        sphere(radius);

        this.drawCharacterSkin();
        this.drawAccessory();

        pop();

        // Render Weapon
        push();
        rotateZ(this.ballRotation + this.weaponSwingAngle);

        if (this.hasFlamethrower) {
            stroke(100);
            strokeWeight(8);
            line(radius, 0, 0, radius + 20, 0, 0);

            noStroke();
            fill(255, random(100, 200), 0, 200);
            ellipse(radius + 35 + random(-5, 5), random(-4, 4), 25, 15);
            fill(255, 50, 0, 180);
            ellipse(radius + 50 + random(-5, 5), random(-8, 8), 35, 22);
        } else {
            this.drawCustomWeapon();
        }

        pop();
        pop();
    }

    drawCharacterSkin() {
        const radius = GameConfig.RADIUS;

        push();
        translate(0, 0, radius - 1);

        if (this.characterIndex === 0) { // Dwarf
            noStroke();
            fill(120, 80, 40);
            beginShape();
            vertex(-10, 4);
            vertex(10, 4);
            vertex(6, 20);
            vertex(0, 26);
            vertex(-6, 20);
            endShape(CLOSE);

        } else if (this.characterIndex === 1) { // Mage
            stroke(210, 170, 255);
            strokeWeight(2);
            line(-10, 0, 10, 0);
            line(0, -10, 0, 10);
            line(-7, -7, 7, 7);
            line(-7, 7, 7, -7);
            noStroke();
            fill(230, 200, 255);
            ellipse(0, 0, 6, 6);

        } else if (this.characterIndex === 2) { // Giant
            noStroke();
            fill(90, 70, 55);
            ellipse(-8, -6, 14, 10);
            ellipse(9, -2, 12, 12);
            ellipse(-2, 10, 16, 10);

        } else if (this.characterIndex === 3) { // Rogue
            noStroke();
            fill(20, 20, 25, 210);
            rectMode(CENTER);
            rect(0, -8, 34, 18, 12, 12, 4, 4);
            fill(230, 230, 255);
            rect(-6, -8, 6, 2);
            rect(6, -8, 6, 2);

        } else if (this.characterIndex === 4) { // Paladin
            noStroke();
            fill(255, 215, 0);
            rectMode(CENTER);
            rect(0, 0, 6, 22);
            rect(0, 0, 22, 6);

        } else if (this.characterIndex === 5) { // Berserker
            stroke(200, 20, 20);
            strokeWeight(4);
            line(-12, -12, 12, 12);
            line(-4, -14, 14, 4);

        } else if (this.characterIndex === 6) { // Knight
            noFill();
            stroke(210);
            strokeWeight(2);
            line(-14, -6, 14, -6);
            line(-14, 4, 14, 4);
            line(-14, 14, 14, 14);
            noStroke();
            fill(160);
            ellipse(-14, -6, 3, 3);
            ellipse(14, 4, 3, 3);

        } else if (this.characterIndex === 7) { // Archer
            stroke(60, 110, 60);
            strokeWeight(5);
            line(-14, -14, 14, 14);
            noStroke();
            fill(90, 140, 90);
            triangle(10, 10, 18, 10, 14, 18);
        }

        pop();
    }

    drawAccessory() {
        const radius = GameConfig.RADIUS;

        push();
        rectMode(CENTER);

        if (this.accessoryType === 0) { // Crown
            translate(0, -radius - 8, 5);
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
        } else if (this.accessoryType === 1) { // Top Hat
            translate(0, -radius - 12, 5);
            fill(30);
            stroke(10);
            rect(0, 8, 38, 5, 2);
            rect(0, -4, 22, 20, 2);
            fill(200, 30, 30);
            rect(0, 4, 22, 4);
        } else if (this.accessoryType === 2) { // Cowboy Hat
            translate(0, -radius - 8, 5);
            fill(133, 94, 66);
            stroke(80, 50, 30);
            ellipse(0, 6, 48, 10);
            rect(0, -2, 22, 12, 4);
        } else if (this.accessoryType === 3) { // Glasses
            translate(0, -6, radius + 2);
            fill(20, 20, 20, 220);
            stroke(255);
            strokeWeight(1.5);
            rect(-10, 0, 14, 10, 2);
            rect(10, 0, 14, 10, 2);
            line(-3, 0, 3, 0);
        } else if (this.accessoryType === 4) { // Horns
            translate(0, -radius + 2, 5);
            fill(220, 220, 200);
            stroke(150);
            triangle(-18, 5, -12, 5, -20, -18);
            triangle(18, 5, 12, 5, 20, -18);
        }
        pop();
    }

    drawCustomWeapon() {
        const radius = GameConfig.RADIUS;

        if (this.weaponType === 0) { // Battleaxe
            stroke(100, 60, 25);
            strokeWeight(6);
            line(radius, 0, 0, radius + this.weaponLength, 0, 0);

            push();
            translate(radius + this.weaponLength - 8, 0, 0);
            if (this.dmgTimer > 0) fill(255, 80, 80);
            else fill(170, 175, 180);
            stroke(220);
            strokeWeight(1.5);
            beginShape();
            vertex(0, -5);
            bezierVertex(12, -22, 18, -18, 16, 0);
            bezierVertex(18, 18, 12, 22, 0, 5);
            vertex(-3, 0);
            endShape(CLOSE);
            pop();

        } else if (this.weaponType === 1) { // Broadsword
            stroke(120, 125, 130);
            strokeWeight(6);
            line(radius + 12, -10, 0, radius + 12, 10, 0);

            push();
            translate(radius + 12, 0, 0);
            if (this.dmgTimer > 0) fill(255, 80, 80);
            else fill(200, 205, 215);
            stroke(140);
            strokeWeight(1);
            beginShape();
            vertex(0, -3.5);
            vertex(this.weaponLength - 15, -3);
            vertex(this.weaponLength - 5, 0);
            vertex(this.weaponLength - 15, 3);
            vertex(0, 3.5);
            endShape(CLOSE);
            pop();

        } else if (this.weaponType === 2) { // Spear
            stroke(110, 70, 30);
            strokeWeight(4);
            line(radius, 0, 0, radius + this.weaponLength + 10, 0, 0);

            push();
            translate(radius + this.weaponLength + 10, 0, 0);
            if (this.dmgTimer > 0) fill(255, 80, 80);
            else fill(220);
            stroke(160);
            strokeWeight(1);
            triangle(0, -5, 15, 0, 0, 5);
            pop();

        } else if (this.weaponType === 3) { // Katana
            stroke(40);
            strokeWeight(4);
            line(radius, 0, 0, radius + 10, 0, 0);

            push();
            translate(radius + 10, 0, 0);
            if (this.dmgTimer > 0) fill(255, 80, 80);
            else fill(230, 235, 240);
            stroke(180);
            strokeWeight(1);
            beginShape();
            vertex(0, -2);
            vertex(this.weaponLength, -4);
            vertex(this.weaponLength + 5, 0);
            vertex(this.weaponLength, 1);
            vertex(0, 2);
            endShape(CLOSE);
            pop();

        } else if (this.weaponType === 4) { // Warhammer
            stroke(90, 50, 20);
            strokeWeight(6);
            line(radius, 0, 0, radius + this.weaponLength, 0, 0);

            push();
            translate(radius + this.weaponLength - 5, 0, 0);
            if (this.dmgTimer > 0) fill(255, 80, 80);
            else fill(130, 135, 140);
            stroke(80);
            strokeWeight(1.5);
            rectMode(CENTER);
            rect(0, 0, 18, 28, 3);
            pop();

        } else if (this.weaponType === 5) { // Minigun
            stroke(100, 100, 100);
            strokeWeight(5);
            line(radius, 0, 0, radius + 18, 0, 0);

            push();
            translate(radius + 18, 0, 0);
            fill(80);
            stroke(120);
            strokeWeight(1);
            box(8, 20, 8);

            push();
            rotateY(this.weaponSwingTimer * 0.5);
            fill(150);
            box(6, 15, 6);
            pop();
            pop();

        } else if (this.weaponType === GameConfig.WEAPON_GODKILLER) { // Godkiller
            noStroke();
            fill(255, 215, 0, 70);
            ellipse(radius + this.weaponLength * 0.55, 0, this.weaponLength * 1.3, 26);
            fill(255, 230, 120, 90);
            ellipse(radius + this.weaponLength * 0.55, 0, this.weaponLength * 0.9, 16);

            stroke(120, 90, 20);
            strokeWeight(5);
            line(radius, 0, 0, radius + 10, 0, 0);

            push();
            translate(radius + 10, 0, 0);
            fill(255, 255, 250);
            stroke(255, 215, 0);
            strokeWeight(2);
            beginShape();
            vertex(0, -3);
            vertex(this.weaponLength - 5, -5);
            vertex(this.weaponLength + 8, 0);
            vertex(this.weaponLength - 5, 5);
            vertex(0, 3);
            endShape(CLOSE);
            pop();

        } else if (this.weaponType === GameConfig.WEAPON_PLUTON) { // Pluton
            stroke(70, 70, 80);
            strokeWeight(8);
            line(radius, 0, 0, radius + 15, 0, 0);

            push();
            translate(radius + 15, 0, 0);
            if (this.dmgTimer > 0) fill(255, 80, 80);
            else fill(190, 195, 205);
            stroke(110);
            strokeWeight(2);
            beginShape();
            vertex(0, -10);
            vertex(this.weaponLength - 10, -14);
            vertex(this.weaponLength + 12, 0);
            vertex(this.weaponLength - 10, 14);
            vertex(0, 10);
            endShape(CLOSE);
            pop();

        } else if (this.weaponType === GameConfig.WEAPON_STICK) { // Stick
            stroke(120, 85, 45);
            strokeWeight(3);
            line(radius, 0, 0, radius + this.weaponLength, 0, 0);
        }
    }
}