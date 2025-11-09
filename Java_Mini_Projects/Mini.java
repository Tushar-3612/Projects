import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import com.itextpdf.text.*;


public class WalletAppGUI extends JFrame {
    Connection conn;
    int currentUserId = -1;
    String currentUsername = "";
    double currentBalance = 0;
    String currentUserType = "";
    JLabel showBalance;

    // Main components
    JTextArea outputArea = new JTextArea(10, 30);
    JPanel mainPanel = new JPanel(new BorderLayout());
    CardLayout cardLayout = new CardLayout();
    JPanel cardPanel = new JPanel(cardLayout);

    // Panel names for card layout
    final String LOGIN_PANEL = "Login Panel";
    final String USER_PANEL = "User Panel";
    final String ADMIN_PANEL = "Admin Panel";
    final String REGISTER_PANEL = "Register Panel";
    final String TRANSACTION_PANEL = "Transaction Panel";

    // Color scheme
    Color WHITE = Color.WHITE;
    Color BLACK = Color.BLACK;
    Color DODGER_BLUE = new Color(30, 144, 255);
    Color GREEN = new Color(46, 204, 113);
    Color RED = new Color(231, 76, 60);
    Color YELLOW = new Color(241, 196, 15);
    Color GRAY = Color.GRAY;
    Color PURPLE = new Color(155, 89, 182);

    // Button colors
    Color DEPOSIT_COLOR = new Color(173, 216, 230); // light blue
    Color WITHDRAW_COLOR = new Color(255, 204, 203); // light pink
    Color BALANCE_COLOR = new Color(224, 255, 255); // light cyan
    Color SEND_COLOR = new Color(255, 255, 204); // light yellow
    Color REQUEST_COLOR = new Color(204, 255, 229); // mint
    Color HISTORY_COLOR = new Color(229, 204, 255); // lavender
    Color LOGOUT_COLOR = new Color(255, 235, 205); // light peach

    public WalletAppGUI() {
        setTitle("Digital Wallet System");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(WHITE);

        // Initialize database connection
        this.conn = connectToDB();

        // Create all panels with proper colors
        cardPanel.add(createLoginPanel(), LOGIN_PANEL);
        cardPanel.add(createRegisterPanel(), REGISTER_PANEL);
        cardPanel.add(createUserPanel(), USER_PANEL);
        cardPanel.add(createAdminPanel(), ADMIN_PANEL);
        cardPanel.add(createTransactionPanel(), TRANSACTION_PANEL);

        // Configure output area
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Arial", Font.PLAIN, 14));
        outputArea.setForeground(BLACK);
        outputArea.setBackground(WHITE);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Create titled border for activity log
        TitledBorder logBorder = BorderFactory.createTitledBorder("Activity Log");
        logBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        logBorder.setTitleColor(BLACK);
        scrollPane.setBorder(logBorder);
        scrollPane.setBackground(WHITE);

        // Add components to main frame
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        add(mainPanel);

        // Show login panel first
        cardLayout.show(cardPanel, LOGIN_PANEL);
    }

