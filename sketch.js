/* global GameState, GameConfig, MapBuilder, Ball, PowerUp, Bullet, Character */

let gameFont;

function preload() {
    // WebGL requires a loaded font file (.ttf / .otf) to draw text
    gameFont = loadFont('https://cdnjs.cloudflare.com/ajax/libs/ink/3.1.10/fonts/Roboto/Roboto-Regular.ttf');
}

// Game State Management
let state = GameState.SETUP;

// Player Customization State
let nameBall1 = "PLAYER 1";
let nameBall2 = "PLAYER 2";
let activeInput = 1; // 1 = P1, 2 = P2
let maxNameLength = 10;

// Customization selections [Player 1, Player 2]
let colorIndex = [0, 1];
let accessoryIndex = [0, 1];
let weaponIndex = [0, 1];
let characterIndex = [0, 1];

// Row layout (relative to customizer box y)
const ROW_CHARACTER_Y = 95;
const ROW_COLOR_Y = 150;
const ROW_ACCESSORY_Y = 205;
const ROW_WEAPON_Y = 260;
const PREVIEW_Y = 308;

let mapIndex = 0;

// Physics variables
let gravity = 0;
let drag = 0;
let wind = 0;

let isPaused = false;
let winnerText = "";

// Sudden Death & Timers
let suddenDeath = false;
let gameTimerFrames = 45 * 60; // 45 seconds at 60 FPS

// Layout Constants
const btnX = 300;
const btnY = 510;
const btnW = 200;
const btnH = 50;

const backX = 40;
const backY = 510;
const backW = 120;
const backH = 50;

const cardX0 = 30;
const cardY = 100;
const cardW = 140;
const cardH = 340;
const cardGap = 10;

// Spawning and Entity Lists
let spawnTimer = 0;
let spawnInterval = 300;
let powerUps = [];
let bullets = [];
let lavaZones = [];
let trees = [];

let ball1, ball2;

function setup() {
    createCanvas(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH, WEBGL);
    textFont(gameFont);
    state = GameState.SETUP;
    resetGame();
}

function resetGame() {
    ball1 = new Ball(150, 100, GameConfig.COLORS[colorIndex[0]], accessoryIndex[0], effectiveWeaponIndex(0), characterIndex[0]);
    ball2 = new Ball(650, 100, GameConfig.COLORS[colorIndex[1]], accessoryIndex[1], effectiveWeaponIndex(1), characterIndex[1]);

    powerUps = [];
    bullets = [];
    spawnTimer = 0;
    gameTimerFrames = 45 * 60;
    suddenDeath = false;
    winnerText = "";
    isPaused = false;

    generateMap();
}

function specialWeaponFor(p) {
    let n = (p === 0 ? nameBall1 : nameBall2).trim().toLowerCase();
    if (n === "aryaman" || n === "cardona" || n === "foster") return GameConfig.WEAPON_GODKILLER;
    if (n === "sushanth") return GameConfig.WEAPON_PLUTON;
    if (n === "sushil" || n === "sushi") return GameConfig.WEAPON_STICK;
    return -1;
}

function effectiveWeaponIndex(p) {
    let special = specialWeaponFor(p);
    return special >= 0 ? special : weaponIndex[p];
}

function generateMap() {
    lavaZones = [];
    trees = [];
    MapBuilder.buildMap(mapIndex, lavaZones, trees);

    gravity = GameConfig.MAP_GRAVITY[mapIndex];
    drag = GameConfig.MAP_DRAG[mapIndex];
    wind = GameConfig.MAP_WIND[mapIndex];
}

