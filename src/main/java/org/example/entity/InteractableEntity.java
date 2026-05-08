package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.AssetRegistry;
import org.example.item.Item;
import org.example.logic.Interactable;
import org.example.state.PlayState;

/**
 * Represents an interactive entity in the game world, such as an NPC or a Stele.
 * Provides dialogue lines and optional rewards upon interaction.
 */
public class InteractableEntity implements Interactable {
    public enum Type { NPC, STELE }

    private double x, y;
    private String name;
    private Type type;
    private String dialogueTreeId;
    private Item rewardItem;
    private String giveQuestId;
    private boolean hasGivenReward = false;

    private String spriteId;

    /**
     * @param x World X pixel coordinate.
     * @param y World Y pixel coordinate.
     * @param name Display name.
     * @param type NPC or Stele.
     */
    public InteractableEntity(double x, double y, String name, Type type) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.type = type;
    }

    /** @param spriteId The ID of the sprite in AssetRegistry. */
    public void setSpriteId(String spriteId) {
        this.spriteId = spriteId;
    }

    /** @param dialogueTreeId The starting node for the conversation. */
    public void setDialogueTreeId(String dialogueTreeId) {
        this.dialogueTreeId = dialogueTreeId;
    }

    /** @return The starting node ID. */
    public String getDialogueTreeId() {
        return dialogueTreeId;
    }

    /** @param item The item granted after finishing dialogue. */
    public void setRewardItem(Item item) {
        this.rewardItem = item;
    }

    /** @param giveQuestId The quest ID offered by this entity. */
    public void setGiveQuestId(String giveQuestId) {
        this.giveQuestId = giveQuestId;
    }

    @Override
    public void onInteract(PlayState state) {
        state.getDialogManager().startDialogue(this, state);
    }

    @Override
    public String getPrompt() {
        return (type == Type.NPC) ? "[E] Talk" : "[E] Inspect";
    }

    @Override
    public double getInteractionRange() {
        return 70.0;
    }

    /**
     * Renders the interactable entity on the canvas.
     * 
     * @param gc   The GraphicsContext used for drawing.
     * @param camX Current camera X offset.
     * @param camY Current camera Y offset.
     */
    public void render(GraphicsContext gc, double camX, double camY) {
        if (type == Type.NPC) {
            javafx.scene.image.Image npcImg = (spriteId != null) ? AssetRegistry.getSprite(spriteId, 0) : null;

            if (npcImg != null) {
                double drawWidth = 64;
                double drawHeight = 64;
                gc.drawImage(npcImg, x - camX + (16 - drawWidth) / 2, y - camY + (16 - drawHeight) / 2, drawWidth, drawHeight);
            } else {
                // Fallback green square
                gc.setFill(Color.MEDIUMSPRINGGREEN);
                gc.fillRect(x - camX, y - camY, 16, 16);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
                gc.strokeRect(x - camX, y - camY, 16, 16);
            }
        } else {
            javafx.scene.image.Image steleImg = (spriteId != null) ? AssetRegistry.getSprite(spriteId, 0) : null;
            if (steleImg != null) {
                double drawWidth = 96;
                double drawHeight = 144;
                // Center horizontally, and ground it vertically (bottom of the 24px placeholder)
                gc.drawImage(steleImg, x - camX + (12 - drawWidth) / 2, y - camY + 24 - drawHeight, drawWidth, drawHeight);
            } else {
                // Fallback grey pillar
                gc.setFill(Color.SLATEGRAY);
                gc.fillRect(x - camX, y - camY, 12, 24);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(0.5);
                gc.strokeRect(x - camX, y - camY, 12, 24);
            }
        }
    }

    // Getters
    /** @return World X. */
    public double getX() { return x; }
    /** @return World Y. */
    public double getY() { return y; }
    /** @return Entity name. */
    public String getName() { return name; }
    /** @return NPC or STELE. */
    public Type getType() { return type; }
    /** @return The reward item. */
    public Item getRewardItem() { return rewardItem; }
    /** @return The quest ID given. */
    public String getGiveQuestId() { return giveQuestId; }
    /** @return true if reward was already taken. */
    public boolean hasGivenReward() { return hasGivenReward; }
    /** @param val reward taken status. */
    public void setHasGivenReward(boolean val) { this.hasGivenReward = val; }
}
