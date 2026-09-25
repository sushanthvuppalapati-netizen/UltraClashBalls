import processing.core.PApplet;
import java.util.ArrayList;

public class Main extends PApplet {

    GameState state = GameState.SETUP;

    // Player Customization State
    String nameBall1 = "PLAYER 1";
    String nameBall2 = "PLAYER 2";
    int activeInput = 1; // 1 = P1, 2 = P2
    int maxNameLength = 10;

    // Customization selections [Player 1, Player 2]
    int[] colorIndex = {0, 1};
    int[] accessoryIndex = {0, 1};
    int[] weaponIndex = {0, 1};
    int[] characterIndex = {0, 1};

    // Row layout (relative to a customizer box's own y) shared by drawing and click handling
    private static final float ROW_CHARACTER_Y = 95;
    private static final float ROW_COLOR_Y = 150;
    private static final float ROW_ACCESSORY_Y = 205;
    private static final float ROW_WEAPON_Y = 260;
    private static final float PREVIEW_Y = 308;

    int mapIndex = 0;

    // Current map's physics (set from GameConfig.MAP_GRAVITY/MAP_DRAG by generateMap())
    float gravity = GameConfig.MAP_GRAVITY[0];
    float drag = GameConfig.MAP_DRAG[0];
    float wind = GameConfig.MAP_WIND[0];

    boolean isPaused = false;

    // Game Over state
    String winnerText = "";

    // Sudden Death state
    boolean suddenDeath = false;
    int gameTimerFrames = 45 * 60; // 45 seconds at 60 FPS

    // Buttons
    float btnX = 300;
    float btnY = 510;
    float btnW = 200;
    float btnH = 50;

    // Back button (map select)
    float backX = 40;
    float backY = 510;
    float backW = 120;
    float backH = 50;

    // Map cards layout
    float cardX0 = 30;
    float cardY = 100;
    float cardW = 140;
    float cardH = 340;
    float cardGap = 10;

    // Power-up Spawning
    int spawnTimer = 0;
    int spawnInterval = 300;
    ArrayList<PowerUp> powerUps = new ArrayList<PowerUp>();

    // Bullets (for minigun)
    ArrayList<Bullet> bullets = new ArrayList<Bullet>();

    // Map Features
    ArrayList<LavaZone> lavaZones = new ArrayList<LavaZone>();
    ArrayList<Tree> trees = new ArrayList<Tree>();

    Ball ball1;
    Ball ball2;

    public static void main(String[] args) {
        PApplet.main("Main");
    }

    @Override
    public void settings() {
        size(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH, P3D);
    }

    @Override
    public void setup() {
        state = GameState.SETUP;
        resetGame();
    }

    private void resetGame() {
        ball1 = new Ball(this, 150, 100, GameConfig.COLORS[colorIndex[0]], accessoryIndex[0], effectiveWeaponIndex(0), characterIndex[0]);
        ball2 = new Ball(this, 650, 100, GameConfig.COLORS[colorIndex[1]], accessoryIndex[1], effectiveWeaponIndex(1), characterIndex[1]);
        powerUps.clear();
        bullets.clear();
        spawnTimer = 0;
        gameTimerFrames = 45 * 60;
        suddenDeath = false;
        winnerText = "";
        isPaused = false;

        generateMap();
    }

    /**
     * Some names unlock a secret weapon that overrides whatever the player picked --
     * -1 if this player's typed name doesn't match any of them.
     */
    private int specialWeaponFor(int p) {
        String n = (p == 0 ? nameBall1 : nameBall2).trim();
        if (n.equalsIgnoreCase("Aryaman")) return GameConfig.WEAPON_GODKILLER;
        if (n.equalsIgnoreCase("Sushanth")) return GameConfig.WEAPON_PLUTON;
        if (n.equalsIgnoreCase("Sushil") || n.equalsIgnoreCase("Sushi")) return GameConfig.WEAPON_STICK;
        if (n.equalsIgnoreCase("Cardona")) return GameConfig.WEAPON_GODKILLER;
        if (n.equalsIgnoreCase("Foster")) return GameConfig.WEAPON_GODKILLER;
        return -1;
    }