function draw() {
    // Re-center coordinates so (0,0) is at top-left like standard Processing
    translate(-width / 2, -height / 2, 0);

    if (state === GameState.SETUP) {
        background(40, 35, 30);
        noLights();
        drawNameInputScreen();
        return;
    }

    if (state === GameState.MAP_SELECT) {
        background(40, 35, 30);
        noLights();
        drawMapSelectScreen();
        return;
    }

    background(GameConfig.MAP_BG[mapIndex]);
    lights();

    if (!isPaused && state === GameState.PLAYING) {
        if (gameTimerFrames > 0) {
            gameTimerFrames--;
            if (gameTimerFrames <= 0) {
                suddenDeath = true;
            }
        }

        if (suddenDeath) {
            ball1.speedX = 0;
            ball1.speedY = 0;
            ball2.speedX = 0;
            ball2.speedY = 0;

            ball1.takeDamage(0.15);
            ball2.takeDamage(0.15);
        } else {
            spawnTimer++;
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0;
                spawnRandomPowerUp();
                spawnRandomPowerUp();

                while (powerUps.length > 2) {
                    powerUps.shift();
                }
            }

            ball1.update(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH, gravity, drag, wind);
            ball2.update(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH, gravity, drag, wind);

            for (let l of lavaZones) {
                l.checkDamage(ball1);
                l.checkDamage(ball2);
            }

            for (let t of trees) {
                t.resolveCollision(ball1);
                t.resolveCollision(ball2);
            }

            resolveBallCollision(ball1, ball2);

            for (let i = powerUps.length - 1; i >= 0; i--) {
                let p = powerUps[i];
                p.update();
                if (p.checkCollision(ball1) || p.checkCollision(ball2)) {
                    powerUps.splice(i, 1);
                }
            }
        }

        ball1.updateWeapon(bullets, ball2, trees);
        ball2.updateWeapon(bullets, ball1, trees);

        checkWeaponHits(ball1, ball2);
        checkWeaponHits(ball2, ball1);

        for (let i = bullets.length - 1; i >= 0; i--) {
            let b = bullets[i];
            b.update();
            if (b.isOffScreen(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH)) {
                bullets.splice(i, 1);
            } else {
                if (b.checkHit(ball1)) {
                    ball1.takeDamage(b.damage);
                    bullets.splice(i, 1);
                } else if (b.checkHit(ball2)) {
                    ball2.takeDamage(b.damage);
                    bullets.splice(i, 1);
                }
            }
        }

        if (ball1.health <= 0 && ball2.health <= 0) {
            state = GameState.GAME_OVER;
            winnerText = "DRAW!";
        } else if (ball1.health <= 0) {
            state = GameState.GAME_OVER;
            winnerText = nameBall2.toUpperCase() + " WINS!";
        } else if (ball2.health <= 0) {
            state = GameState.GAME_OVER;
            winnerText = nameBall1.toUpperCase() + " WINS!";
        }
    }

    for (let l of lavaZones) l.draw(GameConfig.HAZARD_FILL[mapIndex], GameConfig.HAZARD_LINE[mapIndex]);
    for (let t of trees) t.draw(GameConfig.OBSTACLE_COL[mapIndex]);
    for (let p of powerUps) p.draw();
    for (let b of bullets) b.draw();

    ball1.draw(isPaused, nameBall1);
    ball2.draw(isPaused, nameBall2);

    noLights();
    drawTimerHUD();
    drawStatsHUD();

    if (state === GameState.GAME_OVER) {
        drawGameOverScreen();
    }
}

// -------------------------------------------------------------------------
// UI SCREENS & RENDERING
// -------------------------------------------------------------------------

function drawNameInputScreen() {
    textAlign(CENTER, CENTER);
    fill(255);
    textSize(32);
    text("CHARACTER CUSTOMIZATION", GameConfig.SCREEN_WIDTH / 2, 40);

    drawPlayerCustomizer(0, 50, 90, nameBall1);
    drawPlayerCustomizer(1, 450, 90, nameBall2);

    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
        fill(80, 220, 120);
    } else {
        fill(50, 180, 90);
    }
    stroke(255);
    strokeWeight(2);
    rectMode(CORNER);
    rect(btnX, btnY, btnW, btnH, 10);

    fill(255);
    noStroke();
    textSize(22);
    text("SELECT MAP >", GameConfig.SCREEN_WIDTH / 2, btnY + btnH / 2);
}

