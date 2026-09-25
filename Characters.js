/**
 * A predefined character: a cosmetic identity (name, flavor text, accent color,
 * and skin) permanently paired with one class's stats. Players pick a character,
 * not a class directly -- picking the character picks its class too.
 */
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