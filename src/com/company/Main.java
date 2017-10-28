package com.company;

import com.sun.corba.se.impl.orbutil.graph.Graph;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Main {

    static Clock c;
    public static void main(String[] args) {

        /*
        int res1 = Integer.parseInt(JOptionPane.showInputDialog("Input first number of resolution: "));
        int res2 = Integer.parseInt(JOptionPane.showInputDialog("Input second number of resoultion: "));
        System.out.println("Resolution: " + res1 + " x " + res2);
        */

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        System.out.println("Resolution according to toolkit: " + width + " x " + height);

        c = new Clock(width, height);
    }

}

class Clock extends JFrame implements Runnable{

    private Thread thread;
    static JLayeredPane layeredPane = new JLayeredPane();
    int[] resolution = new int[2];
    ImageIcon bgIcon = new ImageIcon(this.getClass().getResource("resources/jack-o-lantern.png"));
    ImageIcon hourHandIcon = new ImageIcon(this.getClass().getResource("resources/broom-yellow.png"));
    ImageIcon minuteHandIcon = new ImageIcon(this.getClass().getResource("resources/broom-blue.png"));
    ImageIcon secondHandIcon = new ImageIcon(this.getClass().getResource("resources/broom-red.png"));
    ImageIcon numbersIcon = new ImageIcon(this.getClass().getResource("resources/halloween-numbers.png"));

    Image secondHand, minuteHand, hourHand;

    JLabel backgroundImage, numbersImage, hourHandImage, minuteHandImage, secondHandImage, digitalClock;

    BufferedImage biSecond, biMinute, biHour;
    //JPanel handsPanel;

    Calendar calendar = GregorianCalendar.getInstance();

    Date date = new Date(System.currentTimeMillis());
    int second, minute, hour;

    public Clock(int width, int height) {
        System.out.println(new Timestamp(System.currentTimeMillis()) + " Creating " + this.getClass().getName() + " thread and Clock object");
        resolution[0] = width;
        resolution[1] = height;

        initFrame();
    }


    public void initFrame() {
        setTitle("Halloween Clock");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(resolution[0], resolution[1]);
        getContentPane().setBackground(Color.BLACK);

        layeredPane.setLayout(new BorderLayout());
        layeredPane.setSize(this.getSize());
        layeredPane.setBackground(Color.BLACK);

        digitalClock = new JLabel(hour + ":" + minute + ":" + second);
        digitalClock.setFont(new Font("Courier New", 0, 32));
        digitalClock.setSize(getWidth() / 12, getWidth() / 36);
        digitalClock.setForeground(new Color(200, 0, 0, 200));
        add(digitalClock, BorderLayout.AFTER_LAST_LINE);

        Image background = bgIcon.getImage().getScaledInstance((int) Math.round(this.getWidth() * 0.5),
                (int) Math.round(this.getWidth() * 0.5), Image.SCALE_DEFAULT);

        backgroundImage = new JLabel(new ImageIcon(background));
        backgroundImage.setBounds(0, 0, resolution[0], resolution[1]);

        Image numbers = numbersIcon.getImage().getScaledInstance((int) Math.round(this.getWidth() * 0.55),
                (int) Math.round(this.getWidth() * 0.55), Image.SCALE_DEFAULT);

        numbersImage = new JLabel(new ImageIcon(numbers));
        numbersImage.setBounds(0, 0, resolution[0], resolution[1]);
        
        hourHand = hourHandIcon.getImage().getScaledInstance((int) Math.round(backgroundImage.getWidth() * 0.16),
                (int) Math.round(backgroundImage.getWidth() * 0.45), Image.SCALE_DEFAULT); //width must be 1/6 of height for proper proportions

        hourHandImage = new JLabel(new ImageIcon(hourHand));
        hourHandImage.setBounds(0, 0, resolution[0], resolution[1]);
        //hourPanel.add(hourHandImage);
        
        minuteHand = minuteHandIcon.getImage().getScaledInstance((int) Math.round(backgroundImage.getWidth() * 0.12),
                (int) Math.round(backgroundImage.getWidth() * 0.6), Image.SCALE_DEFAULT); //width must be 1/6 of height for proper proportions

        minuteHandImage = new JLabel(new ImageIcon(minuteHand));
        minuteHandImage.setBounds(0, 0, resolution[0], resolution[1]);
        //minutePanel.add(minuteHandImage);

        secondHand = secondHandIcon.getImage().getScaledInstance((int) Math.round(backgroundImage.getWidth() * 0.08),
                (int) Math.round(backgroundImage.getWidth() * 0.62), Image.SCALE_DEFAULT); //width must be 1/6 of height for proper proportions

        secondHandImage = new JLabel(new ImageIcon(secondHand));
        secondHandImage.setBounds(0, 0, resolution[0], resolution[1]);
        //secondPanel.add(secondHandImage);
        
        layeredPane.add(backgroundImage, BorderLayout.CENTER, new Integer(0)); //Integers are depth
        layeredPane.add(numbersImage, BorderLayout.CENTER, new Integer(0));
        //layeredPane.add(hourHandImage, BorderLayout.CENTER, new Integer(0));
        //layeredPane.add(minuteHandImage, BorderLayout.CENTER, new Integer(0));
        //layeredPane.add(secondHandImage, BorderLayout.CENTER, new Integer(0));
        initClockHands();

        add(layeredPane, BorderLayout.CENTER);

        setUndecorated(true); //makes it fullscreen
        setVisible(true);
        
        start();
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
                    } else { }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

    }
    
    public void initClockHands() {
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

        //System.out.println(hour + ":" + minute + ":" + second);
    }

    public void updateDigitalClock() {
        String strHour = "" + hour;
        String strMinute = "" + minute;
        String strSecond = "" + second;

        if (strHour.length() == 1) {
            strHour = "0" + strHour;
        }
        if (strMinute.length() == 1) {
            strMinute = "0" + strMinute;
        }
        if (strSecond.length() == 1) {
            strSecond = "0" + strSecond;
        }
        digitalClock.setText(strHour + ":" + strMinute + ":" + strSecond);
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

        while (true) {
            //run
            try {
                Thread.sleep(50);
                updateTimeVars();
                updateDigitalClock();
                repaint();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start () {
        System.out.println(new Timestamp(System.currentTimeMillis()) + " Starting " +  this.getClass().getName() + "!");
        if (thread == null) {
            thread = new Thread (this, "TimedEventsHandlerRunnable");
            thread.start();
        }
    }

}
