/**
 * A predefined character: a cosmetic identity (name, flavor text, accent color,
 * and skin) permanently paired with one class's stats. Players pick a character,
 * not a class directly -- picking the character picks its class too.
 */
public class Character {
    public final String name;
    public final String description;
    public final int color;

    // The class stats attached to this character.
    public final float health;
    public final float speedMult;
    public final float damageMult;

    public Character(String name, String description, int color, float health, float speedMult, float damageMult) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.health = health;
        this.speedMult = speedMult;
        this.damageMult = damageMult;
    }
}