    /** The weapon this player will actually fight with: their secret weapon if their name grants one, else their pick. */
    private int effectiveWeaponIndex(int p) {
        int special = specialWeaponFor(p);
        return special >= 0 ? special : weaponIndex[p];
    }

    private void generateMap() {
        lavaZones.clear();
        trees.clear();
        MapBuilder.buildMap(mapIndex, lavaZones, trees);

        gravity = GameConfig.MAP_GRAVITY[mapIndex];
        drag = GameConfig.MAP_DRAG[mapIndex];
        wind = GameConfig.MAP_WIND[mapIndex];
    }

    @Override
    public void draw() {
        if (state == GameState.SETUP) {
            background(40, 35, 30);
            drawNameInputScreen();
            return;
        }

        if (state == GameState.MAP_SELECT) {
            background(40, 35, 30);
            drawMapSelectScreen();
            return;
        }

        background(GameConfig.MAP_BG[mapIndex]);
        lights();

        if (!isPaused && state == GameState.PLAYING) {
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

                ball1.takeDamage(0.15f);
                ball2.takeDamage(0.15f);
            } else {
                spawnTimer++;
                if (spawnTimer >= spawnInterval) {
                    spawnTimer = 0;
                    spawnRandomPowerUp();
                    spawnRandomPowerUp();

                    while (powerUps.size() > 2) {
                        powerUps.remove(0);
                    }
                }

                ball1.update(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH, gravity, drag, wind);
                ball2.update(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH, gravity, drag, wind);

                for (LavaZone l : lavaZones) {
                    l.checkDamage(ball1);
                    l.checkDamage(ball2);
                }

                for (Tree t : trees) {
                    t.resolveCollision(ball1);
                    t.resolveCollision(ball2);
                }

                resolveBallCollision(ball1, ball2);

                for (int i = powerUps.size() - 1; i >= 0; i--) {
                    PowerUp p = powerUps.get(i);
                    p.update();
                    if (p.checkCollision(ball1)) powerUps.remove(i);
                    else if (p.checkCollision(ball2)) powerUps.remove(i);
                }
            }

            ball1.updateWeapon(bullets, ball2, trees);
            ball2.updateWeapon(bullets, ball1, trees);

            checkWeaponHits(ball1, ball2);
            checkWeaponHits(ball2, ball1);

            // Update and check bullets
            for (int i = bullets.size() - 1; i >= 0; i--) {
                Bullet b = bullets.get(i);
                b.update();
                if (b.isOffScreen(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH)) {
                    bullets.remove(i);
                } else {
                    if (b.checkHit(ball1)) {
                        ball1.takeDamage(b.damage);
                        bullets.remove(i);
                    } else if (b.checkHit(ball2)) {
                        ball2.takeDamage(b.damage);
                        bullets.remove(i);
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

        for (LavaZone l : lavaZones) l.draw(this, GameConfig.HAZARD_FILL[mapIndex], GameConfig.HAZARD_LINE[mapIndex]);
        for (Tree t : trees) t.draw(this, GameConfig.OBSTACLE_COL[mapIndex]);
        for (PowerUp p : powerUps) p.draw(this);
        for (Bullet b : bullets) b.draw(this);

        ball1.draw(this, isPaused, nameBall1);
        ball2.draw(this, isPaused, nameBall2);

        drawTimerHUD();
        drawStatsHUD();

        if (state == GameState.GAME_OVER) {
            drawGameOverScreen();
        }
    }

    private void drawNameInputScreen() {
        hint(DISABLE_DEPTH_TEST);
        camera();
        noLights();

        textAlign(CENTER, CENTER);
        fill(255);
        textSize(32);
        text("CHARACTER CUSTOMIZATION", GameConfig.SCREEN_WIDTH / 2f, 40);

        // Render Player Selection Columns
        drawPlayerCustomizer(0, 50, 90, nameBall1);
        drawPlayerCustomizer(1, 450, 90, nameBall2);

        // Next Button
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
        textSize(22);
        text("SELECT MAP >", GameConfig.SCREEN_WIDTH / 2f, btnY + btnH / 2f);

        hint(ENABLE_DEPTH_TEST);
    }

    private void drawMapSelectScreen() {
        hint(DISABLE_DEPTH_TEST);
        camera();
        noLights();

        textAlign(CENTER, CENTER);
        fill(255);
        textSize(32);
        text("SELECT MAP", GameConfig.SCREEN_WIDTH / 2f, 40);
        textSize(13);
        fill(180);
        text("Click a map or use LEFT / RIGHT arrows. ENTER to start, BACKSPACE to go back.", GameConfig.SCREEN_WIDTH / 2f, 72);

        int totalPages = (GameConfig.MAP_NAMES.length + 4) / 5;
        int page = mapIndex / 5;
        int pageStart = page * 5;
        int pageEnd = Math.min(pageStart + 5, GameConfig.MAP_NAMES.length);

        for (int i = pageStart; i < pageEnd; i++) {
            int slot = i - pageStart;
            float cx = cardX0 + slot * (cardW + cardGap);
            boolean selected = (i == mapIndex);
            boolean hover = mouseX >= cx && mouseX <= cx + cardW && mouseY >= cardY && mouseY <= cardY + cardH;

            // Card
            rectMode(CORNER);
            fill(30, 30, 35, hover ? 255 : 220);
            stroke(selected ? color(255, 220, 60) : (hover ? 200 : 100));
            strokeWeight(selected ? 4 : 1);
            rect(cx, cardY, cardW, cardH, 10);

            // Mini map preview (800x600 scaled to 120x90)
            drawMapPreview(i, cx + 10, cardY + 15, 120, 90);

            // Name
            textAlign(CENTER, CENTER);
            fill(selected ? color(255, 220, 60) : 255);
            textSize(15);
            text(GameConfig.MAP_NAMES[i], cx + cardW / 2f, cardY + 135);

            // Description
            fill(200);
            textSize(12);
            textAlign(CENTER, TOP);
            text(GameConfig.MAP_DESCS[i], cx + cardW / 2f, cardY + 160);

            // Stats
            textAlign(LEFT, TOP);
            fill(160);
            textSize(11);
            String grav = GameConfig.MAP_GRAVITY[i] < 0.15f ? "Low" : (GameConfig.MAP_GRAVITY[i] > 0.32f ? "Heavy" : "Normal");
            String fric = GameConfig.MAP_DRAG[i] > 0.995f ? "Icy" : (GameConfig.MAP_DRAG[i] < 0.95f ? "Thick" : "Normal");
            float w = GameConfig.MAP_WIND[i];
            String wind = Math.abs(w) < 0.02f ? "None" : (Math.abs(w) < 0.06f ? "Light" : "Strong");
            text("Gravity: " + grav, cx + 12, cardY + 232);
            text("Friction: " + fric, cx + 12, cardY + 250);
            text("Wind: " + wind, cx + 12, cardY + 268);
            text("Hazards: " + countHazards(i), cx + 12, cardY + 286);
            text("Obstacles: " + countObstacles(i), cx + 12, cardY + 304);
        }

        // Page dots (click one to jump to that page of maps)
        float dotsY = cardY + cardH + 24;
        float dotSpacing = 26;
        float dotsStartX = GameConfig.SCREEN_WIDTH / 2f - (totalPages - 1) * dotSpacing / 2f;
        noStroke();
        for (int pg = 0; pg < totalPages; pg++) {
            float dx = dotsStartX + pg * dotSpacing;
            boolean isCurrentPage = (pg == page);
            boolean dotHover = dist(mouseX, mouseY, dx, dotsY) < 10;
            fill(isCurrentPage ? color(255, 220, 60) : (dotHover ? color(200) : color(110)));
            ellipse(dx, dotsY, isCurrentPage ? 14 : 10, isCurrentPage ? 14 : 10);
        }
        fill(255);
        textAlign(CENTER, CENTER);
        textSize(12);
        text("Page " + (page + 1) + " / " + totalPages, GameConfig.SCREEN_WIDTH / 2f, dotsY + 20);

        // Back Button
        boolean overBack = mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH;
        fill(overBack ? color(120, 120, 130) : color(80, 80, 90));
        stroke(255);
        strokeWeight(2);
        rectMode(CORNER);
        rect(backX, backY, backW, backH, 10);
        fill(255);
        textAlign(CENTER, CENTER);
        textSize(20);
        text("< BACK", backX + backW / 2f, backY + backH / 2f);

        // Start Button
        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            fill(80, 220, 120);
        } else {
            fill(50, 180, 90);
        }
        stroke(255);
        strokeWeight(2);
        rect(btnX, btnY, btnW, btnH, 10);
        fill(255);
        textSize(22);
        text("START MATCH", GameConfig.SCREEN_WIDTH / 2f, btnY + btnH / 2f);

        hint(ENABLE_DEPTH_TEST);
    }

    private int countHazards(int idx) {
        ArrayList<LavaZone> lz = new ArrayList<LavaZone>();
        ArrayList<Tree> tr = new ArrayList<Tree>();
        MapBuilder.buildMap(idx, lz, tr);
        return lz.size();
    }

    private int countObstacles(int idx) {
        ArrayList<LavaZone> lz = new ArrayList<LavaZone>();
        ArrayList<Tree> tr = new ArrayList<Tree>();
        MapBuilder.buildMap(idx, lz, tr);
        return tr.size();
    }

    private void drawMapPreview(int idx, float px, float py, float pw, float ph) {
        float sx = pw / GameConfig.SCREEN_WIDTH;
        float sy = ph / GameConfig.SCREEN_LENGTH;

        ArrayList<LavaZone> lz = new ArrayList<LavaZone>();
        ArrayList<Tree> tr = new ArrayList<Tree>();
        MapBuilder.buildMap(idx, lz, tr);

        // Background
        rectMode(CORNER);
        fill(GameConfig.MAP_BG[idx]);
        stroke(150);
        strokeWeight(1);
        rect(px, py, pw, ph);

        // Hazards
        rectMode(CENTER);
        fill(GameConfig.HAZARD_FILL[idx]);
        stroke(GameConfig.HAZARD_LINE[idx]);
        strokeWeight(1);
        for (LavaZone l : lz) {
            rect(px + l.x * sx, py + l.y * sy, l.w * sx, l.h * sy, 3);
        }

        // Obstacles
        fill(GameConfig.OBSTACLE_COL[idx]);
        noStroke();
        for (Tree t : tr) {
            ellipse(px + t.x * sx, py + t.y * sy, t.size * 2 * sx, t.size * 2 * sy);
        }

        // Spawn markers
        fill(GameConfig.COLORS[colorIndex[0]]);
        ellipse(px + 150 * sx, py + 100 * sy, 8, 8);
        fill(GameConfig.COLORS[colorIndex[1]]);
        ellipse(px + 650 * sx, py + 100 * sy, 8, 8);

        rectMode(CORNER);
    }

    private void startMatch() {
        if (nameBall1.trim().isEmpty()) nameBall1 = "P1";
        if (nameBall2.trim().isEmpty()) nameBall2 = "P2";
        resetGame();
        state = GameState.PLAYING;
    }

    private void drawPlayerCustomizer(int pIndex, float x, float y, String name) {
        boolean isActive = (activeInput == pIndex + 1);

        fill(30, 30, 35, 220);
        stroke(isActive ? GameConfig.COLORS[colorIndex[pIndex]] : 100);
        strokeWeight(isActive ? 3 : 1);
        rectMode(CORNER);
        rect(x, y, 300, 390, 10);

        textAlign(CENTER, CENTER);
        fill(255);
        textSize(18);
        text("PLAYER " + (pIndex + 1), x + 150, y + 25);

        // Name Box
        stroke(isActive ? GameConfig.COLORS[colorIndex[pIndex]] : 80);
        fill(50);
        rect(x + 20, y + 45, 260, 35, 5);
        fill(255);
        textSize(16);
        text(name + (isActive && frameCount % 60 < 30 ? "|" : ""), x + 150, y + 62);

        // Selection Rows
        Character c = GameConfig.CHARACTERS[characterIndex[pIndex]];
        drawOptionSelector("Character", c.name, x + 20, y + ROW_CHARACTER_Y);
        drawOptionSelector("Color", GameConfig.COLOR_NAMES[colorIndex[pIndex]], x + 20, y + ROW_COLOR_Y);
        drawOptionSelector("Accessory", GameConfig.ACCESSORY_NAMES[accessoryIndex[pIndex]], x + 20, y + ROW_ACCESSORY_Y);

        int special = specialWeaponFor(pIndex);
        if (special >= 0) {
            // Secret weapon unlocked by name -- no arrows, it's not a free pick.
            drawLockedSelector("Weapon", GameConfig.WEAPON_NAMES[special], x + 20, y + ROW_WEAPON_Y);
        } else {
            drawOptionSelector("Weapon", GameConfig.WEAPON_NAMES[weaponIndex[pIndex]], x + 20, y + ROW_WEAPON_Y);
        }

        // Preview Box: live ball preview on the left, its class stats on the right
        fill(20);
        stroke(80);
        rect(x + 20, y + PREVIEW_Y, 260, 72, 5);

        // Ball preview is drawn at full scale internally (fixed radius/weapon length), so it's
        // scaled way down here to fit inside this small box without spilling into the stats text.
        pushMatrix();
        translate(x + 50, y + PREVIEW_Y + 36, 10);
        scale(0.42f);
        Ball tempBall = new Ball(this, 0, 0, GameConfig.COLORS[colorIndex[pIndex]], accessoryIndex[pIndex], effectiveWeaponIndex(pIndex), characterIndex[pIndex]);
        tempBall.draw(this, false, "");
        popMatrix();

        textAlign(LEFT, TOP);
        fill(180);
        textSize(10);
        text("HP: " + (int) c.health, x + 105, y + PREVIEW_Y + 6);
        text("SPD: " + String.format("%.2f", c.speedMult) + "x", x + 105, y + PREVIEW_Y + 20);
        text("DMG: " + String.format("%.2f", c.damageMult) + "x", x + 105, y + PREVIEW_Y + 34);
        text(c.description.replace("\n", " "), x + 105, y + PREVIEW_Y + 48, 170, 22);
    }

    private void drawLockedSelector(String label, String value, float x, float y) {
        textAlign(LEFT, CENTER);
        fill(200);
        textSize(13);
        text(label, x, y);

        fill(40);
        stroke(255, 215, 0, 150);
        strokeWeight(1.5f);
        rect(x, y + 12, 260, 30, 5);

        textAlign(CENTER, CENTER);
        fill(255, 215, 0);
        textSize(14);
        text(value, x + 130, y + 26);
    }

    private void drawOptionSelector(String label, String value, float x, float y) {
        textAlign(LEFT, CENTER);
        fill(200);
        textSize(13);
        text(label, x, y);

        fill(50);
        stroke(100);
        rect(x, y + 12, 260, 30, 5);

        // Left Arrow
        fill(180);
        triangle(x + 10, y + 27, x + 20, y + 18, x + 20, y + 36);
        // Right Arrow
        triangle(x + 250, y + 27, x + 240, y + 18, x + 240, y + 36);

        textAlign(CENTER, CENTER);
        fill(255);
        textSize(14);
        text(value, x + 130, y + 26);
    }

    private void drawTimerHUD() {
        hint(DISABLE_DEPTH_TEST);
        camera();
        noLights();

        textAlign(CENTER, TOP);
        textSize(28);

        if (suddenDeath) {
            fill(255, 50, 50);
            text("SUDDEN DEATH! OVERTIME", GameConfig.SCREEN_WIDTH / 2f, 15);
        } else {
            fill(255);
            int secondsLeft = ceil(gameTimerFrames / 60.0f);
            text("TIME LEFT: " + secondsLeft + "s", GameConfig.SCREEN_WIDTH / 2f, 15);
        }

        hint(ENABLE_DEPTH_TEST);
    }

    private void drawStatsHUD() {
        hint(DISABLE_DEPTH_TEST);
        camera();
        noLights();

        rectMode(CORNER);
        drawPlayerStatsBox(15, 15, nameBall1, ball1, GameConfig.COLORS[colorIndex[0]]);
        drawPlayerStatsBox(GameConfig.SCREEN_WIDTH - 175, 15, nameBall2, ball2, GameConfig.COLORS[colorIndex[1]]);

        hint(ENABLE_DEPTH_TEST);
    }

    private void drawPlayerStatsBox(float x, float y, String name, Ball b, int playerColor) {
        fill(20, 20, 25, 200);
        stroke(playerColor);
        strokeWeight(2);
        rect(x, y, 160, 95, 8);

        textAlign(LEFT, TOP);
        textSize(14);
        fill(playerColor);
        text(name, x + 10, y + 8);

        textSize(12);
        fill(220);
        text("HP: " + ceil(b.health) + " / " + (int) b.maxHealth, x + 10, y + 28);

        float baseDmg = b.hasFlamethrower ? 1.5f : 8.0f;
        float currentDmg = baseDmg * b.dmgMultiplier * b.classDamageMult * b.weaponDamageMult;
        if (b.dmgTimer > 0) fill(255, 80, 80);
        else fill(220);
        text("DMG: " + String.format("%.1f", currentDmg) + (b.dmgMultiplier > 1.0f ? " (x2)" : ""), x + 10, y + 48);

        float currentSpeed = GameConfig.MAX_SPEED * b.speedMultiplier * b.classSpeedMult;
        if (b.speedTimer > 0) fill(255, 220, 0);
        else fill(220);
        text("SPD: " + String.format("%.1f", currentSpeed) + (b.speedMultiplier > 1.0f ? " (x1.5)" : ""), x + 10, y + 68);
    }

    private void checkWeaponHits(Ball attacker, Ball defender) {
        float radius = GameConfig.RADIUS;
        float totalAngle = attacker.ballRotation + attacker.weaponSwingAngle;
        float tipX = attacker.x + cos(totalAngle) * (radius + attacker.weaponLength);
        float tipY = attacker.y + sin(totalAngle) * (radius + attacker.weaponLength);

        if (dist(tipX, tipY, defender.x, defender.y) < radius) {
            float baseDmg = attacker.hasFlamethrower ? 1.5f : 8.0f;
            float finalDmg = baseDmg * attacker.dmgMultiplier * attacker.classDamageMult * attacker.weaponDamageMult;
            defender.takeDamage(finalDmg);

            float pushAngle = atan2(defender.y - attacker.y, defender.x - attacker.x);
            float pushForce = attacker.hasFlamethrower ? 3.0f : 12.0f;

            defender.speedX += cos(pushAngle) * pushForce;
            defender.speedY += sin(pushAngle) * pushForce;
        }
    }

    private void spawnRandomPowerUp() {
        float spawnX = random(50, GameConfig.SCREEN_WIDTH - 50);
        float spawnY = random(50, GameConfig.SCREEN_LENGTH - 100);
        int type = (int) random(4);
        powerUps.add(new PowerUp(spawnX, spawnY, type));
    }

    private void drawGameOverScreen() {
        hint(DISABLE_DEPTH_TEST);
        camera();
        noLights();

        rectMode(CORNER);
        fill(0, 0, 0, 180);
        rect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH);

        textAlign(CENTER, CENTER);
        textSize(44);
        fill(255);
        text(winnerText, GameConfig.SCREEN_WIDTH / 2f, 220);

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
        text("PLAY AGAIN", GameConfig.SCREEN_WIDTH / 2f, btnY + btnH / 2f);

        hint(ENABLE_DEPTH_TEST);
    }

    private void resolveBallCollision(Ball b1, Ball b2) {
        float radius = GameConfig.RADIUS;
        float restitution = GameConfig.RESTITUTION;

        float dx = b2.x - b1.x;
        float dy = b2.y - b1.y;
        float distance = sqrt(dx * dx + dy * dy);
        float minDist = radius * 2;

        if (distance < minDist && distance > 0) {
            float nx = dx / distance;
            float ny = dy / distance;

            float overlap = 0.5f * (minDist - distance);
            b1.x -= nx * overlap;
            b1.y -= ny * overlap;
            b2.x += nx * overlap;
            b2.y += ny * overlap;

            float kx = b1.speedX - b2.speedX;
            float ky = b1.speedY - b2.speedY;
            float p = 2 * (nx * kx + ny * ky) / 2;

            b1.speedX -= p * nx * restitution;
            b1.speedY -= p * ny * restitution;
            b2.speedX += p * nx * restitution;
            b2.speedY += p * ny * restitution;
        }
    }

    @Override
    public void mousePressed() {
        if (state == GameState.SETUP) {
            // Player Box Focus
            if (mouseX >= 50 && mouseX <= 350 && mouseY >= 90 && mouseY <= 480) activeInput = 1;
            else if (mouseX >= 450 && mouseX <= 750 && mouseY >= 90 && mouseY <= 480) activeInput = 2;

            // Handle Customization Arrow Clicks
            handleCustomizationClick(0, 50, 90);
            handleCustomizationClick(1, 450, 90);

            // Next Button Click -> Map Select
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                if (nameBall1.trim().isEmpty()) nameBall1 = "P1";
                if (nameBall2.trim().isEmpty()) nameBall2 = "P2";
                state = GameState.MAP_SELECT;
            }
        } else if (state == GameState.MAP_SELECT) {
            int totalPages = (GameConfig.MAP_NAMES.length + 4) / 5;
            int page = mapIndex / 5;
            int pageStart = page * 5;
            int pageEnd = Math.min(pageStart + 5, GameConfig.MAP_NAMES.length);

            // Map cards (only the current page's 5 are on screen)
            for (int i = pageStart; i < pageEnd; i++) {
                int slot = i - pageStart;
                float cx = cardX0 + slot * (cardW + cardGap);
                if (mouseX >= cx && mouseX <= cx + cardW && mouseY >= cardY && mouseY <= cardY + cardH) {
                    mapIndex = i;
                }
            }

            // Page dots
            float dotsY = cardY + cardH + 24;
            float dotSpacing = 26;
            float dotsStartX = GameConfig.SCREEN_WIDTH / 2f - (totalPages - 1) * dotSpacing / 2f;
            for (int pg = 0; pg < totalPages; pg++) {
                float dx = dotsStartX + pg * dotSpacing;
                if (dist(mouseX, mouseY, dx, dotsY) < 10) {
                    mapIndex = pg * 5;
                }
            }

            // Back
            if (mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH) {
                state = GameState.SETUP;
            }

            // Start
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                startMatch();
            }
        } else if (state == GameState.GAME_OVER) {
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                state = GameState.SETUP;
                resetGame();
            }
        }
    }

    private void handleCustomizationClick(int p, float x, float y) {
        // Character row arrows
        if (mouseY >= y + ROW_CHARACTER_Y + 12 && mouseY <= y + ROW_CHARACTER_Y + 42) {
            if (mouseX >= x + 20 && mouseX <= x + 40) characterIndex[p] = (characterIndex[p] - 1 + GameConfig.CHARACTERS.length) % GameConfig.CHARACTERS.length;
            if (mouseX >= x + 240 && mouseX <= x + 260) characterIndex[p] = (characterIndex[p] + 1) % GameConfig.CHARACTERS.length;
        }
        // Color row arrows
        if (mouseY >= y + ROW_COLOR_Y + 12 && mouseY <= y + ROW_COLOR_Y + 42) {
            if (mouseX >= x + 20 && mouseX <= x + 40) colorIndex[p] = (colorIndex[p] - 1 + GameConfig.COLORS.length) % GameConfig.COLORS.length;
            if (mouseX >= x + 240 && mouseX <= x + 260) colorIndex[p] = (colorIndex[p] + 1) % GameConfig.COLORS.length;
        }
        // Accessory row arrows
        if (mouseY >= y + ROW_ACCESSORY_Y + 12 && mouseY <= y + ROW_ACCESSORY_Y + 42) {
            if (mouseX >= x + 20 && mouseX <= x + 40) accessoryIndex[p] = (accessoryIndex[p] - 1 + GameConfig.ACCESSORY_NAMES.length) % GameConfig.ACCESSORY_NAMES.length;
            if (mouseX >= x + 240 && mouseX <= x + 260) accessoryIndex[p] = (accessoryIndex[p] + 1) % GameConfig.ACCESSORY_NAMES.length;
        }
        // Weapon row arrows -- locked out entirely while a secret name-weapon is active
        if (specialWeaponFor(p) < 0 && mouseY >= y + ROW_WEAPON_Y + 12 && mouseY <= y + ROW_WEAPON_Y + 42) {
            if (mouseX >= x + 20 && mouseX <= x + 40) weaponIndex[p] = (weaponIndex[p] - 1 + GameConfig.PUBLIC_WEAPON_COUNT) % GameConfig.PUBLIC_WEAPON_COUNT;
            if (mouseX >= x + 240 && mouseX <= x + 260) weaponIndex[p] = (weaponIndex[p] + 1) % GameConfig.PUBLIC_WEAPON_COUNT;
        }
    }

    @Override
    public void keyPressed() {
        if (state == GameState.SETUP) {
            if (key == TAB) {
                activeInput = (activeInput == 1) ? 2 : 1;
                return;
            }

            if (key == ENTER || key == RETURN) {
                if (nameBall1.trim().isEmpty()) nameBall1 = "P1";
                if (nameBall2.trim().isEmpty()) nameBall2 = "P2";
                state = GameState.MAP_SELECT;
                return;
            }

            String currentName = (activeInput == 1) ? nameBall1 : nameBall2;

            if (key == BACKSPACE) {
                if (currentName.length() > 0) {
                    currentName = currentName.substring(0, currentName.length() - 1);
                }
            } else if (key >= ' ' && key <= '~') {
                if (currentName.length() < maxNameLength) {
                    currentName += key;
                }
            }

            if (activeInput == 1) nameBall1 = currentName;
            else nameBall2 = currentName;

        } else if (state == GameState.MAP_SELECT) {
            if (keyCode == LEFT) {
                mapIndex = (mapIndex - 1 + GameConfig.MAP_NAMES.length) % GameConfig.MAP_NAMES.length;
            } else if (keyCode == RIGHT) {
                mapIndex = (mapIndex + 1) % GameConfig.MAP_NAMES.length;
            } else if (key == ENTER || key == RETURN) {
                startMatch();
            } else if (key == BACKSPACE) {
                state = GameState.SETUP;
            }

        } else if (state == GameState.PLAYING) {
            if (key == ' ') isPaused = !isPaused;
            setKeyState(keyCode, key, true);
        }
    }

    @Override
    public void keyReleased() {
        if (state == GameState.PLAYING) {
            setKeyState(keyCode, key, false);
        }
    }

    private void setKeyState(int code, char k, boolean pressed) {
        if (k == 'w' || k == 'W') ball1.keyUp = pressed;
        if (k == 's' || k == 'S') ball1.keyDown = pressed;
        if (k == 'a' || k == 'A') ball1.keyLeft = pressed;
        if (k == 'd' || k == 'D') ball1.keyRight = pressed;

        if (code == UP) ball2.keyUp = pressed;
        if (code == DOWN) ball2.keyDown = pressed;
        if (code == LEFT) ball2.keyLeft = pressed;
        if (code == RIGHT) ball2.keyRight = pressed;
    }
}