function drawMapSelectScreen() {
    textAlign(CENTER, CENTER);
    fill(255);
    textSize(32);
    text("SELECT MAP", GameConfig.SCREEN_WIDTH / 2, 40);
    textSize(13);
    fill(180);
    text("Click a map or use LEFT / RIGHT arrows. ENTER to start, BACKSPACE to go back.", GameConfig.SCREEN_WIDTH / 2, 72);

    let totalPages = Math.floor((GameConfig.MAP_NAMES.length + 4) / 5);
    let page = Math.floor(mapIndex / 5);
    let pageStart = page * 5;
    let pageEnd = Math.min(pageStart + 5, GameConfig.MAP_NAMES.length);

    for (let i = pageStart; i < pageEnd; i++) {
        let slot = i - pageStart;
        let cx = cardX0 + slot * (cardW + cardGap);
        let selected = (i === mapIndex);
        let hover = mouseX >= cx && mouseX <= cx + cardW && mouseY >= cardY && mouseY <= cardY + cardH;

        rectMode(CORNER);
        fill(30, 30, 35, hover ? 255 : 220);
        stroke(selected ? color(255, 220, 60) : (hover ? 200 : 100));
        strokeWeight(selected ? 4 : 1);
        rect(cx, cardY, cardW, cardH, 10);

        drawMapPreview(i, cx + 10, cardY + 15, 120, 90);

        textAlign(CENTER, CENTER);
        fill(selected ? color(255, 220, 60) : 255);
        noStroke();
        textSize(15);
        text(GameConfig.MAP_NAMES[i], cx + cardW / 2, cardY + 135);

        fill(200);
        textSize(12);
        textAlign(CENTER, TOP);
        text(GameConfig.MAP_DESCS[i], cx + cardW / 2, cardY + 160);

        textAlign(LEFT, TOP);
        fill(160);
        textSize(11);
        let grav = GameConfig.MAP_GRAVITY[i] < 0.15 ? "Low" : (GameConfig.MAP_GRAVITY[i] > 0.32 ? "Heavy" : "Normal");
        let fric = GameConfig.MAP_DRAG[i] > 0.995 ? "Icy" : (GameConfig.MAP_DRAG[i] < 0.95 ? "Thick" : "Normal");
        let w = GameConfig.MAP_WIND[i];
        let windStr = Math.abs(w) < 0.02 ? "None" : (Math.abs(w) < 0.06 ? "Light" : "Strong");

        text("Gravity: " + grav, cx + 12, cardY + 232);
        text("Friction: " + fric, cx + 12, cardY + 250);
        text("Wind: " + windStr, cx + 12, cardY + 268);
        text("Hazards: " + countHazards(i), cx + 12, cardY + 286);
        text("Obstacles: " + countObstacles(i), cx + 12, cardY + 304);
    }

    let dotsY = cardY + cardH + 24;
    let dotSpacing = 26;
    let dotsStartX = GameConfig.SCREEN_WIDTH / 2 - (totalPages - 1) * dotSpacing / 2;
    noStroke();
    for (let pg = 0; pg < totalPages; pg++) {
        let dx = dotsStartX + pg * dotSpacing;
        let isCurrentPage = (pg === page);
        let dotHover = dist(mouseX, mouseY, dx, dotsY) < 10;
        fill(isCurrentPage ? color(255, 220, 60) : (dotHover ? color(200) : color(110)));
        ellipse(dx, dotsY, isCurrentPage ? 14 : 10, isCurrentPage ? 14 : 10);
    }
    fill(255);
    textAlign(CENTER, CENTER);
    textSize(12);
    text("Page " + (page + 1) + " / " + totalPages, GameConfig.SCREEN_WIDTH / 2, dotsY + 20);

    let overBack = mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH;
    fill(overBack ? color(120, 120, 130) : color(80, 80, 90));
    stroke(255);
    strokeWeight(2);
    rectMode(CORNER);
    rect(backX, backY, backW, backH, 10);
    fill(255);
    noStroke();
    textAlign(CENTER, CENTER);
    textSize(20);
    text("< BACK", backX + backW / 2, backY + backH / 2);

    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
        fill(80, 220, 120);
    } else {
        fill(50, 180, 90);
    }
    stroke(255);
    strokeWeight(2);
    rect(btnX, btnY, btnW, btnH, 10);
    fill(255);
    noStroke();
    textSize(22);
    text("START MATCH", GameConfig.SCREEN_WIDTH / 2, btnY + btnH / 2);
}

