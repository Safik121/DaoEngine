package org.example.logic;

/**
 * Represents a choice the player can make during a dialogue.
 */
public class DialogueChoice {
    private String text;
    private String nextNodeId;

    /**
     * @param text Text displayed to the player.
     * @param nextNodeId ID of the node to jump to if chosen.
     */
    public DialogueChoice(String text, String nextNodeId) {
        this.text = text;
        this.nextNodeId = nextNodeId;
    }

    /** @return The choice label. */
    public String getText() { return text; }
    /** @return The destination node ID. */
    public String getNextNodeId() { return nextNodeId; }
}
