package org.example.logic;

/**
 * Represents a choice the player can make during a dialogue.
 */
public class DialogueChoice {
    private String text;
    private String nextNodeId;

    public DialogueChoice(String text, String nextNodeId) {
        this.text = text;
        this.nextNodeId = nextNodeId;
    }

    public String getText() { return text; }
    public String getNextNodeId() { return nextNodeId; }
}
