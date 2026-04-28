package org.example.logic;

import org.example.GameLogger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for loading and managing branching dialogue trees.
 */
public class DialogueRegistry {
    private static Map<String, DialogueNode> dialogues = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static class DialogueTemplate {
        public String id;
        public String text;
        public List<ChoiceTemplate> choices;
        public String action;
    }

    public static class ChoiceTemplate {
        public String text;
        public String nextNodeId;
    }

    public static void loadDialogues(String resourcePath) {
        try {
            InputStream is = DialogueRegistry.class.getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Dialogue configuration file not found: " + resourcePath);
                return;
            }
            
            Map<String, DialogueTemplate> loaded = mapper.readValue(is, new TypeReference<Map<String, DialogueTemplate>>() {});
            
            for (DialogueTemplate temp : loaded.values()) {
                DialogueNode node = new DialogueNode(temp.id, temp.text);
                node.setAction(temp.action);
                if (temp.choices != null) {
                    for (ChoiceTemplate ct : temp.choices) {
                        node.addChoice(new DialogueChoice(ct.text, ct.nextNodeId));
                    }
                }
                dialogues.put(temp.id, node);
            }
            
            org.example.GameLogger.info("Loaded " + dialogues.size() + " dialogue nodes.");
        } catch (Exception e) {
            System.err.println("Fatal error loading DialogueRegistry data!");
            e.printStackTrace();
        }
    }

    public static DialogueNode getNode(String id) {
        return dialogues.get(id);
    }
}