function drawPlayerCustomizer(pIndex, x, y, name) {
    let isActive = (activeInput === pIndex + 1);

    fill(30, 30, 35, 220);
    stroke(isActive ? GameConfig.COLORS[colorIndex[pIndex]] : 100);
    strokeWeight(isActive ? 3 : 1);
    rectMode(CORNER);
    rect(x, y, 300, 390, 10);

    textAlign(CENTER, CENTER);
    fill(255);
    noStroke();
    textSize(18);
    text("PLAYER " + (pIndex + 1), x + 150, y + 25);

    stroke(isActive ? GameConfig.COLORS[colorIndex[pIndex]] : 80);
    strokeWeight(1);
    fill(50);
    rect(x + 20, y + 45, 260, 35, 5);
    fill(255);
    noStroke();
    textSize(16);
    text(name + (isActive && frameCount % 60 < 30 ? "|" : ""), x + 150, y + 62);

    let c = GameConfig.CHARACTERS[characterIndex[pIndex]];
    drawOptionSelector("Character", c.name, x + 20, y + ROW_CHARACTER_Y);
    drawOptionSelector("Color", GameConfig.COLOR_NAMES[colorIndex[pIndex]], x + 20, y + ROW_COLOR_Y);
    drawOptionSelector("Accessory", GameConfig.ACCESSORY_NAMES[accessoryIndex[pIndex]], x + 20, y + ROW_ACCESSORY_Y);

    let special = specialWeaponFor(pIndex);
    if (special >= 0) {
        drawLockedSelector("Weapon", GameConfig.WEAPON_NAMES[special], x + 20, y + ROW_WEAPON_Y);
    } else {
        drawOptionSelector("Weapon", GameConfig.WEAPON_NAMES[weaponIndex[pIndex]], x + 20, y + ROW_WEAPON_Y);
    }

    fill(20);
    stroke(80);
    rect(x + 20, y + PREVIEW_Y, 260, 72, 5);

    push();
    translate(x + 50, y + PREVIEW_Y + 36, 10);
    scale(0.42);
    lights();
    let tempBall = new Ball(0, 0, GameConfig.COLORS[colorIndex[pIndex]], accessoryIndex[pIndex], effectiveWeaponIndex(pIndex), characterIndex[pIndex]);
    tempBall.draw(false, "");
    pop();

    noLights();
    textAlign(LEFT, TOP);
    fill(180);
    noStroke();
    textSize(10);
    text("HP: " + Math.floor(c.health), x + 105, y + PREVIEW_Y + 6);
    text("SPD: " + c.speedMult.toFixed(2) + "x", x + 105, y + PREVIEW_Y + 20);
    text("DMG: " + c.damageMult.toFixed(2) + "x", x + 105, y + PREVIEW_Y + 34);
    text(c.description.replace("\n", " "), x + 105, y + PREVIEW_Y + 48, 170, 22);
}

function drawOptionSelector(label, value, x, y) {
    textAlign(LEFT, CENTER);
    fill(200);
    noStroke();
    textSize(13);
    text(label, x, y);

    fill(50);
    stroke(100);
    rect(x, y + 12, 260, 30, 5);

    fill(180);
    noStroke();
    triangle(x + 10, y + 27, x + 20, y + 18, x + 20, y + 36);
    triangle(x + 250, y + 27, x + 240, y + 18, x + 240, y + 36);

    textAlign(CENTER, CENTER);
    fill(255);
    textSize(14);
    text(value, x + 130, y + 26);
}

function drawLockedSelector(label, value, x, y) {
    textAlign(LEFT, CENTER);
    fill(200);
    noStroke();
    textSize(13);
    text(label, x, y);

    fill(40);
    stroke(255, 215, 0, 150);
    strokeWeight(1.5);
    rect(x, y + 12, 260, 30, 5);

    textAlign(CENTER, CENTER);
    fill(255, 215, 0);
    noStroke();
    textSize(14);
    text(value, x + 130, y + 26);
}

function drawMapPreview(idx, px, py, pw, ph) {
    let sx = pw / GameConfig.SCREEN_WIDTH;
    let sy = ph / GameConfig.SCREEN_LENGTH;

    let lz = [];
    let tr = [];
    MapBuilder.buildMap(idx, lz, tr);

    rectMode(CORNER);
    fill(GameConfig.MAP_BG[idx]);
    stroke(150);
    strokeWeight(1);
    rect(px, py, pw, ph);

    rectMode(CENTER);
    fill(GameConfig.HAZARD_FILL[idx]);
    stroke(GameConfig.HAZARD_LINE[idx]);
    strokeWeight(1);
    for (let l of lz) {
        rect(px + l.x * sx, py + l.y * sy, l.w * sx, l.h * sy, 3);
    }

    fill(GameConfig.OBSTACLE_COL[idx]);
    noStroke();
    for (let t of tr) {
        ellipse(px + t.x * sx, py + t.y * sy, t.size * 2 * sx, t.size * 2 * sy);
    }

    fill(GameConfig.COLORS[colorIndex[0]]);
    ellipse(px + 150 * sx, py + 100 * sy, 8, 8);
    fill(GameConfig.COLORS[colorIndex[1]]);
    ellipse(px + 650 * sx, py + 100 * sy, 8, 8);

    rectMode(CORNER);
}

