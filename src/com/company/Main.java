package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.Key;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;

public class Main {

    static Clock c;


    public static void main(String[] args) {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        System.out.println("Resolution according to toolkit: " + width + " x " + height);

        JFileChooser chooser = new JFileChooser(new File(currentDirectory().getPath()));

        chooser.setCurrentDirectory(currentDirectory());

        JOptionPane.showMessageDialog(null, "Please select the desired configuration file.", "Configuration", JOptionPane.INFORMATION_MESSAGE);
        int chooserReturnVal = chooser.showOpenDialog(null);

        HashMap<String, String> configHashMap;

        if (chooserReturnVal == JFileChooser.APPROVE_OPTION) {
            File configFile = chooser.getSelectedFile();
            ArrayList<String> lines = ConfigParser.readLinesOfFile(configFile);
            configHashMap = ConfigParser.buildConfigHashMap(lines);
            for (String line : lines) {
                System.out.println(line);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Using default configuration.", "Default", JOptionPane.INFORMATION_MESSAGE);
            configHashMap = ConfigParser.defaultConfiguration();
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

    static JLayeredPane layeredPane = new JLayeredPane();
    int[] resolution = new int[2];
    ImageIcon bgIcon, hourHandIcon, minuteHandIcon, secondHandIcon, numbersIcon;

    Image secondHand, minuteHand, hourHand;
    String digitalClockFont;

    JLabel backgroundImage, numbersImage, hourHandImage, minuteHandImage, secondHandImage, digitalClock, invisibleLabel;

    BufferedImage biSecond, biMinute, biHour;
    //JPanel handsPanel;

    Calendar calendar = GregorianCalendar.getInstance();

    Date date = new Date(System.currentTimeMillis());
    int second, minute, hour, deltaTime, amOrPM;

    boolean initialFullscreen;
    boolean doTickingSound;

    RenderingHints antiAliasing = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    HashMap<String, String> configHashMap;


    public Clock(int width, int height, HashMap<String, String> configHashMap) {
        System.out.println(new Timestamp(System.currentTimeMillis()) + " Creating " + this.getClass().getName() + " thread and Clock object");
        resolution[0] = width;
        resolution[1] = height;
        this.configHashMap = configHashMap;

        String resourceFolder = configHashMap.get("ResourcesFolder");
        digitalClockFont = configHashMap.get("DigitalClockFont");

        System.out.println(resourceFolder);

        bgIcon = new ImageIcon(this.getClass().getResource(resourceFolder + "/background.png"));
        hourHandIcon = new ImageIcon(this.getClass().getResource(resourceFolder + "/hour-hand.png"));
        minuteHandIcon = new ImageIcon(this.getClass().getResource(resourceFolder + "/minute-hand.png"));
        secondHandIcon = new ImageIcon(this.getClass().getResource(resourceFolder + "/second-hand.png"));
        numbersIcon = new ImageIcon(this.getClass().getResource(resourceFolder + "/numbers.png"));

        initialFullscreen = true;
        doTickingSound = false;

        initFrame();
    }


    public void initFrame() {
        setTitle("Halloween Clock by TornadoOfSoup");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(resolution[0], resolution[1]);

        getContentPane().setBackground(Color.decode(configHashMap.get("BackgroundColor")));

        layeredPane.setLayout(new BorderLayout());
        layeredPane.setSize(this.getSize());
        layeredPane.setBackground(Color.BLACK);

        digitalClock = new JLabel(hour + ":" + minute + ":" + second);
        digitalClock.setFont(new Font(digitalClockFont, Font.BOLD, 40));
        digitalClock.setSize(getWidth() / 12, getWidth() / 36);
        //digitalClock.setForeground(new Color(200, 0, 0, 150));
        digitalClock.setForeground(Color.decode(configHashMap.get("DigitalClockColor")));
        digitalClock.setBackground(new Color(0, 0, 0, 0));
        digitalClock.setOpaque(false);
        add(digitalClock, BorderLayout.AFTER_LAST_LINE);

        invisibleLabel = new JLabel("");

        
        double hourHandRatio = (double) hourHandIcon.getImage().getHeight(null) / hourHandIcon.getImage().getWidth(null);
        double minuteHandRatio = (double) minuteHandIcon.getImage().getHeight(null) / minuteHandIcon.getImage().getWidth(null);
        double secondHandRatio = (double) secondHandIcon.getImage().getHeight(null) / secondHandIcon.getImage().getWidth(null);
        

        Image background = bgIcon.getImage().getScaledInstance((int) Math.round(this.getWidth() * 0.5),
                (int) Math.round(this.getWidth() * 0.5), Image.SCALE_DEFAULT);

        backgroundImage = new JLabel(new ImageIcon(background));
        backgroundImage.setBounds(0, 0, resolution[0], resolution[1]);
        
        double hourHandLength = Double.parseDouble(configHashMap.get("HourHandLength"));
        double minuteHandLength = Double.parseDouble(configHashMap.get("MinuteHandLength"));
        double secondHandLength = Double.parseDouble(configHashMap.get("SecondHandLength"));
        
        Image numbers = numbersIcon.getImage().getScaledInstance((int) Math.round(background.getWidth(null) * 1),
                (int) Math.round(background.getWidth(null) * 1), Image.SCALE_DEFAULT);

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
        
        start();
        digitalClock.setVisible(false);
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
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });


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

    public void randomImageFlyBy (BufferedImage img, int speed) {

        int leftOrRight = r.nextInt(2);
        int initY = r.nextInt(resolution[1]);
        int initX;
        if (leftOrRight == 0) { //starting left
            initX = -200;
        } else { //starting right
            initX = resolution[0] + 200;
        }
        int finalY = r.nextInt(resolution[1]);
        double deltaY = -((double) (initY - finalY) / (resolution[0] / speed));

        System.out.println("initY: " + initY + " | finalY: " + finalY + " | deltaY: " + deltaY);

        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g;

                    if (leftOrRight == 0) { //starting left
                        g2d.drawImage(img, initX + (speed * deltaTime), initY + (int)(deltaY * deltaTime), null); //TODO figure out how to stop shared deltaTime from aligning all pictures
                    } else {
                        g2d.drawImage(img, initX - (speed * deltaTime), initY + (int)(deltaY * deltaTime), null);
                    }

            }
        };
        imagePanel.setSize(getSize());
        imagePanel.setBackground(new Color(0, 0, 0, 0));
        imagePanel.setOpaque(false);

        add(imagePanel, BorderLayout.CENTER, layeredPane.highestLayer());
    }

