package org.example.logic;

import org.example.Input;
import org.example.entity.Enemy;
import org.example.entity.LightningStrike;
import org.example.entity.Projectile;
import org.example.entity.LivingEntity;
import org.example.entity.EnemyRegistry;
import org.example.item.Item;
import org.example.item.WeaponConfig;
import org.example.state.PlayState;
import org.example.ConfigManager;
import org.example.GameConfig;
import java.util.Random;

/**
 * Manages all combat-related logic, including weapon fire, projectile lifecycles,
 * collision detection, and environmental hazards (Lightning Strikes).
 */
public class CombatManager {

    private final Random random = new Random();

    /**
     * Internal tracker for active fire bursts that spawn projectiles over time.
     */
    public static class BurstTracker {
        public WeaponConfig config;
        public double angle;
        public double timer;
        public int shotsFired;

        public BurstTracker(WeaponConfig config, double angle) {
            this.config = config;
            this.angle = angle;
            this.timer = 0;
            this.shotsFired = 0;
        }
    }

    public void update(PlayState state, double deltaTime) {
        updateBursts(state, deltaTime);
        updateProjectiles(state, deltaTime);
        updateHazards(state, deltaTime);
    }

    public void handleFiring(PlayState state) {
        if (state.isInventoryOpen() || state.isShowingFullMap() || state.isPaused() || state.getDialogManager().isActive())
            return;

        if (Input.isLmbPressed() && state.getPlayer().canAttack()) {
            Item activeItem = state.getPlayer().getInventory().getItemInHotbar(state.getPlayer().getActiveHotbarSlot());
            if (activeItem != null && activeItem.getType() == Item.Type.WEAPON) {
                WeaponConfig wConfig = activeItem.getWeaponConfig();
                if (wConfig != null) {
                    // Check Qi cost
                    if (state.getPlayer().spendQi(wConfig.qiCost)) {
                        fireWeaponOrSkill(wConfig, state);
                        state.getPlayer().setAttackCooldown(wConfig.cooldown);
                    }
                }
            }
        } else if (Input.isRmbPressed() && state.getPlayer().canAttack()) {
            org.example.logic.Skill activeSkill = state.getPlayer().getActiveSkill();
            if (activeSkill != null) {
                if (state.getPlayer().spendQi(activeSkill.getQiCost())) {
                    fireWeaponOrSkill(activeSkill.getWeaponConfig(), state);
                    state.getPlayer().setAttackCooldown(activeSkill.getCooldown());
                }
            }
        }
    }

    private void fireWeaponOrSkill(WeaponConfig wConfig, PlayState state) {
        double mx = Input.getMouseX() + state.getCameraX();
        double my = Input.getMouseY() + state.getCameraY();
        double baseAngle = Math.atan2(my - (state.getPlayer().getY() + 6), mx - (state.getPlayer().getX() + 6));

        if (wConfig.burstCount > 1) {
            state.getPendingBursts().add(new BurstTracker(wConfig, baseAngle));
        } else {
            fireShot(wConfig, baseAngle, state);
        }
    }

    public void fireShot(WeaponConfig config, double baseAngle, PlayState state) {
        double px = state.getPlayer().getX() + 6;
        double py = state.getPlayer().getY() + 6;

        if (config.projectileCount <= 1) {
            state.getProjectiles().add(new Projectile(px, py, baseAngle, config, state.getPlayer(), true));
        } else {
            double startAngle = baseAngle - Math.toRadians(config.spreadAngle / 2.0);
            double angleStep = (config.projectileCount > 1)
                    ? Math.toRadians(config.spreadAngle) / (config.projectileCount - 1)
                    : 0;

            for (int i = 0; i < config.projectileCount; i++) {
                double currentAngle = startAngle + (angleStep * i);
                state.getProjectiles().add(new Projectile(px, py, currentAngle, config, state.getPlayer(), true));
            }
        }
    }

    private void updateBursts(PlayState state, double deltaTime) {
        for (int i = state.getPendingBursts().size() - 1; i >= 0; i--) {
            BurstTracker burst = state.getPendingBursts().get(i);
            burst.timer -= deltaTime;
            if (burst.timer <= 0) {
                fireShot(burst.config, burst.angle, state);
                burst.shotsFired++;
                burst.timer = burst.config.burstDelay;
                if (burst.shotsFired >= burst.config.burstCount) {
                    state.getPendingBursts().remove(i);
                }
            }
        }
    }