function countHazards(idx) {
    let lz = [], tr = [];
    MapBuilder.buildMap(idx, lz, tr);
    return lz.length;
}

function countObstacles(idx) {
    let lz = [], tr = [];
    MapBuilder.buildMap(idx, lz, tr);
    return tr.length;
}

function drawTimerHUD() {
    textAlign(CENTER, TOP);
    textSize(28);
    noStroke();

    if (suddenDeath) {
        fill(255, 50, 50);
        text("SUDDEN DEATH! OVERTIME", GameConfig.SCREEN_WIDTH / 2, 15);
    } else {
        fill(255);
        let secondsLeft = Math.ceil(gameTimerFrames / 60.0);
        text("TIME LEFT: " + secondsLeft + "s", GameConfig.SCREEN_WIDTH / 2, 15);
    }
}

function drawStatsHUD() {
    rectMode(CORNER);
    drawPlayerStatsBox(15, 15, nameBall1, ball1, GameConfig.COLORS[colorIndex[0]]);
    drawPlayerStatsBox(GameConfig.SCREEN_WIDTH - 175, 15, nameBall2, ball2, GameConfig.COLORS[colorIndex[1]]);
}

function drawPlayerStatsBox(x, y, name, b, playerColor) {
    fill(20, 20, 25, 200);
    stroke(playerColor);
    strokeWeight(2);
    rect(x, y, 160, 95, 8);

    textAlign(LEFT, TOP);
    textSize(14);
    fill(playerColor);
    noStroke();
    text(name, x + 10, y + 8);

    textSize(12);
    fill(220);
    text("HP: " + Math.ceil(b.health) + " / " + Math.floor(b.maxHealth), x + 10, y + 28);

    let baseDmg = b.hasFlamethrower ? 1.5 : 8.0;
    let currentDmg = baseDmg * b.dmgMultiplier * b.classDamageMult * b.weaponDamageMult;
    fill(b.dmgTimer > 0 ? color(255, 80, 80) : color(220));
    text("DMG: " + currentDmg.toFixed(1) + (b.dmgMultiplier > 1.0 ? " (x2)" : ""), x + 10, y + 48);

    let currentSpeed = GameConfig.MAX_SPEED * b.speedMultiplier * b.classSpeedMult;
    fill(b.speedTimer > 0 ? color(255, 220, 0) : color(220));
    text("SPD: " + currentSpeed.toFixed(1) + (b.speedMultiplier > 1.0 ? " (x1.5)" : ""), x + 10, y + 68);
}

function checkWeaponHits(attacker, defender) {
    let radius = GameConfig.RADIUS;
    let totalAngle = attacker.ballRotation + attacker.weaponSwingAngle;
    let tipX = attacker.x + cos(totalAngle) * (radius + attacker.weaponLength);
    let tipY = attacker.y + sin(totalAngle) * (radius + attacker.weaponLength);

    if (dist(tipX, tipY, defender.x, defender.y) < radius) {
        let baseDmg = attacker.hasFlamethrower ? 1.5 : 8.0;
        let finalDmg = baseDmg * attacker.dmgMultiplier * attacker.classDamageMult * attacker.weaponDamageMult;
        defender.takeDamage(finalDmg);

        let pushAngle = atan2(defender.y - attacker.y, defender.x - attacker.x);
        let pushForce = attacker.hasFlamethrower ? 3.0 : 12.0;

        defender.speedX += cos(pushAngle) * pushForce;
        defender.speedY += sin(pushAngle) * pushForce;
    }
}

function spawnRandomPowerUp() {
    let spawnX = random(50, GameConfig.SCREEN_WIDTH - 50);
    let spawnY = random(50, GameConfig.SCREEN_LENGTH - 100);
    let type = Math.floor(random(4));
    powerUps.push(new PowerUp(spawnX, spawnY, type));
}

