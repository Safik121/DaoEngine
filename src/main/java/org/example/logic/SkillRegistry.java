package org.example.logic;

import org.example.item.WeaponConfig;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for available techniques in the game.
 */
public class SkillRegistry {
    private static final Map<String, Skill> skills = new HashMap<>();

    static {
        // Hardcoded for now, would ideally load from a JSON like ItemRegistry
        WeaponConfig fieryPalmConfig = new WeaponConfig();
        fieryPalmConfig.projectileType = WeaponConfig.ProjectileType.FIREBALL;
        fieryPalmConfig.damage = 25.0;
        fieryPalmConfig.speed = 6.0;
        fieryPalmConfig.burstCount = 3;
        fieryPalmConfig.spreadAngle = 30;
        fieryPalmConfig.burstDelay = 0.1;
        fieryPalmConfig.lifeSpan = 1.0;
        fieryPalmConfig.size = 20.0;
        
        skills.put("fiery_palm", new Skill("fiery_palm", "Fiery Palm Technique", 30.0, 1.5, fieryPalmConfig));

        WeaponConfig voidSwordConfig = new WeaponConfig();
        voidSwordConfig.projectileType = WeaponConfig.ProjectileType.FLYING_SWORD;
        voidSwordConfig.damage = 50.0;
        voidSwordConfig.speed = 10.0;
        voidSwordConfig.lifeSpan = 2.0;
        voidSwordConfig.size = 24.0;

        skills.put("void_sword", new Skill("void_sword", "Void Sword Slash", 50.0, 3.0, voidSwordConfig));
    }

    public static Skill getSkill(String id) {
        return skills.get(id);
    }
}
