package org.example.level;

/**
 * Defines the environmental themes for game levels.
 * Each biome affects visuals (tiles) and potentially generation logic.
 */
public enum Biome {
    FOREST("Forest"),
    ICE("Glacial Tundra"),
    FIRE("Volcanic Depths");

    private final String displayName;

    Biome(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Maps a generic tile type ID to a biome-specific sprite ID.
     * 
     * @param tileType The numeric ID of the tile (0=Grass, 1=Wall, 2=Water/Lava, etc.)
     * @return The string ID used for asset registry lookups.
     */
    public String getSpriteId(int tileType) {
        String base = "";
        switch (tileType) {
            case 0: base = "grass"; break;
            case 1: base = "wall"; break;
            case 2: base = this == FIRE ? "lava" : (this == ICE ? "ice" : "water"); break;
            case 3: base = "vein"; break;
            case 4: base = "variety"; break;
            case 5: base = "bridge"; break;
            default: base = "grass"; break;
        }

        // Prefix based on biome
        if (this == FOREST) return "tile_" + base;
        return "tile_" + this.name().toLowerCase() + "_" + base;
    }
}
