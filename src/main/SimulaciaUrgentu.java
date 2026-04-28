package main;

import gui.MainGui;

import javax.swing.*;

public class SimulaciaUrgentu {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainGui gui = new MainGui();
            gui.setVisible(true);
        });
    }
}
