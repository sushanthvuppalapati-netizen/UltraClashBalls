/* global setup, draw, mousePressed, keyPressed, keyReleased, createCanvas, WEBGL, width, height, random, translate, background, lights, constrain, sqrt, dist, atan2, cos, sin, push, pop, rectMode, CORNER, fill, stroke, strokeWeight, noStroke, rect, textAlign, CENTER, TOP, BOTTOM, textSize, text, mouseX, mouseY, UP_ARROW, DOWN_ARROW, LEFT_ARROW, RIGHT_ARROW, TAB, ENTER, RETURN, BACKSPACE, key, keyCode, Ball, LavaZone, PowerUp, MapBuilder, checkWeaponHits */

// Global Game State Enum
const GameState = {
    SETUP: 'SETUP',
    MAP_SELECT: 'MAP_SELECT',
    PLAYING: 'PLAYING',
    GAME_OVER: 'GAME_OVER'
};

// --- GameConfig Fallbacks ---
const GameConfig = {
    SCREEN_WIDTH: 800,
    SCREEN_LENGTH: 600,
    RADIUS: 25,
    RESTITUTION: 0.8,
    PUBLIC_WEAPON_COUNT: 4,
    WEAPON_GODKILLER: 99,
    WEAPON_PLUTON: 98,
    WEAPON_STICK: 97,
    COLORS: ['#ff0000', '#0000ff', '#00ff00', '#ffff00'],
    CHARACTERS: ['CharA', 'CharB', 'CharC'],
    ACCESSORY_NAMES: ['None', 'Hat', 'Glasses'],
    MAP_NAMES: ['Lava Pit', 'Forest Arena', 'Windy Plains', 'Classic Grid', 'Void'],
    MAP_BG: ['#221111', '#112211', '#112222', '#222222', '#000000'],
    MAP_GRAVITY: [0.3, 0.25, 0.3, 0.4, 0.0],
    MAP_DRAG: [0.99, 0.98, 0.99, 0.95, 1.0],
    MAP_WIND: [0.0, 0.0, 0.15, -0.05, 0.0],
    HAZARD_FILL: ['#ff4400', '#ff2200', '#cc3300', '#ff0055', '#ff3300'],
    HAZARD_LINE: ['#ffffff', '#ffcc00', '#ffaa00', '#ffffff', '#ff0000'],
    OBSTACLE_COL: ['#553311', '#225522', '#444444', '#666666', '#333333']
};

// --- State Variables ---
let state = GameState.SETUP;
let nameBall1 = "PLAYER 1";
let nameBall2 = "PLAYER 2";
let activeInput = 1;
let maxNameLength = 10;

// FIXED: Initialized starting indices for [Player 1, Player 2]
let colorIndex = [0, 1];
let accessoryIndex = [0, 0];
let weaponIndex = [0, 0];
let characterIndex = [0, 1];

const ROW_CHARACTER_Y = 95;
const ROW_COLOR_Y = 150;
const ROW_ACCESSORY_Y = 205;
const ROW_WEAPON_Y = 260;

let mapIndex = 0;
let gravity = 0;
let drag = 0;
let wind = 0;

let isPaused = false;
let winnerText = "";
let suddenDeath = false;
let gameTimerFrames = 45 * 60;
let spawnTimer = 0;

// Buttons
let btnX = 300;
let btnY = 510;
let btnW = 200;
let btnH = 50;

// Back button
let backX = 40;
let backY = 510;
let backW = 120;
let backH = 50;

// Map cards layout
let cardX0 = 30;
let cardY = 100;
let cardW = 140;
let cardH = 340;
let cardGap = 10;

// Dynamic Entity Arrays
let powerUps = [];
let bullets = [];
let lavaZones = [];
let trees = [];

let ball1, ball2;

