package org.example.item;

import org.example.entity.Player;
import org.example.state.PlayState;

/**
 * Specialized item representing a technique manual.
 * Triggering this item opens a confirmation dialogue to learn a new skill.
 */
public class SkillBookItem extends Item {
    /**
     * @param id Unique ID.
     * @param name Display name.
     * @param description Brief text.
     */
    public SkillBookItem(String id, String name, String description) {
        super(id, name, description, Type.SKILL_BOOK);
    }

    @Override
    public void use(Player player, PlayState state) {
        if (getSkillId() != null) {
            state.getDialogManager().startSkillLearningDialogue(getSkillId(), this, state);
        }
    }
}
