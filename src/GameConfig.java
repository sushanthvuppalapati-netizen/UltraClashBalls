/**
 * All the static, never-changing game data and physics constants.
 * Pulled out of the old "main" god-class so every other class can
 * reference it without needing an instance of the sketch.
 */
public final class GameConfig {

    private GameConfig() { } // no instances

    // --- Screen ---
    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_LENGTH = 600;

    // --- Shared physics & settings ---
    public static final float RADIUS = 30f;
    public static final float BASE_ACCEL = 0.0f;
    public static final float ACCEL_RAMP_RATE = 0.2f;
    public static final float MAX_SPEED = 15.0f;
    public static final float RESTITUTION = 1.0f;

    // --- Color palette options ---
    public static final int[] COLORS = {
            0xFF00C8FF, // Cyan
            0xFFFF5078, // Pink
            0xFF50FF50, // Green
            0xFFFFB400, // Gold
            0xFFA050FF, // Purple
            0xFFFF6400  // Orange
    };
    public static final String[] COLOR_NAMES = {"Cyan", "Pink", "Green", "Gold", "Purple", "Orange"};
    public static final String[] ACCESSORY_NAMES = {"Crown", "Top Hat", "Cowboy Hat", "Glasses", "Horns"};

    // Indices 0..PUBLIC_WEAPON_COUNT-1 are freely selectable in the customizer.
    // Indices at/after that are secret weapons only granted by typing a specific name
    // (see Main.specialWeaponFor) -- they're never reachable by cycling arrows.
    public static final int PUBLIC_WEAPON_COUNT = 6;
    public static final int WEAPON_GODKILLER = 6;
    public static final int WEAPON_PLUTON = 7;
    public static final int WEAPON_STICK = 8;

    public static final String[] WEAPON_NAMES = {
            "Battleaxe", "Broadsword", "Spear", "Katana", "Warhammer", "Minigun",
            "The Godkiller", "Pluton", "The Stick"
    };

    // [damageMult, rotationSpeedMult] for each weapon
    public static final float[][] WEAPON_STATS = {
            {1.2f, 0.8f},   // Battleaxe - high damage, slow
            {1.0f, 1.0f},   // Broadsword - balanced
            {0.9f, 1.2f},   // Spear - medium damage, fast
            {0.8f, 1.4f},   // Katana - low damage, very fast
            {1.3f, 0.7f},   // Warhammer - very high damage, very slow
            {0.4f, 1.1f},   // Minigun - very low damage, rapid fire
            {3.0f, 5.0f},   // The Godkiller - secret, absurdly OP
            {3.0f, 5.0f},   // Pluton - secret, absurdly OP
            {0.1f, 0.5f}    // The Stick - secret, a joke weapon
    };

    // --- CHARACTER ROSTER ---
    // Each character is a fixed (visual skin + class stats) pairing. Players choose
    // a character; they no longer choose a class separately.
    public static final Character[] CHARACTERS = {
            new Character("Dwarf", "Swift and sturdy.\nFast movement but\nmoderate health.",
                    0xFFA67C52, 80, 1.3f, 0.9f),
            new Character("Mage", "Master of magic.\nPowerful attacks but\nvery fragile.",
                    0xFF7B68EE, 70, 0.8f, 1.4f),
            new Character("Giant", "Towering colossus.\nVast health pool but\nslow and heavy.",
                    0xFF8B4513, 140, 0.7f, 0.9f),
            new Character("Rogue", "Shadow assassin.\nLightning fast but\nextremely fragile.",
                    0xFF2F4F4F, 60, 1.5f, 0.8f),
            new Character("Paladin", "Balanced warrior.\nStandard strength with\nextra durability.",
                    0xFFFFD700, 130, 1.0f, 1.0f),
            new Character("Berserker", "Frenzied fighter.\nDeadly attacks but\nlow endurance.",
                    0xFFDC143C, 80, 0.9f, 1.3f),
            new Character("Knight", "Armored defender.\nSturdier than most\nbut slower.",
                    0xFF696969, 120, 0.85f, 1.0f),
            new Character("Archer", "Swift marksman.\nQuick and agile with\nmoderate vitality.",
                    0xFF228B22, 90, 1.2f, 0.9f)
    };

