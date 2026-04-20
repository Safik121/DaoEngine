package org.example.logic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.item.WeaponConfig;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for available techniques in the game.
 */
public class SkillRegistry {
    private static final Map<String, Skill> skills = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void loadSkills(String resourcePath) {
        try {
            InputStream is = SkillRegistry.class.getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Skill configuration not found: " + resourcePath);
                return;
            }

            List<Skill> skillList = mapper.readValue(is, new TypeReference<List<Skill>>() {});
            skills.clear();
            for (Skill s : skillList) {
                skills.put(s.getId(), s);
            }
            System.out.println("Loaded " + skills.size() + " techniques from JSON.");
        } catch (Exception e) {
            System.err.println("Fatal error loading SkillRegistry data!");
            e.printStackTrace();
        }
    }

    public static Skill getSkill(String id) {
        return skills.get(id);
    }
}
