/* global p5 */

class Character {
    constructor(name, description, color, health, speedMult, damageMult) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.health = health;
        this.speedMult = speedMult;
        this.damageMult = damageMult;
    }
}

class GameConfig {
    // --- Screen ---
    static SCREEN_WIDTH = 800;
    static SCREEN_LENGTH = 600;

    // --- Shared physics & settings ---
    static RADIUS = 30;
    static BASE_ACCEL = 0.0;
    static ACCEL_RAMP_RATE = 0.2;
    static MAX_SPEED = 15.0;
    static RESTITUTION = 1.0;

    // --- Color palette options ---
    static COLORS = [
        '#00C8FF', // Cyan
        '#FF5078', // Pink
        '#50FF50', // Green
        '#FFB400', // Gold
        '#A050FF', // Purple
        '#FF6400'  // Orange
    ];
    static COLOR_NAMES = ["Cyan", "Pink", "Green", "Gold", "Purple", "Orange"];
    static ACCESSORY_NAMES = ["Crown", "Top Hat", "Cowboy Hat", "Glasses", "Horns"];

    // Weapon indices
    static PUBLIC_WEAPON_COUNT = 6;
    static WEAPON_GODKILLER = 6;
    static WEAPON_PLUTON = 7;
    static WEAPON_STICK = 8;

    static WEAPON_NAMES = [
        "Battleaxe", "Broadsword", "Spear", "Katana", "Warhammer", "Minigun",
        "The Godkiller", "Pluton", "The Stick"
    ];

    // [damageMult, rotationSpeedMult]
    static WEAPON_STATS = [
        [1.2, 0.8],   // Battleaxe - high damage, slow
        [1.0, 1.0],   // Broadsword - balanced
        [0.9, 1.2],   // Spear - medium damage, fast
        [0.8, 1.4],   // Katana - low damage, very fast
        [1.3, 0.7],   // Warhammer - very high damage, very slow
        [0.4, 1.1],   // Minigun - very low damage, rapid fire
        [3.0, 5.0],   // The Godkiller - secret, absurdly OP
        [3.0, 5.0],   // Pluton - secret, absurdly OP
        [0.1, 0.5]    // The Stick - secret, a joke weapon
    ];

    // --- CHARACTER ROSTER ---
    static CHARACTERS = [
        new Character("Dwarf", "Swift and sturdy.\nFast movement but\nmoderate health.",
            '#A67C52', 80, 1.3, 0.9),
        new Character("Mage", "Master of magic.\nPowerful attacks but\nvery fragile.",
            '#7B68EE', 70, 0.8, 1.4),
        new Character("Giant", "Towering colossus.\nVast health pool but\nslow and heavy.",
            '#8B4513', 140, 0.7, 0.9),
        new Character("Rogue", "Shadow assassin.\nLightning fast but\nextremely fragile.",
            '#2F4F4F', 60, 1.5, 0.8),
        new Character("Paladin", "Balanced warrior.\nStandard strength with\nextra durability.",
            '#FFD700', 130, 1.0, 1.0),
        new Character("Berserker", "Frenzied fighter.\nDeadly attacks but\nlow endurance.",
            '#DC143C', 80, 0.9, 1.3),
        new Character("Knight", "Armored defender.\nSturdier than most\nbut slower.",
            '#696969', 120, 0.85, 1.0),
        new Character("Archer", "Swift marksman.\nQuick and agile with\nmoderate vitality.",
            '#228B22', 90, 1.2, 0.9)
    ];

    // --- MAP DATA ---
    static MAP_NAMES = [
        "Lava Fields", "Frozen Lake", "Stone Pillars", "Volcano Core", "Moon Base",
        "Windy Canyon", "Sky Islands", "Toxic Swamp", "Crystal Caverns", "Desert Dunes",
        "Storm Peaks", "Underwater Reef", "Neon Grid", "Haunted Graveyard", "Sunken Ship"
    ];

    static MAP_DESCS = [
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
    ];

    // Background, hazard fill, hazard outline, obstacle color per map
    static MAP_BG = [
        '#28231E', '#1E3346', '#2A2A30', '#3A1410', '#0A0A1E',
        '#4A3B24', '#1A2A3A', '#1E2A1A', '#241A38', '#3A2E1A',
        '#20242E', '#0A2A3A', '#0A0A20', '#1A1A1E', '#2A2A38'
    ];

    static HAZARD_FILL = [
        '#DC3C00', '#0A2A5A', '#000000', '#FF3C00', '#50E050',
        '#C2A46B', '#0A1A2A', '#4A7A2A', '#000000', '#DCC078',
        '#3A3AFF', '#FFD700', '#000000', '#2A2A2A', '#DC5000'
    ];

    static HAZARD_LINE = [
        '#FF8C00', '#60B0FF', '#000000', '#FFC800', '#B0FF80',
        '#E8D2A0', '#6FA8DC', '#8AFA3A', '#000000', '#F0E0A0',
        '#AAAAFF', '#FFF176', '#000000', '#6A6A6A', '#FFA050'
    ];

    static OBSTACLE_COL = [
        '#1E9632', '#DCF0FF', '#8C8C96', '#3C2A28', '#787882',
        '#8B7355', '#E8F0FF', '#3A2A1A', '#9B6BFF', '#2E7D32',
        '#556070', '#FF7043', '#00FFC8', '#9A9A9A', '#5A4A3A'
    ];

    // Physics per map: gravity, drag, wind
    static MAP_GRAVITY = [
        0.2725, 0.2725, 0.2725, 0.2725, 0.08,
        0.2725, 0.14, 0.2725, 0.2725, 0.2725,
        0.38, 0.12, 0.2725, 0.2725, 0.2725
    ];

    static MAP_DRAG = [
        0.985, 0.998, 0.985, 0.985, 0.99,
        0.985, 0.985, 0.92, 0.985, 0.985,
        0.985, 0.90, 0.985, 0.985, 0.93
    ];

    static MAP_WIND = [
        0, 0, 0, 0, 0,
        0.05, 0.01, 0, 0, -0.05,
        0.07, 0, 0, 0.03, 0
    ];
}