package org.example.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single node in a dialogue tree.
 */
public class DialogueNode {
    private String id;
    private String text;
    private List<DialogueChoice> choices;
    private String action; // Optional command like "GIVE_QUEST:kill_slimes"

    /**
     * @param id Unique identifier.
     * @param text The text spoken by the NPC.
     */
    public DialogueNode(String id, String text) {
        this.id = id;
        this.text = text;
        this.choices = new ArrayList<>();
    }

    /** @param choice A choice leading to another node. */
    public void addChoice(DialogueChoice choice) {
        this.choices.add(choice);
    }

    /** @return Unique ID. */
    public String getId() { return id; }
    /** @return Speaker text. */
    public String getText() { return text; }
    /** @return List of available responses. */
    public List<DialogueChoice> getChoices() { return choices; }
    /** @return Side-effect action string. */
    public String getAction() { return action; }
    /** @param action Side-effect command. */
    public void setAction(String action) { this.action = action; }
}
