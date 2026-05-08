package org.example.state;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.AssetRegistry;
import org.example.ConfigManager;
import org.example.Input;
import org.example.entity.EnemyConfig;
import org.example.entity.EnemyRegistry;
import org.example.item.ItemConfig;
import org.example.item.ItemRegistry;
import org.example.item.RecipeConfig;
import org.example.logic.CultivationRank;
import org.example.logic.CultivationRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * The Book of Knowledge (Lexicon) state.
 * Displays information about Items, Enemies, and Crafting Recipes.
 */
public class LexiconState implements GameState {

    private enum Tab { ITEMS, ENEMIES, RECIPES, CULTIVATION }
    private Tab currentTab = Tab.ITEMS;

    private boolean returnRequested = false;

    // Selection state
    private String selectedId = null;
    private RecipeConfig selectedRecipe = null;

    // Scrolling state
    private double scrollOffset = 0;
    private double maxScroll = 0;
    private boolean clickWasPressed = false;

    // UI Constants
    private final int width = ConfigManager.getInstance().getConfig().engine.width;
    private final int height = ConfigManager.getInstance().getConfig().engine.height;
    private final double sidebarWidth = 300;
    private final double tabHeight = 60;
    private final double sidebarStartY = 80;
    private final double sidebarHeight = height - 160;
    
    // Aesthetic Colors
    private static final Color GOLD = Color.web("#D4AF37");
    private static final Color DARK_INK = Color.web("#1A1A1A");

    public LexiconState() {
        // Default selection
        autoSelectFirst();
        calculateMaxScroll();
    }

    private void autoSelectFirst() {
        switch (currentTab) {
            case ITEMS:
                selectedId = ItemRegistry.getAllItems().keySet().stream().findFirst().orElse(null);
                break;
            case ENEMIES:
                selectedId = EnemyRegistry.getAllIds().stream().findFirst().orElse(null);
                break;
            case RECIPES:
                selectedRecipe = ItemRegistry.getAllRecipes().stream().findFirst().orElse(null);
                break;
            case CULTIVATION:
                selectedId = getUniqueRealms().stream().findFirst().orElse(null);
                break;
        }
    }

    private List<String> getUniqueRealms() {
        List<String> realms = new ArrayList<>();
        for (CultivationRank rank : CultivationRegistry.getFullProgressionPath()) {
            if (!realms.contains(rank.getTitle())) {
                realms.add(rank.getTitle());
            }
        }
        return realms;
    }

    private void calculateMaxScroll() {
        int count = 0;
        double itemHeight = 40;
        switch (currentTab) {
            case ITEMS: count = ItemRegistry.getAllItems().size(); break;
            case ENEMIES: count = EnemyRegistry.getAllIds().size(); break;
            case RECIPES: count = ItemRegistry.getAllRecipes().size(); break;
            case CULTIVATION: count = getUniqueRealms().size(); break;
        }
        
        double totalContentHeight = count * itemHeight + 40; // padding
        maxScroll = Math.max(0, totalContentHeight - sidebarHeight);
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }

    @Override
    public void update(double deltaTime) {
        double mx = Input.getMouseX();
        double my = Input.getMouseY();
        boolean click = Input.isLmbPressed();

        // Check Back Button
        if (click && !clickWasPressed && mx >= 20 && mx <= 120 && my >= height - 60 && my <= height - 20) {
            org.example.logic.SoundManager.playSound("click");
            returnRequested = true;
        }

        // Check Tabs
        for (int i = 0; i < Tab.values().length; i++) {
            double tx = width / 2.0 - 310 + i * 155;
            if (click && !clickWasPressed && mx >= tx && mx <= tx + 140 && my >= 10 && my <= tabHeight) {
                org.example.logic.SoundManager.playSound("click");
                currentTab = Tab.values()[i];
                autoSelectFirst();
                scrollOffset = 0;
                calculateMaxScroll();
            }
        }

        // Handle Scrolling
        double scroll = Input.getScrollAndReset();
        if (scroll != 0 && mx >= 20 && mx <= 20 + sidebarWidth && my >= sidebarStartY && my <= sidebarStartY + sidebarHeight) {
            scrollOffset -= scroll * 0.5; // Sensitivity adjustment
            if (scrollOffset < 0) scrollOffset = 0;
            if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        }

        // Check Sidebar Selection
        if (click && !clickWasPressed && mx >= 20 && mx <= 20 + sidebarWidth && my >= sidebarStartY && my <= sidebarStartY + sidebarHeight) {
            org.example.logic.SoundManager.playSound("click");
            handleSidebarClick(mx, my);
        }

        clickWasPressed = click;
    }

