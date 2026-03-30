package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import org.example.item.Item;

/**
 * Represents an interactive entity in the game world, such as an NPC or a Stele.
 * Provides dialogue lines and optional rewards upon interaction.
 */
public class InteractableEntity {
    public enum Type { NPC, STELE }

    private double x, y;
    private String name;
    private Type type;
    private List<String> dialogueLines;
    private Item rewardItem;
    private boolean hasGivenReward = false;

    public InteractableEntity(double x, double y, String name, Type type) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.type = type;
        this.dialogueLines = new ArrayList<>();
    }

    /**
     * Adds a line of text to be displayed sequentially during interaction.
     * 
     * @param line The text line to be added.
     */
    public void addDialogue(String line) {
        dialogueLines.add(line);
    }

    /**
     * Sets an optional item to be granted upon completing the dialogue.
     * 
     * @param item The reward item.
     */
    public void setRewardItem(Item item) {
        this.rewardItem = item;
    }

    /**
     * Renders the interactable entity on the canvas.
     * NPCs appear as green squares, while Steles appear as grey pillars.
     * 
     * @param gc   The GraphicsContext used for drawing.
     * @param camX Current camera X offset.
     * @param camY Current camera Y offset.
     */
    public void render(GraphicsContext gc, double camX, double camY) {
        if (type == Type.NPC) {
            gc.setFill(Color.MEDIUMSPRINGGREEN);
            gc.fillRect(x - camX, y - camY, 16, 16);
            // Simple "head"
            gc.strokeRect(x - camX, y - camY, 16, 16);
        } else {
            gc.setFill(Color.SLATEGRAY);
            gc.fillRect(x - camX, y - camY, 12, 24);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(0.5);
            gc.strokeRect(x - camX, y - camY, 12, 24);
        }
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public String getName() { return name; }
    public Type getType() { return type; }
    public List<String> getDialogueLines() { return dialogueLines; }
    public Item getRewardItem() { return rewardItem; }
    public boolean hasGivenReward() { return hasGivenReward; }
    public void setHasGivenReward(boolean val) { this.hasGivenReward = val; }
}