    public void conjureGhost() {
        BufferedImage ghost = null;
        try {
            int direction = r.nextInt(2);
            if (direction == 0) { //left
                ghost = ImageIO.read(this.getClass().getResource("resources/ghost-moving-right.png"));
                //randomImageFlyBy(rem, 10);
                add(new MovingImage(ghost, resolution, (r.nextInt(15) + 5), null, (r.nextFloat() + 0.2f) / 2, MovingImage.DIE_WHEN_OFF_SCREEN, MovingImage.LEFT_TO_RIGHT), BorderLayout.CENTER, layeredPane.highestLayer());
            } else { //right
                ghost = ImageIO.read(this.getClass().getResource("resources/ghost-moving-left.png"));
                //randomImageFlyBy(rem, 10);
                add(new MovingImage(ghost, resolution, (r.nextInt(15) + 5), null, (r.nextFloat() + 0.2f) / 2, MovingImage.DIE_WHEN_OFF_SCREEN, MovingImage.RIGHT_TO_LEFT), BorderLayout.CENTER, layeredPane.highestLayer());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void conjureSpoopy() {
        try {
            BufferedImage spoopy = ImageIO.read(this.getClass().getResource("resources/spoopy.png"));
            add(new MovingImage(spoopy, resolution, (r.nextInt(12) + 5), null, (r.nextFloat() + 0.5f) / 2, MovingImage.DIE_WHEN_OFF_SCREEN, MovingImage.RANDOM_DIRECTION), BorderLayout.CENTER, layeredPane.highestLayer());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void conjureTravis() {
        try {
            BufferedImage travis = ImageIO.read(this.getClass().getResource("resources/travis.png"));
            add(new MovingImage(travis, resolution, (r.nextInt(10) + 5), null, (r.nextFloat() + 0.3f) / 2, MovingImage.DIE_WHEN_OFF_SCREEN, MovingImage.RANDOM_DIRECTION), BorderLayout.CENTER, layeredPane.highestLayer());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void conjureRem() {
        BufferedImage rem = null;
        try {
            rem = ImageIO.read(this.getClass().getResource("resources/rem.png"));
            //randomImageFlyBy(rem, 10);
            add(new MovingImage(rem, resolution, (r.nextInt(15)+5), null, (r.nextFloat() + 0.3f) / 2, MovingImage.DIE_WHEN_OFF_SCREEN, MovingImage.RANDOM_DIRECTION), BorderLayout.CENTER, layeredPane.highestLayer());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void conjureGlowingRem() {
        BufferedImage rem = null;
        try {
            rem = ImageIO.read(this.getClass().getResource("resources/rem-glowing.png"));
            //randomImageFlyBy(rem, 10);
            add(new MovingImage(rem, resolution, (r.nextInt(15)+5), null, (r.nextFloat() + 0.7f) / 2, MovingImage.DIE_WHEN_OFF_SCREEN, MovingImage.RANDOM_DIRECTION), BorderLayout.CENTER, layeredPane.highestLayer());
        } catch (IOException ex) {
            ex.printStackTrace();
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


    @Override
    public void run() {
        int currentSecond = second;
        int framerate = Integer.parseInt(configHashMap.get("Framerate"));
        boolean doFlyingImages = Boolean.parseBoolean(configHashMap.get("FlyingImages"));
        while (true) {
            //run
            try {
                Thread.sleep(1000/framerate);
                updateTimeVars();
                updateDigitalClock();
                repaint();

                if (doTickingSound) {
                    if (currentSecond != second) {
                        SoundEffects.TICK.play();
                    }
                }


                if (doFlyingImages) {
                    if (r.nextInt(500) == 0) {
                        conjureGhost();
                    }

                    if (r.nextInt(300 * 60) == 0) {
                        conjureGlowingRem();
                    }

                    if (r.nextInt(1000) == 0) {
                        conjureSpoopy();
                    }

                    if (r.nextInt(600 * 60) == 0) {
                        conjureTravis();
                    }
                }

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


                currentSecond = second;
                deltaTime++;
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