    private void handleSidebarClick(double mx, double my) {
        double startY = sidebarStartY + 10; // Matches the visual start of the first item capsule (startY - 20)
        double itemHeight = 40;
        // Adjusted for scrollOffset
        int index = (int)((my - startY + scrollOffset) / itemHeight);

        switch (currentTab) {
            case ITEMS:
                List<String> ids = new ArrayList<>(ItemRegistry.getAllItems().keySet());
                if (index >= 0 && index < ids.size()) selectedId = ids.get(index);
                break;
            case ENEMIES:
                List<String> eIds = new ArrayList<>(EnemyRegistry.getAllIds());
                if (index >= 0 && index < eIds.size()) selectedId = eIds.get(index);
                break;
            case RECIPES:
                List<RecipeConfig> rList = ItemRegistry.getAllRecipes();
                if (index >= 0 && index < rList.size()) selectedRecipe = rList.get(index);
                break;
            case CULTIVATION:
                List<String> realmIds = getUniqueRealms();
                if (index >= 0 && index < realmIds.size()) selectedId = realmIds.get(index);
                break;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        drawBackground(gc);
        drawTabs(gc);
        drawSidebar(gc);
        drawDetailView(gc);
        drawBackButton(gc);
    }

    private void drawBackground(GraphicsContext gc) {
        LinearGradient grad = new LinearGradient(0, 0, 0, 1, true, null,
                new Stop(0, DARK_INK),
                new Stop(1, Color.BLACK)
        );
        gc.setFill(grad);
        gc.fillRect(0, 0, width, height);

        // Ancient Scroll Border effect
        gc.setStroke(GOLD);
        gc.setLineWidth(2);
        gc.strokeRect(10, 10, width - 20, height - 20);
    }

    private void drawTabs(GraphicsContext gc) {
        for (int i = 0; i < Tab.values().length; i++) {
            Tab t = Tab.values()[i];
            double tx = width / 2.0 - 310 + i * 155;
            boolean active = (currentTab == t);

            gc.setFill(active ? GOLD : Color.web("#444444"));
            gc.fillRect(tx, 10, 140, tabHeight - 10);
            
            gc.setStroke(GOLD);
            gc.strokeRect(tx, 10, 140, tabHeight - 10);

            gc.setFill(active ? Color.BLACK : GOLD);
            gc.setFont(Font.font("Serif", FontWeight.BOLD, 18));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText(t.name(), tx + 70, 42);
        }
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }

    private void drawSidebar(GraphicsContext gc) {
        // Draw the container
        gc.setFill(Color.web("#222222"));
        gc.fillRect(20, sidebarStartY, sidebarWidth, sidebarHeight);
        gc.setStroke(GOLD);
        gc.strokeRect(20, sidebarStartY, sidebarWidth, sidebarHeight);

        // Prepare for clipping
        gc.save();
        gc.beginPath();
        gc.rect(20, sidebarStartY, sidebarWidth, sidebarHeight);
        gc.clip();

        double startY = sidebarStartY + 30 - scrollOffset;
        double itemHeight = 40;

        switch (currentTab) {
            case ITEMS:
                renderIdList(gc, new ArrayList<>(ItemRegistry.getAllItems().keySet()), startY, itemHeight);
                break;
            case ENEMIES:
                renderIdList(gc, new ArrayList<>(EnemyRegistry.getAllIds()), startY, itemHeight);
                break;
            case RECIPES:
                renderRecipeList(gc, startY, itemHeight);
                break;
            case CULTIVATION:
                renderIdList(gc, getUniqueRealms(), startY, itemHeight);
                break;
        }

        gc.restore();

        // Draw visual scrollbar if needed
        if (maxScroll > 0) {
            double barH = (sidebarHeight / (maxScroll + sidebarHeight)) * sidebarHeight;
            double barY = sidebarStartY + (scrollOffset / maxScroll) * (sidebarHeight - barH);
            gc.setFill(GOLD);
            gc.setGlobalAlpha(0.4);
            gc.fillRect(20 + sidebarWidth - 6, barY, 4, barH);
            gc.setGlobalAlpha(1.0);
        }
    }

    private void renderIdList(GraphicsContext gc, List<String> ids, double startY, double itemHeight) {
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            boolean active = id.equals(selectedId);
            double y = startY + i * itemHeight;

            // Simple culling - don't draw if far outside bounds
            if (y < sidebarStartY - 40 || y > sidebarStartY + sidebarHeight + 40) continue;
            
            if (active) {
                gc.setFill(GOLD);
                gc.setGlobalAlpha(0.2);
                gc.fillRect(25, y - 20, sidebarWidth - 10, itemHeight);
                gc.setGlobalAlpha(1.0);
            }

            gc.setFill(active ? Color.WHITE : GOLD);
            gc.setFont(Font.font("Serif", 16));
            String label = id;
            if (currentTab == Tab.ITEMS) label = ItemRegistry.getAllItems().get(id).name;
            if (currentTab == Tab.ENEMIES) label = EnemyRegistry.getAllConfigs().get(id).name;
            if (currentTab == Tab.CULTIVATION) label = id;
            
            gc.fillText(label, 40, y);
        }
    }

