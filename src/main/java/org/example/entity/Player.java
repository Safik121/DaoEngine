package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.example.Input;
import org.example.item.Inventory;
import org.example.level.Level;
import org.example.AssetRegistry;
import org.example.ConfigManager;
import org.example.GameConfig;

/**
 * Represents the player entity in the game.
 * Handles player position, size, movement logic, statistics, and rendering.
 */
public class Player extends LivingEntity {

    /** Current Spiritual Energy (Qi). */
    private double qi;
    /** Maximum Qi capacity. */
    private double maxQi = ConfigManager.getInstance().getConfig().player.initialMaxQi;
    /** Whether the player is currently meditating (Qi recovery). */
    private boolean isMeditating = false;


    /** Player's inventory system. */
    private Inventory inventory;
    /** Current active slot in the hotbar (0-4). */
    private int activeHotbarSlot = 0;
    /** Current attack cooldown in frames. */
    private double attackCooldown = 0;
    /** Maximum attack cooldown in frames for UI progress. */
    private double maxAttackCooldown = 1;
    
    /** Current skill (technique) cooldown in frames. */
    private double skillCooldown = 0;
    /** Maximum skill cooldown in frames for UI progress. */
    private double maxSkillCooldown = 1;
    /** Timer (seconds) for cycling through animation frames. */
    private double animationTimer = 0;
    /** The player's currently equipped active technique. */
    private org.example.logic.Skill activeSkill;
    /** Manager for temporary status effects. */
    private org.example.logic.StatusEffectManager statusEffectManager;
    /** Manager for cultivation progression. */
    private org.example.logic.CultivationManager cultivationManager;

    /**
     * Constructs a new Player at the specified starting position.
     * 
     * @param startX Initial X coordinate.
     * @param startY Initial Y coordinate.
     */
    public Player(double startX, double startY) {
        super(startX, startY, 
            ConfigManager.getInstance().getConfig().player.size, 
            ConfigManager.getInstance().getConfig().player.initialMaxHp, 
            ConfigManager.getInstance().getConfig().player.baseSpeed);
        this.qi = maxQi;
        this.inventory = new Inventory();
        this.statusEffectManager = new org.example.logic.StatusEffectManager(this);
        this.cultivationManager = new org.example.logic.CultivationManager();
        this.activeSkill = org.example.logic.SkillRegistry.getSkill("fiery_palm");
    }

    public org.example.logic.StatusEffectManager getStatusEffectManager() {
        return statusEffectManager;
    }
    
    public org.example.logic.StatusEffectManager getBuffManager() {
        return statusEffectManager;
    }

    /**
     * Updates player statistics from external data (e.g. during a Game Load).
     * 
     * @param hp    Current health.
     * @param maxHp Maximum health capacity.
     * @param qi    Current spiritual energy.
     * @param maxQi Maximum spiritual energy capacity.
     */
    public void setStats(double hp, double maxHp, double qi, double maxQi) {
        stats.setMaxHp(maxHp);
        stats.setHp(hp);
        this.qi = qi;
        this.maxQi = maxQi;
    }

    public void setMeditating(boolean meditating) {
        this.isMeditating = meditating;
    }

    /**
     * Updates the player's movement and cooldowns based on input.
     * 
     * @param level     The current level data for collision checks.
     * @param deltaTime Time elapsed since the last frame in seconds.
     */
    public void update(Level level, double deltaTime) {
        animationTimer += deltaTime;
        if (animationTimer > 10.0)
            animationTimer -= 10.0;
            
        // Cooldowns and buffs should process even during meditation
        updateCooldowns(deltaTime);
        statusEffectManager.update(deltaTime); 

        // --- 1. Meditation Logic ---
        // Preserve external state (like the menu) or check for manual intervention
        boolean manualMeditation = Input.isKeyPressed(KeyCode.SPACE);
        boolean activelyMeditating = isMeditating || manualMeditation;
 
        if (activelyMeditating) {
            GameConfig.BalanceConfig bal = ConfigManager.getInstance().getConfig().balance;
            // Regenerate stats during meditation (Section 3.2 of the vision doc)
            if (stats.getHp() < stats.getMaxHp())
                stats.heal(bal.meditationHpRate); // Slow heal
            if (qi < maxQi)
                qi += bal.meditationQiRate; // Faster Qi regen
            return; // Cannot move while meditating
        }

        // --- 2. Tile Effects ---
        int tx = (int) ((x + size / 2) / level.tileSize);
        int ty = (int) ((y + size / 2) / level.tileSize);
        int tileType = 0;
        if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
            tileType = level.data.get(ty).get(tx);
        }