// --- Setup & Lifecycle ---
function setup() {
    createCanvas(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH, WEBGL);
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

function startMatch() {
    state = GameState.PLAYING;
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

    if (typeof MapBuilder !== 'undefined') {
        MapBuilder.buildMap(mapIndex, lavaZones, trees);
    } else {
        if (mapIndex === 0) {
            lavaZones.push(new LavaZone(200, 450, 400, 50));
        }
    }

    gravity = GameConfig.MAP_GRAVITY[mapIndex];
    drag = GameConfig.MAP_DRAG[mapIndex];
    wind = GameConfig.MAP_WIND[mapIndex];
}

function spawnRandomPowerUp() {
    powerUps.push(new PowerUp(random(100, width - 100), random(100, height - 200)));
}

// Placeholder for ball physics collisions
function resolveBallCollision(b1, b2) {
    if (typeof b1.resolveCollision === 'function') {
        b1.resolveCollision(b2);
    }
}

// --- Draw Loop ---
function draw() {
    translate(-width / 2, -height / 2, 0);

    if (state === GameState.SETUP) {
        background(40, 35, 30);
        drawNameInputScreen();
        return;
    }

    if (state === GameState.MAP_SELECT) {
        background(40, 35, 30);
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
            if (spawnTimer >= 300) {
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

        if (typeof checkWeaponHits !== 'undefined') {
            checkWeaponHits(ball1, ball2);
            checkWeaponHits(ball2, ball1);
        }

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

    drawTimerHUD();
    drawStatsHUD();

    if (state === GameState.GAME_OVER) {
        drawGameOverScreen();
    }
}

// --- Screen Rendering Methods ---
function drawGameOverScreen() {
    push();
    rectMode(CORNER);
    fill(0, 0, 0, 180);
    rect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH);

    textAlign(CENTER, CENTER);
    textSize(44);
    fill(255);
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
    textSize(24);
    text("PLAY AGAIN", GameConfig.SCREEN_WIDTH / 2, btnY + btnH / 2);
    pop();
}

function drawNameInputScreen() {
    push();
    textAlign(CENTER, CENTER);
    fill(255);
    textSize(32);
    text("CHARACTER SETUP", width / 2, 40);

    strokeWeight(activeInput === 1 ? 3 : 1);
    stroke(activeInput === 1 ? '#50dc78' : 100);
    fill(activeInput === 1 ? 50 : 30);
    rect(50, 90, 300, 390, 8);

    strokeWeight(activeInput === 2 ? 3 : 1);
    stroke(activeInput === 2 ? '#50dc78' : 100);
    fill(activeInput === 2 ? 50 : 30);
    rect(450, 90, 300, 390, 8);

    noStroke();
    fill(255);
    textSize(18);
    text("Name: " + nameBall1, 200, 130);
    text("Name: " + nameBall2, 600, 130);

    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) fill(80, 220, 120);
    else fill(50, 180, 90);
    rect(btnX, btnY, btnW, btnH, 8);
    fill(255);
    text("NEXT", btnX + btnW / 2, btnY + btnH / 2);
    pop();
}

function drawMapSelectScreen() {
    push();
    fill(255);
    textSize(30);
    textAlign(CENTER, CENTER);
    text("SELECT MAP", width / 2, 40);

    let page = Math.floor(mapIndex / 5);
    let pageStart = page * 5;
    let pageEnd = Math.min(pageStart + 5, GameConfig.MAP_NAMES.length);

    for (let i = pageStart; i < pageEnd; i++) {
        let slot = i - pageStart;
        let cx = cardX0 + slot * (cardW + cardGap);

        if (mapIndex === i) {
            fill(80, 220, 120);
            stroke(255);
            strokeWeight(3);
        } else {
            fill(60);
            stroke(120);
            strokeWeight(1);
        }
        rect(cx, cardY, cardW, cardH, 8);

        noStroke();
        fill(255);
        textSize(16);
        text(GameConfig.MAP_NAMES[i], cx + cardW / 2, cardY + 30);
    }

    // Back Button
    if (mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH) fill(200, 80, 80);
    else fill(160, 50, 50);
    rect(backX, backY, backW, backH, 8);
    fill(255);
    textSize(18);
    text("BACK", backX + backW / 2, backY + backH / 2);

    // Play Button
    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) fill(80, 220, 120);
    else fill(50, 180, 90);
    rect(btnX, btnY, btnW, btnH, 8);
    fill(255);
    textSize(20);
    text("START", btnX + btnW / 2, btnY + btnH / 2);
    pop();
}

function drawTimerHUD() {
    push();
    fill(255);
    textSize(20);
    textAlign(CENTER, TOP);
    let secondsLeft = Math.ceil(gameTimerFrames / 60);
    text("Time: " + secondsLeft + (suddenDeath ? " (SUDDEN DEATH)" : ""), width / 2, 10);
    pop();
}

function drawStatsHUD() {
    push();
    textSize(16);
    fill(255);
    textAlign(LEFT, TOP);
    if (ball1) text(nameBall1 + " HP: " + Math.max(0, Math.ceil(ball1.health)), 20, 20);
    textAlign(RIGHT, TOP);
    if (ball2) text(nameBall2 + " HP: " + Math.max(0, Math.ceil(ball2.health)), width - 20, 20);
    pop();
}

function mousePressed() {
    if (state === GameState.SETUP) {
        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            state = GameState.MAP_SELECT;
        }
    } else if (state === GameState.MAP_SELECT) {
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

        if (mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH) {
            state = GameState.SETUP;
        }

        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            resetGame();
            startMatch();
        }
    } else if (state === GameState.GAME_OVER) {
        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            state = GameState.SETUP;
        }
    }
}