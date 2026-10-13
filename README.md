# Campus Rush: Deadline Rescue

A Java Swing proof of concept for a project covering mouse, menu, and timer events. No external libraries, image assets, database, or account required. Java 8-compatible source; use JDK 8 or newer.

## Open in Apache NetBeans

1. Extract the project ZIP if using the download.
2. Select **File > Open Project**, select the **CampusRush** folder containing `pom.xml`, and click **Open Project**. This is a Maven project, not an individual file import.
3. Right-click the project and choose **Run**, or select it and press **F6**.
4. If asked for a main class, choose `campusrush.CampusRush`.

NetBeans includes Maven. The first Maven build needs internet access to download build plugins. Set a JDK under **Tools > Java Platforms** if NetBeans cannot find one. The game itself works offline after building. The project includes `nbactions.xml` with the Run Project action.

## Play

Start a two-minute shift. All requested items are already in your bag: click a building or request card to travel there. Matching requests complete automatically on arrival. You can redirect while moving. Successful deliveries earn 100 points plus twice the seconds remaining; missed deadlines subtract 25, with a minimum score of zero.

New requests appear every 9 seconds on Easy or every 6 seconds on Hard, capped at three active requests. Easy has faster movement and 35-second new deadlines; Hard has slower movement and 22-second new deadlines. Choose difficulty under **Game > Difficulty (next shift)**. It applies when starting/restarting, not mid-game.

Pause freezes movement, deadlines, and new requests. Instructions and About dialogs pause a running game automatically and resume it when closed. Restart asks before discarding a live shift. Results appear when the shift ends. Hover buildings for details.

For keyboard travel, click an open area of the campus map to focus it, use the arrow keys to select a building, then press Enter to set it as the destination. The selected building is outlined in orange.

## Event mapping

| Event or Swing class | Implementation |
| --- | --- |
| `MouseListener` via `MouseAdapter` | Building click and mouse exit in `CampusMap` |
| `MouseMotionListener` via `MouseMotionAdapter` | Building hover, cursor, and tooltip |
| `KeyListener` via `KeyAdapter` | Arrow-key building selection and Enter-to-travel on the focused map |
| `ItemListener` | Easy/Hard radio menu selection, responding only to the selected item |
| `ActionListener` | Buttons, command menu items, and timer callback |
| `JMenuBar`, `JMenu`, `JMenuItem`, `JRadioButtonMenuItem` | Game, difficulty, help, and exit menus |
| `javax.swing.Timer` | 40 ms game loop: runner animation, countdowns, generation, and end-of-game |
| `JPanel.paintComponent` | Campus map, highlighted route, runner, request badges, and overlays |
| `JOptionPane` | Instructions, restart confirmation, and results |

Confirm your instructor's exact required event classes: the assignment screenshot does not enumerate them. This PoC covers the three named topic areas, but does not implement drag-and-drop, blocked paths, sound, persistence, or pathfinding. The runner uses straight walkways connecting buildings.

## Source layout

- `src/main/java/campusrush/CampusRush.java`: window, layout, menus, mouse/key/item handlers, rendering, timer.
- `src/main/java/campusrush/GameModel.java`: game state, movement, deliveries, scoring, difficulty.
- `src/test/java/campusrush/GameModelChecks.java`: dependency-free game-rule regression checks.

The UI is created on the Swing event-dispatch thread. The Swing timer updates game state on that same thread.

## Verification of this proof of concept

The current sources compile against the Java 8 API with Java compiler lint enabled, and Apache NetBeans' bundled Maven builds the application JAR successfully. The standalone game-model checks passed. A desktop run and manual interaction with the new keyboard and difficulty controls were not exercised during this verification. No separate linter is configured. This is a source proof of concept, not a completed assignment submission.

## Build without NetBeans

With Maven: `mvn clean package`, then `java -jar target/CampusRush-1.0-SNAPSHOT.jar`.

With just a JDK (no Maven downloads):

```sh
mkdir -p target/classes target/test-classes
javac -d target/classes src/main/java/campusrush/*.java
java -cp target/classes campusrush.CampusRush
```

To run the focused model checks:

```sh
javac -cp target/classes -d target/test-classes src/test/java/campusrush/GameModelChecks.java
java -cp target/classes:target/test-classes campusrush.GameModelChecks
```

On Windows, replace the classpath separator `:` with `;`. The checks are a standalone main class; Maven's default test discovery does not run them automatically.

## Short demo sequence

Start Easy, click an open map area and use an arrow key plus Enter to travel, then click Engineering and show the score after arrival. Redirect to Library, pause and show frozen countdowns, resume, hover a building, then select Hard and restart. Show About to explain the mouse, key, item, action, menu, and timer event handling.
