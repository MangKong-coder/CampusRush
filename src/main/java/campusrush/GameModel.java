package campusrush;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Game rules independent of Swing. All calls from the UI occur on the EDT. */
public final class GameModel {
    public static final String[] BUILDINGS = {"Student Hub", "Engineering", "Library", "Science Hall", "Arts Building"};
    public static final double[][] LOCATIONS = {{.18, .48}, {.48, .21}, {.81, .35}, {.73, .77}, {.29, .80}};
    private static final String[] ITEMS = {"Calculator", "USB drive", "Lab notebook"};
    public static final double DURATION = 120;
    public enum State { READY, RUNNING, PAUSED, FINISHED }
    public static final class Delivery {
        public final String item;
        public final int destination;
        public double seconds;
        Delivery(String item, int destination, double seconds) {
            this.item = item; this.destination = destination; this.seconds = seconds;
        }
    }
    public final List<Delivery> deliveries = new ArrayList<Delivery>();
    public State state = State.READY;
    public boolean hard;
    public int score, completed, missed, destination = -1;
    public double remaining = DURATION, x = LOCATIONS[0][0], y = LOCATIONS[0][1];
    public String message = "Start a shift, then click a building to deliver its requests.";
    private double spawnClock;
    private final Random random;

    public GameModel(Random random) { this.random = random; }

    public void start(boolean hardMode) {
        hard = hardMode; score = completed = missed = 0; remaining = DURATION;
        destination = -1; x = LOCATIONS[0][0]; y = LOCATIONS[0][1]; spawnClock = 0;
        deliveries.clear(); state = State.RUNNING;
        deliveries.add(new Delivery("Calculator", 1, hard ? 22 : 35));
        deliveries.add(new Delivery("USB drive", 2, hard ? 30 : 45));
        message = "First requests are ready. Click Engineering or Library to get moving!";
    }

    public void togglePause() {
        if (state == State.RUNNING) state = State.PAUSED;
        else if (state == State.PAUSED) state = State.RUNNING;
    }

    public void travelTo(int building) {
        if (state != State.RUNNING || building < 0 || building >= BUILDINGS.length) return;
        destination = building;
        message = "Heading to " + BUILDINGS[building] + ". You can change destination anytime.";
    }

    public void tick(double seconds) {
        if (state != State.RUNNING || seconds <= 0) return;
        double elapsed = Math.min(seconds, remaining);
        remaining = Math.max(0, remaining - elapsed);
        // Expired requests cannot be delivered on the same tick.
        for (int i = deliveries.size() - 1; i >= 0; i--) {
            Delivery d = deliveries.get(i); d.seconds -= elapsed;
            if (d.seconds <= 0) {
                deliveries.remove(i); missed++; score = Math.max(0, score - 25);
                message = "Missed " + d.item + " for " + BUILDINGS[d.destination] + ". Keep going!";
            }
        }
        if (destination >= 0) {
            double dx = LOCATIONS[destination][0] - x, dy = LOCATIONS[destination][1] - y;
            double distance = Math.hypot(dx, dy), step = elapsed * (hard ? .10 : .13);
            if (distance <= step) {
                x = LOCATIONS[destination][0]; y = LOCATIONS[destination][1];
                arrive(destination); destination = -1;
            } else { x += dx / distance * step; y += dy / distance * step; }
        }
        spawnClock += elapsed;
        if (remaining > 0 && spawnClock >= (hard ? 6 : 9)) {
            spawnClock = 0;
            if (deliveries.size() < 3) {
                int building = 1 + random.nextInt(4);
                deliveries.add(new Delivery(ITEMS[random.nextInt(ITEMS.length)], building, hard ? 22 : 35));
                message = "New request at " + BUILDINGS[building] + "!";
            }
        }
        if (remaining <= 0) { state = State.FINISHED; destination = -1; message = "Shift complete! Start a new game to try again."; }
    }

    private void arrive(int building) {
        int count = 0, earned = 0;
        for (int i = deliveries.size() - 1; i >= 0; i--) {
            Delivery d = deliveries.get(i);
            if (d.destination == building) {
                earned += 100 + (int) Math.ceil(d.seconds) * 2;
                deliveries.remove(i); completed++; count++;
            }
        }
        score += earned;
        message = count > 0 ? "Delivered " + count + " at " + BUILDINGS[building] + "! +" + earned + " points"
                : "Arrived at " + BUILDINGS[building] + ". No deliveries here right now.";
    }
}
