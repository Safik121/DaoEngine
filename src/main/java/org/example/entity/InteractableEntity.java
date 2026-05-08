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

    public InteractableEntity(double x, double y, String name, Type type) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.type = type;
    }

    public void setSpriteId(String spriteId) {
        this.spriteId = spriteId;
    }

    public void setDialogueTreeId(String dialogueTreeId) {
        this.dialogueTreeId = dialogueTreeId;
    }

    public String getDialogueTreeId() {
        return dialogueTreeId;
    }

    public void setRewardItem(Item item) {
        this.rewardItem = item;
    }

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
    public double getX() { return x; }
    public double getY() { return y; }
    public String getName() { return name; }
    public Type getType() { return type; }
    public Item getRewardItem() { return rewardItem; }
    public String getGiveQuestId() { return giveQuestId; }
    public boolean hasGivenReward() { return hasGivenReward; }
    public void setHasGivenReward(boolean val) { this.hasGivenReward = val; }
}
