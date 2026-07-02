import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.io.File;
import javax.sound.sampled.*;

public class Main extends JFrame {

    private CardLayout cardLayout;
    private JPanel appContainer;
    private ScannerPanel scannerPanel;
    private ResultPage resultPage;
    private DashboardPage dashboardPage;

    private Timer scanTimer;
    private boolean scanning = false;
    private boolean flashOn = false;
    private int scanLineY = 190;

    private String currentImagePath = "food.png";

    private String foodName = "Salmon Grain Bowl";
    private int calories = 550;
    private int protein = 200;
    private int carbs = 120;
    private int fats = 150;
    private int fiber = 80;

    private int totalCalories = 0;
    private final int dailyGoal = 2000;

    public Main() {
        setTitle("NutriLens AI Scanner");
        setSize(390, 700);
        setMinimumSize(new Dimension(390, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        appContainer = new JPanel(cardLayout);

        scannerPanel = new ScannerPanel();
        appContainer.add(createScannerPage(), "scanner");

        resultPage = new ResultPage();
        JScrollPane resultScrollPane = new JScrollPane(resultPage);
        resultScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        resultScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        resultScrollPane.setBorder(null);
        appContainer.add(resultScrollPane, "result");

        dashboardPage = new DashboardPage();
        appContainer.add(dashboardPage, "dashboard");

        add(appContainer);
        setVisible(true);
    }

    private JPanel createScannerPage() {
        scannerPanel.setLayout(null);

        JLabel title = new JLabel("AI Scanner", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.BLACK);
        title.setBounds(115, 35, 160, 35);
        scannerPanel.add(title);

        JLabel close = new JLabel("×");
        close.setFont(new Font("Arial", Font.BOLD, 30));
        close.setForeground(Color.BLACK);
        close.setBounds(25, 35, 40, 35);
        scannerPanel.add(close);

        JLabel hint = new JLabel("Tap scan to analyze food", SwingConstants.CENTER);
        hint.setFont(new Font("Arial", Font.BOLD, 15));
        hint.setForeground(Color.WHITE);
        hint.setBounds(55, 515, 280, 30);
        scannerPanel.add(hint);

        JButton flashButton = createTextIconButton("FLASH");
        flashButton.setBounds(10, 575, 90, 40);
        scannerPanel.add(flashButton);

        JButton scanButton = createWhiteButton("SCAN");
        scanButton.setFont(new Font("Arial", Font.BOLD, 18));
        scanButton.setBounds(135, 560, 120, 60);
        scannerPanel.add(scanButton);

        JButton galleryButton = createTextIconButton("PHOTOS");
        galleryButton.setBounds(270, 575, 100, 40);
        scannerPanel.add(galleryButton);

      

        scanButton.addActionListener(e -> startScan());

        galleryButton.addActionListener(e -> {
            playButtonSound();
            chooseImage();
        });

        flashButton.addActionListener(e -> {
            playButtonSound();
            flashOn = !flashOn;
            flashButton.setText(flashOn ? "FLASH ON" : "FLASH");
            scannerPanel.repaint();
        });

        

        return scannerPanel;
    }

    private JButton createTextIconButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 9));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createWhiteButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createBlackButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createGreenButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(46, 125, 50));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose Food Image");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png"));

        int option = chooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            currentImagePath = selectedFile.getAbsolutePath();

            scannerPanel.setImage(currentImagePath);
            resultPage.setImage(currentImagePath);

            JOptionPane.showMessageDialog(this, "Image selected successfully. Click SCAN to analyze.");
        }
    }

    private void startScan() {
        playButtonSound();

        scanning = true;
        scanLineY = 190;

        scanTimer = new Timer(20, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scanLineY += 5;

                if (scanLineY > 450) {
                    scanTimer.stop();
                    scanning = false;
                    scannerPanel.repaint();

                    resultPage.updateResult(currentImagePath, foodName, calories, protein, carbs, fats, fiber);
                    cardLayout.show(appContainer, "result");
                }

                scannerPanel.repaint();
            }
        });

        scanTimer.start();
    }

    private void playButtonSound() {
        try {
            File soundFile = new File("scan.wav");

            if (!soundFile.exists()) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.start();

        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void drawCoverImage(Graphics g, Image image, int panelWidth, int panelHeight, ImageObserver observer) {
        int imgWidth = image.getWidth(observer);
        int imgHeight = image.getHeight(observer);

        if (imgWidth <= 0 || imgHeight <= 0) return;

        double scale = Math.max((double) panelWidth / imgWidth, (double) panelHeight / imgHeight);
        int newWidth = (int) (imgWidth * scale);
        int newHeight = (int) (imgHeight * scale);

        int x = (panelWidth - newWidth) / 2;
        int y = (panelHeight - newHeight) / 2;

        g.drawImage(image, x, y, newWidth, newHeight, observer);
    }

    class ScannerPanel extends JPanel {
        private Image background;

        public ScannerPanel() {
            setImage(currentImagePath);
        }

        public void setImage(String path) {
            background = new ImageIcon(path).getImage();
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (background != null) {
                drawCoverImage(g, background, getWidth(), getHeight(), this);
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(flashOn ? new Color(255, 255, 255, 25) : new Color(0, 0, 0, 45));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setStroke(new BasicStroke(5));
            g2.setColor(Color.WHITE);

            int x = 55;
            int y = 190;
            int w = 280;
            int h = 260;
            int corner = 50;

            g2.drawLine(x, y, x + corner, y);
            g2.drawLine(x, y, x, y + corner);
            g2.drawLine(x + w - corner, y, x + w, y);
            g2.drawLine(x + w, y, x + w, y + corner);
            g2.drawLine(x, y + h - corner, x, y + h);
            g2.drawLine(x, y + h, x + corner, y + h);
            g2.drawLine(x + w - corner, y + h, x + w, y + h);
            g2.drawLine(x + w, y + h - corner, x + w, y + h);

            if (scanning) {
                g2.setColor(new Color(0, 255, 100, 220));
                g2.setStroke(new BasicStroke(4));
                g2.drawLine(x, scanLineY, x + w, scanLineY);
            }
        }
    }

    class ResultPage extends JPanel {

        private ResultImagePanel imagePanel;
        private JLabel foodNameLabel;
        private JLabel caloriesLabel;
        private JLabel proteinLabel;
        private JLabel carbsLabel;
        private JLabel fatsLabel;
        private JLabel fiberLabel;

        public ResultPage() {
            setLayout(null);
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(390, 800));

            imagePanel = new ResultImagePanel();
            imagePanel.setBounds(0, 0, 390, 430);
            imagePanel.setLayout(null);
            add(imagePanel);

            imagePanel.add(createFoodTag("Salmon", 55, 155));
            imagePanel.add(createFoodTag("Quinoa", 195, 230));
            imagePanel.add(createFoodTag("Avocado", 245, 300));
            imagePanel.add(createFoodTag("Cucumber", 120, 300));

            JPanel card = new JPanel();
            card.setLayout(null);
            card.setBackground(Color.WHITE);
            card.setBounds(0, 400, 390, 380);
            add(card);

            JLabel mealType = new JLabel("Breakfast");
            mealType.setFont(new Font("Arial", Font.PLAIN, 12));
            mealType.setBounds(20, 15, 200, 20);
            card.add(mealType);

            foodNameLabel = new JLabel("Salmon Grain Bowl");
            foodNameLabel.setFont(new Font("Arial", Font.BOLD, 22));
            foodNameLabel.setBounds(20, 40, 330, 35);
            card.add(foodNameLabel);

            JPanel nutritionBox = new JPanel();
            nutritionBox.setLayout(null);
            nutritionBox.setBackground(new Color(245, 245, 245));
            nutritionBox.setBounds(20, 90, 350, 165);
            card.add(nutritionBox);

            JLabel totalTitle = new JLabel("Total nutrition");
            totalTitle.setFont(new Font("Arial", Font.BOLD, 13));
            totalTitle.setBounds(15, 10, 150, 20);
            nutritionBox.add(totalTitle);

            caloriesLabel = new JLabel("550 kcal");
            caloriesLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            caloriesLabel.setBounds(15, 30, 100, 18);
            nutritionBox.add(caloriesLabel);

            JLabel ingredientLabel = new JLabel("4 ingredients");
            ingredientLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            ingredientLabel.setBounds(230, 20, 120, 20);
            nutritionBox.add(ingredientLabel);

            proteinLabel = createNutrientLabel("Protein", "200 kcal", 15, 60);
            carbsLabel = createNutrientLabel("Carbs", "120 kcal", 15, 85);
            fatsLabel = createNutrientLabel("Fats", "150 kcal", 15, 110);
            fiberLabel = createNutrientLabel("Fiber", "80 kcal", 15, 135);

            nutritionBox.add(proteinLabel);
            nutritionBox.add(carbsLabel);
            nutritionBox.add(fatsLabel);
            nutritionBox.add(fiberLabel);

            JButton updateButton = createBlackButton("Update details");
            updateButton.setBounds(25, 280, 140, 42);
            card.add(updateButton);

            JButton addMealButton = createBlackButton("Add meal");
            addMealButton.setBounds(215, 280, 140, 42);
            card.add(addMealButton);

            updateButton.addActionListener(e -> {
                playButtonSound();
                JOptionPane.showMessageDialog(Main.this, "Details updated successfully.");
            });

            addMealButton.addActionListener(e -> {
                playButtonSound();
                totalCalories += calories;
                dashboardPage.updateDashboard();
                JOptionPane.showMessageDialog(Main.this, foodName + " added successfully!");
                cardLayout.show(appContainer, "dashboard");
            });
        }

        public void setImage(String path) {
            imagePanel.setImage(path);
        }

        public void updateResult(String imagePath, String name, int cal, int pro, int carb, int fat, int fib) {
            imagePanel.setImage(imagePath);
            foodNameLabel.setText(name);
            caloriesLabel.setText(cal + " kcal");
            proteinLabel.setText(formatNutrient("Protein", pro + " kcal"));
            carbsLabel.setText(formatNutrient("Carbs", carb + " kcal"));
            fatsLabel.setText(formatNutrient("Fats", fat + " kcal"));
            fiberLabel.setText(formatNutrient("Fiber", fib + " kcal"));
            repaint();
        }

        private JLabel createFoodTag(String text, int x, int y) {
            JLabel label = new JLabel(text, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(new Color(245, 245, 245));
            label.setForeground(Color.BLACK);
            label.setFont(new Font("Arial", Font.PLAIN, 9));
            label.setBounds(x, y, 75, 18);
            return label;
        }

        private JLabel createNutrientLabel(String name, String value, int x, int y) {
            JLabel label = new JLabel(formatNutrient(name, value));
            label.setForeground(Color.BLACK);
            label.setFont(new Font("Arial", Font.PLAIN, 12));
            label.setBounds(x, y, 320, 18);
            return label;
        }

        private String formatNutrient(String name, String value) {
            return name + "                                      " + value;
        }
    }

    class ResultImagePanel extends JPanel {
        private Image background;

        public ResultImagePanel() {
            setImage(currentImagePath);
        }

        public void setImage(String path) {
            background = new ImageIcon(path).getImage();
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (background != null) {
                drawCoverImage(g, background, getWidth(), getHeight(), this);
            }
        }
    }

    class DashboardPage extends JPanel {
        private JLabel totalLabel;
        private JLabel remainingLabel;
        private JProgressBar progressBar;

        public DashboardPage() {
            setLayout(null);
            setBackground(Color.WHITE);

            JLabel title = new JLabel("NutriLens Dashboard", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 24));
            title.setForeground(new Color(46, 125, 50));
            title.setBounds(30, 50, 330, 40);
            add(title);

            JLabel subtitle = new JLabel("Daily Calorie Summary", SwingConstants.CENTER);
            subtitle.setFont(new Font("Arial", Font.BOLD, 16));
            subtitle.setBounds(30, 100, 330, 30);
            add(subtitle);

            totalLabel = new JLabel("Today's Calories: 0 kcal", SwingConstants.CENTER);
            totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
            totalLabel.setBounds(30, 180, 330, 40);
            add(totalLabel);

            remainingLabel = new JLabel("Remaining: 2000 kcal", SwingConstants.CENTER);
            remainingLabel.setFont(new Font("Arial", Font.BOLD, 18));
            remainingLabel.setBounds(30, 230, 330, 40);
            add(remainingLabel);

            progressBar = new JProgressBar(0, dailyGoal);
            progressBar.setBounds(45, 300, 300, 35);
            progressBar.setStringPainted(true);
            progressBar.setString("0 / 2000 kcal");
            add(progressBar);

            JButton backButton = createGreenButton("Back to Scanner");
            backButton.setBounds(105, 520, 180, 45);
            add(backButton);

            backButton.addActionListener(e -> {
                playButtonSound();
                cardLayout.show(appContainer, "scanner");
            });
        }

        public void updateDashboard() {
            int remaining = dailyGoal - totalCalories;

            totalLabel.setText("Today's Calories: " + totalCalories + " kcal");

            if (remaining >= 0) {
                remainingLabel.setText("Remaining: " + remaining + " kcal");
            } else {
                remainingLabel.setText("Exceeded by: " + Math.abs(remaining) + " kcal");
            }

            progressBar.setValue(Math.min(totalCalories, dailyGoal));
            progressBar.setString(totalCalories + " / " + dailyGoal + " kcal");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}