function drawGameOverScreen() {
    rectMode(CORNER);
    fill(0, 0, 0, 180);
    rect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH);

    textAlign(CENTER, CENTER);
    textSize(44);
    fill(255);
    noStroke();
    text(winnerText, GameConfig.SCREEN_WIDTH / 2, 220);

    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
        fill(80, 220, 120);
    } else {
        fill(50, 180, 90);
    }
    stroke(255);
    strokeWeight(2);
    rect(btnX, btnY, btnW, btnH, 10);

    fill(255);
    noStroke();
    textSize(24);
    text("PLAY AGAIN", GameConfig.SCREEN_WIDTH / 2, btnY + btnH / 2);
}

function resolveBallCollision(b1, b2) {
    let radius = GameConfig.RADIUS;
    let restitution = GameConfig.RESTITUTION;

    let dx = b2.x - b1.x;
    let dy = b2.y - b1.y;
    let distance = sqrt(dx * dx + dy * dy);
    let minDist = radius * 2;

    if (distance < minDist && distance > 0) {
        let nx = dx / distance;
        let ny = dy / distance;

        let overlap = 0.5 * (minDist - distance);
        b1.x -= nx * overlap;
        b1.y -= ny * overlap;
        b2.x += nx * overlap;
        b2.y += ny * overlap;

        let kx = b1.speedX - b2.speedX;
        let ky = b1.speedY - b2.speedY;
        let p = 2 * (nx * kx + ny * ky) / 2;

        b1.speedX -= p * nx * restitution;
        b1.speedY -= p * ny * restitution;
        b2.speedX += p * nx * restitution;
        b2.speedY += p * ny * restitution;
    }
}

function startMatch() {
    if (nameBall1.trim() === "") nameBall1 = "P1";
    if (nameBall2.trim() === "") nameBall2 = "P2";
    resetGame();
    state = GameState.PLAYING;
}

// -------------------------------------------------------------------------
// INPUT LISTENERS
// -------------------------------------------------------------------------

function mousePressed() {
    if (state === GameState.SETUP) {
        if (mouseX >= 50 && mouseX <= 350 && mouseY >= 90 && mouseY <= 480) activeInput = 1;
        else if (mouseX >= 450 && mouseX <= 750 && mouseY >= 90 && mouseY <= 480) activeInput = 2;

        handleCustomizationClick(0, 50, 90);
        handleCustomizationClick(1, 450, 90);

        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            if (nameBall1.trim() === "") nameBall1 = "P1";
            if (nameBall2.trim() === "") nameBall2 = "P2";
            state = GameState.MAP_SELECT;
        }
    } else if (state === GameState.MAP_SELECT) {
        let totalPages = Math.floor((GameConfig.MAP_NAMES.length + 4) / 5);
        let page = Math.floor(mapIndex / 5);
        let pageStart = page * 5;
        let pageEnd = Math.min(pageStart + 5, GameConfig.MAP_NAMES.length);

        for (let i = pageStart; i < pageEnd; i++) {
            let slot = i - pageStart;
            let cx = cardX0 + slot * (cardW + cardGap);
            if (mouseX >= cx && mouseX <= cx + cardW && mouseY >= cardY && mouseY <= cardY + cardH) {
                mapIndex = i;
            }
        }

        let dotsY = cardY + cardH + 24;
        let dotSpacing = 26;
        let dotsStartX = GameConfig.SCREEN_WIDTH / 2 - (totalPages - 1) * dotSpacing / 2;
        for (let pg = 0; pg < totalPages; pg++) {
            let dx = dotsStartX + pg * dotSpacing;
            if (dist(mouseX, mouseY, dx, dotsY) < 10) {
                mapIndex = pg * 5;
            }
        }

        if (mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH) {
            state = GameState.SETUP;
        }

        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            startMatch();
        }
    } else if (state === GameState.GAME_OVER) {
        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            state = GameState.SETUP;
            resetGame();
        }
    }
}