    private Connection connectToDB() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/walletdb", "root", "3001");
            outputArea.append("Connected to database successfully\n");
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            outputArea.append("Database connection failed: " + e.getMessage() + "\n");
            return null;
        }
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("DIGITAL WALLET SYSTEM", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usernameLabel.setForeground(BLACK);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setForeground(BLACK);
        usernameField.setBackground(WHITE);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setForeground(BLACK);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setForeground(BLACK);
        passwordField.setBackground(WHITE);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(DODGER_BLUE);
        loginButton.setForeground(Color.BLACK);
        loginButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(loginButton, gbc);

        JButton registerButton = new JButton("Register New Account");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setBackground(GREEN);
        registerButton.setForeground(Color.BLACK);
        registerButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        gbc.gridy = 4;
        panel.add(registerButton, gbc);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            try {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    currentUserId = rs.getInt("id");
                    currentUsername = username;
                    currentBalance = rs.getDouble("balance");
                    currentUserType = rs.getString("user_type");
                    logActivity("login", 0);

                    if ("admin".equalsIgnoreCase(currentUserType)) {
                        cardLayout.show(cardPanel, ADMIN_PANEL);
                    } else {
                        cardLayout.show(cardPanel, USER_PANEL);
                    }
                    updateUserInfo();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            cardLayout.show(cardPanel, REGISTER_PANEL);
        });

        return panel;
    }

    private void generateMiniStatementPDF() {
        try {
            String query = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date DESC LIMIT 5";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();

            // Create a unique filename with timestamp
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String fileName = "MiniStatement_" + currentUsername + "_" + timestamp + ".pdf";

            // Create PDF document
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Add title
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLUE);
            Paragraph title = new Paragraph("Mini Statement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add user info
            com.itextpdf.text.Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Account Holder: " + currentUsername, infoFont));
            document.add(new Paragraph("Account Type: " + currentUserType, infoFont));
            document.add(new Paragraph("Current Balance: ₹" + String.format("%.2f", currentBalance), infoFont));
            document.add(new Paragraph(" "));

            // Add statement period
            com.itextpdf.text.Font periodFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
            String period = "Statement Period: Last 5 transactions up to " +
                    new java.text.SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new java.util.Date());
            document.add(new Paragraph(period, periodFont));
            document.add(new Paragraph(" "));

            // Create table for transactions
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Add table headers
            com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            table.addCell(new Phrase("Date/Time", headerFont));
            table.addCell(new Phrase("Transaction Type", headerFont));
            table.addCell(new Phrase("Amount (₹)", headerFont));

            // Add transaction data
            com.itextpdf.text.Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            while (rs.next()) {
                String date = new java.text.SimpleDateFormat("dd-MMM-yyyy HH:mm:ss")
                        .format(rs.getTimestamp("date"));
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");

                table.addCell(new Phrase(date, dataFont));
                table.addCell(new Phrase(type, dataFont));
                table.addCell(new Phrase(String.format("%.2f", amount), dataFont));
            }

            document.add(table);

            // Add footer
            com.itextpdf.text.Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
            Paragraph footer = new Paragraph("Generated by BHARAT KOLHE \n  TUSHAR NAGARE", footerFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();

            JOptionPane.showMessageDialog(this,
                    "Mini Statement generated successfully!\nSaved as: " + fileName,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error generating mini statement: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("REGISTER NEW ACCOUNT", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usernameLabel.setForeground(BLACK);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setForeground(BLACK);
        usernameField.setBackground(WHITE);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setForeground(BLACK);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setForeground(BLACK);
        passwordField.setBackground(WHITE);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(passwordField, gbc);

        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(new Font("Arial", Font.BOLD, 14));
        confirmLabel.setForeground(BLACK);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(confirmLabel, gbc);

        JPasswordField confirmField = new JPasswordField(20);
        confirmField.setFont(new Font("Arial", Font.PLAIN, 14));
        confirmField.setForeground(BLACK);
        confirmField.setBackground(WHITE);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(confirmField, gbc);

        JLabel typeLabel = new JLabel("User Type:");
        typeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        typeLabel.setForeground(BLACK);
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(typeLabel, gbc);

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"user", "admin"});
        typeCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        typeCombo.setForeground(BLACK);
        typeCombo.setBackground(WHITE);
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(typeCombo, gbc);

        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setBackground(GREEN);
        registerButton.setForeground(BLACK);
        registerButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(registerButton, gbc);

        JButton backButton = new JButton("Back to Login");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(RED);
        backButton.setForeground(BLACK);
        backButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        gbc.gridy = 6;
        panel.add(backButton, gbc);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());
            String type = (String) typeCombo.getSelectedItem();

            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM users WHERE username=?");
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Username already exists", "Registration Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                PreparedStatement ps = conn.prepareStatement("INSERT INTO users(username, password, user_type, balance) VALUES (?, ?, ?, 0.0)");
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, type);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Registration successful! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(cardPanel, LOGIN_PANEL);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> {
            cardLayout.show(cardPanel, LOGIN_PANEL);
        });

        return panel;
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // User info panel
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        infoPanel.setBackground(WHITE);

        JLabel userLabel = new JLabel("User: ", JLabel.CENTER);
        JLabel balanceLabel = new JLabel("Balance: ", JLabel.CENTER);
        JLabel showBalance=new JLabel("",JLabel.CENTER);
        JLabel typeLabel = new JLabel("Type: ", JLabel.CENTER);

        Font infoFont = new Font("Arial", Font.BOLD, 16);
        userLabel.setFont(infoFont);
        userLabel.setForeground(BLACK);
        balanceLabel.setFont(infoFont);
        balanceLabel.setForeground(BLACK);
        typeLabel.setFont(infoFont);
        typeLabel.setForeground(BLACK);

        infoPanel.add(userLabel);
        infoPanel.add(balanceLabel);
        infoPanel.add(typeLabel);

        JButton miniStatementButton = new JButton("Mini Statement");
        miniStatementButton.setFont(new Font("Arial", Font.BOLD, 14));
        miniStatementButton.setBackground(new Color(255, 228, 196)); // Light color
        miniStatementButton.setForeground(Color.BLACK);
        miniStatementButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        miniStatementButton.addActionListener(e -> generateMiniStatementPDF());

        TitledBorder infoBorder = BorderFactory.createTitledBorder("Account Information");
        infoBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        infoBorder.setTitleColor(BLACK);
        infoPanel.setBorder(infoBorder);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        buttonPanel.setBackground(WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        TitledBorder buttonBorder = BorderFactory.createTitledBorder("Actions");
        buttonBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        buttonBorder.setTitleColor(BLACK);
        buttonPanel.setBorder(buttonBorder);

        JButton depositButton = createStyledButton("Deposit", DEPOSIT_COLOR, Color.BLUE);
        JButton withdrawButton = createStyledButton("Withdraw", WITHDRAW_COLOR, Color.RED);
        JButton balanceButton = createStyledButton("Check Balance", BALANCE_COLOR, Color.CYAN);
        JButton sendButton = createStyledButton("Send Money", SEND_COLOR, Color.ORANGE);
        JButton receiveButton = createStyledButton("Request Money", REQUEST_COLOR, Color.GREEN.darker());
        JButton historyButton = createStyledButton("Transaction History", HISTORY_COLOR, Color.MAGENTA);
        JButton logoutButton = createStyledButton("Logout", LOGOUT_COLOR, Color.RED.darker());

        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(balanceButton);
        buttonPanel.add(sendButton);
        buttonPanel.add(receiveButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(logoutButton);
        buttonPanel.add(miniStatementButton);

        depositButton.addActionListener(e -> showTransactionDialog("deposit"));
        withdrawButton.addActionListener(e -> showTransactionDialog("withdraw"));
        balanceButton.addActionListener(e -> {
            outputArea.append("Current Balance: ₹" + String.format("%.2f", currentBalance) + "\n");
        });
        sendButton.addActionListener(e -> showSendMoneyDialog());
        receiveButton.addActionListener(e -> showRequestMoneyDialog());
        historyButton.addActionListener(e -> showTransactionHistory());
        logoutButton.addActionListener(e -> logout());

        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);

        panel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent evt) {
                updateUserInfo();
                userLabel.setText("User: " + currentUsername);
                balanceLabel.setText("Balance: ₹" + String.format("%.2f", currentBalance));
                typeLabel.setText("Type: " + currentUserType);
            }
        });

        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Admin info panel
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        infoPanel.setBackground(WHITE);

        JLabel userLabel = new JLabel("Admin: ", JLabel.CENTER);
        JLabel balanceLabel = new JLabel("Balance: ", JLabel.CENTER);
         showBalance=new JLabel("");
        JLabel typeLabel = new JLabel("Type: admin", JLabel.CENTER);

        Font infoFont = new Font("Arial", Font.BOLD, 16);
        userLabel.setFont(infoFont);
        userLabel.setForeground(BLACK);
        balanceLabel.setFont(infoFont);
        balanceLabel.setForeground(BLACK);
        typeLabel.setFont(infoFont);
        typeLabel.setForeground(BLACK);

        infoPanel.add(userLabel);
        infoPanel.add(balanceLabel);
        infoPanel.add(typeLabel);

        TitledBorder infoBorder = BorderFactory.createTitledBorder("Admin Information");
        infoBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        infoBorder.setTitleColor(BLACK);
        infoPanel.setBorder(infoBorder);

        // Admin buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        buttonPanel.setBackground(WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        TitledBorder buttonBorder = BorderFactory.createTitledBorder("Admin Actions");
        buttonBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        buttonBorder.setTitleColor(BLACK);
        buttonPanel.setBorder(buttonBorder);

        JButton viewUsersButton = createStyledButton("View All Users", DODGER_BLUE, BLACK);
        JButton viewTransactionsButton = createStyledButton("View All Transactions", PURPLE, WHITE);
        JButton viewActivitiesButton = createStyledButton("View Activity Logs", YELLOW, BLACK);
        JButton userPanelButton = createStyledButton("Switch to User Panel", GREEN, WHITE);
        JButton logoutButton = createStyledButton("Logout", RED, WHITE);

        buttonPanel.add(viewUsersButton);
        buttonPanel.add(viewTransactionsButton);
        buttonPanel.add(viewActivitiesButton);
        buttonPanel.add(userPanelButton);
        buttonPanel.add(logoutButton);

        viewUsersButton.addActionListener(e -> showAllUsers());
        viewTransactionsButton.addActionListener(e -> showAllTransactions());
        viewActivitiesButton.addActionListener(e -> showAllActivities());
        userPanelButton.addActionListener(e -> cardLayout.show(cardPanel, USER_PANEL));
        logoutButton.addActionListener(e -> logout());

        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);

        panel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent evt) {
                updateUserInfo();
                userLabel.setText("Admin: " + currentUsername);
                balanceLabel.setText("Balance: ₹" + String.format("%.2f", currentBalance));
            }
        });

        return panel;
    }

    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel infoLabel = new JLabel("Transaction details will be shown here", JLabel.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoLabel.setForeground(BLACK);
        panel.add(infoLabel, BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(RED);
        backButton.setForeground(WHITE);
        backButton.addActionListener(e -> {
            if ("admin".equalsIgnoreCase(currentUserType)) {
                cardLayout.show(cardPanel, ADMIN_PANEL);
            } else {
                cardLayout.show(cardPanel, USER_PANEL);
            }
        });
        panel.add(backButton, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor, Color borderColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        button.setPreferredSize(new Dimension(150, 40));
        return button;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        return createStyledButton(text, bgColor, bgColor.darker());
    }

    private void showTransactionDialog(String type) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setBackground(WHITE);

        JLabel label = new JLabel("Enter amount to " + type + ":");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(BLACK);

        JTextField amountField = new JTextField();
        amountField.setFont(new Font("Arial", Font.PLAIN, 14));
        amountField.setForeground(BLACK);
        amountField.setBackground(WHITE);

        panel.add(label);
        panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                type.substring(0, 1).toUpperCase() + type.substring(1),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (type.equals("withdraw") && amount > currentBalance) {
                    JOptionPane.showMessageDialog(this, "Insufficient balance", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                processTransaction(type, amount);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void processTransaction(String type, double amount) {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance = balance " +
                    (type.equals("deposit") ? "+" : "-") + " ? WHERE id=?");
            ps.setDouble(1, amount);
            ps.setInt(2, currentUserId);
            ps.executeUpdate();

            logTransaction(type, amount);
            logActivity(type, amount);

            if (type.equals("deposit")) {
                currentBalance += amount;
            } else {
                currentBalance -= amount;
            }

            outputArea.append(type.substring(0, 1).toUpperCase() + type.substring(1) +
                    " of ₹" + String.format("%.2f", amount) + " successful\n");

            showBalance.setText(Double.toString(currentBalance));
            updateUserInfo();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSendMoneyDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBackground(WHITE);

        JLabel recipientLabel = new JLabel("Recipient username:");
        recipientLabel.setFont(new Font("Arial", Font.BOLD, 14));
        recipientLabel.setForeground(BLACK);

        JTextField recipientField = new JTextField();
        recipientField.setFont(new Font("Arial", Font.PLAIN, 14));
        recipientField.setForeground(BLACK);
        recipientField.setBackground(WHITE);

        JLabel amountLabel = new JLabel("Amount to send:");
        amountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        amountLabel.setForeground(BLACK);

        JTextField amountField = new JTextField();
        amountField.setFont(new Font("Arial", Font.PLAIN, 14));
        amountField.setForeground(BLACK);
        amountField.setBackground(WHITE);

        panel.add(recipientLabel);
        panel.add(recipientField);
        panel.add(amountLabel);
        panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Send Money", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String recipient = recipientField.getText();
                double amount = Double.parseDouble(amountField.getText());

                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (amount > currentBalance) {
                    JOptionPane.showMessageDialog(this, "Insufficient balance", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (recipient.equalsIgnoreCase(currentUsername)) {
                    JOptionPane.showMessageDialog(this, "Cannot send money to yourself", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                PreparedStatement checkStmt = conn.prepareStatement("SELECT id FROM users WHERE username=?");
                checkStmt.setString(1, recipient);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Recipient not found", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int recipientId = rs.getInt("id");

                conn.setAutoCommit(false);

                try {
                    PreparedStatement deductStmt = conn.prepareStatement("UPDATE users SET balance = balance - ? WHERE id=?");
                    deductStmt.setDouble(1, amount);
                    deductStmt.setInt(2, currentUserId);
                    deductStmt.executeUpdate();

                    PreparedStatement addStmt = conn.prepareStatement("UPDATE users SET balance = balance + ? WHERE id=?");
                    addStmt.setDouble(1, amount);
                    addStmt.setInt(2, recipientId);
                    addStmt.executeUpdate();

                    logTransaction("send", amount);
                    logTransaction(recipientId, "receive", amount);

                    conn.commit();

                    currentBalance -= amount;
                    outputArea.append("Sent ₹" + String.format("%.2f", amount) + " to " + recipient + "\n");
                    updateUserInfo();

                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showRequestMoneyDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBackground(WHITE);

        JLabel senderLabel = new JLabel("Sender username:");
        senderLabel.setFont(new Font("Arial", Font.BOLD, 14));
        senderLabel.setForeground(BLACK);

        JTextField senderField = new JTextField();
        senderField.setFont(new Font("Arial", Font.PLAIN, 14));
        senderField.setForeground(BLACK);
        senderField.setBackground(WHITE);

        JLabel amountLabel = new JLabel("Amount to request:");
        amountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        amountLabel.setForeground(BLACK);

        JTextField amountField = new JTextField();
        amountField.setFont(new Font("Arial", Font.PLAIN, 14));
        amountField.setForeground(BLACK);
        amountField.setBackground(WHITE);

        panel.add(senderLabel);
        panel.add(senderField);
        panel.add(amountLabel);
        panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Request Money", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String sender = senderField.getText();
            String amountStr = amountField.getText();

            outputArea.append("Request sent to " + sender + " for ₹" + amountStr + "\n");
            JOptionPane.showMessageDialog(this, "Request sent to " + sender + " for ₹" + amountStr,
                    "Request Sent", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showTransactionHistory() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM transactions WHERE user_id=? ORDER BY date DESC LIMIT 20");
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder("Last 20 Transactions:\n");
            sb.append(String.format("%-10s %-10s %-10s %-20s\n", "Type", "Amount", "Status", "Date"));
            sb.append("--------------------------------------------------\n");

            while (rs.next()) {
                sb.append(String.format("%-10s ₹%-9.2f %-10s %-20s\n",
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        "Completed",
                        rs.getTimestamp("date").toString().substring(0, 16)));
            }

            outputArea.setText(sb.toString());

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAllUsers() {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT id, username, user_type, balance FROM users");
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder("All Users:\n");
            sb.append(String.format("%-5s %-15s %-10s %-10s\n", "ID", "Username", "Type", "Balance"));
            sb.append("----------------------------------------\n");

            while (rs.next()) {
                sb.append(String.format("%-5d %-15s %-10s ₹%-9.2f\n",
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("user_type"),
                        rs.getDouble("balance")));
            }

            outputArea.setText(sb.toString());

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAllTransactions() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT t.*, u.username FROM transactions t JOIN users u ON t.user_id = u.id " +
                            "ORDER BY t.date DESC LIMIT 20");
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder("Last 20 Transactions (All Users):\n");
            sb.append(String.format("%-10s %-15s %-10s %-10s %-20s\n",
                    "User", "Type", "Amount", "Status", "Date"));
            sb.append("------------------------------------------------------------\n");

            while (rs.next()) {
                sb.append(String.format("%-10s %-15s ₹%-9.2f %-10s %-20s\n",
                        rs.getString("username"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        "Completed",
                        rs.getTimestamp("date").toString().substring(0, 16)));
            }

            outputArea.setText(sb.toString());

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAllActivities() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT a.*, u.username FROM user_activity_log a JOIN users u ON a.user_id = u.id " +
                            "ORDER BY a.activity_time DESC LIMIT 20");
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder("Last 20 Activities:\n");
            sb.append(String.format("%-10s %-15s %-10s %-20s\n",
                    "User", "Activity", "Amount", "Time"));
            sb.append("--------------------------------------------------\n");

            while (rs.next()) {
                sb.append(String.format("%-10s %-15s ₹%-9.2f %-20s\n",
                        rs.getString("username"),
                        rs.getString("activity_type"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("activity_time").toString().substring(0, 16)));
            }

            outputArea.setText(sb.toString());

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        try {
            logActivity("logout", 0);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        currentUserId = -1;
        currentUsername = "";
        currentBalance = 0;
        currentUserType = "";
        outputArea.setText("");
        cardLayout.show(cardPanel, LOGIN_PANEL);
    }

    private void updateUserInfo() {
        if (currentUserId != -1) {
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT balance FROM users WHERE id=?");
                ps.setInt(1, currentUserId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    currentBalance = rs.getDouble("balance");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    void logTransaction(String type, double amount) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO transactions (user_id, type, amount) VALUES (?, ?, ?)");
        ps.setInt(1, currentUserId);
        ps.setString(2, type);
        ps.setDouble(3, amount);
        ps.executeUpdate();
    }

    void logTransaction(int userId, String type, double amount) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO transactions (user_id, type, amount) VALUES (?, ?, ?)");
        ps.setInt(1, userId);
        ps.setString(2, type);
        ps.setDouble(3, amount);
        ps.executeUpdate();
    }

    void logActivity(String activityType, double amount) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO user_activity_log (user_id, activity_type, amount) VALUES (?, ?, ?)");
        ps.setInt(1, currentUserId);
        ps.setString(2, activityType);
        ps.setDouble(3, amount);
        ps.executeUpdate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            WalletAppGUI app = new WalletAppGUI();
            app.setVisible(true);
        });
    }
}
