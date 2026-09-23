class GameConfig {
    // --- Screen ---
    static int SCREEN_WIDTH = 800;
    static int SCREEN_LENGTH = 600;

    // --- Shared physics & settings ---
    static float RADIUS = 30;
    static float BASE_ACCEL = 0.0;
    static float ACCEL_RAMP_RATE = 0.2;
    static float MAX_SPEED = 15.0;
    static float RESTITUTION = 1.0;

    // --- Color palette options ---
    static int[] COLORS = {
            #00C8FF, // Cyan
            #FF5078, // Pink
            #50FF50, // Green
            #FFB400, // Gold
            #A050FF, // Purple
            #FF6400  // Orange
    };
    static String[] COLOR_NAMES = {"Cyan", "Pink", "Green", "Gold", "Purple", "Orange"};
    static String[] ACCESSORY_NAMES = {"Crown", "Top Hat", "Cowboy Hat", "Glasses", "Horns"};
    static String[] WEAPON_NAMES = {"Battleaxe", "Broadsword", "Spear", "Katana", "Warhammer", "Minigun"};

    // [damageMult, rotationSpeedMult] for each weapon
    static float[][] WEAPON_STATS = {
            {1.2, 0.8},   // Battleaxe - high damage, slow
            {1.0, 1.0},   // Broadsword - balanced
            {0.9, 1.2},   // Spear - medium damage, fast
            {0.8, 1.4},   // Katana - low damage, very fast
            {1.3, 0.7},   // Warhammer - very high damage, very slow
            {0.4, 1.1}    // Minigun - very low damage, rapid fire
    };

    // --- CLASS DATA ---
    static String[] CLASS_NAMES = {"Dwarf", "Mage", "Giant", "Rogue", "Paladin", "Berserker", "Knight", "Archer"};
    static String[] CLASS_DESCS = {
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
    static float[][] CLASS_STATS = {
            {80, 1.3, 0.9},    // Dwarf
            {70, 0.8, 1.4},    // Mage
            {140, 0.7, 0.9},   // Giant
            {60, 1.5, 0.8},    // Rogue
            {130, 1.0, 1.0},   // Paladin
            {80, 0.9, 1.3},    // Berserker
            {120, 0.85, 1.0},  // Knight
            {90, 1.2, 0.9}     // Archer
    };
    static int[] CLASS_COLORS = {
            #A67C52,  // Dwarf - brown
            #7B68EE,  // Mage - blue-purple
            #8B4513,  // Giant - dark brown
            #2F4F4F,  // Rogue - dark slate
            #FFD700,  // Paladin - gold
            #DC143C,  // Berserker - crimson
            #696969,  // Knight - dim gray
            #228B22   // Archer - forest green
    };

    // --- MAP DATA ---
    static String[] MAP_NAMES = {
            "Lava Fields", "Frozen Lake", "Stone Pillars", "Volcano Core", "Moon Base",
            "Swamp Marsh", "Crystal Caverns", "Sky Islands", "Desert Dunes", "Toxic Wasteland",
            "Thunderstorm Arena", "Underwater Ruins", "Candy Kingdom", "Space Station", "Neon Grid"
    };
    static String[] MAP_DESCS = {
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

    static int[] MAP_BG = {
            #28231E, #1E3346, #2A2A30, #3A1410, #0A0A1E,
            #1E2A18, #140A28, #2A1E50, #4A3A1E, #1A2410,
            #20242E, #0A1E3C, #3A1E2E, #05050F, #060010
    };
    static int[] HAZARD_FILL = {
            #DC3C00, #0A2A5A, #000000, #FF3C00, #50E050,
            #4B3B22, #5A1EDC, #0A0018, #C8A050, #7CFF00,
            #FFF060, #00E0FF, #FF8CB4, #FF3030, #00FFE0
    };
    static int[] HAZARD_LINE = {
            #FF8C00, #60B0FF, #000000, #FFC800, #B0FF80,
            #7A5C34, #B080FF, #6030A0, #8C6A30, #C8FF80,
            #8080FF, #0080A0, #FFE0F0, #FFFFFF, #FF00E0
    };
    static int[] OBSTACLE_COL = {
            #1E9632, #DCF0FF, #8C8C96, #3C2A28, #787882,
            #2F4B2F, #9AD8FF, #C8B4FF, #6A4A2A, #3C4A28,
            #505864, #5A6A78, #E8385A, #9098A8, #3050FF
    };

    // Physics per map
    static float[] MAP_GRAVITY = {
            0.2725, 0.2725, 0.2725, 0.2725, 0.08,
            0.2725, 0.2725, 0.05, 0.2725, 0.2725,
            0.42, 0.10, 0.2725, 0.02, 0.2725
    };
    static float[] MAP_DRAG = {
            0.985, 0.998, 0.985, 0.985, 0.99,
            0.94, 0.985, 0.995, 0.985, 0.96,
            0.985, 0.90, 0.985, 0.999, 0.998
    };
}