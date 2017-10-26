package com.company;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        int res1 = Integer.parseInt(JOptionPane.showInputDialog("Input first number of resolution: "));
        int res2 = Integer.parseInt(JOptionPane.showInputDialog("Input second number of resoultion: "));

        System.out.println("Resolution: " + res1 + " x " + res2);
    }
}
