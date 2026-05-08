package org.example.level;

import java.util.List;
import org.example.entity.InteractableEntity;

/**
 * POJO for configuring an interactable entity (NPC or Stele) from JSON.
 */
public class InteractableConfig {
    /** Unique name or display name of the entity. */
    public String name;
    /** The type of entity (NPC, STELE). */
    public InteractableEntity.Type type;
    /** Sequential lines of dialogue to display (Legacy). */
    public List<String> dialogue;
    /** Unique ID for a branching dialogue tree. */
    public String dialogueTreeId;
    /** Optional item ID to be granted upon completion. */
    public String rewardItemId;
    /** Optional Quest ID to be granted upon interacting with this entity. */
    public String giveQuestId;
    /** Optional Sprite ID defined in assets.json for custom rendering. */
    public String spriteId;
    /** How many instances to spawn of this specific configuration. */
    public int count = 1;

    public InteractableConfig() {}

    public InteractableConfig(String name, InteractableEntity.Type type, List<String> dialogue, String rewardItemId) {
        this.name = name;
        this.type = type;
        this.dialogue = dialogue;
        this.rewardItemId = rewardItemId;
    }
}
