package org.example.ui;

import org.example.entity.InteractableEntity;
import org.example.entity.Player;
import org.example.GameLogger;
import org.example.item.Item;
import org.example.logic.DialogueNode;
import org.example.logic.DialogueChoice;
import org.example.logic.DialogueRegistry;
import org.example.logic.Quest;
import org.example.logic.QuestRegistry;
import org.example.logic.Skill;
import org.example.logic.SkillRegistry;
import org.example.state.PlayState;

import java.util.ArrayList;

/**
 * Manages the progression of dialogue using a node-based tree system.
 * Supports branching paths, choices, and event triggers.
 */
public class DialogManager {

    private InteractableEntity activeDialogue = null;
    private DialogueNode currentNode = null;

    // Skill Learning Context
    private Item activeBook = null;
    private String pendingSkillId = null;

    /**
     * Attempts to start a dialogue with an entity using its dialogue tree.
     * @param entity The entity to interact with.
     */
    public void startDialogue(InteractableEntity entity, PlayState state) {
        String treeId = entity.getDialogueTreeId();
        if (treeId == null || treeId.isEmpty()) {
            GameLogger.warning(entity.getName() + " has no dialogue assigned.");
            return;
        }

        DialogueNode startNode = DialogueRegistry.getNode(treeId);
        if (startNode == null) {
            GameLogger.error("Dialogue tree not found: " + treeId);
            return;
        }

        this.activeDialogue = entity;
        this.currentNode = startNode;
        handleNodeAction(state);
        GameLogger.info("Started dialogue tree [" + treeId + "] with " + entity.getName());
    }

    /**
     * Specialized dialogue for learning a new technique from a Skill Book.
     */
    public void startSkillLearningDialogue(String skillId, Item book, PlayState state) {
        Skill skill = SkillRegistry.getSkill(skillId);
        if (skill == null) return;

        this.activeBook = book;
        this.pendingSkillId = skillId;
        
        // Use a dummy entity for skill books
        this.activeDialogue = new org.example.entity.InteractableEntity(0, 0, "Ancient Manual", 
            org.example.entity.InteractableEntity.Type.STELE);
            
        DialogueNode node = new DialogueNode("skill_learning", 
            "The ancient text hums with power as you read: '" + skill.getName() + "'. " +
            "Do you wish to internalize this technique? (Warning: This will replace your current technique)");
        
        node.getChoices().add(new DialogueChoice("Yes, Master the Dao", "confirm_learn"));
        node.getChoices().add(new DialogueChoice("No, Not Yet", "exit"));
        
        this.currentNode = node;
    }

    /**
     * Advances to the next logical step in the dialogue.
     * If choices are present, this does nothing (waiting for choice selection).
     * @param state Reference to the PlayState.
     * @return true if dialogue is still active, false if it just closed.
     */
    public boolean advance(PlayState state) {
        if (activeDialogue == null || currentNode == null) return false;

        if (!currentNode.getChoices().isEmpty()) {
            return true; 
        }

        handleCompletion(state);
        close();
        return false;
    }

    public void selectChoice(int index, PlayState state) {
        if (currentNode == null || index < 0 || index >= currentNode.getChoices().size()) return;

        DialogueChoice choice = currentNode.getChoices().get(index);
        String nextId = choice.getNextNodeId();

        if (nextId == null || nextId.equalsIgnoreCase("exit")) {
            handleCompletion(state);
            close();
        } else if (nextId.equals("confirm_learn")) {
            handleSkillReplacement(state);
            close();
        } else {
            currentNode = DialogueRegistry.getNode(nextId);
            if (currentNode != null) {
                handleNodeAction(state);
            } else {
                close();
            }
        }
    }

    private void handleNodeAction(PlayState state) {
        if (currentNode == null || currentNode.getAction() == null) return;

        String action = currentNode.getAction();
        if (action.startsWith("GIVE_QUEST:")) {
            String qId = action.substring("GIVE_QUEST:".length());
            if (!state.getQuestManager().hasQuest(qId)) {
                Quest q = QuestRegistry.createQuest(qId);
                if (q != null) {
                    state.getQuestManager().addQuest(q, state);
                }
            }
        } else if (action.startsWith("HEAL:")) {
            try {
                double amount = Double.parseDouble(action.substring("HEAL:".length()));
                state.heal(amount);
            } catch (Exception e) {
            }
        } else if (action.startsWith("ADD_QI:")) {
            try {
                double amount = Double.parseDouble(action.substring("ADD_QI:".length()));
                state.getPlayer().restoreQi(amount);
                state.addNotification("Spirit Power + " + (int) amount);
            } catch (Exception e) {
            }
        } else if (action.startsWith("ADD_HP:")) {
            try {
                double amount = Double.parseDouble(action.substring("ADD_HP:".length()));
                state.getPlayer().getStats().setMaxHp(state.getPlayer().getMaxHp() + amount);
                state.getPlayer().heal(amount);
                state.addNotification("Constitution + " + (int) amount);
            } catch (Exception e) {
            }
        } else if (action.startsWith("ADD_STR:")) {
            try {
                double amount = Double.parseDouble(action.substring("ADD_STR:".length()));
                state.getPlayer().getStats().setStrength(state.getPlayer().getStats().getStrength() + amount);
                state.addNotification("Strength + " + (int) amount);
            } catch (Exception e) {
            }
        }
    }

    private void handleSkillReplacement(PlayState state) {
        if (pendingSkillId == null || activeBook == null) return;
        
        Skill skill = SkillRegistry.getSkill(pendingSkillId);
        if (skill != null) {
            state.getPlayer().setActiveSkill(skill);
            state.addNotification("TECHNIQUE MASTERED: " + skill.getName());
            
            // Consume the book
            state.getPlayer().getInventory().removeItem(activeBook.getId(), 1);
            GameLogger.info("Mastered " + skill.getName() + " and consumed " + activeBook.getName());
        }
    }

    private void handleCompletion(PlayState state) {
        Player player = state.getPlayer();
        if (activeDialogue != null && activeDialogue.getRewardItem() != null && !activeDialogue.hasGivenReward()) {
            if (player.getInventory().addItem(activeDialogue.getRewardItem())) {
                GameLogger.info("Received reward: " + activeDialogue.getRewardItem().getName());
                activeDialogue.setHasGivenReward(true);
            }
        }
    }

    public void close() {
        this.activeDialogue = null;
        this.currentNode = null;
        this.activeBook = null;
        this.pendingSkillId = null;
    }

    public boolean isActive() {
        return activeDialogue != null;
    }

    public InteractableEntity getActiveDialogue() {
        return activeDialogue;
    }

    public DialogueNode getCurrentNode() {
        return currentNode;
    }
}
