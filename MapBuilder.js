/* global LavaZone, Tree */

class MapBuilder {
    static buildMap(idx, lz, tr) {
        if (idx === 0) { // Lava Fields
            lz.push(new LavaZone(400, 500, 180, 80));
            lz.push(new LavaZone(200, 300, 120, 60));
            lz.push(new LavaZone(600, 300, 120, 60));

            tr.push(new Tree(200, 180, 35));
            tr.push(new Tree(600, 180, 35));
            tr.push(new Tree(400, 250, 40));
            tr.push(new Tree(100, 420, 30));
            tr.push(new Tree(700, 420, 30));
        } else if (idx === 1) { // Frozen Lake
            lz.push(new LavaZone(400, 350, 100, 70));
            lz.push(new LavaZone(180, 450, 80, 50));
            lz.push(new LavaZone(620, 450, 80, 50));

            tr.push(new Tree(400, 180, 30));
            tr.push(new Tree(120, 300, 28));
            tr.push(new Tree(680, 300, 28));
        } else if (idx === 2) { // Stone Pillars
            tr.push(new Tree(250, 200, 30));
            tr.push(new Tree(550, 200, 30));
            tr.push(new Tree(400, 300, 45));
            tr.push(new Tree(250, 400, 30));
            tr.push(new Tree(550, 400, 30));
            tr.push(new Tree(120, 300, 25));
            tr.push(new Tree(680, 300, 25));
            tr.push(new Tree(400, 140, 25));
            tr.push(new Tree(400, 480, 25));
        } else if (idx === 3) { // Volcano Core
            lz.push(new LavaZone(400, 320, 260, 160));
            lz.push(new LavaZone(120, 520, 120, 50));
            lz.push(new LavaZone(680, 520, 120, 50));

            tr.push(new Tree(250, 200, 30));
            tr.push(new Tree(550, 200, 30));
            tr.push(new Tree(200, 400, 28));
            tr.push(new Tree(600, 400, 28));
        } else if (idx === 4) { // Moon Base
            lz.push(new LavaZone(400, 300, 90, 90));
            lz.push(new LavaZone(150, 480, 110, 50));
            lz.push(new LavaZone(650, 480, 110, 50));

            tr.push(new Tree(250, 250, 35));
            tr.push(new Tree(550, 250, 35));
            tr.push(new Tree(400, 470, 30));
            tr.push(new Tree(100, 300, 25));
            tr.push(new Tree(700, 300, 25));
        } else if (idx === 5) { // Windy Canyon
            lz.push(new LavaZone(200, 470, 140, 60));
            lz.push(new LavaZone(600, 470, 140, 60));
            lz.push(new LavaZone(400, 250, 100, 50));

            tr.push(new Tree(120, 350, 25));
            tr.push(new Tree(680, 350, 25));
            tr.push(new Tree(300, 150, 30));
            tr.push(new Tree(500, 150, 30));
        } else if (idx === 6) { // Sky Islands
            lz.push(new LavaZone(250, 450, 90, 60));
            lz.push(new LavaZone(550, 450, 90, 60));
            lz.push(new LavaZone(400, 550, 120, 40));

            tr.push(new Tree(200, 250, 35));
            tr.push(new Tree(600, 250, 35));
            tr.push(new Tree(400, 180, 40));
            tr.push(new Tree(300, 380, 25));
            tr.push(new Tree(500, 380, 25));
        } else if (idx === 7) { // Toxic Swamp
            lz.push(new LavaZone(200, 400, 110, 70));
            lz.push(new LavaZone(600, 400, 110, 70));
            lz.push(new LavaZone(400, 200, 90, 60));
            lz.push(new LavaZone(400, 500, 150, 40));

            tr.push(new Tree(150, 250, 28));
            tr.push(new Tree(650, 250, 28));
            tr.push(new Tree(400, 350, 30));
        } else if (idx === 8) { // Crystal Caverns
            tr.push(new Tree(200, 150, 30));
            tr.push(new Tree(600, 150, 30));
            tr.push(new Tree(200, 450, 30));
            tr.push(new Tree(600, 450, 30));
            tr.push(new Tree(400, 300, 45));
            tr.push(new Tree(120, 300, 22));
            tr.push(new Tree(680, 300, 22));
            tr.push(new Tree(400, 480, 25));
        } else if (idx === 9) { // Desert Dunes
            lz.push(new LavaZone(250, 480, 130, 55));
            lz.push(new LavaZone(550, 480, 130, 55));
            lz.push(new LavaZone(400, 280, 100, 50));

            tr.push(new Tree(150, 350, 22));
            tr.push(new Tree(650, 350, 22));
            tr.push(new Tree(300, 180, 25));
            tr.push(new Tree(500, 180, 25));
            tr.push(new Tree(400, 420, 28));
        } else if (idx === 10) { // Storm Peaks
            lz.push(new LavaZone(200, 250, 90, 60));
            lz.push(new LavaZone(600, 250, 90, 60));
            lz.push(new LavaZone(400, 420, 110, 60));
            lz.push(new LavaZone(400, 150, 70, 40));

            tr.push(new Tree(130, 420, 28));
            tr.push(new Tree(670, 420, 28));
            tr.push(new Tree(300, 320, 25));
            tr.push(new Tree(500, 320, 25));
        } else if (idx === 11) { // Underwater Reef
            lz.push(new LavaZone(250, 350, 100, 60));
            lz.push(new LavaZone(550, 350, 100, 60));
            lz.push(new LavaZone(400, 470, 140, 50));

            tr.push(new Tree(150, 220, 25));
            tr.push(new Tree(650, 220, 25));
            tr.push(new Tree(300, 150, 22));
            tr.push(new Tree(500, 150, 22));
            tr.push(new Tree(400, 280, 32));
            tr.push(new Tree(400, 450, 28));
        } else if (idx === 12) { // Neon Grid
            tr.push(new Tree(200, 200, 25));
            tr.push(new Tree(400, 200, 25));
            tr.push(new Tree(600, 200, 25));
            tr.push(new Tree(200, 350, 25));
            tr.push(new Tree(400, 350, 25));
            tr.push(new Tree(600, 350, 25));
            tr.push(new Tree(200, 500, 25));
            tr.push(new Tree(400, 500, 25));
            tr.push(new Tree(600, 500, 25));
        } else if (idx === 13) { // Haunted Graveyard
            lz.push(new LavaZone(200, 300, 80, 50));
            lz.push(new LavaZone(600, 300, 80, 50));
            lz.push(new LavaZone(300, 470, 80, 50));
            lz.push(new LavaZone(500, 470, 80, 50));

            tr.push(new Tree(150, 180, 20));
            tr.push(new Tree(650, 180, 20));
            tr.push(new Tree(400, 180, 22));
            tr.push(new Tree(250, 400, 20));
            tr.push(new Tree(550, 400, 20));
            tr.push(new Tree(400, 470, 24));
        } else if (idx === 14) { // Sunken Ship
            lz.push(new LavaZone(300, 300, 110, 60));
            lz.push(new LavaZone(500, 300, 110, 60));
            lz.push(new LavaZone(400, 470, 140, 50));

            tr.push(new Tree(150, 250, 28));
            tr.push(new Tree(650, 250, 28));
            tr.push(new Tree(400, 200, 30));
            tr.push(new Tree(250, 420, 24));
            tr.push(new Tree(550, 420, 24));
        }
    }
}