    private void renderRecipeList(GraphicsContext gc, double startY, double itemHeight) {
        List<RecipeConfig> recipes = ItemRegistry.getAllRecipes();
        for (int i = 0; i < recipes.size(); i++) {
            RecipeConfig rc = recipes.get(i);
            boolean active = (rc == selectedRecipe);
            double y = startY + i * itemHeight;

            if (y < sidebarStartY - 40 || y > sidebarStartY + sidebarHeight + 40) continue;

            if (active) {
                gc.setFill(GOLD);
                gc.setGlobalAlpha(0.2);
                gc.fillRect(25, y - 20, sidebarWidth - 10, itemHeight);
                gc.setGlobalAlpha(1.0);
            }

            gc.setFill(active ? Color.WHITE : GOLD);
            gc.setFont(Font.font("Serif", 16));
            ItemConfig res = ItemRegistry.getAllItems().get(rc.result);
            gc.fillText(res != null ? res.name : "Unknown", 40, y);
        }
    }

    private void drawDetailView(GraphicsContext gc) {
        double x = 20 + sidebarWidth + 20;
        double y = sidebarStartY;
        double w = width - x - 20;
        double h = sidebarHeight;

        gc.setFill(Color.rgb(40, 40, 40, 0.8));
        gc.fillRect(x, y, w, h);
        gc.setStroke(GOLD);
        gc.strokeRect(x, y, w, h);

        switch (currentTab) {
            case ITEMS: renderItemDetails(gc, x, y, w); break;
            case ENEMIES: renderEnemyDetails(gc, x, y, w); break;
            case RECIPES: renderRecipeDetails(gc, x, y, w); break;
            case CULTIVATION: renderCultivationDetails(gc, x, y, w); break;
        }
    }

