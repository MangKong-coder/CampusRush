package campusrush;

import java.util.Random;

/** Dependency-free regression checks. Run this main class with assertions unnecessary. */
public final class GameModelChecks {
    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
    public static void main(String[] args) {
        GameModel game = new GameModel(new Random(7));
        game.start(false); game.travelTo(1);
        for (int i = 0; i < 150; i++) game.tick(.04);
        check(game.completed == 1 && game.score > 100, "Successful arrival delivers and awards time bonus");
        game.togglePause(); double clock = game.remaining, x = game.x;
        double deadline = game.deliveries.get(0).seconds; int count = game.deliveries.size();
        game.tick(15); game.travelTo(4);
        check(game.remaining == clock && game.x == x && game.deliveries.get(0).seconds == deadline
                && game.deliveries.size() == count && game.destination == -1, "Pause freezes every game system and ignores travel");
        game.togglePause(); game.tick(.04); check(game.remaining < clock, "Resume advances time");
        game.start(false); game.tick(9); check(game.deliveries.size() == 3, "Timer creates new request");
        game.tick(9); check(game.deliveries.size() == 3, "Request board is capped at three");
        game.start(false); game.tick(36); check(game.missed == 1 && game.score == 0, "Expiration penalizes without negative score");
        game.start(true); check(game.hard && game.deliveries.get(0).seconds == 22, "Hard mode uses shorter deadlines");
        game.travelTo(4); game.travelTo(2); check(game.destination == 2, "Runner can be redirected");
        game.start(false); for (int i = 0; i < 3001; i++) game.tick(.04);
        check(game.state == GameModel.State.FINISHED && game.remaining == 0, "Shift finishes at two minutes");
        clock = game.remaining; game.tick(10); check(game.remaining == clock, "Finished game does not advance");
        game.start(false); check(game.score == 0 && game.missed == 0 && game.completed == 0
                && game.remaining == 120 && game.deliveries.size() == 2 && game.destination == -1, "Restart resets all game state");
        System.out.println("PASS: delivery, scoring, pause/resume, request generation/cap, expiration, difficulty, redirection, finish, restart");
    }
}