        // Calculate movement speed. Base speed is from LivingEntity.
        double currentSpeed = stats.getSpeed();
        if (tileType == 2)
            currentSpeed *= 0.5; // Water slow
        if (tileType == 3 && qi < maxQi)
            qi += ConfigManager.getInstance().getConfig().balance.spiritVeinQiRate * (deltaTime * 60.0); // Spirit Vein regen

        double moveX = 0;
        double moveY = 0;

        if (Input.isKeyPressed(KeyCode.W) || Input.isKeyPressed(KeyCode.UP))
            moveY -= 1;
        if (Input.isKeyPressed(KeyCode.S) || Input.isKeyPressed(KeyCode.DOWN))
            moveY += 1;
        if (Input.isKeyPressed(KeyCode.A) || Input.isKeyPressed(KeyCode.LEFT)) {
            moveX -= 1;
            facingLeft = true;
        }
        if (Input.isKeyPressed(KeyCode.D) || Input.isKeyPressed(KeyCode.RIGHT)) {
            moveX += 1;
            facingLeft = false;
        }

        if (moveX != 0 || moveY != 0) {
            double length = Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= length;
            moveY /= length;
        }

        double dx = moveX * currentSpeed * (deltaTime * 60.0);
        double dy = moveY * currentSpeed * (deltaTime * 60.0);

        // Apply movement on the X axis if no wall is present
        if (!isSolid(x + dx, y, level)) {
            x += dx;
        }

