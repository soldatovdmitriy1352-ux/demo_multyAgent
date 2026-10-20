package com.example.demo;

import com.example.demo.orchestrator.OrchestratorResult;
import com.example.demo.orchestrator.Verdict;
import com.example.demo.service.AgentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.File;

/**
 * Main window for the Multi-Agent System (Swing UI).
 */
public class MainWindow extends JFrame {
    private static final Color DARK_BACKGROUND = new Color(30, 30, 30);
    private static final Color DARK_PANEL = new Color(45, 45, 45);
    private static final Color DARK_TEXT = new Color(212, 212, 212);
    private static final Color ACCENT_BLUE = new Color(86, 156, 214);
    private static final Color BUTTON_GREEN = new Color(22, 130, 93);
    private static final Color BUTTON_RED = new Color(140, 29, 29);

    // UI Components
    private JTextArea logArea;
    private JTextArea resultArea;
    private JTextArea taskInput;
    private JTextField modelInput;
    private JTextField urlInput;
    private JTextField buildDirInput;
    private JPasswordField apiKeyInput;
    private JComboBox<String> providerCombo;
    private JButton runBtn;
    private JButton stopBtn;
    private JCheckBox plannerCheck;
    private JCheckBox coderCheck;
    private JCheckBox testerCheck;
    private JCheckBox criticCheck;
    private JCheckBox builderCheck;
    private JLabel statusLabel;

    private AgentService agentService;
    private final String[] providers = {"openai", "ollama", "openrouter", "custom"};

