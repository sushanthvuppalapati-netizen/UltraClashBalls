import java.util.ArrayList;

/**
 * Builds the hazard/obstacle layout for a given map index. Pulled out of
 * the sketch class since it's pure data assembly with no rendering or
 * sketch state involved. Also reused by the map-select screen to preview
 * and count hazards/obstacles.
 *
 * Indices 0-4 are the original 5 maps. Indices 5-14 are 10 new maps,
 * each with a distinct hazard/obstacle layout to match its physics
 * profile in GameConfig (gravity/drag).
 */
public final class MapBuilder {

    private MapBuilder() { }

    public static void buildMap(int idx, ArrayList<LavaZone> lz, ArrayList<Tree> tr) {
        if (idx == 0) { // Lava Fields
            lz.add(new LavaZone(400, 500, 180, 80));
            lz.add(new LavaZone(200, 300, 120, 60));
            lz.add(new LavaZone(600, 300, 120, 60));

            tr.add(new Tree(200, 180, 35));
            tr.add(new Tree(600, 180, 35));
            tr.add(new Tree(400, 250, 40));
            tr.add(new Tree(100, 420, 30));
            tr.add(new Tree(700, 420, 30));
        } else if (idx == 1) { // Frozen Lake
            lz.add(new LavaZone(400, 350, 100, 70));
            lz.add(new LavaZone(180, 450, 80, 50));
            lz.add(new LavaZone(620, 450, 80, 50));

            tr.add(new Tree(400, 180, 30));
            tr.add(new Tree(120, 300, 28));
            tr.add(new Tree(680, 300, 28));
        } else if (idx == 2) { // Stone Pillars
            tr.add(new Tree(250, 200, 30));
            tr.add(new Tree(550, 200, 30));
            tr.add(new Tree(400, 300, 45));
            tr.add(new Tree(250, 400, 30));
            tr.add(new Tree(550, 400, 30));
            tr.add(new Tree(120, 300, 25));
            tr.add(new Tree(680, 300, 25));
            tr.add(new Tree(400, 140, 25));
            tr.add(new Tree(400, 480, 25));
        } else if (idx == 3) { // Volcano Core
            lz.add(new LavaZone(400, 320, 260, 160));
            lz.add(new LavaZone(120, 520, 120, 50));
            lz.add(new LavaZone(680, 520, 120, 50));

            tr.add(new Tree(250, 200, 30));
            tr.add(new Tree(550, 200, 30));
            tr.add(new Tree(200, 400, 28));
            tr.add(new Tree(600, 400, 28));
        } else if (idx == 4) { // Moon Base
            lz.add(new LavaZone(400, 300, 90, 90));
            lz.add(new LavaZone(150, 480, 110, 50));
            lz.add(new LavaZone(650, 480, 110, 50));

            tr.add(new Tree(250, 250, 35));
            tr.add(new Tree(550, 250, 35));
            tr.add(new Tree(400, 470, 30));
            tr.add(new Tree(100, 300, 25));
            tr.add(new Tree(700, 300, 25));
        } else if (idx == 5) { // Swamp Marsh - sticky mud bogs
            lz.add(new LavaZone(200, 220, 140, 90));
            lz.add(new LavaZone(600, 220, 140, 90));
            lz.add(new LavaZone(400, 460, 200, 70));

            tr.add(new Tree(400, 140, 35));
            tr.add(new Tree(120, 420, 28));
            tr.add(new Tree(680, 420, 28));
            tr.add(new Tree(300, 340, 22));
            tr.add(new Tree(500, 340, 22));
        } else if (idx == 6) { // Crystal Caverns - scattered energy shards, stalagmite maze
            lz.add(new LavaZone(400, 300, 60, 60));
            lz.add(new LavaZone(200, 480, 70, 40));
            lz.add(new LavaZone(600, 480, 70, 40));

            tr.add(new Tree(150, 200, 30));
            tr.add(new Tree(650, 200, 30));
            tr.add(new Tree(400, 150, 35));
            tr.add(new Tree(280, 380, 25));
            tr.add(new Tree(520, 380, 25));
            tr.add(new Tree(400, 480, 28));
        } else if (idx == 7) { // Sky Islands - void gaps, floating rocks
            lz.add(new LavaZone(250, 500, 150, 50));
            lz.add(new LavaZone(550, 500, 150, 50));
            lz.add(new LavaZone(400, 260, 90, 40));

            tr.add(new Tree(150, 250, 40));
            tr.add(new Tree(650, 250, 40));
            tr.add(new Tree(400, 160, 40));
            tr.add(new Tree(400, 400, 28));
        } else if (idx == 8) { // Desert Dunes - quicksand pits
            lz.add(new LavaZone(250, 250, 130, 70));
            lz.add(new LavaZone(550, 250, 130, 70));
            lz.add(new LavaZone(400, 480, 160, 60));

            tr.add(new Tree(150, 450, 25));
            tr.add(new Tree(650, 450, 25));
            tr.add(new Tree(400, 170, 28));
        } else if (idx == 9) { // Toxic Wasteland - one huge acid pit
            lz.add(new LavaZone(400, 300, 220, 130));
            lz.add(new LavaZone(150, 500, 100, 50));
            lz.add(new LavaZone(650, 500, 100, 50));

            tr.add(new Tree(250, 170, 28));
            tr.add(new Tree(550, 170, 28));
        } else if (idx == 10) { // Thunderstorm Arena - electrified puddles, broken pillars
            lz.add(new LavaZone(200, 350, 90, 90));
            lz.add(new LavaZone(600, 350, 90, 90));
            lz.add(new LavaZone(400, 490, 130, 50));

            tr.add(new Tree(400, 170, 35));
            tr.add(new Tree(150, 200, 25));
            tr.add(new Tree(650, 200, 25));
        } else if (idx == 11) { // Underwater Ruins - eel zones, ancient pillars
            lz.add(new LavaZone(400, 320, 100, 100));
            lz.add(new LavaZone(180, 480, 80, 50));
            lz.add(new LavaZone(620, 480, 80, 50));

            tr.add(new Tree(150, 220, 30));
            tr.add(new Tree(650, 220, 30));
            tr.add(new Tree(400, 160, 35));
            tr.add(new Tree(300, 400, 25));
            tr.add(new Tree(500, 400, 25));
        } else if (idx == 12) { // Candy Kingdom - hot caramel pools, candy pillars
            lz.add(new LavaZone(400, 300, 120, 80));
            lz.add(new LavaZone(200, 480, 90, 50));
            lz.add(new LavaZone(600, 480, 90, 50));

            tr.add(new Tree(250, 190, 30));
            tr.add(new Tree(550, 190, 30));
            tr.add(new Tree(400, 140, 25));
        } else if (idx == 13) { // Space Station - vacuum leaks, cargo crates
            lz.add(new LavaZone(400, 300, 70, 70));
            lz.add(new LavaZone(180, 200, 60, 60));
            lz.add(new LavaZone(620, 200, 60, 60));

            tr.add(new Tree(300, 460, 28));
            tr.add(new Tree(500, 460, 28));
            tr.add(new Tree(400, 170, 30));
        } else if (idx == 14) { // Neon Grid - laser-grid hazard cross, center pillar
            lz.add(new LavaZone(400, 200, 300, 15));
            lz.add(new LavaZone(400, 420, 300, 15));
            lz.add(new LavaZone(220, 310, 15, 220));
            lz.add(new LavaZone(580, 310, 15, 220));

            tr.add(new Tree(400, 310, 35));
        }
    }
}