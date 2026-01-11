import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class InventoryManagementSystem extends JFrame {

    // -------------------- DATABASE CONFIG --------------------
    private static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
    private static final String USER = "root";
    private static final String PASS = "shahani";

    // -------------------- MODERN COLOR PALETTE --------------------
    private static final Color BG_DARK        = new Color(30, 41, 59);      // slate-900
    private static final Color BG_LIGHT       = new Color(248, 250, 252);   // slate-50
    private static final Color ACCENT_PRIMARY = new Color(59, 130, 246);    // blue-500
    private static final Color ACCENT_SUCCESS = new Color(34, 197, 94);     // green-500
    private static final Color ACCENT_DANGER  = new Color(239, 68, 68);     // red-500
    private static final Color ACCENT_WARNING = new Color(245, 158, 11);    // amber-500
    private static final Color TEXT_PRIMARY   = new Color(15, 23, 42);      // slate-900
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);   // slate-500
    private static final Color CARD_BG        = Color.WHITE;
    private static final Color BORDER_COLOR   = new Color(226, 232, 240);   // slate-200
    private static final Color ROW_ALT        = new Color(249, 250, 251);   // slate-50

    // -------------------- GLOBAL COMPONENTS --------------------
    private JTable inventoryTable;
    private DefaultTableModel inventoryTableModel;
    private JTable categoriesTable;
    private DefaultTableModel categoriesTableModel;
    private JTable logsTable;
    private DefaultTableModel logsTableModel;
    private TableRowSorter<DefaultTableModel> inventorySorter;

    private Connection connect() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Connection Failed!\n" + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // -------------------- MAIN METHOD --------------------
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(InventoryManagementSystem::new);
    }

    // -------------------- CONSTRUCTOR --------------------
    public InventoryManagementSystem() {
        showLogin();
    }

    // -------------------- LOGIN WINDOW --------------------
    private void showLogin() {
        JFrame loginFrame = new JFrame("Login - Inventory System");
        loginFrame.setSize(420, 340);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Welcome Back");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_PRIMARY);
        gbc.gridy = 0;
        panel.add(lblTitle, gbc);

        JTextField txtUser = new JTextField(20);
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUser.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), "Username"));
        gbc.gridy = 1;
        panel.add(txtUser, gbc);

        JPasswordField txtPass = new JPasswordField(20);
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), "Password"));
        gbc.gridy = 2;
        panel.add(txtPass, gbc);

        JButton btnLogin = createModernButton("Sign In", ACCENT_PRIMARY);
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 0, 0, 0);
        panel.add(btnLogin, gbc);

        btnLogin.addActionListener(e -> {
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(loginFrame, "Please fill in all fields!",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (username.equals("kabeer") && password.equals("kabeershahani")) {
                loginFrame.dispose();
                showDashboard();
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid username or password!",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        loginFrame.getContentPane().add(panel);
        loginFrame.setVisible(true);
    }

    // -------------------- DASHBOARD (MAIN WINDOW) --------------------
    private JFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private void showDashboard() {
        mainFrame = new JFrame("Inventory Management System");
        mainFrame.setSize(1200, 750);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLayout(new BorderLayout());

        // Modern Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(BG_DARK);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(32, 16, 32, 16));

        JLabel logo = new JLabel("Inventory");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(40));

        String[] menuItems = {"Dashboard", "Inventory", "Categories", "Activity Log"};
        for (String item : menuItems) {
            JButton btn = createSidebarButton(item);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = createModernButton("Logout", ACCENT_DANGER);
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(btnLogout);

        mainFrame.add(sidebar, BorderLayout.WEST);

        // Card panels
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        mainPanel.add(createDashboardPanel(), "dashboard");
        mainPanel.add(createInventoryPanel(),  "inventory");
        mainPanel.add(createCategoriesPanel(), "categories");
        mainPanel.add(createLogPanel(),        "logs");

        mainFrame.add(mainPanel, BorderLayout.CENTER);

        // Navigation
        ActionListener navListener = e -> {
            String cmd = e.getActionCommand();
            cardLayout.show(mainPanel, cmd.toLowerCase().replace(" ", ""));
            if (cmd.equals("Activity Log")) loadLogs();
            else if (cmd.equals("Inventory")) loadInventory();
            else if (cmd.equals("Categories")) loadCategories();
        };

        for (Component c : sidebar.getComponents()) {
            if (c instanceof JButton btn && !btn.getText().equals("Logout")) {
                btn.setActionCommand(btn.getText());
                btn.addActionListener(navListener);
            }
        }

        btnLogout.addActionListener(e -> {
            mainFrame.dispose();
            showLogin();
        });

        mainFrame.setVisible(true);
        loadInventory(); // Initial load
    }

    // -------------------- UTILITY METHODS --------------------
    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(BG_DARK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(45, 55, 75));
                btn.setOpaque(true);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setOpaque(false);
            }
        });

        return btn;
    }

    private JButton createModernButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(bgColor);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.darker());
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    private JComboBox<String> createStyledComboBox() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return combo;
    }

    // -------------------- DASHBOARD PANEL --------------------
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(BG_LIGHT);

        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT_PRIMARY);
        title.setBounds(40, 30, 400, 50);
        panel.add(title);

        // Stats Row
        panel.add(createStatBox("Total Products", countProducts(), ACCENT_SUCCESS, 40, 120));
        panel.add(createStatBox("Total Categories", countCategories(), ACCENT_PRIMARY, 340, 120));
        panel.add(createStatBox("Low Stock Alert", countLowStock(), ACCENT_DANGER, 640, 120));
        panel.add(createStatBox("Out of Stock", countOutOfStock(), ACCENT_WARNING, 940, 120));

        // Recent Activity Section
        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setBounds(40, 320, 1100, 320);
        recentPanel.setBackground(CARD_BG);
        recentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel recentTitle = new JLabel("Recent Activity");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        recentTitle.setForeground(TEXT_PRIMARY);
        recentPanel.add(recentTitle, BorderLayout.NORTH);

        // Recent Activity Table
        String[] logColumns = {"Time", "User", "Action", "Details"};
        logsTableModel = new DefaultTableModel(logColumns, 0);
        logsTable = new JTable(logsTableModel);
        logsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logsTable.setRowHeight(35);
        logsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        logsTable.getTableHeader().setBackground(BG_DARK);
        logsTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(logsTable);
        recentPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(recentPanel);
        loadRecentLogs();

        return panel;
    }

    private JPanel createStatBox(String title, int value, Color bgColor, int x, int y) {
        JPanel box = new JPanel(null);
        box.setBounds(x, y, 250, 150);
        box.setBackground(bgColor);
        box.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        box.setOpaque(true);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setBounds(20, 20, 210, 30);
        box.add(lblTitle);

        JLabel lblValue = new JLabel(String.valueOf(value));
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblValue.setBounds(20, 50, 210, 70);
        box.add(lblValue);

        return box;
    }

    // -------------------- INVENTORY PANEL --------------------
    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Product Inventory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);

        JTextField searchField = createStyledTextField("Search products...");
        searchField.setPreferredSize(new Dimension(250, 40));
        searchPanel.add(searchField);

        JButton btnSearch = createModernButton("Search", ACCENT_PRIMARY);
        btnSearch.setPreferredSize(new Dimension(100, 40));
        searchPanel.add(btnSearch);

        JButton btnClear = createModernButton("Clear", TEXT_SECONDARY);
        btnClear.setPreferredSize(new Dimension(100, 40));
        searchPanel.add(btnClear);

        headerPanel.add(searchPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Product Name", "Category", "Quantity", "Price", "Status", "Last Updated"};
        inventoryTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        inventoryTable = new JTable(inventoryTableModel);
        inventorySorter = new TableRowSorter<>(inventoryTableModel);
        inventoryTable.setRowSorter(inventorySorter);

        // Style table
        inventoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inventoryTable.setRowHeight(40);
        inventoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        inventoryTable.getTableHeader().setBackground(BG_DARK);
        inventoryTable.getTableHeader().setForeground(Color.WHITE);
        inventoryTable.setShowGrid(true);
        inventoryTable.setGridColor(BORDER_COLOR);

        // Custom renderer for status column
        inventoryTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton btnAdd = createModernButton("Add Product", ACCENT_SUCCESS);
        JButton btnEdit = createModernButton("Edit Product", ACCENT_PRIMARY);
        JButton btnDelete = createModernButton("Delete Product", ACCENT_DANGER);
        JButton btnRefresh = createModernButton("Refresh", TEXT_SECONDARY);
        JButton btnExport = createModernButton("Export CSV", ACCENT_WARNING);

        Dimension btnSize = new Dimension(140, 45);
        btnAdd.setPreferredSize(btnSize);
        btnEdit.setPreferredSize(btnSize);
        btnDelete.setPreferredSize(btnSize);
        btnRefresh.setPreferredSize(btnSize);
        btnExport.setPreferredSize(btnSize);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnExport);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnAdd.addActionListener(e -> showAddProductDialog());
        btnEdit.addActionListener(e -> editProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnRefresh.addActionListener(e -> loadInventory());
        btnExport.addActionListener(e -> exportToCSV());
        btnClear.addActionListener(e -> {
            searchField.setText("");
            inventorySorter.setRowFilter(null);
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterTable(searchField.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { filterTable(searchField.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { filterTable(searchField.getText()); }
        });

        return panel;
    }

    private void filterTable(String query) {
        if (query.trim().length() == 0) {
            inventorySorter.setRowFilter(null);
        } else {
            inventorySorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 2)); // Search in name and category
        }
    }

    // -------------------- CATEGORIES PANEL --------------------
    private JPanel createCategoriesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Product Categories");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);

        JButton btnAddCat = createModernButton("Add Category", ACCENT_SUCCESS);
        btnAddCat.setPreferredSize(new Dimension(140, 45));
        headerPanel.add(btnAddCat, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Category Name", "Product Count", "Description", "Created Date"};
        categoriesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        categoriesTable = new JTable(categoriesTableModel);

        // Style table
        categoriesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoriesTable.setRowHeight(40);
        categoriesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        categoriesTable.getTableHeader().setBackground(BG_DARK);
        categoriesTable.getTableHeader().setForeground(Color.WHITE);
        categoriesTable.setShowGrid(true);
        categoriesTable.setGridColor(BORDER_COLOR);

        JScrollPane scrollPane = new JScrollPane(categoriesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton btnEditCat = createModernButton("Edit Category", ACCENT_PRIMARY);
        JButton btnDeleteCat = createModernButton("Delete Category", ACCENT_DANGER);
        JButton btnRefreshCat = createModernButton("Refresh", TEXT_SECONDARY);

        Dimension btnSize = new Dimension(140, 45);
        btnEditCat.setPreferredSize(btnSize);
        btnDeleteCat.setPreferredSize(btnSize);
        btnRefreshCat.setPreferredSize(btnSize);

        buttonPanel.add(btnEditCat);
        buttonPanel.add(btnDeleteCat);
        buttonPanel.add(btnRefreshCat);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnAddCat.addActionListener(e -> showAddCategoryDialog());
        btnEditCat.addActionListener(e -> editCategory());
        btnDeleteCat.addActionListener(e -> deleteCategory());
        btnRefreshCat.addActionListener(e -> loadCategories());

        return panel;
    }

    // -------------------- ACTIVITY LOG PANEL --------------------
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Activity Log");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);

        JButton btnClearLogs = createModernButton("Clear Old Logs", ACCENT_DANGER);
        btnClearLogs.setPreferredSize(new Dimension(140, 45));
        headerPanel.add(btnClearLogs, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Timestamp", "User", "Action", "Details", "IP Address"};
        logsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        logsTable = new JTable(logsTableModel);

        // Style table
        logsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logsTable.setRowHeight(40);
        logsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        logsTable.getTableHeader().setBackground(BG_DARK);
        logsTable.getTableHeader().setForeground(Color.WHITE);
        logsTable.setShowGrid(true);
        logsTable.setGridColor(BORDER_COLOR);

        JScrollPane scrollPane = new JScrollPane(logsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton btnRefreshLogs = createModernButton("Refresh Logs", ACCENT_PRIMARY);
        JButton btnExportLogs = createModernButton("Export Logs", ACCENT_SUCCESS);

        Dimension btnSize = new Dimension(140, 45);
        btnRefreshLogs.setPreferredSize(btnSize);
        btnExportLogs.setPreferredSize(btnSize);

        buttonPanel.add(btnRefreshLogs);
        buttonPanel.add(btnExportLogs);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnRefreshLogs.addActionListener(e -> loadLogs());
        btnExportLogs.addActionListener(e -> exportLogsToCSV());
        btnClearLogs.addActionListener(e -> clearOldLogs());

        return panel;
    }

    // -------------------- DATABASE OPERATIONS --------------------
    private void loadInventory() {
        inventoryTableModel.setRowCount(0);
        try (Connection conn = connect()) {
            String sql = "SELECT p.id, p.name, c.name as category, p.quantity, p.price, " +
                    "CASE WHEN p.quantity = 0 THEN 'Out of Stock' " +
                    "WHEN p.quantity < 10 THEN 'Low Stock' " +
                    "ELSE 'In Stock' END as status, " +
                    "p.updated_at FROM products p " +
                    "LEFT JOIN categories c ON p.category_id = c.id " +
                    "ORDER BY p.updated_at DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("category"));
                row.add(rs.getInt("quantity"));
                row.add(String.format("$%.2f", rs.getDouble("price")));
                row.add(rs.getString("status"));
                row.add(rs.getTimestamp("updated_at").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                inventoryTableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading inventory: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCategories() {
        categoriesTableModel.setRowCount(0);
        try (Connection conn = connect()) {
            String sql = "SELECT c.*, COUNT(p.id) as product_count " +
                    "FROM categories c " +
                    "LEFT JOIN products p ON c.id = p.category_id " +
                    "GROUP BY c.id " +
                    "ORDER BY c.created_at DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getInt("product_count"));
                row.add(rs.getString("description"));
                row.add(rs.getDate("created_at").toString());
                categoriesTableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading categories: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadLogs() {
        logsTableModel.setRowCount(0);
        try (Connection conn = connect()) {
            String sql = "SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 100";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getTimestamp("timestamp").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                row.add(rs.getString("user"));
                row.add(rs.getString("action"));
                row.add(rs.getString("details"));
                row.add(rs.getString("ip_address"));
                logsTableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading logs: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRecentLogs() {
        logsTableModel.setRowCount(0);
        try (Connection conn = connect()) {
            String sql = "SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 10";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getTimestamp("timestamp").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
                row.add(rs.getString("user"));
                row.add(rs.getString("action"));
                row.add(rs.getString("details"));
                logsTableModel.addRow(row);
            }
        } catch (SQLException e) {
            // Silent fail for dashboard
        }
    }

    private void addLog(String user, String action, String details) {
        try (Connection conn = connect()) {
            String sql = "INSERT INTO activity_logs (user, action, details, ip_address) VALUES (?, ?, ?, '127.0.0.1')";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }

    // -------------------- DIALOG METHODS --------------------
    private void showAddProductDialog() {
        JDialog dialog = new JDialog(mainFrame, "Add New Product", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("Add New Product");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        gbc.gridwidth = 2;
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        // Product Name
        panel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtName = createStyledTextField("Enter product name");
        txtName.setPreferredSize(new Dimension(200, 35));
        panel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Category
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> comboCategory = createStyledComboBox();
        comboCategory.setPreferredSize(new Dimension(200, 35));
        loadCategoriesToCombo(comboCategory);
        panel.add(comboCategory, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Quantity
        panel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        JSpinner spinnerQty = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        spinnerQty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(spinnerQty, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Price
        panel.add(new JLabel("Price ($):"), gbc);
        gbc.gridx = 1;
        JSpinner spinnerPrice = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 99999.99, 0.01));
        spinnerPrice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(spinnerPrice, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Description
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        JTextArea txtDesc = new JTextArea(3, 20);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDesc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        JScrollPane scroll = new JScrollPane(txtDesc);
        panel.add(scroll, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton btnSave = createModernButton("Save Product", ACCENT_SUCCESS);
        JButton btnCancel = createModernButton("Cancel", TEXT_SECONDARY);

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        panel.add(buttonPanel, gbc);

        dialog.add(panel, BorderLayout.CENTER);

        // Action Listeners
        btnSave.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Product name is required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = connect()) {
                String sql = "INSERT INTO products (name, category_id, quantity, price, description) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtName.getText().trim());
                ps.setInt(2, getCategoryId(comboCategory.getSelectedItem().toString()));
                ps.setInt(3, (Integer) spinnerQty.getValue());
                ps.setDouble(4, (Double) spinnerPrice.getValue());
                ps.setString(5, txtDesc.getText().trim());
                ps.executeUpdate();

                addLog("kabeer", "ADD_PRODUCT", "Added product: " + txtName.getText());
                loadInventory();
                dialog.dispose();

                JOptionPane.showMessageDialog(mainFrame, "Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving product: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void editProduct() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Please select a product to edit!", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (int) inventoryTableModel.getValueAt(inventoryTable.convertRowIndexToModel(selectedRow), 0);

        try (Connection conn = connect()) {
            String sql = "SELECT * FROM products WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                showEditProductDialog(rs);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Error loading product: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEditProductDialog(ResultSet rs) throws SQLException {
        JDialog dialog = new JDialog(mainFrame, "Edit Product", true);
        dialog.setSize(500, 550);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int productId = rs.getInt("id");

        JLabel title = new JLabel("Edit Product (ID: " + productId + ")");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridwidth = 2;
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        // Product Name
        panel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtName = createStyledTextField("");
        txtName.setText(rs.getString("name"));
        txtName.setPreferredSize(new Dimension(200, 35));
        panel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Category
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> comboCategory = createStyledComboBox();
        comboCategory.setPreferredSize(new Dimension(200, 35));
        loadCategoriesToCombo(comboCategory);
        comboCategory.setSelectedItem(getCategoryName(rs.getInt("category_id")));
        panel.add(comboCategory, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Quantity
        panel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        JSpinner spinnerQty = new JSpinner(new SpinnerNumberModel(rs.getInt("quantity"), 0, 9999, 1));
        spinnerQty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(spinnerQty, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Price
        panel.add(new JLabel("Price ($):"), gbc);
        gbc.gridx = 1;
        JSpinner spinnerPrice = new JSpinner(new SpinnerNumberModel(rs.getDouble("price"), 0.0, 99999.99, 0.01));
        spinnerPrice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(spinnerPrice, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Description
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        JTextArea txtDesc = new JTextArea(4, 20);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDesc.setText(rs.getString("description"));
        txtDesc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        JScrollPane scroll = new JScrollPane(txtDesc);
        panel.add(scroll, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton btnSave = createModernButton("Save Changes", ACCENT_SUCCESS);
        JButton btnCancel = createModernButton("Cancel", TEXT_SECONDARY);

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        panel.add(buttonPanel, gbc);

        dialog.add(panel, BorderLayout.CENTER);

        btnSave.addActionListener(e -> {
            try (Connection conn = connect()) {
                String sql = "UPDATE products SET name = ?, category_id = ?, quantity = ?, price = ?, description = ?, updated_at = NOW() WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtName.getText().trim());
                ps.setInt(2, getCategoryId(comboCategory.getSelectedItem().toString()));
                ps.setInt(3, (Integer) spinnerQty.getValue());
                ps.setDouble(4, (Double) spinnerPrice.getValue());
                ps.setString(5, txtDesc.getText().trim());
                ps.setInt(6, productId);
                ps.executeUpdate();

                addLog("kabeer", "UPDATE_PRODUCT", "Updated product ID: " + productId);
                loadInventory();
                dialog.dispose();

                JOptionPane.showMessageDialog(mainFrame, "Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating product: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void deleteProduct() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Please select a product to delete!", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (int) inventoryTableModel.getValueAt(inventoryTable.convertRowIndexToModel(selectedRow), 0);
        String productName = (String) inventoryTableModel.getValueAt(inventoryTable.convertRowIndexToModel(selectedRow), 1);

        int confirm = JOptionPane.showConfirmDialog(mainFrame,
                "Are you sure you want to delete product:\n" + productName + " (ID: " + productId + ")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = connect()) {
                String sql = "DELETE FROM products WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, productId);
                ps.executeUpdate();

                addLog("kabeer", "DELETE_PRODUCT", "Deleted product ID: " + productId + " - " + productName);
                loadInventory();

                JOptionPane.showMessageDialog(mainFrame, "Product deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(mainFrame, "Error deleting product: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddCategoryDialog() {
        JDialog dialog = new JDialog(mainFrame, "Add New Category", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Add New Category");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridwidth = 2;
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        // Category Name
        panel.add(new JLabel("Category Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtName = createStyledTextField("Enter category name");
        txtName.setPreferredSize(new Dimension(200, 35));
        panel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Description
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        JTextArea txtDesc = new JTextArea(3, 20);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDesc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        JScrollPane scroll = new JScrollPane(txtDesc);
        panel.add(scroll, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton btnSave = createModernButton("Save Category", ACCENT_SUCCESS);
        JButton btnCancel = createModernButton("Cancel", TEXT_SECONDARY);

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        panel.add(buttonPanel, gbc);

        dialog.add(panel, BorderLayout.CENTER);

        btnSave.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Category name is required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = connect()) {
                String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtName.getText().trim());
                ps.setString(2, txtDesc.getText().trim());
                ps.executeUpdate();

                addLog("kabeer", "ADD_CATEGORY", "Added category: " + txtName.getText());
                loadCategories();
                dialog.dispose();

                JOptionPane.showMessageDialog(mainFrame, "Category added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving category: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editCategory() {
        int selectedRow = categoriesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Please select a category to edit!", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int categoryId = (int) categoriesTableModel.getValueAt(selectedRow, 0);
        String categoryName = (String) categoriesTableModel.getValueAt(selectedRow, 1);
        String description = (String) categoriesTableModel.getValueAt(selectedRow, 3);

        JDialog dialog = new JDialog(mainFrame, "Edit Category", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Edit Category (ID: " + categoryId + ")");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridwidth = 2;
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        // Category Name
        panel.add(new JLabel("Category Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtName = createStyledTextField("");
        txtName.setText(categoryName);
        txtName.setPreferredSize(new Dimension(200, 35));
        panel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Description
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        JTextArea txtDesc = new JTextArea(3, 20);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDesc.setText(description);
        txtDesc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        JScrollPane scroll = new JScrollPane(txtDesc);
        panel.add(scroll, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton btnSave = createModernButton("Save Changes", ACCENT_SUCCESS);
        JButton btnCancel = createModernButton("Cancel", TEXT_SECONDARY);

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        panel.add(buttonPanel, gbc);

        dialog.add(panel, BorderLayout.CENTER);

        btnSave.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Category name is required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = connect()) {
                String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtName.getText().trim());
                ps.setString(2, txtDesc.getText().trim());
                ps.setInt(3, categoryId);
                ps.executeUpdate();

                addLog("kabeer", "UPDATE_CATEGORY", "Updated category ID: " + categoryId);
                loadCategories();
                dialog.dispose();

                JOptionPane.showMessageDialog(mainFrame, "Category updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating category: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void deleteCategory() {
        int selectedRow = categoriesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Please select a category to delete!", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int categoryId = (int) categoriesTableModel.getValueAt(selectedRow, 0);
        String categoryName = (String) categoriesTableModel.getValueAt(selectedRow, 1);
        int productCount = (int) categoriesTableModel.getValueAt(selectedRow, 2);

        if (productCount > 0) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Cannot delete category '" + categoryName + "'!\nIt contains " + productCount + " product(s).\nPlease reassign or delete the products first.",
                    "Cannot Delete", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(mainFrame,
                "Are you sure you want to delete category:\n" + categoryName + " (ID: " + categoryId + ")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = connect()) {
                String sql = "DELETE FROM categories WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, categoryId);
                ps.executeUpdate();

                addLog("kabeer", "DELETE_CATEGORY", "Deleted category ID: " + categoryId + " - " + categoryName);
                loadCategories();

                JOptionPane.showMessageDialog(mainFrame, "Category deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(mainFrame, "Error deleting category: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // -------------------- HELPER METHODS --------------------
    private void loadCategoriesToCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        try (Connection conn = connect()) {
            String sql = "SELECT name FROM categories ORDER BY name";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                combo.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            combo.addItem("Uncategorized");
        }
    }

    private int getCategoryId(String categoryName) {
        try (Connection conn = connect()) {
            String sql = "SELECT id FROM categories WHERE name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, categoryName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            // Return default category ID 1 if exists
        }
        return 1; // Default category ID
    }

    private String getCategoryName(int categoryId) {
        try (Connection conn = connect()) {
            String sql = "SELECT name FROM categories WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            // Return default
        }
        return "Uncategorized";
    }

    private int countProducts() {
        try (Connection conn = connect()) {
            String sql = "SELECT COUNT(*) FROM products";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }

    private int countCategories() {
        try (Connection conn = connect()) {
            String sql = "SELECT COUNT(*) FROM categories";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }

    private int countLowStock() {
        try (Connection conn = connect()) {
            String sql = "SELECT COUNT(*) FROM products WHERE quantity < 10 AND quantity > 0";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }

    private int countOutOfStock() {
        try (Connection conn = connect()) {
            String sql = "SELECT COUNT(*) FROM products WHERE quantity = 0";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Inventory to CSV");
        fileChooser.setSelectedFile(new java.io.File("inventory_export.csv"));

        if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile())) {
                // Write headers
                for (int i = 0; i < inventoryTableModel.getColumnCount(); i++) {
                    writer.write(inventoryTableModel.getColumnName(i));
                    if (i < inventoryTableModel.getColumnCount() - 1) writer.write(",");
                }
                writer.write("\n");

                // Write data
                for (int i = 0; i < inventoryTableModel.getRowCount(); i++) {
                    for (int j = 0; j < inventoryTableModel.getColumnCount(); j++) {
                        Object value = inventoryTableModel.getValueAt(i, j);
                        writer.write(value != null ? value.toString() : "");
                        if (j < inventoryTableModel.getColumnCount() - 1) writer.write(",");
                    }
                    writer.write("\n");
                }

                addLog("kabeer", "EXPORT_CSV", "Exported inventory to CSV");
                JOptionPane.showMessageDialog(mainFrame, "Inventory exported successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame, "Error exporting CSV: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportLogsToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Logs to CSV");
        fileChooser.setSelectedFile(new java.io.File("activity_logs.csv"));

        if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile())) {
                // Write headers
                for (int i = 0; i < logsTableModel.getColumnCount(); i++) {
                    writer.write(logsTableModel.getColumnName(i));
                    if (i < logsTableModel.getColumnCount() - 1) writer.write(",");
                }
                writer.write("\n");

                // Write data
                for (int i = 0; i < logsTableModel.getRowCount(); i++) {
                    for (int j = 0; j < logsTableModel.getColumnCount(); j++) {
                        Object value = logsTableModel.getValueAt(i, j);
                        writer.write(value != null ? value.toString() : "");
                        if (j < logsTableModel.getColumnCount() - 1) writer.write(",");
                    }
                    writer.write("\n");
                }

                addLog("kabeer", "EXPORT_LOGS", "Exported activity logs to CSV");
                JOptionPane.showMessageDialog(mainFrame, "Logs exported successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame, "Error exporting logs: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearOldLogs() {
        int confirm = JOptionPane.showConfirmDialog(mainFrame,
                "This will delete all logs older than 30 days.\nAre you sure?",
                "Clear Old Logs", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = connect()) {
                String sql = "DELETE FROM activity_logs WHERE timestamp < DATE_SUB(NOW(), INTERVAL 30 DAY)";
                PreparedStatement ps = conn.prepareStatement(sql);
                int deleted = ps.executeUpdate();

                addLog("kabeer", "CLEAR_LOGS", "Cleared " + deleted + " old log entries");
                loadLogs();

                JOptionPane.showMessageDialog(mainFrame, "Cleared " + deleted + " old log entries!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(mainFrame, "Error clearing logs: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // -------------------- CUSTOM CELL RENDERER --------------------
    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value != null) {
                String status = value.toString();
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setHorizontalAlignment(SwingConstants.CENTER);

                switch (status) {
                    case "In Stock":
                        setForeground(new Color(34, 197, 94)); // green
                        setBackground(new Color(220, 252, 231)); // light green
                        break;
                    case "Low Stock":
                        setForeground(new Color(245, 158, 11)); // amber
                        setBackground(new Color(254, 243, 199)); // light amber
                        break;
                    case "Out of Stock":
                        setForeground(new Color(239, 68, 68)); // red
                        setBackground(new Color(254, 226, 226)); // light red
                        break;
                    default:
                        setForeground(Color.BLACK);
                        setBackground(Color.WHITE);
                }

                if (isSelected) {
                    setBackground(getBackground().darker());
                }
            }

            return c;
        }
    }
}