    private void updateProjectiles(PlayState state, double deltaTime) {
        for (int i = state.getProjectiles().size() - 1; i >= 0; i--) {
            Projectile p = state.getProjectiles().get(i);
            p.update(state.getGameMap(), deltaTime);

            if (p.isFriendly()) {
                // Collision with enemies
                for (Enemy enemy : state.getEnemies()) {
                    if (p.checkCollision(enemy)) {
                        applyDamage(p, enemy, deltaTime);
                        state.getParticleManager().spawnHitSpark(enemy.getX() + enemy.getSize() / 2, enemy.getY() + enemy.getSize() / 2);

                        if (p.getType() != WeaponConfig.ProjectileType.BEAM && p.getType() != WeaponConfig.ProjectileType.AOE_ZONE) {
                            p.deactivate();
                            break;
                        }
                    }
                }
            } else {
                // Collision with player
                if (p.checkCollision(state.getPlayer())) {
                    applyDamage(p, state.getPlayer(), deltaTime);
                    if (p.getType() != WeaponConfig.ProjectileType.BEAM && p.getType() != WeaponConfig.ProjectileType.AOE_ZONE) {
                        p.deactivate();
                    }
                }
            }

            if (!p.isActive()) {
                state.getProjectiles().remove(i);
            }
        }
    }

    private void applyDamage(Projectile p, LivingEntity target, double deltaTime) {
        double damage = p.getDamage();
        if (p.getType() == WeaponConfig.ProjectileType.BEAM || p.getType() == WeaponConfig.ProjectileType.AOE_ZONE) {
            damage *= deltaTime;
        }
        target.takeDamage(damage);
    }

    private void updateHazards(PlayState state, double deltaTime) {
        if (!state.isInTribulation()) return;

        double timer = state.getLightningTimer() - deltaTime;
        if (timer <= 0) {
            GameConfig.BalanceConfig bal = ConfigManager.getInstance().getConfig().balance;
            double lx = state.getPlayer().getX() + 6;
            double ly = state.getPlayer().getY() + 6;
            state.getActiveStrikes().add(new LightningStrike(lx, ly));
            timer = bal.lightningIntervalMin + random.nextDouble() * (bal.lightningIntervalMax - bal.lightningIntervalMin);
        }
        state.setLightningTimer(timer);

        for (int i = state.getActiveStrikes().size() - 1; i >= 0; i--) {
            LightningStrike strike = state.getActiveStrikes().get(i);
            strike.update(deltaTime);

            if (strike.isDealingDamage()) {
                handleStrikeDamage(state, strike);
                strike.markDamaged();
            }

            if (strike.isExpired()) {
                state.getActiveStrikes().remove(i);
            }
        }
    }

    public void triggerTribulation(PlayState state) {
        state.setInTribulation(true);

        // Spawn the entire Tribulation wave immediately
        for (int i = 0; i < state.getCurrentLevelConfig().tribulationTotalEnemies; i++) {
            double[] pos = state.getGameMap().getRandomFreePositionAwayFrom(24, state.getPlayer().getX(), state.getPlayer().getY(), 250);
            if (pos != null) {
                String enemyId = state.getCurrentLevelConfig().enemyPool
                        .get(random.nextInt(state.getCurrentLevelConfig().enemyPool.size()));
                state.getEnemies().add(EnemyRegistry.createEnemy(enemyId, pos[0], pos[1], true,
                        state.getCurrentLevelConfig().tribulationScalingFactor));
            }
        }
    }

    private void handleStrikeDamage(PlayState state, LightningStrike strike) {
        GameConfig.BalanceConfig bal = ConfigManager.getInstance().getConfig().balance;
        // Player damage
        double distP = Math.sqrt(Math.pow(strike.getX() - state.getPlayer().getX(), 2) + Math.pow(strike.getY() - state.getPlayer().getY(), 2));
        if (distP < strike.getRadius()) {
            state.getPlayer().takeDamage(bal.lightningPlayerDamage);
        }
        // Enemy damage
        for (Enemy e : state.getEnemies()) {
            double distE = Math.sqrt(Math.pow(strike.getX() - e.getX(), 2) + Math.pow(strike.getY() - e.getY(), 2));
            if (distE < strike.getRadius()) {
                e.takeDamage(bal.lightningEnemyDamage);
            }
        }
    }
}
