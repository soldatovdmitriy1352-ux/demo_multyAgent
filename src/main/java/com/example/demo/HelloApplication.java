package com.example.demo;

import com.example.demo.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Main entry point for the Swing-based Multi-Agent System.
 */
public class HelloApplication {
    private static final Logger logger = LoggerFactory.getLogger(HelloApplication.class);

    public static void main(String[] args) {
        // Load configuration
        new AppConfig();

        // Initialize UI on EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                logger.warn("Could not set look and feel: {}", e.getMessage());
            }

            // Create and show main window directly on EDT
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });

        logger.info("Multi-Agent System started");
    }
}