function handleCustomizationClick(p, x, y) {
    if (mouseY >= y + ROW_CHARACTER_Y + 12 && mouseY <= y + ROW_CHARACTER_Y + 42) {
        if (mouseX >= x + 20 && mouseX <= x + 40) characterIndex[p] = (characterIndex[p] - 1 + GameConfig.CHARACTERS.length) % GameConfig.CHARACTERS.length;
        if (mouseX >= x + 240 && mouseX <= x + 260) characterIndex[p] = (characterIndex[p] + 1) % GameConfig.CHARACTERS.length;
    }
    if (mouseY >= y + ROW_COLOR_Y + 12 && mouseY <= y + ROW_COLOR_Y + 42) {
        if (mouseX >= x + 20 && mouseX <= x + 40) colorIndex[p] = (colorIndex[p] - 1 + GameConfig.COLORS.length) % GameConfig.COLORS.length;
        if (mouseX >= x + 240 && mouseX <= x + 260) colorIndex[p] = (colorIndex[p] + 1) % GameConfig.COLORS.length;
    }
    if (mouseY >= y + ROW_ACCESSORY_Y + 12 && mouseY <= y + ROW_ACCESSORY_Y + 42) {
        if (mouseX >= x + 20 && mouseX <= x + 40) accessoryIndex[p] = (accessoryIndex[p] - 1 + GameConfig.ACCESSORY_NAMES.length) % GameConfig.ACCESSORY_NAMES.length;
        if (mouseX >= x + 240 && mouseX <= x + 260) accessoryIndex[p] = (accessoryIndex[p] + 1) % GameConfig.ACCESSORY_NAMES.length;
    }
    if (specialWeaponFor(p) < 0 && mouseY >= y + ROW_WEAPON_Y + 12 && mouseY <= y + ROW_WEAPON_Y + 42) {
        if (mouseX >= x + 20 && mouseX <= x + 40) weaponIndex[p] = (weaponIndex[p] - 1 + GameConfig.PUBLIC_WEAPON_COUNT) % GameConfig.PUBLIC_WEAPON_COUNT;
        if (mouseX >= x + 240 && mouseX <= x + 260) weaponIndex[p] = (weaponIndex[p] + 1) % GameConfig.PUBLIC_WEAPON_COUNT;
    }
}

function keyPressed() {
    if (state === GameState.SETUP) {
        if (keyCode === TAB) {
            activeInput = (activeInput === 1) ? 2 : 1;
            return false;
        }

        if (keyCode === ENTER || keyCode === RETURN) {
            if (nameBall1.trim() === "") nameBall1 = "P1";
            if (nameBall2.trim() === "") nameBall2 = "P2";
            state = GameState.MAP_SELECT;
            return;
        }

        let currentName = (activeInput === 1) ? nameBall1 : nameBall2;

        if (keyCode === BACKSPACE) {
            if (currentName.length > 0) {
                currentName = currentName.substring(0, currentName.length - 1);
            }
        } else if (key.length === 1 && key >= ' ' && key <= '~') {
            if (currentName.length < maxNameLength) {
                currentName += key;
            }
        }

        if (activeInput === 1) nameBall1 = currentName;
        else nameBall2 = currentName;

    } else if (state === GameState.MAP_SELECT) {
        if (keyCode === LEFT_ARROW) {
            mapIndex = (mapIndex - 1 + GameConfig.MAP_NAMES.length) % GameConfig.MAP_NAMES.length;
        } else if (keyCode === RIGHT_ARROW) {
            mapIndex = (mapIndex + 1) % GameConfig.MAP_NAMES.length;
        } else if (keyCode === ENTER || keyCode === RETURN) {
            startMatch();
        } else if (keyCode === BACKSPACE) {
            state = GameState.SETUP;
        }

    } else if (state === GameState.PLAYING) {
        if (key === ' ') isPaused = !isPaused;
        setKeyState(keyCode, key, true);
    }
}

function keyReleased() {
    if (state === GameState.PLAYING) {
        setKeyState(keyCode, key, false);
    }
}

function setKeyState(code, k, pressed) {
    if (k === 'w' || k === 'W') ball1.keyUp = pressed;
    if (k === 's' || k === 'S') ball1.keyDown = pressed;
    if (k === 'a' || k === 'A') ball1.keyLeft = pressed;
    if (k === 'd' || k === 'D') ball1.keyRight = pressed;

    if (code === UP_ARROW) ball2.keyUp = pressed;
    if (code === DOWN_ARROW) ball2.keyDown = pressed;
    if (code === LEFT_ARROW) ball2.keyLeft = pressed;
    if (code === RIGHT_ARROW) ball2.keyRight = pressed;
}