    // --- MAP DATA ---
    public static final String[] MAP_NAMES = {
            "Lava Fields", "Frozen Lake", "Stone Pillars", "Volcano Core", "Moon Base",
            "Windy Canyon", "Sky Islands", "Toxic Swamp", "Crystal Caverns", "Desert Dunes",
            "Storm Peaks", "Underwater Reef", "Neon Grid", "Haunted Graveyard", "Sunken Ship"
    };
    public static final String[] MAP_DESCS = {
            "The classic arena.\nLava pools and\nleafy trees.",
            "Slippery ice!\nVery low friction.\nWatch the icy holes.",
            "A dense maze of\nstone pillars.\nNo hazards.",
            "One huge lava pit\nin the middle.\nStay off the floor!",
            "Low gravity.\nFloaty movement and\nacid pools.",
            "Steady crosswind.\nSand pits slow you.\nRock spires block shots.",
            "Floating platforms.\nVery light gravity.\nMind the fall zones.",
            "Thick mud saps\nyour speed. Toxic\npools poison on contact.",
            "A glittering pillar\nmaze. No hazards,\njust obstacles.",
            "Sand blows from\nthe west. Cacti\nand sinking dunes.",
            "Howling gale and\nheavy gravity.\nLightning pools hurt.",
            "Heavy water drag.\nElectric eel zones\nshock on contact.",
            "Futuristic arena.\nNo hazards, a neat\ngrid of pillars.",
            "A ghostly breeze.\nOpen graves hurt,\ntombstones block.",
            "Waterlogged deck\nslows you down.\nDeck fires still burn."
    };
    // Background, hazard fill, hazard outline, obstacle color per map
    public static final int[] MAP_BG = {
            0xFF28231E, 0xFF1E3346, 0xFF2A2A30, 0xFF3A1410, 0xFF0A0A1E,
            0xFF4A3B24, 0xFF1A2A3A, 0xFF1E2A1A, 0xFF241A38, 0xFF3A2E1A,
            0xFF20242E, 0xFF0A2A3A, 0xFF0A0A20, 0xFF1A1A1E, 0xFF2A2A38
    };
    public static final int[] HAZARD_FILL = {
            0xFFDC3C00, 0xFF0A2A5A, 0xFF000000, 0xFFFF3C00, 0xFF50E050,
            0xFFC2A46B, 0xFF0A1A2A, 0xFF4A7A2A, 0xFF000000, 0xFFDCC078,
            0xFF3A3AFF, 0xFFFFD700, 0xFF000000, 0xFF2A2A2A, 0xFFDC5000
    };
    public static final int[] HAZARD_LINE = {
            0xFFFF8C00, 0xFF60B0FF, 0xFF000000, 0xFFFFC800, 0xFFB0FF80,
            0xFFE8D2A0, 0xFF6FA8DC, 0xFF8AFA3A, 0xFF000000, 0xFFF0E0A0,
            0xFFAAAAFF, 0xFFFFF176, 0xFF000000, 0xFF6A6A6A, 0xFFFFA050
    };
    public static final int[] OBSTACLE_COL = {
            0xFF1E9632, 0xFFDCF0FF, 0xFF8C8C96, 0xFF3C2A28, 0xFF787882,
            0xFF8B7355, 0xFFE8F0FF, 0xFF3A2A1A, 0xFF9B6BFF, 0xFF2E7D32,
            0xFF556070, 0xFFFF7043, 0xFF00FFC8, 0xFF9A9A9A, 0xFF5A4A3A
    };
    // Physics per map: gravity, drag (friction; closer to 1 = more slippery), wind (constant sideways push)
    public static final float[] MAP_GRAVITY = {
            0.2725f, 0.2725f, 0.2725f, 0.2725f, 0.08f,
            0.2725f, 0.14f, 0.2725f, 0.2725f, 0.2725f,
            0.38f, 0.12f, 0.2725f, 0.2725f, 0.2725f
    };
    public static final float[] MAP_DRAG = {
            0.985f, 0.998f, 0.985f, 0.985f, 0.99f,
            0.985f, 0.985f, 0.92f, 0.985f, 0.985f,
            0.985f, 0.90f, 0.985f, 0.985f, 0.93f
    };
    public static final float[] MAP_WIND = {
            0f, 0f, 0f, 0f, 0f,
            0.05f, 0.01f, 0f, 0f, -0.05f,
            0.07f, 0f, 0f, 0.03f, 0f
    };
}