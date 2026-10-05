package campusrush;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Entry point: run this file or use Run Project in Apache NetBeans. */
@SuppressWarnings("serial") // Swing component serialization is not used by this application.
public final class CampusRush extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color INK = new Color(26, 45, 55);
    private static final Color TEAL = new Color(0, 118, 108);
    private static final Color PAPER = new Color(247, 247, 239);
    private final GameModel game = new GameModel(new Random());
    private final JLabel stats = new JLabel();
    private final JLabel status = new JLabel();
    private final JLabel requestHeading = new JLabel("DELIVERY BOARD");
    private final JButton[] requestButtons = new JButton[3];
    private final JButton start = new JButton("Start shift");
    private final JButton pause = new JButton("Pause");
    private final JMenuItem pauseMenu = new JMenuItem("Pause / Resume");
    private final JRadioButtonMenuItem easy = new JRadioButtonMenuItem("Easy", true);
    private final JRadioButtonMenuItem hard = new JRadioButtonMenuItem("Hard");
    private final CampusMap map = new CampusMap();
    private long lastTick = System.nanoTime();
    private final Timer timer;
    private final JMenuBar menuBar;

    public CampusRush() {
        super(new BorderLayout(20, 16));
        setPreferredSize(new Dimension(1120, 700));
        menuBar = createMenus();
        JPanel root = this;
        root.setBackground(PAPER); root.setBorder(new EmptyBorder(24, 26, 18, 26));

        JPanel header = new JPanel(new BorderLayout(16, 0)); header.setOpaque(false);
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 5)); titles.setOpaque(false);
        JLabel title = new JLabel("CAMPUS RUSH"); title.setFont(new Font("SansSerif", Font.BOLD, 30)); title.setForeground(INK);
        JLabel subtitle = new JLabel("DEADLINE RESCUE  /  One runner. A campus full of deadlines.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12)); subtitle.setForeground(TEAL);
        titles.add(title); titles.add(subtitle); header.add(titles, BorderLayout.WEST);
        stats.setForeground(INK); stats.setFont(new Font("Monospaced", Font.BOLD, 17));
        header.add(stats, BorderLayout.EAST); root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(18, 0)); center.setOpaque(false);
        center.add(map, BorderLayout.CENTER);
        JPanel board = new JPanel(); board.setLayout(new BoxLayout(board, BoxLayout.Y_AXIS));
        board.setOpaque(false); board.setPreferredSize(new Dimension(275, 450));
        requestHeading.setFont(new Font("SansSerif", Font.BOLD, 16)); requestHeading.setForeground(INK);
        board.add(requestHeading); board.add(Box.createVerticalStrut(8));
        JLabel hint = new JLabel("Click a request to head there."); hint.setForeground(TEAL); board.add(hint);
        board.add(Box.createVerticalStrut(18));
        for (int i = 0; i < requestButtons.length; i++) {
            final int slot = i;
            JButton button = new JButton(); button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setAlignmentX(Component.LEFT_ALIGNMENT); button.setMaximumSize(new Dimension(275, 110));
            button.setPreferredSize(new Dimension(275, 110)); button.setFont(new Font("SansSerif", Font.PLAIN, 14));
            button.setBackground(Color.WHITE); button.setForeground(INK); button.setFocusPainted(false);
            button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(218, 224, 218)), new EmptyBorder(14, 14, 14, 10)));
            button.addActionListener(e -> { if (slot < game.deliveries.size()) { game.travelTo(game.deliveries.get(slot).destination); refresh(); } });
            requestButtons[i] = button; board.add(button); board.add(Box.createVerticalStrut(12));
        }
        board.add(Box.createVerticalGlue());
        JLabel rules = new JLabel("<html><b>THE SHIFT</b><br><br>All items are already in your bag.<br>Reach a building to deliver them.<br><br>Successful delivery: 100 + time bonus<br>Missed deadline: -25 points<br>Maximum: 3 active requests</html>");
        rules.setForeground(INK); rules.setFont(new Font("SansSerif", Font.PLAIN, 12)); board.add(rules);
        center.add(board, BorderLayout.EAST); root.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(12, 12)); footer.setOpaque(false);
        status.setFont(new Font("SansSerif", Font.PLAIN, 13)); status.setForeground(INK); footer.add(status, BorderLayout.NORTH);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); controls.setOpaque(false);
        start.setBackground(TEAL); start.setForeground(Color.WHITE); start.setOpaque(true);
        start.addActionListener(e -> newGame()); pause.addActionListener(e -> togglePause());
        JButton help = new JButton("How to play"); help.addActionListener(e -> showHelp());
        controls.add(start); controls.add(pause); controls.add(help); footer.add(controls, BorderLayout.WEST);
        JLabel caption = new JLabel("Mouse + menu + timer events  /  Java Swing PoC"); caption.setForeground(TEAL);
        footer.add(caption, BorderLayout.EAST); root.add(footer, BorderLayout.SOUTH);

        // Swing Timer fires on the event-dispatch thread, keeping UI and model in sync.
        timer = new Timer(40, e -> {
            long now = System.nanoTime();
            double elapsed = (now - lastTick) / 1_000_000_000.0; lastTick = now;
            GameModel.State previous = game.state;
            game.tick(elapsed); refresh();
            if (previous == GameModel.State.RUNNING && game.state == GameModel.State.FINISHED) {
                JOptionPane.showMessageDialog(this, "Shift complete!\n\nScore: " + game.score + "\nDelivered: " + game.completed + "\nMissed: " + game.missed,
                        "Shift results", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        refresh(); timer.start();
    }

    private JMenuBar createMenus() {
        JMenuBar bar = new JMenuBar(); JMenu menu = new JMenu("Game"); menu.setMnemonic(KeyEvent.VK_G);
        JMenuItem restart = new JMenuItem("New Game");
        restart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        restart.addActionListener(e -> newGame()); menu.add(restart);
        pauseMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        pauseMenu.addActionListener(e -> togglePause()); menu.add(pauseMenu);
        JMenu difficulty = new JMenu("Difficulty (next shift)"); ButtonGroup group = new ButtonGroup();
        group.add(easy); group.add(hard); difficulty.add(easy); difficulty.add(hard);
        easy.addActionListener(e -> { status.setText("Easy selected for the next shift."); });
        hard.addActionListener(e -> { status.setText("Hard selected for the next shift."); });
        menu.add(difficulty); menu.addSeparator();
        JMenuItem exit = new JMenuItem("Exit"); exit.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
        }); menu.add(exit);
        JMenu help = new JMenu("Help"); JMenuItem instructions = new JMenuItem("How to play");
        instructions.addActionListener(e -> showHelp()); help.add(instructions);
        JMenuItem about = new JMenuItem("About / Event classes");
        about.addActionListener(e -> pausedDialog("Campus Rush - Java Swing proof of concept\n\nMouseListener: click a building\nMouseMotionListener: hover a building\nActionListener: buttons, menu items, and timer ticks\njavax.swing.Timer: movement, deadlines, request generation\n\nNo external assets or libraries.", "About")); help.add(about);
        bar.add(menu); bar.add(help); return bar;
    }

    private void newGame() {
        if (game.state == GameModel.State.RUNNING || game.state == GameModel.State.PAUSED) {
            boolean wasRunning = game.state == GameModel.State.RUNNING; game.state = GameModel.State.PAUSED;
            int answer = JOptionPane.showConfirmDialog(this, "Restart this shift? Current progress will be reset.", "New Game", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) { if (wasRunning) game.state = GameModel.State.RUNNING; lastTick = System.nanoTime(); refresh(); return; }
        }
        game.start(hard.isSelected()); lastTick = System.nanoTime(); refresh();
    }

    private void togglePause() { game.togglePause(); lastTick = System.nanoTime(); refresh(); }
    private void showHelp() {
        pausedDialog("Complete as many deliveries as possible in a two-minute shift.\n\n1. Start a shift. Your bag already contains every requested item.\n2. Click a building or delivery card to move the runner there.\n3. On arrival, matching requests are delivered automatically.\n4. Beat each request's deadline to earn points and a time bonus.\n\nYou may redirect the runner while moving. New requests appear regularly.\nEasy: faster runner, 35-second new requests.\nHard: slower runner, 22-second new requests.\n\nPause freezes movement, deadlines, and new requests.\nChoose difficulty from Game before starting your next shift.", "How to play");
    }
    private void pausedDialog(String text, String title) {
        boolean wasRunning = game.state == GameModel.State.RUNNING;
        if (wasRunning) game.state = GameModel.State.PAUSED;
        refresh(); JOptionPane.showMessageDialog(this, text, title, JOptionPane.INFORMATION_MESSAGE);
        if (wasRunning) game.state = GameModel.State.RUNNING;
        lastTick = System.nanoTime(); refresh();
    }

    private void refresh() {
        int seconds = (int) Math.ceil(game.remaining);
        stats.setText(String.format("%02d:%02d  |  %04d pts", seconds / 60, seconds % 60, game.score));
        status.setText(game.state == GameModel.State.PAUSED ? "Paused. Resume to continue your shift." : game.message);
        pause.setText(game.state == GameModel.State.PAUSED ? "Resume" : "Pause");
        boolean active = game.state == GameModel.State.RUNNING || game.state == GameModel.State.PAUSED;
        pause.setEnabled(active); pauseMenu.setEnabled(active); start.setText(active ? "Restart shift" : "Start shift");
        requestHeading.setText("DELIVERY BOARD / " + (game.hard ? "HARD" : "EASY"));
        for (int i = 0; i < requestButtons.length; i++) {
            JButton button = requestButtons[i];
            if (i < game.deliveries.size()) {
                GameModel.Delivery d = game.deliveries.get(i);
                String color = d.seconds <= 10 ? "#B74025" : "#00766C";
                button.setText("<html><b>" + d.item + "</b><br>" + GameModel.BUILDINGS[d.destination]
                        + "<br><font color='" + color + "'><b>" + (int) Math.ceil(d.seconds) + "s remaining</b></font></html>");
                button.setEnabled(game.state == GameModel.State.RUNNING);
            } else { button.setText("<html><font color='#7A8585'>" + (game.state == GameModel.State.READY ? "Start to receive requests" : "No active request") + "</font></html>"); button.setEnabled(false); }
        }
        map.repaint();
    }

    private final class CampusMap extends JPanel {
        private static final long serialVersionUID = 1L;
        private int hovered = -1;
        CampusMap() {
            setBackground(new Color(232, 240, 227)); setPreferredSize(new Dimension(700, 500));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { int b = buildingAt(e.getPoint()); if (b >= 0) { game.travelTo(b); refresh(); } }
                @Override public void mouseExited(MouseEvent e) { hovered = -1; setToolTipText(null); repaint(); }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    hovered = buildingAt(e.getPoint()); setCursor(Cursor.getPredefinedCursor(hovered >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    setToolTipText(hovered >= 0 ? GameModel.BUILDINGS[hovered] + ": click to travel and deliver matching requests" : null); repaint();
                }
            });
        }
        private int px(double x) { return (int) (x * getWidth()); }
        private int py(double y) { return (int) (y * getHeight()); }
        private Rectangle buildingRect(int i) { return new Rectangle(px(GameModel.LOCATIONS[i][0]) - 60, py(GameModel.LOCATIONS[i][1]) - 30, 120, 60); }
        private int buildingAt(Point p) { for (int i = 0; i < 5; i++) if (buildingRect(i).contains(p)) return i; return -1; }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics); Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(213, 226, 207));
            for (int x = 25; x < getWidth(); x += 48) for (int y = 35; y < getHeight(); y += 48) g.fillOval(x, y, 4, 4);
            g.setColor(new Color(210, 223, 200)); g.fill(new Ellipse2D.Double(getWidth() * .38, getHeight() * .39, getWidth() * .25, getHeight() * .25));
            // Straight campus walkways connect each building; no pathfinding is required.
            g.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.setColor(new Color(247, 246, 227));
            for (int i = 0; i < 5; i++) for (int j = i + 1; j < 5; j++)
                g.drawLine(px(GameModel.LOCATIONS[i][0]), py(GameModel.LOCATIONS[i][1]), px(GameModel.LOCATIONS[j][0]), py(GameModel.LOCATIONS[j][1]));
            if (game.destination >= 0) {
                g.setColor(TEAL); g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{7, 7}, 0));
                g.drawLine(px(game.x), py(game.y), px(GameModel.LOCATIONS[game.destination][0]), py(GameModel.LOCATIONS[game.destination][1]));
            }
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            for (int i = 0; i < 5; i++) {
                Rectangle r = buildingRect(i); int requests = 0;
                for (GameModel.Delivery d : game.deliveries) if (d.destination == i) requests++;
                g.setColor(new Color(0, 0, 0, 20)); g.fillRoundRect(r.x + 3, r.y + 5, r.width, r.height, 12, 12);
                g.setColor(i == hovered || i == game.destination ? TEAL : INK); g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
                g.setColor(Color.WHITE); String name = GameModel.BUILDINGS[i];
                g.drawString(name, r.x + (r.width - g.getFontMetrics().stringWidth(name)) / 2, r.y + 27);
                g.setFont(new Font("SansSerif", Font.PLAIN, 10)); String sub = i == 0 ? "RUNNER BASE" : "CLICK TO TRAVEL";
                g.drawString(sub, r.x + (r.width - g.getFontMetrics().stringWidth(sub)) / 2, r.y + 44); g.setFont(new Font("SansSerif", Font.BOLD, 12));
                if (requests > 0) { g.setColor(new Color(232, 116, 62)); g.fillOval(r.x + 105, r.y - 10, 24, 24); g.setColor(Color.WHITE); g.drawString("" + requests, r.x + 113, r.y + 7); }
            }
            int rx = px(game.x), ry = py(game.y);
            g.setColor(new Color(232, 116, 62)); g.fillOval(rx - 12, ry - 12, 24, 24);
            g.setStroke(new BasicStroke(3)); g.setColor(Color.WHITE); g.drawOval(rx - 12, ry - 12, 24, 24);
            g.setColor(INK); g.setFont(new Font("SansSerif", Font.BOLD, 11)); g.drawString("YOU", rx - 12, ry + 29);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12)); g.drawString("CAMPUS MAP  /  Click a building to set your destination", 20, 27);
            if (game.state != GameModel.State.RUNNING) {
                g.setColor(new Color(247, 247, 239, 230)); g.fillRoundRect(getWidth() / 2 - 155, getHeight() / 2 - 32, 310, 64, 16, 16);
                String text = game.state == GameModel.State.READY ? "READY FOR YOUR FIRST SHIFT?" : game.state == GameModel.State.PAUSED ? "SHIFT PAUSED" : "SHIFT COMPLETE";
                g.setColor(INK); g.setFont(new Font("SansSerif", Font.BOLD, 16)); g.drawString(text, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2, getHeight() / 2 + 5);
            }
            g.dispose();
        }
    }

    public JMenuBar getGameMenuBar() { return menuBar; }
    public void stopTimer() { timer.stop(); }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Campus Rush needs a desktop display. Run it in Apache NetBeans on your desktop.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
            catch (Exception ignored) { /* Default Swing look and feel remains usable. */ }
            CampusRush game = new CampusRush();
            JFrame frame = new JFrame("Campus Rush | Deadline Rescue");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(game); frame.setJMenuBar(game.getGameMenuBar());
            frame.setMinimumSize(new Dimension(900, 760)); frame.pack(); frame.setLocationRelativeTo(null);
            frame.addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) { game.stopTimer(); }
            });
            frame.setVisible(true);
        });
    }
}