        // Apply movement on the Y axis if no wall is present
        if (!isSolid(x, y + dy, level)) {
            y += dy;
        }

    }

    /**
     * Checks if a specific position is occupied by a solid tile or is out of
     * bounds.
     * Tests all four corners of the player's bounding box.
     * 
     * @param targetX The target X coordinate to check.
     * @param targetY The target Y coordinate to check.
     * @param level   The current level data.
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
     * @param gc   The GraphicsContext used for drawing.
     * @param cameraX Camera X offset.
     * @param cameraY Camera Y offset.
     */
    @Override
    public void render(GraphicsContext gc, double cameraX, double cameraY) {
        // --- 1. Draw Sprite ---
        String spriteId = "player_idle";

        if (isMeditating) {
            spriteId = "player_meditate";
        } else if (Input.isKeyPressed(KeyCode.W) || Input.isKeyPressed(KeyCode.S) ||
                Input.isKeyPressed(KeyCode.A) || Input.isKeyPressed(KeyCode.D)) {
            spriteId = "player_walk";
        }

        // Calculate frame index
        int frameCount = 1;
        if (spriteId.equals("player_idle")) frameCount = 6;
        else if (spriteId.equals("player_walk")) frameCount = 6;
        else if (spriteId.equals("player_meditate")) frameCount = 2;
        
        int frameIndex = (int) (animationTimer / 0.15) % frameCount;

        javafx.scene.image.Image sprite = AssetRegistry.getSprite(spriteId, frameIndex);
        if (sprite != null) {
            // Calculate dynamic rendering size to maintain aspect ratio
            double spriteW = sprite.getWidth();
            double spriteH = sprite.getHeight();
            double renderW = 32; // Standard hero display width
            double renderH = 32 * (spriteH / spriteW); // Maintain 1:1 aspect ratio
            
            // --- Hero Scaling & Alignment ---
            // We increase the rendering size to 64px (2 tiles) for the hero sprites
            // to compensate for high-resolution sources with significant transparent padding.
            if (spriteId.equals("player_idle") || spriteId.equals("player_walk") || spriteId.equals("player_meditate")) {
                renderW = 64;
                renderH = 64 * (spriteH / spriteW); 
            }

            // Calculations ensure the character's 'feet' are centered at (x, y)
            double ox = x - cameraX - (renderW - size) / 2;
            double oy = y - cameraY - (renderH - size);

            // --- Horizontal Orientation (Flip) ---
            // If facing left, we flip the entire rendering context horizontally.
            if (facingLeft) {
                gc.save();
                gc.translate(ox + renderW, oy);
                gc.scale(-1, 1);
                gc.drawImage(sprite, 0, 0, renderW, renderH);
                gc.restore();
            } else {
                gc.drawImage(sprite, ox, oy, renderW, renderH);
            }
        } else {
            // Fallback to blue square
            gc.setFill(Color.BLUE);
            gc.fillRect(x - cameraX, y - cameraY, size, size);
        }

        // --- 2. Meditation Aura ---
        if (isMeditating) {
            gc.setGlobalAlpha(0.3);
            gc.setFill(Color.LIGHTBLUE);
            gc.fillOval(x - cameraX - size/2, y - cameraY - size/2, size * 2, size * 2);
            gc.setGlobalAlpha(1.0);
        }
    }

    /** @return Player's current X coordinate in pixels. */
    public double getX() {
        return x;
    }

    /** Sets the player's X coordinate. */
    public void setX(double x) {
        this.x = x;
    }

    /** @return Player's current Y coordinate in pixels. */
    public double getY() {
        return y;
    }

    /** Sets the player's Y coordinate. */
    public void setY(double y) {
        this.y = y;
    }

    // Health methods are now handled by LivingEntity/AttributeSet

    /** @return Player's current Spiritual Energy (Qi). */
    public double getQi() {
        return qi;
    }

    /** Sets the player's remaining Qi, clamped between 0 and maxQi. */
    public void setQi(double qi) {
        this.qi = Math.max(0, Math.min(qi, maxQi));
    }

    /** @return Player's maximum Spiritual Energy capacity. */
    public double getMaxQi() {
        return maxQi;
    }
    
    public org.example.logic.CultivationManager getCultivationManager() {
        return cultivationManager;
    }

    /** Sets the player's maximum Qi. */
    public void setMaxQi(double val) {
        this.maxQi = val;
    }

    /** @return true if the player is currently in a meditation state. */
    public boolean isMeditating() {
        return isMeditating;
    }

    /** @return The player's inventory system. */
    public Inventory getInventory() {
        return inventory;
    }

    /** @return The index of the currently active hotbar slot. */
    public int getActiveHotbarSlot() {
        return activeHotbarSlot;
    }

    /** Sets the index of the currently active hotbar slot. */
    public void setActiveHotbarSlot(int slot) {
        this.activeHotbarSlot = slot;
    }

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

    public boolean canUseSkill() {
        return skillCooldown <= 0;
    }

    /**
     * Sets the attack cooldown based on weapon properties.
     * 
     * @param seconds Cooldown time in seconds.
     */
    public void setAttackCooldown(double cooldown) {
        this.attackCooldown = cooldown * 60.0;
        this.maxAttackCooldown = Math.max(1, this.attackCooldown);
    }

    public void setSkillCooldown(double cooldown) {
        this.skillCooldown = cooldown * 60.0;
        this.maxSkillCooldown = Math.max(1, this.skillCooldown);
    }
    
    public double getAttackCooldownRatio() {
        return Math.max(0, Math.min(1.0, attackCooldown / maxAttackCooldown));
    }

    public double getSkillCooldownRatio() {
        return Math.max(0, Math.min(1.0, skillCooldown / maxSkillCooldown));
    }
    
    public org.example.logic.Skill getActiveSkill() {
        return activeSkill;
    }
    
    public void setActiveSkill(org.example.logic.Skill skill) {
        this.activeSkill = skill;
    }

    /**
     * Updates the player's cooldown timers (attack, etc.).
     * 
     * @param deltaTime Time elapsed since the last frame in seconds.
     */
    private void updateCooldowns(double deltaTime) {
        double decrement = (deltaTime * 60.0);
        
        if (attackCooldown > 0) {
            attackCooldown -= decrement;
            if (attackCooldown < 0) attackCooldown = 0;
        }
        
        if (skillCooldown > 0) {
            skillCooldown -= decrement;
            if (skillCooldown < 0) skillCooldown = 0;
        }
    }

    /**
     * Overridden to handle specific player damage logic (currently basic).
     */
    @Override
    public void takeDamage(double amount) {
        super.takeDamage(amount);
    }



    /**
     * Restores the player's Qi by a specific amount, up to max Qi.
     */
    public void restoreQi(double amount) {
        this.qi += amount;
        if (this.qi > maxQi)
            this.qi = maxQi;
    }
}