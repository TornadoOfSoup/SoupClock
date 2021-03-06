package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

import static com.company.Main.currentDirectory;
import static com.company.Utils.*;

public class Main {

    static Clock c;


    public static void main(String[] args) {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        System.out.println("Resolution according to toolkit: " + width + " x " + height);
        HashMap<String, String> configHashMap = new HashMap<>();

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("default")) {
                configHashMap = ConfigParser.defaultConfiguration(true);
                configHashMap.put("AutoStart", "true");
            } else {
                if (args[0].equals("config") && args.length >= 2) {
                    File configFile = new File(args[1]);
                    ArrayList<String> lines = ConfigParser.readLinesOfFile(configFile);
                    configHashMap = ConfigParser.buildConfigHashMap(lines);
                    configHashMap.put("AutoStart", "true");
                    for (String line : lines) {
                        System.out.println(line);
                    }
                } else {
                    configHashMap = ConfigParser.defaultConfiguration(true);
                }
            }
        } else {
            JFileChooser chooser = new JFileChooser(new File(currentDirectory().getPath()));

            chooser.setCurrentDirectory(currentDirectory());

            JOptionPane.showMessageDialog(null, "Please select the desired configuration file.", "Configuration", JOptionPane.INFORMATION_MESSAGE);
            int chooserReturnVal = chooser.showOpenDialog(null);

            if (chooserReturnVal == JFileChooser.APPROVE_OPTION) {
                File configFile = chooser.getSelectedFile();
                ArrayList<String> lines = ConfigParser.readLinesOfFile(configFile);
                configHashMap = ConfigParser.buildConfigHashMap(lines);
                for (String line : lines) {
                    System.out.println(line);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Using default configuration.", "Default", JOptionPane.INFORMATION_MESSAGE);
                configHashMap = ConfigParser.defaultConfiguration(true);
            }
        }

        c = new Clock(width, height, configHashMap);
    }

    public static File currentDirectory() {
        try {
            return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

}

class Clock extends JFrame implements Runnable{

    private Thread thread;

    static Random r = new Random();
    static String resourceFolder, generalResourceFolder;

    int[] resolution = new int[2];
    ImageIcon bgIcon, hourHandIcon, minuteHandIcon, secondHandIcon, numbersIcon;

    Image secondHand, minuteHand, hourHand;
    String digitalClockFont;
    JPanel guiPanel, schedulePanel, datePanel;

    JLabel backgroundImage, numbersImage, hourHandImage, minuteHandImage, secondHandImage, digitalClock,
            invisibleLabel, dateLabel;

    BufferedImage biSecond, biMinute, biHour;
    //JPanel handsPanel;

    Calendar calendar = GregorianCalendar.getInstance();


    Date date = new Date(System.currentTimeMillis());
    int second, minute, hour, deltaTime, amOrPM, year, month, day;

    int scheduleSize;
    int dateFormat;

    boolean initialFullscreen, doTickingSound, doSnow;

    RenderingHints antiAliasing = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    HashMap<String, String> configHashMap;
    ArrayList<Period> schedulePeriods;
    Schedule schedule;

    ArrayList<JLabel> periods = new ArrayList<>();

    static JLayeredPane layeredPane = new JLayeredPane();
    static JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    public Clock(int width, int height, HashMap<String, String> configHashMap) {
        System.out.println(new Timestamp(System.currentTimeMillis()) + " Creating " + this.getClass().getName() + " thread and Clock object");
        resolution[0] = width;
        resolution[1] = height;
        this.configHashMap = configHashMap;

        resourceFolder = configHashMap.get("ResourcesFolder");
        generalResourceFolder = resourceFolder.substring(0, resourceFolder.lastIndexOf("/")) + "/general"; //TODO test to see if this actually goes to the resources folder
        digitalClockFont = configHashMap.get("DigitalClockFont");

        System.out.println(resourceFolder);
        System.out.println(generalResourceFolder);

        bgIcon = new ImageIcon(this.getClass().getResource(resourceFolder + "/background.png"));
        hourHandIcon = new ImageIcon(this.getClass().getResource(resourceFolder + "/hour-hand.png"));
        minuteHandIcon = new ImageIcon(this.getClass().getResource(resourceFolder + "/minute-hand.png"));
        secondHandIcon = new ImageIcon(this.getClass().getResource(resourceFolder + "/second-hand.png"));
        numbersIcon = new ImageIcon(this.getClass().getResource(resourceFolder + "/numbers.png"));

        initialFullscreen = true;
        doTickingSound = false;
        doSnow = Boolean.parseBoolean(configHashMap.get("DoSnow"));

        boolean autoStart = false;

        if (configHashMap.containsKey("AutoStart")) {
            autoStart = Boolean.parseBoolean(configHashMap.get("AutoStart"));
            if (autoStart == true) {
                schedule = new Schedule(0);
            }
        }
        if (autoStart == false) {
            int scheduleNumber = Integer.parseInt(JOptionPane.showInputDialog("Please input a number representing which schedule you want to use.\n" +
                    "(0 for none, 1 for normal, 2 for H Period at beginning of day,\n" +
                    " 3 for H Period at end of day, 4 for a custom schedule)"));

            if (scheduleNumber == 4) {
                JOptionPane.showMessageDialog(null, "Please select the desired schedule file.", "Schedule", JOptionPane.INFORMATION_MESSAGE);
                JFileChooser chooser = new JFileChooser(new File(currentDirectory().getPath()));
                int scheduleChooserVal = chooser.showOpenDialog(null);

                if (scheduleChooserVal == JFileChooser.APPROVE_OPTION) {
                    ArrayList<String> lines = ScheduleParser.readLinesOfFile(chooser.getSelectedFile());
                    schedule = ScheduleParser.buildSchedule(lines);
                } else {
                    JOptionPane.showMessageDialog(null, "Using default schedule.", "Default", JOptionPane.INFORMATION_MESSAGE);
                    schedule = new Schedule(1);
                }
            } else {
                schedule = new Schedule(scheduleNumber);
            }
        }
        scheduleSize = Integer.parseInt(configHashMap.get("ScheduleSize"));
        dateFormat = Integer.parseInt(configHashMap.get("DateFormat"));

        MailCheckRunnable mailRunnable = new MailCheckRunnable();
        mailRunnable.start();

        initFrame();
    }


    public void initFrame() {
        double clockSizeRatio = Double.parseDouble(configHashMap.get("ClockSize"));
        double numberClockRatio = Double.parseDouble(configHashMap.get("NumberClockRatio"));
        setTitle("Halloween Clock by TornadoOfSoup");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(resolution[0], resolution[1]);

        getContentPane().setBackground(Color.decode(configHashMap.get("BackgroundColor")));

        layeredPane.setLayout(new BorderLayout());
        layeredPane.setSize(this.getSize());
        layeredPane.setBackground(Color.BLACK);

        digitalClock = new JLabel(hour + ":" + minute + ":" + second);
        digitalClock.setFont(new Font(digitalClockFont, Font.BOLD, Integer.parseInt(configHashMap.get("DigitalClockSize"))));
        digitalClock.setSize(getWidth() / 12, getWidth() / 36);
        //digitalClock.setForeground(new Color(200, 0, 0, 150));
        digitalClock.setForeground(Color.decode(configHashMap.get("DigitalClockColor")));
        digitalClock.setBackground(new Color(0, 0, 0, 0));
        digitalClock.setOpaque(false);
        add(digitalClock, BorderLayout.SOUTH);

        invisibleLabel = new JLabel("");

        double hourHandRatio = (double) hourHandIcon.getImage().getHeight(null) / hourHandIcon.getImage().getWidth(null);
        double minuteHandRatio = (double) minuteHandIcon.getImage().getHeight(null) / minuteHandIcon.getImage().getWidth(null);
        double secondHandRatio = (double) secondHandIcon.getImage().getHeight(null) / secondHandIcon.getImage().getWidth(null);

        Image background = bgIcon.getImage().getScaledInstance((int) Math.round(this.getHeight() * clockSizeRatio),
                (int) Math.round(this.getHeight() * clockSizeRatio), Image.SCALE_DEFAULT);

        backgroundImage = new JLabel(new ImageIcon(background));
        backgroundImage.setBounds(0, 0, resolution[0], resolution[1]);

        double hourHandLength = Double.parseDouble(configHashMap.get("HourHandLength"));
        double minuteHandLength = Double.parseDouble(configHashMap.get("MinuteHandLength"));
        double secondHandLength = Double.parseDouble(configHashMap.get("SecondHandLength"));

        Image numbers = numbersIcon.getImage().getScaledInstance((int) Math.round(background.getWidth(null) * numberClockRatio),
                (int) Math.round(background.getWidth(null) * numberClockRatio), Image.SCALE_DEFAULT);

        numbersImage = new JLabel(new ImageIcon(numbers));
        numbersImage.setBounds(0, 0, resolution[0], resolution[1]);

        hourHand = hourHandIcon.getImage().getScaledInstance((int) Math.round(backgroundImage.getWidth() * hourHandLength / hourHandRatio),
                (int) Math.round(backgroundImage.getWidth() * hourHandLength), Image.SCALE_DEFAULT); //width must be 1/6 of height for proper proportions

        hourHandImage = new JLabel(new ImageIcon(hourHand));
        hourHandImage.setBounds(0, 0, resolution[0], resolution[1]);
        //hourPanel.add(hourHandImage);

        minuteHand = minuteHandIcon.getImage().getScaledInstance((int) Math.round(backgroundImage.getWidth() * minuteHandLength / minuteHandRatio),
                (int) Math.round(backgroundImage.getWidth() * minuteHandLength), Image.SCALE_DEFAULT); //width must be 1/6 of height for proper proportions

        minuteHandImage = new JLabel(new ImageIcon(minuteHand));
        minuteHandImage.setBounds(0, 0, resolution[0], resolution[1]);
        //minutePanel.add(minuteHandImage);

        secondHand = secondHandIcon.getImage().getScaledInstance((int) Math.round(backgroundImage.getWidth() * secondHandLength / secondHandRatio),
                (int) Math.round(backgroundImage.getWidth() * secondHandLength), Image.SCALE_DEFAULT); //width must be 1/6 of height for proper proportions

        secondHandImage = new JLabel(new ImageIcon(secondHand));
        secondHandImage.setBounds(0, 0, resolution[0], resolution[1]);
        //secondPanel.add(secondHandImage);

        layeredPane.add(backgroundImage, BorderLayout.CENTER, new Integer(0)); //Integers are depth
        layeredPane.add(numbersImage, BorderLayout.CENTER, new Integer(0));
        layeredPane.add(invisibleLabel, BorderLayout.CENTER, new Integer(0)); //fixes analog clock numbers being moved by digital clock
        //layeredPane.add(hourHandImage, BorderLayout.CENTER, new Integer(0));
        //layeredPane.add(minuteHandImage, BorderLayout.CENTER, new Integer(0));
        //layeredPane.add(secondHandImage, BorderLayout.CENTER, new Integer(0));

        createClockHands();
        //createLightningHandler(0, 0);

        add(layeredPane, BorderLayout.CENTER);

        setUndecorated(initialFullscreen); //makes it fullscreen
        setVisible(true);

        createGUI();

        start();
        digitalClock.setVisible(Boolean.parseBoolean(configHashMap.get("DigitalClock")));
        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("KeyType event fired, key is " + e.getKeyChar());
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    int x = JOptionPane.showConfirmDialog(null,"Do you want to shutdown?", "Shutdown", JOptionPane.YES_NO_OPTION);
                    if (x == 0) {
                        System.exit(0);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    digitalClock.setFont(new Font("Courier New", Font.BOLD, digitalClock.getFont().getSize() + 2));
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!(digitalClock.getFont().getSize() <= 2)) {
                        digitalClock.setFont(new Font("Courier New", Font.BOLD, digitalClock.getFont().getSize() - 2));
                    }
                } else if (e.getKeyChar() == '0') { //defaults
                    digitalClock.setFont(new Font("Courier New", Font.BOLD, 40));
                    digitalClock.setVisible(true);
                    doTickingSound = false;
                } else if (e.getKeyChar() == '1') {
                    digitalClock.setVisible(!digitalClock.isVisible());
                } else if (e.getKeyChar() == '2') {
                    dispose();
                    setUndecorated(!isUndecorated());
                    setVisible(true);
                } else if (e.getKeyChar() == '3') {
                    doTickingSound = !doTickingSound;
                } else if (e.getKeyChar() == '4') {
                    conjureRem();
                } else if (e.getKeyChar() == '5') {
                    conjureGlowingRem();
                } else if (e.getKeyChar() == '6') {
                    conjureGhost();
                } else if (e.getKeyChar() == '7') {
                    conjureSpoopy();
                } else if (e.getKeyChar() == '8') {
                    conjureTravis();
                } else if (e.getKeyChar() == '9') {
                    digitalClock.setText("Made by TornadoOfSoup");
                } else if (e.getKeyChar() == 'd') {
                    dateLabel.setVisible(!dateLabel.isVisible());
                } else if (e.getKeyChar() == '[') {
                    scheduleSize++;
                } else if (e.getKeyChar() == ']') {
                    scheduleSize--;
                } else if (e.getKeyChar() == '\\') {
                    scheduleSize = Integer.parseInt(configHashMap.get("ScheduleSize"));
                } else if (e.getKeyChar() == '-') {
                    //conjureControlledImage(snowflake1, 200, -50, randomNumberBetweenTwoFloats(-2, 2), 10, 0.9f);
                } else if (e.getKeyChar() == '=') {
                    //conjureControlledImage(snowflake2, 200, -50, randomNumberBetweenTwoFloats(-2, 2), 10, 0.9f);
                } else if (e.getKeyChar() == 's') {
                    doSnow = !doSnow;
                } else if (e.getKeyChar() == 'r') {
                    Utils.restart("default");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });


    }

    public void createGUI() {
        guiPanel = new JPanel(new BorderLayout());
        guiPanel.setOpaque(false);
        guiPanel.setBackground(new Color(0, 0, 0, 0));

        //createSchedulePanel(); //commented out because digital clock is taking its place
        //TODO properly refactor any confusion between digital clock and schedule
        createDatePanel();

        add(guiPanel);
    }

    /*
    public void createSchedulePanel() {
        schedulePanel = new JPanel();
        schedulePanel.setLayout(new BoxLayout(schedulePanel, BoxLayout.Y_AXIS));
        schedulePanel.setOpaque(false);
        schedulePanel.setBackground(new Color(0, 0, 0, 0));
        Color periodLabelColor = Color.decode(configHashMap.get("ScheduleColor"));
        Font periodFont = new Font(configHashMap.get("ScheduleFont"), Font.PLAIN, scheduleSize);

        schedulePeriods = schedule.getPeriods();

        int longestEntryLength = 0;

        for (Map.Entry<String, Period> entry : schedulePeriods.entrySet()) {
            if (entry.getKey().length() > longestEntryLength) {
                longestEntryLength = entry.getKey().length();
            }
        }

        for (Map.Entry<String, Period> entry : schedulePeriods.entrySet()) {
            String periodName = entry.getKey();
            Period period = entry.getValue();

            int numOfSpaces = longestEntryLength - (periodName.length()) + 2;

            if (numOfSpaces < 1) {
                numOfSpaces = 1;
            }

            JLabel label = new JLabel(periodName + ": ".replace(" ",
                    multiplyString(" ", numOfSpaces)) + period.toString() + " ");
            label.setForeground(periodLabelColor);
            label.setFont(periodFont);
            label.setOpaque(true);
            label.setAlignmentY(Component.CENTER_ALIGNMENT);
            periods.add(label);
            //System.out.println(label.getText());
        }

        String scheduleLocation = configHashMap.get("ScheduleLocation");

        if (scheduleLocation.equals("CENTER") || scheduleLocation.equals("BOTTOM")) {
            schedulePanel.add(Box.createVerticalGlue());
        }

        if (!schedule.getName().equals("")) {
            JLabel nameLabel = new JLabel(schedule.getName());
            nameLabel.setHorizontalAlignment(JLabel.CENTER);
            nameLabel.setForeground(periodLabelColor);
            nameLabel.setBackground(new Color(0, 0, 0, 0));
            nameLabel.setFont(periodFont);
            nameLabel.setOpaque(true);
            nameLabel.setSize(getLargestLabelSizeInList(periods));
            periods.add(0, nameLabel);
        }
        for (JLabel label : periods) {
            schedulePanel.add(label, BorderLayout.WEST);
        }

        if (scheduleLocation.equals("CENTER") || scheduleLocation.equals("TOP")) {
            schedulePanel.add(Box.createVerticalGlue());
        }
        guiPanel.add(schedulePanel);
    }
*/

    public void createDatePanel() {
        datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        datePanel.setOpaque(false);
        datePanel.setBackground(new Color(0, 0, 0, 0));

        dateLabel = new JLabel(month + "/" + day + "/" + year);
        dateLabel.setForeground(Color.decode(configHashMap.get("DigitalClockColor")));
        dateLabel.setFont(new Font(digitalClockFont, Font.BOLD, Integer.parseInt(configHashMap.get("DigitalClockSize"))));

        datePanel.add(dateLabel);

        guiPanel.add(datePanel, BorderLayout.SOUTH);
    }

    public void createClockHands() {
        updateTimeVars();

        JPanel hourPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(getWidth(), getHeight());
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                biHour = toBufferedImage(hourHand);

                Graphics2D g2dHours = (Graphics2D) g;
                g2dHours.setRenderingHints(antiAliasing);

                g2dHours.rotate(Math.toRadians(((hour - 0) * 30) + (minute * 0.5)), getWidth() / 2, getHeight() / 2);
                g2dHours.drawImage(biHour, (getWidth() - biHour.getWidth(null)) / 2,
                        (getHeight() - biHour.getHeight(null)) / 2, null);
            }
        };
        hourPanel.setBackground(new Color(0, 0,0, 0));
        hourPanel.setSize(getSize());
        hourPanel.setOpaque(false);

        add(hourPanel, BorderLayout.CENTER, layeredPane.highestLayer());

        JPanel minutePanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(getWidth(), getHeight());
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                biMinute = toBufferedImage(minuteHand);

                Graphics2D g2dMinutes = (Graphics2D) g;
                g2dMinutes.setRenderingHints(antiAliasing);

                g2dMinutes.rotate(Math.toRadians(minute * 6), getWidth() / 2, getHeight() / 2);
                g2dMinutes.drawImage(biMinute, (getWidth() - biMinute.getWidth(null)) / 2,
                        (getHeight() - biMinute.getHeight(null)) / 2, null);
            }
        };
        minutePanel.setBackground(new Color(0, 0,0, 0));
        minutePanel.setSize(getSize());
        minutePanel.setOpaque(false);

        add(minutePanel, BorderLayout.CENTER, layeredPane.highestLayer());

        JPanel secondPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(getWidth(), getHeight());
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                biSecond = toBufferedImage(secondHand);

                Graphics2D g2dSeconds = (Graphics2D) g;
                g2dSeconds.setRenderingHints(antiAliasing);

                g2dSeconds.rotate(Math.toRadians(second * 6), getWidth() / 2, getHeight() / 2);
                g2dSeconds.drawImage(biSecond, (getWidth() - biSecond.getWidth(null)) / 2,
                        (getHeight() - biSecond.getHeight(null)) / 2, null);
            }
        };
        secondPanel.setBackground(new Color(0, 0,0, 0));
        secondPanel.setSize(getSize());
        secondPanel.setOpaque(false);

        add(secondPanel, BorderLayout.CENTER, layeredPane.highestLayer());
    }

    public void createLightningHandler(int intervalInSeconds, int variationInSeconds) { //TODO make this work
        JPanel lightningPanel = new JPanel() {
            @Override
            public void paintComponents(Graphics g) {
                super.paintComponents(g);

                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.WHITE);
                g2d.drawLine(getWidth() / 3, 0, getWidth() / 3, 100);
                g2d.drawLine(getWidth() / 3 * 2, 0, getWidth() / 3 * 2, 100);
            }
        };

        lightningPanel.setSize(getSize());
        //lightningPanel.setBackground(new Color(0, 0, 0, 0));
        lightningPanel.setOpaque(false);

        add(lightningPanel, BorderLayout.CENTER, layeredPane.highestLayer());
    }

    public void conjureGhost() {
        BufferedImage ghost = null;
        try {
            int direction = r.nextInt(2);
            if (direction == 0) { //left
                ghost = ImageIO.read(this.getClass().getResource(resourceFolder + "/ghost-moving-right.png"));
                //randomImageFlyBy(rem, 10);
                add(new MovingImage(ghost, resolution, (r.nextInt(15) + 5), null, (r.nextFloat() + 0.2f) / 2, MovingImage.DIE_WHEN_OFF_SCREEN, MovingImage.LEFT_TO_RIGHT), BorderLayout.CENTER, layeredPane.highestLayer());
            } else { //right
                ghost = ImageIO.read(this.getClass().getResource(resourceFolder + "/ghost-moving-left.png"));
                //randomImageFlyBy(rem, 10);
                add(new MovingImage(ghost, resolution, (r.nextInt(15) + 5), null, (r.nextFloat() + 0.2f) / 2, MovingImage.DIE_WHEN_OFF_SCREEN, MovingImage.RIGHT_TO_LEFT), BorderLayout.CENTER, layeredPane.highestLayer());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void conjureSpoopy() {
        try {
            BufferedImage spoopy = ImageIO.read(this.getClass().getResource(resourceFolder + "/spoopy.png"));
            add(new MovingImage(spoopy, resolution, (r.nextInt(12) + 5), null, (r.nextFloat() + 0.5f) / 2, MovingImage.DIE_WHEN_OFF_SCREEN, MovingImage.RANDOM_DIRECTION), BorderLayout.CENTER, layeredPane.highestLayer());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void conjureTravis() {
        try {
            BufferedImage travis = ImageIO.read(this.getClass().getResource(resourceFolder + "/travis.png"));
            add(new MovingImage(travis, resolution, (r.nextInt(10) + 5), null, (r.nextFloat() + 0.3f) / 2, MovingImage.DIE_WHEN_OFF_SCREEN, MovingImage.RANDOM_DIRECTION), BorderLayout.CENTER, layeredPane.highestLayer());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void conjureRem() {
        BufferedImage rem = null;
        try {
            rem = ImageIO.read(this.getClass().getResource(resourceFolder + "/rem.png"));
            //randomImageFlyBy(rem, 10);
            add(new MovingImage(rem, resolution, (r.nextInt(15)+5), null, (r.nextFloat() + 0.3f) / 2, MovingImage.DIE_WHEN_OFF_SCREEN, MovingImage.RANDOM_DIRECTION), BorderLayout.CENTER, layeredPane.highestLayer());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void conjureGlowingRem() {
        BufferedImage rem = null;
        try {
            rem = ImageIO.read(this.getClass().getResource(resourceFolder + "/rem-glowing.png"));
            //randomImageFlyBy(rem, 10);
            add(new MovingImage(rem, resolution, (r.nextInt(15)+5), null, (r.nextFloat() + 0.7f) / 2, MovingImage.DIE_WHEN_OFF_SCREEN, MovingImage.RANDOM_DIRECTION), BorderLayout.CENTER, layeredPane.highestLayer());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void conjureFlyingImage(String path) {
        try {
            BufferedImage image = ImageIO.read(this.getClass().getResource(path));
            add(new MovingImage(image, resolution, (r.nextInt(12) + 5), null, (r.nextFloat() + 0.5f) / 2, MovingImage.DIE_WHEN_OFF_SCREEN, MovingImage.RANDOM_DIRECTION), BorderLayout.CENTER, layeredPane.highestLayer());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void conjureFlyingImage(String[] imageArgs) {
        String path = "", startingSide = "";
        String[] speedBounds = new String[0], opacityBounds = new String[0];
        try {
            path = imageArgs[0];
            startingSide = imageArgs[2];
            speedBounds = imageArgs[3].split("-");
            opacityBounds = imageArgs[4].split("-");
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        try {
            int direction;
            if (startingSide.equalsIgnoreCase("left")) {
                direction = MovingImage.LEFT_TO_RIGHT;
            } else if (startingSide.equalsIgnoreCase("right")) {
                direction = MovingImage.RIGHT_TO_LEFT;
            } else {
                direction = MovingImage.RANDOM_DIRECTION;
            }
            int lowerSpeedBound = Integer.parseInt(speedBounds[0]);
            int upperSpeedBound = Integer.parseInt(speedBounds[1]);

            float lowerOpacityBound = Float.parseFloat(opacityBounds[0]);
            float upperOpacityBound = Float.parseFloat(opacityBounds[1]);
            float opacity;

            BufferedImage image = ImageIO.read(this.getClass().getResource(path));
            if (upperOpacityBound == lowerOpacityBound) {
                opacity = upperOpacityBound;
            } else {
                opacity = randomNumberBetweenTwoFloats(lowerOpacityBound, upperOpacityBound);
            }
            add(new MovingImage(image, resolution, (r.nextInt(upperSpeedBound - lowerSpeedBound) + lowerSpeedBound), null,
                    opacity, MovingImage.DIE_WHEN_OFF_SCREEN, direction), BorderLayout.CENTER, layeredPane.highestLayer());
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            for (String s : imageArgs) {
                System.out.println(s);
            }
        }
    }

    /**
     * Used for conjuring images with more control.
     * Set topmostLayer to true to render on top of clock, false to render it behind the clock.
     * @param image
     * @param initX
     * @param initY
     * @param deltaX
     * @param deltaY
     * @param alpha
     * @param topmostLayer
     */
    public void conjureControlledImage(BufferedImage image, int initX, int initY, double deltaX, double deltaY, float alpha, boolean topmostLayer) {
        if (topmostLayer) {
            add(new MovingImage(image, resolution, deltaX, deltaY, initX, initY, null, alpha, MovingImage.DIE_WHEN_OFF_SCREEN), BorderLayout.CENTER, new Integer(0));
        } else {
            add(new MovingImage(image, resolution, deltaX, deltaY, initX, initY, null, alpha, MovingImage.DIE_WHEN_OFF_SCREEN));
        }
    }


    public void initClockHands_NONWORKING() {
        /*updateTimeVars();

        BufferedImage biSecond = toBufferedImage(secondHand);

        secondPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(biSecond.getWidth(), biSecond.getHeight());
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.rotate(Math.toRadians(second * 6), this.getWidth() / 2, this.getHeight() / 2);
                g2.drawImage(biSecond, (this.getWidth() - biSecond.getWidth(null)) / 2,
                        (this.getHeight() - biSecond.getHeight(null)) / 2, null);
            }
        };
        secondPanel.setBackground(new Color(0, 0,0, 0));
        secondPanel.setOpaque(false);

        BufferedImage biMinute = toBufferedImage(minuteHand);
        minutePanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(biMinute.getWidth(), biMinute.getHeight());
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.rotate(Math.toRadians(minute * 6), this.getWidth() / 2, this.getHeight() / 2);
                g2.drawImage(biMinute, (this.getWidth() - biMinute.getWidth(null)) / 2,
                        (this.getHeight() - biMinute.getHeight(null)) / 2, null);
            }
        };
        minutePanel.setBackground(new Color(0, 0,0, 0));
        minutePanel.setOpaque(false);

        BufferedImage biHour = toBufferedImage(hourHand);
        hourPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(biHour.getWidth(), biHour.getHeight());
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.rotate(Math.toRadians(hour * 15), this.getWidth() / 2, this.getHeight() / 2);
                g2.drawImage(biHour, (this.getWidth() - biHour.getWidth(null)) / 2,
                        (this.getHeight() - biHour.getHeight(null)) / 2, null);
            }
        };
        hourPanel.setBackground(new Color(0, 0,0, 0));
        hourPanel.setOpaque(false);

        //add(hourPanel, BorderLayout.CENTER, layeredPane.highestLayer());
        //add(minutePanel, BorderLayout.CENTER, layeredPane.highestLayer() + 1);
        //add(secondPanel, BorderLayout.CENTER, layeredPane.highestLayer() + 2);
        */
    }


    public void updateTimeVars() {
        date = new Date(System.currentTimeMillis());
        calendar.setTime(date);

        second = calendar.get(Calendar.SECOND);
        minute = calendar.get(Calendar.MINUTE);
        hour = calendar.get(Calendar.HOUR);
        amOrPM = calendar.get(Calendar.AM_PM);

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);

        //System.out.println(hour + ":" + minute + ":" + second);
    }

    public void updateDigitalClock() {
        String strHour = "" + hour;
        String strMinute = "" + minute;
        String strSecond = "" + second;
        String amOrPM;

        if (this.amOrPM == calendar.AM) {
            amOrPM = "AM";
        } else if (this.amOrPM == calendar.PM){
            amOrPM = "PM";
        } else {
            amOrPM = "" + this.amOrPM;
        }

        if (strHour.length() == 1) {
            strHour = "0" + strHour;
        }
        if (strMinute.length() == 1) {
            strMinute = "0" + strMinute;
        }
        if (strSecond.length() == 1) {
            strSecond = "0" + strSecond;
        }

        if (strHour.equals("00")) { //if it's 12 o'clock don't display 00
            strHour = "12";
        }

        digitalClock.setText(strHour + ":" + strMinute + ":" + strSecond + " " + amOrPM);
    }

    public void updateDate() {

        if (dateFormat == 1) {
            String yearString = (year + "").substring(2);
            dateLabel.setText(month + "/" + day + "/" + yearString);
        } else if (dateFormat == 2){
            dateLabel.setText(month + "/" + day);
        } else {
            dateLabel.setText(month + "/" + day + "/" + year);
        }
    }

    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    public BufferedImage toBufferedImage(Image img) //taken from https://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public void removeCompletedImages() {
        for (Component c: getContentPane().getComponents()) {
            if (c instanceof MovingImage) {
                if (((MovingImage) c).completed) {
                    getContentPane().remove(c);
                    //System.out.println("Removed " + c.getName());
                }
            }
        }
    }


    @Override
    public void run() {
        int currentSecond = second;
        int framerate = Integer.parseInt(configHashMap.get("Framerate"));
        boolean doFlyingImages = Boolean.parseBoolean(configHashMap.get("FlyingImages"));
        boolean doSchedule = Boolean.parseBoolean(configHashMap.get("Schedule"));
        String[] images = configHashMap.get("RandomImages").split("\\|");
        Color periodHighlightColor = Color.decode(configHashMap.get("PeriodHighlightColor"));
        //periodHighlightColor = new Color(periodHighlightColor.getRed(), periodHighlightColor.getGreen(), periodHighlightColor.getBlue(), 128);
        //commented out because the period list isn't a thing anymore
        //TODO proper refactoring

        BufferedImage snowflake1 = null, doge = null;
        float snowSizeFactor = Float.parseFloat(configHashMap.get("SnowSizeFactor"));
        try {
            snowflake1 = ImageIO.read(this.getClass().getResource(generalResourceFolder + "/snowflake1.png"));
            doge = ImageIO.read(this.getClass().getResource(generalResourceFolder + "/doge.png"));

            snowflake1 = toBufferedImage(snowflake1.getScaledInstance((int)(snowflake1.getWidth() * snowSizeFactor),
                    (int)(snowflake1.getHeight() * snowSizeFactor), Image.SCALE_DEFAULT));

            doge = toBufferedImage(doge.getScaledInstance((int)(doge.getWidth() * snowSizeFactor),
                    (int)(doge.getHeight() * snowSizeFactor), Image.SCALE_DEFAULT));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < images.length; i++) {
            images[i] = images[i].trim();
        }

        int snowMinX = (int) (0 - (resolution[0] * 0.5));
        int snowMaxX = (int) (resolution[0] * 1.5);
        int snowDeltaX = Integer.parseInt(configHashMap.get("SnowDeltaX"));
        float snowDeltaYMin = Integer.parseInt(configHashMap.get("SnowDeltaYMin"));
        float snowDeltaYMax = Integer.parseInt(configHashMap.get("SnowDeltaYMax"));
        double snowPerFrame = Double.parseDouble(configHashMap.get("SnowPerFrame"));
        double internalSnowFactor = 0;

        if (doSchedule) {
            schedulePeriods = schedule.getPeriods();
        }

        while (true) {
            //run
            Font periodFont = new Font(configHashMap.get("ScheduleFont"), Font.PLAIN, scheduleSize);
            try {
                Thread.sleep(1000/framerate);
                updateTimeVars();
                //updateDigitalClock(); //commented out to replace digital clock with schedule
                //TODO proper refactoring
                updateDate();

                if (doTickingSound) {
                    if (currentSecond != second) {
                        SoundEffects.TICK.play();
                    }
                }

                if (doFlyingImages) {
                    for (String image : images) {
                        String[] imageArgs = image.split(", ");
                        if (imageArgs.length == 5) {
                            if (r.nextInt((int) Math.round(framerate * Double.parseDouble(imageArgs[1]))) == 0) {
                                conjureFlyingImage(imageArgs);
                            }
                        }
                    }
                }

                if (doSchedule) {
                    for (Period period : schedulePeriods){
                        String timesString = period.getTimeString();

                        Time startTime = period.getStartTime();
                        Time endTime = period.getEndTime();
                        Time currentTime;

                        if (amOrPM == Calendar.PM) {
                            currentTime = parseTime((hour + 12) + ":" + minute + ":" + second);
                        } else {
                            currentTime = parseTime(hour + ":" + minute + ":" + second);
                        }
                        if (currentTime.after(endTime)) {
                            continue;
                        } else if (currentTime.after(startTime)) {
                            //TODO make this work lol
                            System.out.println(period.name + ": " + timesString);
                            digitalClock.setText(period.name + ": " + timesString);
                        }

                    }

                    /*for (JLabel label : periods) {

                        String[] times = label.getText().substring(label.getText().indexOf(":") + 1, label.getText().length())
                            .split(" - "); //should get the times of the period
                        if ((!schedule.getName().isEmpty())) {

                            if (!periodFont.equals(label.getFont())) { //still have to do this
                                label.setFont(periodFont);
                            }

                            i++; //now i doesn't equal 0 so it will process as normal
                        } else {

                            //System.out.println(times[0] + "   |   " + times[1]);
                            Time startTime = parseTimeWithMeridian(times[0].replace(" ", ""));
                            Time endTime = parseTimeWithMeridian(times[1].replace(" ", ""));

                            if (!periodFont.equals(label.getFont())) {
                                label.setFont(periodFont);
                            }

                            Time currentTime;
                            if (amOrPM == Calendar.PM) {
                                currentTime = parseTime((hour + 12) + ":" + minute + ":" + second);
                            } else {
                                currentTime = parseTime(hour + ":" + minute + ":" + second);
                            }
                            if ((currentTime.after(startTime) || currentTime.equals(startTime)) && currentTime.before(endTime)) {
                                int index = periods.indexOf(label);
                                //System.out.println("time in period " + index);
                                label.setBackground(periodHighlightColor);
                                periods.set(index, label);
                            } else {
                                label.setBackground(new Color(0, 0, 0, 0));
                            }
                        }
                    }*/
                }

                if (doSnow) {
                    //TODO possibly add a variable wind (which would be deltaX) that changes over time
                    internalSnowFactor += snowPerFrame;
                    while (internalSnowFactor >= 1) {
                        int x = randomNumberWithinBounds(snowMinX, snowMaxX);
                        if (r.nextInt(1000) == 0) {
                            conjureControlledImage(doge, x, -doge.getHeight(), snowDeltaX, randomNumberBetweenTwoFloats(snowDeltaYMin, snowDeltaYMax), 0.95f, true); //1 in 1000 chance to be a doge instead
                        } else {
                            if (r.nextInt(3) == 0) { // 1/3 of snowflakes are topmost layer
                                conjureControlledImage(snowflake1, x, -snowflake1.getHeight(), snowDeltaX, randomNumberBetweenTwoFloats(snowDeltaYMin, snowDeltaYMax), 0.95f, true);
                            } else {
                                conjureControlledImage(snowflake1, x, -snowflake1.getHeight(), snowDeltaX, randomNumberBetweenTwoFloats(snowDeltaYMin, snowDeltaYMax), 0.95f, false);
                            }
                        }
                        internalSnowFactor -= 1;
                    }
                }

                /*
                if (hour == 8 && minute == 15 && amOrPM == calendar.AM) {
                    if (second >= 0 && second <= 10) {
                        conjureGhost();
                        if (second % 2 == 0) {
                            conjureSpoopy();
                        }
                    }
                }

                if (hour == 8 && minute == 19 && amOrPM == calendar.AM) {
                    if (second >= 0 && second <= 5) {
                        conjureGhost();
                    }
                }


                if (hour == 9 && minute == 7 && amOrPM == calendar.AM) {
                    if (second >= 0 && second <= 10) {
                        conjureGhost();
                        if (second % 2 == 0) {
                            conjureSpoopy();
                        }
                    }
                }

                if (hour == 9 && minute == 11 && amOrPM == calendar.AM) {
                    if (second >= 0 && second <= 5) {
                        conjureGhost();
                    }
                }

                if (hour == 9 && minute == 59 && amOrPM == calendar.AM) {
                    if (second >= 0 && second <= 10) {
                        conjureGhost();
                        if (second % 2 == 0) {
                            conjureSpoopy();
                        }
                    }
                }

                if (hour == 10 && minute == 3 && amOrPM == calendar.AM) {
                    if (second >= 0 && second <= 5) {
                        conjureGhost();
                    }
                }

                if (hour == 10 && minute == 51 && amOrPM == calendar.AM) {
                    if (second >= 0 && second <= 10) {
                        conjureGhost();
                        if (second % 2 == 0) {
                            conjureSpoopy();
                        }
                    }
                }

                if (hour == 10 && minute == 55 && amOrPM == calendar.AM) {
                    if (second >= 0 && second <= 5) {
                        conjureGhost();
                    }
                }

                if (hour == 11 && minute == 43 && amOrPM == calendar.AM) {
                    if (second >= 0 && second <= 10) {
                        conjureGhost();
                        if (second % 2 == 0) {
                            conjureSpoopy();
                        }
                    }
                }

                if (hour == 11 && minute == 47 && amOrPM == calendar.AM) {
                    if (second >= 0 && second <= 5) {
                        conjureGhost();
                    }
                }

                if (hour == 12 && minute == 35 && amOrPM == calendar.PM) {
                    if (second >= 0 && second <= 15) {
                        conjureGhost();
                        if (second % 2 == 0) {
                            conjureSpoopy();
                        }
                        if (second % 3 == 0) {
                            conjureGlowingRem();
                        }
                    }
                }

                if (hour == 1 && minute == 5 && amOrPM == calendar.PM) {
                    if (second >= 0 && second <= 5) {
                        conjureGhost();
                    }
                }

                if (hour == 1 && minute == 53 && amOrPM == calendar.PM) {
                    if (second >= 0 && second <= 10) {
                        conjureGhost();
                        if (second % 2 == 0) {
                            conjureSpoopy();
                        }
                    }
                }

                if (hour == 1 && minute == 57 && amOrPM == calendar.PM) {
                    if (second >= 0 && second <= 5) {
                        conjureGhost();
                    }
                }

                if (hour == 2 && minute == 45 && amOrPM == calendar.PM) {
                    conjureGhost();
                    conjureSpoopy();
                    conjureGlowingRem();
                    if (second >= 25 && second <= 35) {
                        conjureTravis();
                    }
                }

                if (hour == 4 && minute == 20 && amOrPM == calendar.PM) {
                    if (second <= 30) {
                        conjureSpoopy();
                        if (second % 2 == 0) {
                            conjureGlowingRem();
                        }
                    }
                }

*/
                removeCompletedImages();
                currentSecond = second;
                deltaTime++;
                repaint();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start () {
        System.out.println(new Timestamp(System.currentTimeMillis()) + " Starting " +  this.getClass().getName() + "!");
        if (thread == null) {
            thread = new Thread (this, "ClockRunnable");
            thread.start();
        }
    }

}
