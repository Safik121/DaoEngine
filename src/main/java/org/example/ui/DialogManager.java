package org.example.ui;

import org.example.entity.InteractableEntity;
import org.example.entity.Player;
import org.example.GameLogger;
import org.example.logic.DialogueNode;
import org.example.logic.DialogueChoice;
import org.example.logic.DialogueRegistry;
import org.example.logic.Quest;
import org.example.logic.QuestRegistry;
import org.example.state.PlayState;

/**
 * Manages the progression of dialogue using a node-based tree system.
 * Supports branching paths, choices, and event triggers.
 */
public class DialogManager {

    private InteractableEntity activeDialogue = null;
    private DialogueNode currentNode = null;

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
     * Advances to the next logical step in the dialogue.
     * If choices are present, this does nothing (waiting for choice selection).
     * @param state Reference to the PlayState.
     * @return true if dialogue is still active, false if it just closed.
     */
    public boolean advance(PlayState state) {
        if (activeDialogue == null || currentNode == null) return false;

        // If there are choices, we cannot 'advance' with just E/Click.
        // Hitting E while choices are active should maybe select the first one?
        // For now, we wait for a specific choice selection.
        if (!currentNode.getChoices().isEmpty()) {
            return true; 
        }

        // Single path dialogue or end of node
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
            } catch (Exception e) {}
        }
    }

    private void handleCompletion(PlayState state) {
        Player player = state.getPlayer();
        if (activeDialogue.getRewardItem() != null && !activeDialogue.hasGivenReward()) {
            if (player.getInventory().addItem(activeDialogue.getRewardItem())) {
                GameLogger.info("Received reward: " + activeDialogue.getRewardItem().getName());
                activeDialogue.setHasGivenReward(true);
            }
        }
    }

    public void close() {
        this.activeDialogue = null;
        this.currentNode = null;
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
