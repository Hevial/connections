package server;

import models.Game;
import models.GameState;
import server.db.DBManager;

public class GameManager implements Runnable {

    private int gameDuration;
    private GameState currentGameState;

    public GameManager(int gameDuration) {
        this.gameDuration = gameDuration;
    }

    @Override
    public void run() {

        DBManager dbManager = DBManager.getInstance();

        try {
            Game currentGame = dbManager.loadNextGame();
            currentGameState = new GameState(currentGame, System.currentTimeMillis(), gameDuration);
            System.out.println("\nNew game started: ");
            currentGameState.printGameState();
        } catch (Exception e) {
            System.out.println("GameManager interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }

}
