package org.example;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

// Importujeme naše nové stavy
import org.example.state.GameState;
import org.example.state.MenuState;
import org.example.state.PlayState;

/**
 * The main application class for DaoEngine: Path to Immortality.
 * This class initializes the JavaFX environment, sets up the game loop,
 * and manages transitions between different game states.
 */
public class DaoEngineApp extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    /** The current active game state. */
    private GameState currentState;

    /**
     * Initializes the JavaFX stage, scene, and canvases.
     * Sets up the initial game state and the input event handlers.
     * 
     * @param primaryStage The primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DaoEngine: Path to Immortality");

        Group root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1. Initial game state is the Main Menu
        currentState = new MenuState();

        // 2. Keyboard Event Handling (Input tracking added)
        scene.setOnKeyPressed(event -> {
            Input.addKey(event.getCode()); // Register the key as active

            // Maintain state transition to game on ENTER
            if (event.getCode() == KeyCode.ENTER && currentState instanceof MenuState) {
                currentState = new PlayState();
            }
        });

        scene.setOnKeyReleased(event -> {
            Input.removeKey(event.getCode()); // Remove the key when released
        });

        // 3. Main Game Loop using AnimationTimer
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                update();
                render(gc);
            }
        };
        gameLoop.start();

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Updates the current game state's logic.
     */
    private void update() {
        // Delegate update call to the active state
        if (currentState != null) {
            currentState.update();
        }
    }

    /**
     * Renders the current game state's visual elements.
     * 
     * @param gc The GraphicsContext to render to.
     */
    private void render(GraphicsContext gc) {
        // Delegate render call to the active state
        if (currentState != null) {
            currentState.render(gc);
        }
    }

    /**
     * Main entry point for the application.
     * 
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}