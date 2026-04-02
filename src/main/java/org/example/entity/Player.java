package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.example.Input;
import org.example.item.Inventory;
import org.example.level.Level;
import org.example.AssetRegistry;
import java.util.List;

/**
 * Represents the player entity in the game.
 * Handles player position, size, movement logic, statistics, and rendering.
 */
public class Player {
    /** The X coordinate of the player in pixels. */
    private double x;
    /** The Y coordinate of the player in pixels. */
    private double y;
    /** The size of the player entity. */
    private double size;

    /** Current Health Points (HP). */
    private double hp;
    /** Maximum Health Points. */
    private double maxHp = 100.0;
    /** Current Spiritual Energy (Qi). */
    private double qi;
    /** Maximum Qi capacity. */
    private double maxQi = 50.0;
    /** Whether the player is currently meditating. */
    private boolean isMeditating = false;

    /** Player's inventory system. */
    private Inventory inventory;
    /** Current active slot in the hotbar (0-4). */
    private int activeHotbarSlot = 0;
    /** Current attack cooldown in frames. */
    private double attackCooldown = 0;
    /** Timer (seconds) for cycling through animation frames. */
    private double animationTimer = 0;

    /**
     * Constructs a new Player at the specified starting position.
     * 
     * @param startX Initial X coordinate.
     * @param startY Initial Y coordinate.
     */
    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.size = 12;
        this.hp = maxHp;
        this.qi = maxQi;
        this.inventory = new Inventory();
    }

    /**
     * Updates the player's movement and cooldowns based on input.
     * 
     * @param level The current level data for collision checks.
     * @param deltaTime Time elapsed since the last frame in seconds.
     */
    public void update(Level level, double deltaTime) {
        animationTimer += deltaTime;
        if (animationTimer > 10.0) animationTimer -= 10.0;

        // --- 1. Meditation Logic ---
        isMeditating = Input.isKeyPressed(KeyCode.SPACE);
        
        if (isMeditating) {
            // Regenerate stats during meditation (Section 3.2 of the vision doc)
            if (hp < maxHp) hp += 0.1; // Slow heal
            if (qi < maxQi) qi += 0.2; // Faster Qi regen
            return; // Cannot move while meditating
        }

        // --- 2. Tile Effects ---
        int tx = (int) ((x + size / 2) / level.tileSize);
        int ty = (int) ((y + size / 2) / level.tileSize);
        int tileType = 0;
        if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
            tileType = level.data.get(ty).get(tx);
        }

        double speed = 3.0;
        if (tileType == 2) speed *= 0.5; // Water slow
        if (tileType == 3 && qi < maxQi) qi += 0.05 * (deltaTime * 60.0); // Spirit Vein regen

        double moveX = 0;
        double moveY = 0;

        if (Input.isKeyPressed(KeyCode.W) || Input.isKeyPressed(KeyCode.UP)) moveY -= 1;
        if (Input.isKeyPressed(KeyCode.S) || Input.isKeyPressed(KeyCode.DOWN)) moveY += 1;
        if (Input.isKeyPressed(KeyCode.A) || Input.isKeyPressed(KeyCode.LEFT)) moveX -= 1;
        if (Input.isKeyPressed(KeyCode.D) || Input.isKeyPressed(KeyCode.RIGHT)) moveX += 1;

        if (moveX != 0 || moveY != 0) {
            double length = Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= length;
            moveY /= length;
        }

        double dx = moveX * speed * (deltaTime * 60.0);
        double dy = moveY * speed * (deltaTime * 60.0);

        // Apply movement on the X axis if no wall is present
        if (!isSolid(x + dx, y, level)) {
            x += dx;
        }

        // Apply movement on the Y axis if no wall is present
        if (!isSolid(x, y + dy, level)) {
            y += dy;
        }

        updateCooldowns(deltaTime);
    }

    /**
     * Checks if a specific position is occupied by a solid tile or is out of bounds.
     * Tests all four corners of the player's bounding box.
     * 
     * @param targetX The target X coordinate to check.
     * @param targetY The target Y coordinate to check.
     * @param level The current level data.
     * @return true if the position is solid/blocked, false otherwise.
     */
    private boolean isSolid(double targetX, double targetY, Level level) {
        int leftCol = (int) (targetX / level.tileSize);
        int rightCol = (int) ((targetX + size - 0.1) / level.tileSize);
        int topRow = (int) (targetY / level.tileSize);
        int bottomRow = (int) ((targetY + size - 0.1) / level.tileSize);

        // 1. Boundary check
        if (leftCol < 0 || rightCol >= level.width || topRow < 0 || bottomRow >= level.height) {
            return true;
        }

        // 2. Grid check: collision if any of the corners hit a solid tile (value 1)
        if (level.data.get(topRow).get(leftCol) == 1 ||
                level.data.get(topRow).get(rightCol) == 1 ||
                level.data.get(bottomRow).get(leftCol) == 1 ||
                level.data.get(bottomRow).get(rightCol) == 1) {
            return true;
        }

        return false;
    }

    /**
     * Renders the player entity using the provided GraphicsContext.
     * Selects the correct sprite from AssetRegistry based on player state.
     * 
     * @param gc The GraphicsContext used for drawing.
     * @param camX Camera X offset.
     * @param camY Camera Y offset.
     */
    public void render(GraphicsContext gc, double camX, double camY) {
        // --- 1. Draw Sprite ---
        String spriteId = "player_idle";
        
        if (isMeditating) {
            spriteId = "player_meditate";
        } else if (Input.isKeyPressed(KeyCode.W) || Input.isKeyPressed(KeyCode.S) || 
                   Input.isKeyPressed(KeyCode.A) || Input.isKeyPressed(KeyCode.D)) {
            spriteId = "player_walk";
        }

        // Calculate frame index
        int frameCount = spriteId.equals("player_walk") ? 4 : 1;
        int frameIndex = (int) (animationTimer / 0.15) % frameCount;
        
        javafx.scene.image.Image sprite = AssetRegistry.getSprite(spriteId, frameIndex);
        if (sprite != null) {
            gc.drawImage(sprite, x - camX, y - camY, size, size);
        } else {
            // Fallback to blue square
            gc.setFill(Color.BLUE);
            gc.fillRect(x - camX, y - camY, size, size);
        }

        // --- 2. Meditation Aura ---
        if (isMeditating) {
            gc.setGlobalAlpha(0.3);
            gc.setFill(Color.LIGHTBLUE);
            gc.fillOval(x - camX - 5, y - camY - 5, size + 10, size + 10);
            gc.setGlobalAlpha(1.0);
        }
    }

    /** @return Player's current X coordinate in pixels. */
    public double getX() { return x; }
    /** Sets the player's X coordinate. */
    public void setX(double x) { this.x = x; }
    /** @return Player's current Y coordinate in pixels. */
    public double getY() { return y; }
    /** Sets the player's Y coordinate. */
    public void setY(double y) { this.y = y; }
    /** @return Player's current Health Points. */
    public double getHp() { return hp; }
    /** @return Player's maximum Health Points. */
    public double getMaxHp() { return maxHp; }
    /** Sets the player's maximum HP. */
    public void setMaxHp(double val) { this.maxHp = val; }
    /** @return Player's current Spiritual Energy (Qi). */
    public double getQi() { return qi; }
    /** @return Player's maximum Spiritual Energy capacity. */
    public double getMaxQi() { return maxQi; }
    /** Sets the player's maximum Qi. */
    public void setMaxQi(double val) { this.maxQi = val; }
    /** @return true if the player is currently in a meditation state. */
    public boolean isMeditating() { return isMeditating; }
    /** @return The player's inventory system. */
    public Inventory getInventory() { return inventory; }
    /** @return The index of the currently active hotbar slot. */
    public int getActiveHotbarSlot() { return activeHotbarSlot; }
    /** Sets the index of the currently active hotbar slot. */
    public void setActiveHotbarSlot(int slot) { this.activeHotbarSlot = slot; }

    /**
     * Spends Qi to perform an action.
     * 
     * @param amount The amount of Qi to spend.
     * @return true if player had enough Qi, false otherwise.
     */
    public boolean spendQi(double amount) {
        if (qi >= amount) {
            qi -= amount;
            return true;
        }
        return false;
    }

    /**
     * Checks if the player is ready to attack.
     * 
     * @return true if cooldown is zero.
     */
    public boolean canAttack() {
        return attackCooldown <= 0;
    }

    /**
     * Sets the attack cooldown based on weapon properties.
     * 
     * @param seconds Cooldown time in seconds.
     */
    public void setAttackCooldown(double seconds) {
        this.attackCooldown = seconds * 60.0; // Changed to double
    }

    /**
     * Updates the player's cooldown timers (attack, etc.).
     * 
     * @param deltaTime Time elapsed since the last frame in seconds.
     */
    private void updateCooldowns(double deltaTime) {
        if (attackCooldown > 0) {
            attackCooldown -= (deltaTime * 60.0);
            if (attackCooldown < 0) attackCooldown = 0;
        }
    }

    /**
     * Applies damage to the player. HP will not drop below 0.
     * 
     * @param amount The amount of damage to take.
     */
    public void takeDamage(double amount) {
        this.hp -= amount;
        if (this.hp < 0) this.hp = 0;
    }

    /**
     * Heals the player by a specific amount, up to max HP.
     */
    public void heal(double amount) {
        this.hp += amount;
        if (this.hp > maxHp) this.hp = maxHp;
    }

    /**
     * Restores the player's Qi by a specific amount, up to max Qi.
     */
    public void restoreQi(double amount) {
        this.qi += amount;
        if (this.qi > maxQi) this.qi = maxQi;
    }
}