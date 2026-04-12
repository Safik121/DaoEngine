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

    public DialogueNode(String id, String text) {
        this.id = id;
        this.text = text;
        this.choices = new ArrayList<>();
    }

    public void addChoice(DialogueChoice choice) {
        this.choices.add(choice);
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public List<DialogueChoice> getChoices() { return choices; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