    public MainWindow() {
        setTitle("🤖 Multi-Agent System");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize service
        agentService = new AgentService();

        // Create main panel with BorderLayout
        setLayout(new BorderLayout(10, 10));
        add(createTopPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
        add(createStatusBar(), BorderLayout.PAGE_END);

        logArea.setText("Система готова к работе.\n");
    }

    /**
     * Top panel: settings + agents + buttons.
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(DARK_BACKGROUND);
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("🤖 Multi-Agent System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_BLUE);
        topPanel.add(titleLabel, BorderLayout.PAGE_START);

        // Content: left (settings) + right (agents + buttons)
        JPanel content = new JPanel(new GridLayout(1, 2, 15, 0));
        content.setBackground(DARK_BACKGROUND);

        content.add(createSettingsPanel());
        content.add(createAgentsPanel());

        topPanel.add(content, BorderLayout.CENTER);
        return topPanel;
    }

    /**
     * Left column: settings (provider, model, URL, API key, task).
     */
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 1, 0, 10));
        panel.setBackground(DARK_BACKGROUND);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(63, 63, 70)),
                "⚙️ Настройки",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                ACCENT_BLUE
        ));

        // Provider
        panel.add(new JLabel("Провайдер:"));
        providerCombo = new JComboBox<>(providers);
        providerCombo.setBackground(DARK_PANEL);
        providerCombo.setForeground(DARK_TEXT);
        panel.add(providerCombo);

        // Model
        panel.add(new JLabel("Модель:"));
        modelInput = new JTextField("bean");
        modelInput.setBackground(DARK_PANEL);
        modelInput.setForeground(DARK_TEXT);
        panel.add(modelInput);

        // URL
        panel.add(new JLabel("URL API:"));
        urlInput = new JTextField("https://ollama.com/api");
        urlInput.setBackground(DARK_PANEL);
        urlInput.setForeground(DARK_TEXT);
        panel.add(urlInput);

        // API Key
        panel.add(new JLabel("API Key:"));
        apiKeyInput = new JPasswordField("");
        apiKeyInput.setBackground(DARK_PANEL);
        apiKeyInput.setForeground(DARK_TEXT);
        panel.add(apiKeyInput);

        // Task
        panel.add(new JLabel("Задача:"));
        taskInput = new JTextArea(5, 30);
        taskInput.setBackground(DARK_PANEL);
        taskInput.setForeground(DARK_TEXT);
        taskInput.setLineWrap(true);
        taskInput.setWrapStyleWord(true);
        JScrollPane taskScroll = new JScrollPane(taskInput);
        taskScroll.setBackground(DARK_PANEL);
        panel.add(taskScroll);

        return panel;
    }

    /**
     * Right column: agents checkboxes + build dir + run/stop buttons.
     */
    private JPanel createAgentsPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 10));
        panel.setBackground(DARK_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Agents section
        JPanel agentsPanel = new JPanel(new GridLayout(6, 1, 0, 5));
        agentsPanel.setBackground(DARK_BACKGROUND);
        agentsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(63, 63, 70)),
                "👥 Агенты",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                ACCENT_BLUE
        ));

        plannerCheck = new JCheckBox("Planner", true);
        coderCheck = new JCheckBox("Coder", true);
        testerCheck = new JCheckBox("Tester", true);
        criticCheck = new JCheckBox("Critic", true);
        builderCheck = new JCheckBox("Builder", true);

        updateCheckBoxStyle(plannerCheck);
        updateCheckBoxStyle(coderCheck);
        updateCheckBoxStyle(testerCheck);
        updateCheckBoxStyle(criticCheck);
        updateCheckBoxStyle(builderCheck);

        agentsPanel.add(plannerCheck);
        agentsPanel.add(coderCheck);
        agentsPanel.add(testerCheck);
        agentsPanel.add(criticCheck);
        agentsPanel.add(builderCheck);
        panel.add(agentsPanel);

        // Build directory
        JPanel dirPanel = new JPanel(new BorderLayout(5, 0));
        dirPanel.setBackground(DARK_BACKGROUND);
        dirPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(63, 63, 70)),
                "📁 Директория сборки",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                ACCENT_BLUE
        ));

        buildDirInput = new JTextField("./build");
        buildDirInput.setBackground(DARK_PANEL);
        buildDirInput.setForeground(DARK_TEXT);
        dirPanel.add(buildDirInput, BorderLayout.CENTER);

        JButton browseBtn = new JButton("📂 Обзор");
        browseBtn.setBackground(DARK_PANEL);
        browseBtn.setForeground(DARK_TEXT);
        browseBtn.setFocusPainted(false);
        browseBtn.addActionListener(e -> onBrowseDir());
        dirPanel.add(browseBtn, BorderLayout.EAST);

        panel.add(dirPanel);

        // Run / Stop buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(DARK_BACKGROUND);

        runBtn = new JButton("▶ Запустить");
        runBtn.setBackground(BUTTON_GREEN);
        runBtn.setForeground(Color.WHITE);
        runBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        runBtn.setFocusPainted(false);
        runBtn.addActionListener(e -> onRunAgents());
        buttonPanel.add(runBtn);

        stopBtn = new JButton("⏹ Остановить");
        stopBtn.setBackground(BUTTON_RED);
        stopBtn.setForeground(Color.WHITE);
        stopBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        stopBtn.setFocusPainted(false);
        stopBtn.addActionListener(e -> onStopAgents());
        stopBtn.setEnabled(false);
        buttonPanel.add(stopBtn);

        panel.add(buttonPanel);
        return panel;
    }

    /**
     * Bottom panel: log + result tabs.
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(DARK_BACKGROUND);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(DARK_PANEL);
        logArea.setForeground(DARK_TEXT);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBackground(DARK_PANEL);

        // Result area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBackground(DARK_PANEL);
        resultArea.setForeground(DARK_TEXT);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBackground(DARK_PANEL);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(DARK_PANEL);
        tabs.setForeground(DARK_TEXT);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.addTab("📋 Лог", logScroll);
        tabs.addTab("✅ Результат", resultScroll);

        bottomPanel.add(tabs, BorderLayout.CENTER);
        return bottomPanel;
    }

    /**
     * Status bar at the bottom.
     */
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = new JLabel("Готово");
        statusLabel.setForeground(DARK_TEXT);
        panel.add(statusLabel, BorderLayout.WEST);

        return panel;
    }

    private void updateCheckBoxStyle(JCheckBox checkBox) {
        checkBox.setBackground(DARK_BACKGROUND);
        checkBox.setForeground(DARK_TEXT);
    }

    private void onBrowseDir() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File directory = fileChooser.getSelectedFile();
            buildDirInput.setText(directory.getAbsolutePath());
        }
    }

    private void onRunAgents() {
        String task = taskInput.getText().trim();
        if (task.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, введите задачу.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String provider = (String) providerCombo.getSelectedItem();
        String model = modelInput.getText().trim();
        String baseUrl = urlInput.getText().trim();
        String apiKey = new String(apiKeyInput.getPassword());
        String buildDir = buildDirInput.getText().trim();

        if (buildDir.isEmpty()) {
            buildDir = "./build";
        }

        // Update UI
        runBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        statusLabel.setText("Запуск...");
        logArea.setText("");
        resultArea.setText("");

        addLog("Запуск мультиагентной системы...");
        addLog("Провайдер: " + provider);
        addLog("Модель: " + model);
        addLog("URL: " + baseUrl);
        addLog("Директория: " + buildDir);

        // Check which agents are enabled
        java.util.List<String> enabledAgents = new java.util.ArrayList<>();
        if (plannerCheck.isSelected()) enabledAgents.add("planner");
        if (coderCheck.isSelected()) enabledAgents.add("coder");
        if (testerCheck.isSelected()) enabledAgents.add("tester");
        if (criticCheck.isSelected()) enabledAgents.add("critic");
        if (builderCheck.isSelected()) enabledAgents.add("builder");

        addLog("Включены агенты: " + String.join(", ", enabledAgents));

        // Run the service
        agentService.run(task, provider, model, baseUrl, apiKey, buildDir, new AgentService.UpdateCallback() {
            @Override
            public void onLog(String message) {
                SwingUtilities.invokeLater(() -> addLog(message));
            }

            @Override
            public void onStatus(String status) {
                SwingUtilities.invokeLater(() -> statusLabel.setText(status));
            }

            @Override
            public void onSuccess(OrchestratorResult result) {
                SwingUtilities.invokeLater(() -> {
                    addLog("✅ Оркестрация завершена успешно!");
                    displayResult(result);
                    runBtn.setEnabled(true);
                    stopBtn.setEnabled(false);
                });
            }

            @Override
            public void onError(String error) {
                SwingUtilities.invokeLater(() -> {
                    addLog("❌ Ошибка: " + error);
                    statusLabel.setText("Ошибка");
                    runBtn.setEnabled(true);
                    stopBtn.setEnabled(false);
                });
            }
        });
    }

    private void onStopAgents() {
        agentService.stop();
        addLog("⏹ Остановка...");
        statusLabel.setText("Остановлено");
        runBtn.setEnabled(true);
        stopBtn.setEnabled(false);
    }

    private void addLog(String message) {
        String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + timestamp + "] " + message + "\n");
    }

    private void displayResult(OrchestratorResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== РЕЗУЛЬТАТ ===\n\n");

        if (result.plan() != null) {
            sb.append("ПЛАН:\n");
            sb.append(result.plan()).append("\n\n");
        }

        if (result.buildInfo() != null) {
            sb.append("ДИРЕКТОРИЯ: ").append(result.buildInfo().buildDirectory()).append("\n");
            sb.append("СОЗДАНО ФАЙЛОВ: ").append(result.buildInfo().createdFiles().size()).append("\n");
            sb.append("ОШИБКИ: ").append(result.buildInfo().errors().size()).append("\n\n");

            if (!result.buildInfo().createdFiles().isEmpty()) {
                sb.append("СОЗДАННЫЕ ФАЙЛЫ:\n");
                for (String file : result.buildInfo().createdFiles()) {
                    sb.append("  - ").append(file).append("\n");
                }
                sb.append("\n");
            }

            if (!result.buildInfo().errors().isEmpty()) {
                sb.append("ОШИБКИ:\n");
                for (String error : result.buildInfo().errors()) {
                    sb.append("  - ").append(error).append("\n");
                }
                sb.append("\n");
            }
        }

        if (!result.verdicts().isEmpty()) {
            sb.append("ВЕРДИКТЫ:\n");
            for (Verdict verdict : result.verdicts()) {
                sb.append("  ").append(verdict.agentName())
                        .append(": ").append(verdict.status()).append("\n");
            }
            sb.append("\n");
        }

        resultArea.setText(sb.toString());
    }
}
