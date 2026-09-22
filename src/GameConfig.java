/**
 * All the static, never-changing game data and physics constants.
 * Pulled out of the old "main" god-class so every other class can
 * reference it without needing an instance of the sketch.
 */
public final class GameConfig {

    private GameConfig() { } // no instances

    // --- Screen ---
    // This is the LOGICAL/virtual resolution the whole game is designed
    // and laid out in. Main.java runs fullscreen at the device's actual
    // resolution and scales this virtual canvas to fit (letterboxed),
    // so none of the layout code below needs to change.
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
    public static final String[] WEAPON_NAMES = {"Battleaxe", "Broadsword", "Spear", "Katana", "Warhammer", "Minigun"};

    // [damageMult, rotationSpeedMult] for each weapon
    public static final float[][] WEAPON_STATS = {
            {1.2f, 0.8f},   // Battleaxe - high damage, slow
            {1.0f, 1.0f},   // Broadsword - balanced
            {0.9f, 1.2f},   // Spear - medium damage, fast
            {0.8f, 1.4f},   // Katana - low damage, very fast
            {1.3f, 0.7f},   // Warhammer - very high damage, very slow
            {0.4f, 1.1f}    // Minigun - very low damage, rapid fire
    };

    // --- CLASS DATA ---
    public static final String[] CLASS_NAMES = {"Dwarf", "Mage", "Giant", "Rogue", "Paladin", "Berserker", "Knight", "Archer"};
    public static final String[] CLASS_DESCS = {
            "Swift and sturdy.\nFast movement but\nmoderate health.",
            "Master of magic.\nPowerful attacks but\nvery fragile.",
            "Towering colossus.\nVast health pool but\nslow and heavy.",
            "Shadow assassin.\nLightning fast but\nextremely fragile.",
            "Balanced warrior.\nStandard strength with\nextra durability.",
            "Frenzied fighter.\nDeadly attacks but\nlow endurance.",
            "Armored defender.\nSturdier than most\nbut slower.",
            "Swift marksman.\nQuick and agile with\nmoderate vitality."
    };
    // [health, speedMult, damageMult] for each class
    public static final float[][] CLASS_STATS = {
            {80, 1.3f, 0.9f},    // Dwarf
            {70, 0.8f, 1.4f},    // Mage
            {140, 0.7f, 0.9f},   // Giant
            {60, 1.5f, 0.8f},    // Rogue
            {130, 1.0f, 1.0f},   // Paladin
            {80, 0.9f, 1.3f},    // Berserker
            {120, 0.85f, 1.0f},  // Knight
            {90, 1.2f, 0.9f}     // Archer
    };
    public static final int[] CLASS_COLORS = {
            0xFFA67C52,  // Dwarf - brown
            0xFF7B68EE,  // Mage - blue-purple
            0xFF8B4513,  // Giant - dark brown
            0xFF2F4F4F,  // Rogue - dark slate
            0xFFFFD700,  // Paladin - gold
            0xFFDC143C,  // Berserker - crimson
            0xFF696969,  // Knight - dim gray
            0xFF228B22   // Archer - forest green
    };

    // --- MAP DATA ---
    // Indices 0-4 are the original maps; 5-14 are 10 new maps, each with
    // its own hazard/obstacle layout (see MapBuilder) plus a distinct
    // gravity/friction combo so it plays differently, not just re-skinned.
    public static final String[] MAP_NAMES = {
            "Lava Fields", "Frozen Lake", "Stone Pillars", "Volcano Core", "Moon Base",
            "Swamp Marsh", "Crystal Caverns", "Sky Islands", "Desert Dunes", "Toxic Wasteland",
            "Thunderstorm Arena", "Underwater Ruins", "Candy Kingdom", "Space Station", "Neon Grid"
    };
    public static final String[] MAP_DESCS = {
            "The classic arena.\nLava pools and\nleafy trees.",
            "Slippery ice!\nVery low friction.\nWatch the icy holes.",
            "A dense maze of\nstone pillars.\nNo hazards.",
            "One huge lava pit\nin the middle.\nStay off the floor!",
            "Low gravity.\nFloaty movement and\nacid pools.",
            "Sticky mud everywhere.\nHigh friction slows\nyou down fast.",
            "A glittering maze\nof stalagmites and\nenergy shards.",
            "Floating islands in\nthe void. Extremely\nlow gravity.",
            "Scorching sand with\nhidden quicksand\npits.",
            "A radioactive bog.\nOne huge acid pit\ndominates center.",
            "Heavy storm gravity\nand electrified\npuddles.",
            "Sunken city ruins.\nWater resistance\nslows movement.",
            "A sugary dreamland.\nHot caramel pools\nburn on contact.",
            "Zero gravity drift\nthrough a derelict\nstation.",
            "A digital arena of\nlaser grids. Very\nslick floors."
    };
    // Background, hazard fill, hazard outline, obstacle color per map
    public static final int[] MAP_BG = {
            0xFF28231E, 0xFF1E3346, 0xFF2A2A30, 0xFF3A1410, 0xFF0A0A1E,
            0xFF1E2A18, 0xFF140A28, 0xFF2A1E50, 0xFF4A3A1E, 0xFF1A2410,
            0xFF20242E, 0xFF0A1E3C, 0xFF3A1E2E, 0xFF05050F, 0xFF060010
    };
    public static final int[] HAZARD_FILL = {
            0xFFDC3C00, 0xFF0A2A5A, 0xFF000000, 0xFFFF3C00, 0xFF50E050,
            0xFF4B3B22, 0xFF5A1EDC, 0xFF0A0018, 0xFFC8A050, 0xFF7CFF00,
            0xFFFFF060, 0xFF00E0FF, 0xFFFF8CB4, 0xFFFF3030, 0xFF00FFE0
    };
    public static final int[] HAZARD_LINE = {
            0xFFFF8C00, 0xFF60B0FF, 0xFF000000, 0xFFFFC800, 0xFFB0FF80,
            0xFF7A5C34, 0xFFB080FF, 0xFF6030A0, 0xFF8C6A30, 0xFFC8FF80,
            0xFF8080FF, 0xFF0080A0, 0xFFFFE0F0, 0xFFFFFFFF, 0xFFFF00E0
    };
    public static final int[] OBSTACLE_COL = {
            0xFF1E9632, 0xFFDCF0FF, 0xFF8C8C96, 0xFF3C2A28, 0xFF787882,
            0xFF2F4B2F, 0xFF9AD8FF, 0xFFC8B4FF, 0xFF6A4A2A, 0xFF3C4A28,
            0xFF505864, 0xFF5A6A78, 0xFFE8385A, 0xFF9098A8, 0xFF3050FF
    };
    // Physics per map
    public static final float[] MAP_GRAVITY = {
            0.2725f, 0.2725f, 0.2725f, 0.2725f, 0.08f,
            0.2725f, 0.2725f, 0.05f, 0.2725f, 0.2725f,
            0.42f, 0.10f, 0.2725f, 0.02f, 0.2725f
    };
    public static final float[] MAP_DRAG = {
            0.985f, 0.998f, 0.985f, 0.985f, 0.99f,
            0.94f, 0.985f, 0.995f, 0.985f, 0.96f,
            0.985f, 0.90f, 0.985f, 0.999f, 0.998f
    };
}