    private void renderItemDetails(GraphicsContext gc, double x, double y, double w) {
        ItemConfig config = ItemRegistry.getAllItems().get(selectedId);
        if (config == null) return;

        gc.setFill(GOLD);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 32));
        gc.fillText(config.name, x + 30, y + 50);

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Serif", 18));
        gc.fillText("Type: " + config.type, x + 30, y + 80);

        // Draw Item Icon
        drawSprite(gc, config.spriteId, x + w - 110, y + 20, 80);

        gc.setStroke(GOLD);
        gc.strokeLine(x + 30, y + 100, x + w - 30, y + 100);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Serif", 20));
        String desc = config.detailedDescription != null ? config.detailedDescription : config.description;
        drawWrappedText(gc, desc, x + 30, y + 140, w - 60);
    }

    private void renderEnemyDetails(GraphicsContext gc, double x, double y, double w) {
        EnemyConfig config = EnemyRegistry.getAllConfigs().get(selectedId);
        if (config == null) return;

        gc.setFill(GOLD);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 32));
        gc.fillText(config.name, x + 30, y + 50);

        gc.setFill(Color.web(config.color));
        gc.fillOval(x + 30, y + 70, 30, 30);
        gc.setStroke(Color.WHITE);
        gc.strokeOval(x + 30, y + 70, 30, 30);

        // Draw Enemy Sprite
        drawSprite(gc, config.spriteId, x + w - 130, y + 20, 100);

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Serif", 14));
        gc.fillText("Stats - HP: " + config.hp + " | Damage: " + config.damage + " | Speed: " + config.speed, x + 75, y + 90);

        gc.setStroke(GOLD);
        gc.strokeLine(x + 30, y + 110, x + w - 30, y + 110);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Serif", 20));
        drawWrappedText(gc, config.behaviorDescription != null ? config.behaviorDescription : "No behavior data available.", x + 30, y + 150, w - 60);
    }

    private void renderRecipeDetails(GraphicsContext gc, double x, double y, double w) {
        if (selectedRecipe == null) return;

        ItemConfig i1 = ItemRegistry.getAllItems().get(selectedRecipe.input1);
        ItemConfig i2 = ItemRegistry.getAllItems().get(selectedRecipe.input2);
        ItemConfig res = ItemRegistry.getAllItems().get(selectedRecipe.result);

        gc.setFill(GOLD);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 32));
        gc.fillText("Crafting Recipe", x + 30, y + 50);

        gc.setStroke(GOLD);
        gc.strokeLine(x + 30, y + 70, x + w - 30, y + 70);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Serif", 22));
        gc.fillText((i1 != null ? i1.name : "Unknown") + "  +  " + (i2 != null ? i2.name : "Unknown"), x + 50, y + 180);
        
        gc.setFill(GOLD);
        gc.fillText("  =  ", x + 50, y + 230);

        gc.setFill(Color.AQUAMARINE);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 28));
        gc.fillText(res != null ? res.name : "Unknown Result", x + 80, y + 230);

        if (res != null) {
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font("Serif", 16));
            gc.fillText(res.description, x + 80, y + 260);
        }

        // Draw visual icons for recipes
        drawSprite(gc, i1 != null ? i1.spriteId : null, x + 50, y + 80, 54);
        gc.setFill(GOLD);
        gc.fillText("+", x + 115, y + 120);
        drawSprite(gc, i2 != null ? i2.spriteId : null, x + 140, y + 80, 54);
        gc.fillText("=", x + 210, y + 120);
        drawSprite(gc, res != null ? res.spriteId : null, x + 245, y + 75, 70);
    }

    private void renderCultivationDetails(GraphicsContext gc, double x, double y, double w) {
        if (selectedId == null) return;
        
        // Find the first occurrence of this realm to get its description
        CultivationRank rankExample = null;
        int tierCount = 0;
        for (CultivationRank r : CultivationRegistry.getFullProgressionPath()) {
            if (r.getTitle().equals(selectedId)) {
                if (rankExample == null) rankExample = r;
                tierCount++;
            }
        }
        
        if (rankExample == null) return;

        gc.setFill(GOLD);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 32));
        gc.fillText(selectedId, x + 30, y + 50);

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Serif", 18));
        gc.fillText("Classification: Celestial Realm", x + 30, y + 80);
        gc.fillText("Total Tiers: " + tierCount, x + 30, y + 105);

        gc.setStroke(GOLD);
        gc.strokeLine(x + 30, y + 120, x + w - 30, y + 120);

        String desc = rankExample.getDescription();
        if (desc == null || desc.isEmpty()) desc = "Mysterious realms that lie beyond mortal understanding.";

        // Split by delimiter '|' for sub-header formatting
        String[] parts = desc.split("\\|");
        double nextY = y + 150;

        if (parts.length >= 2) {
            // Sub-header (Ancient/Alt Name)
            gc.setFill(GOLD);
            gc.setFont(Font.font("Serif", FontWeight.BOLD, 22));
            gc.fillText(parts[0].trim(), x + 30, nextY);
            
            // Lore Body
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Serif", 18));
            drawWrappedText(gc, parts[1].trim(), x + 30, nextY + 30, w - 60);
        } else {
            // Fallback for non-segmented lore
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Serif", 20));
            drawWrappedText(gc, desc, x + 30, nextY, w - 60);
        }

        // Aesthetics
        gc.setFill(GOLD);
        gc.setGlobalAlpha(0.1);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 120));
        double detailH = 600 - 160; // Total height - sidebar padding (matches sidebarHeight)
        gc.fillText("仙", x + w - 160, y + detailH - 40);
        gc.setGlobalAlpha(1.0);
    }

    private void drawSprite(GraphicsContext gc, String spriteId, double x, double y, double size) {
        if (spriteId != null) {
            javafx.scene.image.Image img = AssetRegistry.getSprite(spriteId, 0);
            if (img != null) {
                gc.drawImage(img, x, y, size, size);
                return;
            }
        }
        // Placeholder
        gc.setFill(Color.web("#333333"));
        gc.fillRect(x, y, size, size);
        gc.setStroke(GOLD);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, size, size);
        gc.setFill(GOLD);
        gc.setFont(Font.font("Serif", size * 0.5));
        gc.fillText("?", x + size * 0.3, y + size * 0.7);
    }

    private void drawWrappedText(GraphicsContext gc, String text, double x, double y, double maxWidth) {
        if (text == null) return;
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        double currentY = y;
        double lineHeight = 25;

        for (String word : words) {
            String testLine = line + word + " ";
            if (testLine.length() * 9 > maxWidth) {
                gc.fillText(line.toString(), x, currentY);
                line = new StringBuilder(word + " ");
                currentY += lineHeight;
            } else {
                line.append(word).append(" ");
            }
        }
        gc.fillText(line.toString(), x, currentY);
    }

    private void drawBackButton(GraphicsContext gc) {
        double mx = Input.getMouseX();
        double my = Input.getMouseY();
        boolean hover = mx >= 20 && mx <= 120 && my >= height - 60 && my <= height - 20;

        gc.setFill(hover ? GOLD : Color.web("#444444"));
        gc.fillRect(20, height - 60, 100, 40);
        gc.setStroke(GOLD);
        gc.strokeRect(20, height - 60, 100, 40);

        gc.setFill(hover ? Color.BLACK : GOLD);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 18));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText("BACK", 70, height - 34);
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }

    /** @return true if the user clicked the BACK button. */
    public boolean isReturnRequested() {
        return returnRequested;
    }

    /** @param returnRequested New state of return request. */
    public void setReturnRequested(boolean returnRequested) {
        this.returnRequested = returnRequested;
    }
}
