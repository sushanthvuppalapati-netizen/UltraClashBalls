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
    int[] classIndex = {0, 1};

    int mapIndex = 0;

    // Current map's physics (set from GameConfig.MAP_GRAVITY/MAP_DRAG by generateMap())
    float gravity = GameConfig.MAP_GRAVITY[0];
    float drag = GameConfig.MAP_DRAG[0];

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

    // Map select grid layout (5 columns x 3 rows fits all 15 maps)
    int mapCols = 5;
    float mapCardW = 145;
    float mapCardH = 125;
    float mapColGap = 8;
    float mapRowGap = 10;
    float mapStartX = 20;
    float mapStartY = 95;

    // Exit button - closes the whole program. Shown top-right on every
    // menu screen, and bottom-right on the in-game HUD (so it doesn't
    // overlap the player 2 stats box, which also sits top-right).
    float exitBtnW = 70;
    float exitBtnH = 35;
    float exitMenuX = GameConfig.SCREEN_WIDTH - exitBtnW - 15;
    float exitMenuY = 15;
    float exitGameX = GameConfig.SCREEN_WIDTH - exitBtnW - 15;
    float exitGameY = GameConfig.SCREEN_LENGTH - exitBtnH - 15;

    // --- Virtual-resolution scaling ---
    // The whole game is laid out against GameConfig.SCREEN_WIDTH x
    // SCREEN_LENGTH (800x600). Main runs fullscreen at the device's real
    // resolution and scales/centers ("letterboxes") that virtual canvas
    // to fit, so all existing layout code keeps working unmodified.
    float viewScale = 1;
    float viewOffsetX = 0;
    float viewOffsetY = 0;

    // Mouse position translated into virtual (800x600) space. Every bit
    // of UI hit-testing below uses these instead of the raw mouseX/mouseY.
    float vMouseX = 0;
    float vMouseY = 0;

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
        // Fullscreen at the device's native resolution. The 800x600
        // virtual canvas is scaled to fit inside it every frame (see
        // computeViewport()) so gameplay/UI coordinates never change.
        fullScreen(P3D);
    }

    @Override
    public void setup() {
        state = GameState.SETUP;
        resetGame();
    }

    private void resetGame() {
        ball1 = new Ball(this, 150, 100, GameConfig.COLORS[colorIndex[0]], accessoryIndex[0], weaponIndex[0], classIndex[0]);
        ball2 = new Ball(this, 650, 100, GameConfig.COLORS[colorIndex[1]], accessoryIndex[1], weaponIndex[1], classIndex[1]);
        powerUps.clear();
        bullets.clear();
        spawnTimer = 0;
        gameTimerFrames = 45 * 60;
        suddenDeath = false;
        winnerText = "";
        isPaused = false;

        generateMap();
    }

    private void generateMap() {
        lavaZones.clear();
        trees.clear();
        MapBuilder.buildMap(mapIndex, lavaZones, trees);

        gravity = GameConfig.MAP_GRAVITY[mapIndex];
        drag = GameConfig.MAP_DRAG[mapIndex];
    }

    // Recomputes the scale/offset that maps the 800x600 virtual canvas
    // onto the actual (fullscreen) window size, preserving aspect ratio
    // and centering it (letterboxing) on mismatched aspect ratios.
    private void computeViewport() {
        viewScale = min((float) width / GameConfig.SCREEN_WIDTH, (float) height / GameConfig.SCREEN_LENGTH);
        viewOffsetX = (width - GameConfig.SCREEN_WIDTH * viewScale) / 2f;
        viewOffsetY = (height - GameConfig.SCREEN_LENGTH * viewScale) / 2f;
    }

    // Converts the real mouse position into virtual (800x600) space so
    // every hit-test in the UI can keep using nice round coordinates.
    private void updateVirtualMouse() {
        computeViewport();
        vMouseX = (mouseX - viewOffsetX) / viewScale;
        vMouseY = (mouseY - viewOffsetY) / viewScale;
    }

    @Override
    public void draw() {
        updateVirtualMouse();

        if (state == GameState.SETUP) {
            background(40, 35, 30);
            drawNameInputScreen();
            return;
        }

        if (state == GameState.CLASS_SELECT) {
            background(40, 35, 30);
            drawClassSelectScreen();
            return;
        }

        if (state == GameState.MAP_SELECT) {
            background(40, 35, 30);
            drawMapSelectScreen();
            return;
        }

        background(GameConfig.MAP_BG[mapIndex]);
        lights();

        pushMatrix();
        translate(viewOffsetX, viewOffsetY);
        scale(viewScale);

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

                ball1.update(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH, gravity, drag);
                ball2.update(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH, gravity, drag);

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

        popMatrix();

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
        pushMatrix();
        translate(viewOffsetX, viewOffsetY);
        scale(viewScale);

        textAlign(CENTER, CENTER);
        fill(255);
        textSize(32);
        text("CHARACTER CUSTOMIZATION", GameConfig.SCREEN_WIDTH / 2f, 40);

        // Render Player Selection Columns
        drawPlayerCustomizer(0, 50, 90, nameBall1);
        drawPlayerCustomizer(1, 450, 90, nameBall2);

        // Next Button
        if (vMouseX >= btnX && vMouseX <= btnX + btnW && vMouseY >= btnY && vMouseY <= btnY + btnH) {
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
        text("SELECT CLASS >", GameConfig.SCREEN_WIDTH / 2f, btnY + btnH / 2f);

        drawExitButton(exitMenuX, exitMenuY);

        popMatrix();
        hint(ENABLE_DEPTH_TEST);
    }

    private void drawClassSelectScreen() {
        hint(DISABLE_DEPTH_TEST);
        camera();
        noLights();
        pushMatrix();
        translate(viewOffsetX, viewOffsetY);
        scale(viewScale);

        textAlign(CENTER, CENTER);
        fill(255);
        textSize(32);
        text("SELECT CLASS", GameConfig.SCREEN_WIDTH / 2f, 40);
        textSize(13);
        fill(180);
        text("Choose a class for each player. Click a card or use arrow keys. ENTER to continue, BACKSPACE to go back.", GameConfig.SCREEN_WIDTH / 2f, 72);

        // Layout: 4 columns, 2 rows
        float classCardW = 150;
        float classCardH = 160;
        float colGap = 10;
        float rowGap = 20;
        float startX = 20;
        float startY = 110;

        for (int i = 0; i < GameConfig.CLASS_NAMES.length; i++) {
            int col = i % 4;
            int row = i / 4;
            float cx = startX + col * (classCardW + colGap);
            float cy = startY + row * (classCardH + rowGap);

            drawClassCard(i, cx, cy, classCardW, classCardH);
        }

        // Back Button
        boolean overBack = vMouseX >= backX && vMouseX <= backX + backW && vMouseY >= backY && vMouseY <= backY + backH;
        fill(overBack ? color(120, 120, 130) : color(80, 80, 90));
        stroke(255);
        strokeWeight(2);
        rectMode(CORNER);
        rect(backX, backY, backW, backH, 10);
        fill(255);
        textAlign(CENTER, CENTER);
        textSize(20);
        text("< BACK", backX + backW / 2f, backY + backH / 2f);

        // Next Button
        if (vMouseX >= btnX && vMouseX <= btnX + btnW && vMouseY >= btnY && vMouseY <= btnY + btnH) {
            fill(80, 220, 120);
        } else {
            fill(50, 180, 90);
        }
        stroke(255);
        strokeWeight(2);
        rect(btnX, btnY, btnW, btnH, 10);
        fill(255);
        textSize(22);
        text("SELECT MAP >", GameConfig.SCREEN_WIDTH / 2f, btnY + btnH / 2f);

        drawExitButton(exitMenuX, exitMenuY);

        popMatrix();
        hint(ENABLE_DEPTH_TEST);
    }

    private void drawClassCard(int classIdx, float x, float y, float w, float h) {
        boolean p1Selected = (classIndex[0] == classIdx);
        boolean p2Selected = (classIndex[1] == classIdx);
        boolean selected = p1Selected || p2Selected;
        boolean hover = vMouseX >= x && vMouseX <= x + w && vMouseY >= y && vMouseY <= y + h;

        // Card background
        rectMode(CORNER);
        fill(30, 30, 35, hover ? 255 : 220);
        stroke(selected ? color(255, 220, 60) : (hover ? 200 : 100));
        strokeWeight(selected ? 4 : 1);
        rect(x, y, w, h, 10);

        // Class color indicator bar
        fill(GameConfig.CLASS_COLORS[classIdx]);
        noStroke();
        rect(x, y, w, 8, 10, 10, 0, 0);

        // Name
        textAlign(CENTER, TOP);
        fill(selected ? color(255, 220, 60) : 255);
        textSize(16);
        text(GameConfig.CLASS_NAMES[classIdx], x + w / 2f, y + 12);

        // Description (smaller font, wrapped)
        fill(180);
        textSize(9);
        textAlign(CENTER, TOP);
        text(GameConfig.CLASS_DESCS[classIdx], x + 8, y + 35, w - 16, 50);

        // Stats display
        float[] stats = GameConfig.CLASS_STATS[classIdx];
        textAlign(LEFT, TOP);
        fill(150);
        textSize(10);
        text("HP: " + (int) stats[0], x + 8, y + 95);
        text("SPD: " + String.format("%.2f", stats[1]) + "x", x + 8, y + 110);
        text("DMG: " + String.format("%.2f", stats[2]) + "x", x + 8, y + 125);

        // Selection indicators
        if (p1Selected) {
            fill(GameConfig.COLORS[colorIndex[0]]);
            textSize(11);
            textAlign(CENTER, BOTTOM);
            text("P1", x + w / 4f, y + h - 3);
        }
        if (p2Selected) {
            fill(GameConfig.COLORS[colorIndex[1]]);
            textSize(11);
            textAlign(CENTER, BOTTOM);
            text("P2", x + 3 * w / 4f, y + h - 3);
        }
    }

    private void drawMapSelectScreen() {
        hint(DISABLE_DEPTH_TEST);
        camera();
        noLights();
        pushMatrix();
        translate(viewOffsetX, viewOffsetY);
        scale(viewScale);

        textAlign(CENTER, CENTER);
        fill(255);
        textSize(32);
        text("SELECT MAP", GameConfig.SCREEN_WIDTH / 2f, 40);
        textSize(13);
        fill(180);
        text("Click a map or use LEFT / RIGHT arrows. ENTER to start, BACKSPACE to go back.", GameConfig.SCREEN_WIDTH / 2f, 72);

        // 5 columns x 3 rows grid - fits all 15 maps on one screen
        for (int i = 0; i < GameConfig.MAP_NAMES.length; i++) {
            int col = i % mapCols;
            int row = i / mapCols;
            float cx = mapStartX + col * (mapCardW + mapColGap);
            float cy = mapStartY + row * (mapCardH + mapRowGap);

            drawMapCard(i, cx, cy, mapCardW, mapCardH);
        }

        // Back Button
        boolean overBack = vMouseX >= backX && vMouseX <= backX + backW && vMouseY >= backY && vMouseY <= backY + backH;
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
        if (vMouseX >= btnX && vMouseX <= btnX + btnW && vMouseY >= btnY && vMouseY <= btnY + btnH) {
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

        drawExitButton(exitMenuX, exitMenuY);

        popMatrix();
        hint(ENABLE_DEPTH_TEST);
    }

    private void drawMapCard(int idx, float x, float y, float w, float h) {
        boolean selected = (idx == mapIndex);
        boolean hover = vMouseX >= x && vMouseX <= x + w && vMouseY >= y && vMouseY <= y + h;

        rectMode(CORNER);
        fill(30, 30, 35, hover ? 255 : 220);
        stroke(selected ? color(255, 220, 60) : (hover ? 200 : 100));
        strokeWeight(selected ? 4 : 1);
        rect(x, y, w, h, 8);

        // Mini map preview, centered near the top of the card
        float pw = 64, ph = 44;
        drawMapPreview(idx, x + (w - pw) / 2f, y + 7, pw, ph);

        // Name
        textAlign(CENTER, TOP);
        fill(selected ? color(255, 220, 60) : 255);
        textSize(12);
        text(GameConfig.MAP_NAMES[idx], x + w / 2f, y + 55);

        // Compact gravity/friction readout
        fill(170);
        textSize(9);
        textAlign(CENTER, TOP);
        String grav = GameConfig.MAP_GRAVITY[idx] < 0.15f ? "Low Gravity"
                : (GameConfig.MAP_GRAVITY[idx] > 0.35f ? "Heavy Gravity" : "Normal Gravity");
        String fric = GameConfig.MAP_DRAG[idx] > 0.995f ? "Icy"
                : (GameConfig.MAP_DRAG[idx] < 0.96f ? "Sticky" : "Normal Friction");
        text(grav, x + w / 2f, y + 74);
        text(fric, x + w / 2f, y + 87);

        // Selection indicator
        if (selected) {
            fill(255, 220, 60);
            textSize(10);
            textAlign(CENTER, BOTTOM);
            text("SELECTED", x + w / 2f, y + h - 4);
        }
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

    // Draws a red EXIT button at the given position (in virtual/800x600
    // space) that quits the whole program when clicked.
    private void drawExitButton(float x, float y) {
        boolean hover = vMouseX >= x && vMouseX <= x + exitBtnW && vMouseY >= y && vMouseY <= y + exitBtnH;

        rectMode(CORNER);
        fill(hover ? color(255, 80, 80) : color(190, 50, 50));
        stroke(255);
        strokeWeight(2);
        rect(x, y, exitBtnW, exitBtnH, 8);

        fill(255);
        textAlign(CENTER, CENTER);
        textSize(16);
        text("EXIT", x + exitBtnW / 2f, y + exitBtnH / 2f);
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
        drawOptionSelector("Color", GameConfig.COLOR_NAMES[colorIndex[pIndex]], x + 20, y + 100);
        drawOptionSelector("Accessory", GameConfig.ACCESSORY_NAMES[accessoryIndex[pIndex]], x + 20, y + 170);
        drawOptionSelector("Weapon", GameConfig.WEAPON_NAMES[weaponIndex[pIndex]], x + 20, y + 240);

        // Preview Box
        fill(20);
        stroke(80);
        rect(x + 20, y + 310, 260, 65, 5);

        // Draw live preview ball
        pushMatrix();
        translate(x + 150, y + 342, 10);
        Ball tempBall = new Ball(this, 0, 0, GameConfig.COLORS[colorIndex[pIndex]], accessoryIndex[pIndex], weaponIndex[pIndex], classIndex[pIndex]);
        tempBall.draw(this, false, "");
        popMatrix();
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
        pushMatrix();
        translate(viewOffsetX, viewOffsetY);
        scale(viewScale);

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

        popMatrix();
        hint(ENABLE_DEPTH_TEST);
    }

    private void drawStatsHUD() {
        hint(DISABLE_DEPTH_TEST);
        camera();
        noLights();
        pushMatrix();
        translate(viewOffsetX, viewOffsetY);
        scale(viewScale);

        rectMode(CORNER);
        drawPlayerStatsBox(15, 15, nameBall1, ball1, GameConfig.COLORS[colorIndex[0]]);
        drawPlayerStatsBox(GameConfig.SCREEN_WIDTH - 175, 15, nameBall2, ball2, GameConfig.COLORS[colorIndex[1]]);

        drawExitButton(exitGameX, exitGameY);

        popMatrix();
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
        float currentDmg = baseDmg * b.dmgMultiplier * b.classDamageMult;
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
        pushMatrix();
        translate(viewOffsetX, viewOffsetY);
        scale(viewScale);

        rectMode(CORNER);
        fill(0, 0, 0, 180);
        rect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_LENGTH);

        textAlign(CENTER, CENTER);
        textSize(44);
        fill(255);
        text(winnerText, GameConfig.SCREEN_WIDTH / 2f, 220);

        if (vMouseX >= btnX && vMouseX <= btnX + btnW && vMouseY >= btnY && vMouseY <= btnY + btnH) {
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

        popMatrix();
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
        updateVirtualMouse();

        // Exit button - same hit-test everywhere it's drawn, regardless
        // of which screen/state we're on.
        boolean inGame = (state == GameState.PLAYING || state == GameState.GAME_OVER);
        float ex = inGame ? exitGameX : exitMenuX;
        float ey = inGame ? exitGameY : exitMenuY;
        if (vMouseX >= ex && vMouseX <= ex + exitBtnW && vMouseY >= ey && vMouseY <= ey + exitBtnH) {
            exit();
            return;
        }

        if (state == GameState.SETUP) {
            // Player Box Focus
            if (vMouseX >= 50 && vMouseX <= 350 && vMouseY >= 90 && vMouseY <= 480) activeInput = 1;
            else if (vMouseX >= 450 && vMouseX <= 750 && vMouseY >= 90 && vMouseY <= 480) activeInput = 2;

            // Handle Customization Arrow Clicks
            handleCustomizationClick(0, 50, 90);
            handleCustomizationClick(1, 450, 90);

            // Next Button Click -> Class Select
            if (vMouseX >= btnX && vMouseX <= btnX + btnW && vMouseY >= btnY && vMouseY <= btnY + btnH) {
                if (nameBall1.trim().isEmpty()) nameBall1 = "P1";
                if (nameBall2.trim().isEmpty()) nameBall2 = "P2";
                state = GameState.CLASS_SELECT;
            }
        } else if (state == GameState.CLASS_SELECT) {
            // Class card clicks
            float classCardW = 150;
            float classCardH = 160;
            float colGap = 10;
            float rowGap = 20;
            float startX = 20;
            float startY = 110;

            for (int i = 0; i < GameConfig.CLASS_NAMES.length; i++) {
                int col = i % 4;
                int row = i / 4;
                float cx = startX + col * (classCardW + colGap);
                float cy = startY + row * (classCardH + rowGap);

                if (vMouseX >= cx && vMouseX <= cx + classCardW && vMouseY >= cy && vMouseY <= cy + classCardH) {
                    // Toggle class for both players (cycles through options)
                    if (classIndex[0] == i && classIndex[1] == i) {
                        classIndex[0] = i;
                        classIndex[1] = (i + 1) % GameConfig.CLASS_NAMES.length;
                    } else if (classIndex[0] == i) {
                        classIndex[0] = (i + 1) % GameConfig.CLASS_NAMES.length;
                    } else if (classIndex[1] == i) {
                        classIndex[1] = (i + 1) % GameConfig.CLASS_NAMES.length;
                    } else {
                        classIndex[0] = i;
                    }
                }
            }

            // Back button
            if (vMouseX >= backX && vMouseX <= backX + backW && vMouseY >= backY && vMouseY <= backY + backH) {
                state = GameState.SETUP;
            }

            // Next button -> Map Select
            if (vMouseX >= btnX && vMouseX <= btnX + btnW && vMouseY >= btnY && vMouseY <= btnY + btnH) {
                state = GameState.MAP_SELECT;
            }
        } else if (state == GameState.MAP_SELECT) {
            // Map cards (5 columns x 3 rows)
            for (int i = 0; i < GameConfig.MAP_NAMES.length; i++) {
                int col = i % mapCols;
                int row = i / mapCols;
                float cx = mapStartX + col * (mapCardW + mapColGap);
                float cy = mapStartY + row * (mapCardH + mapRowGap);

                if (vMouseX >= cx && vMouseX <= cx + mapCardW && vMouseY >= cy && vMouseY <= cy + mapCardH) {
                    mapIndex = i;
                }
            }

            // Back
            if (vMouseX >= backX && vMouseX <= backX + backW && vMouseY >= backY && vMouseY <= backY + backH) {
                state = GameState.CLASS_SELECT;
            }

            // Start
            if (vMouseX >= btnX && vMouseX <= btnX + btnW && vMouseY >= btnY && vMouseY <= btnY + btnH) {
                startMatch();
            }
        } else if (state == GameState.GAME_OVER) {
            if (vMouseX >= btnX && vMouseX <= btnX + btnW && vMouseY >= btnY && vMouseY <= btnY + btnH) {
                state = GameState.SETUP;
                resetGame();
            }
        }
    }

    private void handleCustomizationClick(int p, float x, float y) {
        // Color row arrows
        if (vMouseY >= y + 112 && vMouseY <= y + 142) {
            if (vMouseX >= x + 20 && vMouseX <= x + 40) colorIndex[p] = (colorIndex[p] - 1 + GameConfig.COLORS.length) % GameConfig.COLORS.length;
            if (vMouseX >= x + 240 && vMouseX <= x + 260) colorIndex[p] = (colorIndex[p] + 1) % GameConfig.COLORS.length;
        }
        // Accessory row arrows
        if (vMouseY >= y + 182 && vMouseY <= y + 212) {
            if (vMouseX >= x + 20 && vMouseX <= x + 40) accessoryIndex[p] = (accessoryIndex[p] - 1 + GameConfig.ACCESSORY_NAMES.length) % GameConfig.ACCESSORY_NAMES.length;
            if (vMouseX >= x + 240 && vMouseX <= x + 260) accessoryIndex[p] = (accessoryIndex[p] + 1) % GameConfig.ACCESSORY_NAMES.length;
        }
        // Weapon row arrows
        if (vMouseY >= y + 252 && vMouseY <= y + 282) {
            if (vMouseX >= x + 20 && vMouseX <= x + 40) weaponIndex[p] = (weaponIndex[p] - 1 + GameConfig.WEAPON_NAMES.length) % GameConfig.WEAPON_NAMES.length;
            if (vMouseX >= x + 240 && vMouseX <= x + 260) weaponIndex[p] = (weaponIndex[p] + 1) % GameConfig.WEAPON_NAMES.length;
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
                state = GameState.CLASS_SELECT;
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

        } else if (state == GameState.CLASS_SELECT) {
            if (keyCode == LEFT) {
                classIndex[0] = (classIndex[0] - 1 + GameConfig.CLASS_NAMES.length) % GameConfig.CLASS_NAMES.length;
            } else if (keyCode == RIGHT) {
                classIndex[0] = (classIndex[0] + 1) % GameConfig.CLASS_NAMES.length;
            } else if (keyCode == UP) {
                classIndex[1] = (classIndex[1] - 1 + GameConfig.CLASS_NAMES.length) % GameConfig.CLASS_NAMES.length;
            } else if (keyCode == DOWN) {
                classIndex[1] = (classIndex[1] + 1) % GameConfig.CLASS_NAMES.length;
            } else if (key == ENTER || key == RETURN) {
                state = GameState.MAP_SELECT;
            } else if (key == BACKSPACE) {
                state = GameState.SETUP;
            }

        } else if (state == GameState.MAP_SELECT) {
            if (keyCode == LEFT) {
                mapIndex = (mapIndex - 1 + GameConfig.MAP_NAMES.length) % GameConfig.MAP_NAMES.length;
            } else if (keyCode == RIGHT) {
                mapIndex = (mapIndex + 1) % GameConfig.MAP_NAMES.length;
            } else if (keyCode == UP) {
                mapIndex = (mapIndex - mapCols + GameConfig.MAP_NAMES.length) % GameConfig.MAP_NAMES.length;
            } else if (keyCode == DOWN) {
                mapIndex = (mapIndex + mapCols) % GameConfig.MAP_NAMES.length;
            } else if (key == ENTER || key == RETURN) {
                startMatch();
            } else if (key == BACKSPACE) {
                state = GameState.CLASS_SELECT;
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