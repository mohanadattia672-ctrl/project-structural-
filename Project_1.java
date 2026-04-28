

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class Project_1 {

    private JFrame frame;
    private JTextField nameField, ageField, idField;
    private JTable table;
    private DefaultTableModel model;

    // --- بيانات قاعدة البيانات ---
    private final String DB_URL = "jdbc:mysql://localhost:3306/school_db";
    private final String USER = "root";
    private final String PASS = "";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Project_1().initialize());
    }

    private void initialize() {
        frame = new JFrame("نظام إدارة الطلاب - النسخة الاحترافية");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(15, 15));

        // --- لوحة الإدخال ---
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("بيانات الطالب"));

        inputPanel.add(new JLabel(" الاسم بالكامل:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel(" رقم القيد (ID):"));
        idField = new JTextField();
        inputPanel.add(idField);

        inputPanel.add(new JLabel(" السن:"));
        ageField = new JTextField();
        inputPanel.add(ageField);

        // --- لوحة الأزرار ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton addButton = new JButton("إضافة طالب ➕");
        JButton deleteButton = new JButton("حذف ❌");
        JButton clearButton = new JButton("مسح الخانات 🧹");

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        // --- الجدول ---
        String[] columns = {"الاسم", "رقم القيد", "السن"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        styleTable();
        JScrollPane scrollPane = new JScrollPane(table);

        // --- اللوجيك (العمليات) ---
        addButton.addActionListener(e -> addStudentAction());

        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                model.removeRow(row);
                clearFields();
            } else {
                showError("برجاء اختيار صف من الجدول لحذفه!");
            }
        });

        clearButton.addActionListener(e -> clearFields());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                nameField.setText(model.getValueAt(row, 0).toString());
                idField.setText(model.getValueAt(row, 1).toString());
                ageField.setText(model.getValueAt(row, 2).toString());
            }
        });

        // تجميع الواجهة
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(inputPanel, BorderLayout.CENTER);
        topContainer.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(topContainer, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // --- اللوجيك الأساسي ومنع الأخطاء ---
    private void addStudentAction() {
        String name = nameField.getText().trim();
        String id = idField.getText().trim();
        String age = ageField.getText().trim();

        // 1. التحقق من الخانات الفارغة
        if (name.isEmpty() || id.isEmpty() || age.isEmpty()) {
            showError("جميع الخانات مطلوبة!");
            return;
        }

        // 2. التحقق من الاسم (حروف فقط)
        if (!name.matches("^[a-zA-Z\\s\\u0600-\\u06FF]+$")) {
            showError("الاسم يجب أن يحتوي على حروف فقط!");
            return;
        }

        // 3. التحقق من الـ ID (أرقام فقط)
        if (!id.matches("\\d+")) {
            showError("رقم القيد يجب أن يكون أرقاماً فقط!");
            return;
        }

        // 4. التحقق من السن (رقم منطقي)
        try {
            int ageInt = Integer.parseInt(age);
            if (ageInt < 16 || ageInt > 60) {
                showError("السن يجب أن يكون بين 16 و 60 سنة!");
                return;
            }
        } catch (NumberFormatException e) {
            showError("السن يجب أن يكون رقماً صحيحاً!");
            return;
        }

        // إذا نجحت الاختبارات:
        model.addRow(new Object[]{name, id, age});
        saveToDatabase(name, id, age); // استدعاء ميثود قاعدة البيانات
        JOptionPane.showMessageDialog(frame, "تمت إضافة الطالب بنجاح! ✅");
        clearFields();
    }

    private void saveToDatabase(String name, String studentId, String age) {
        String sql = "INSERT INTO students (name, student_id, age) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, studentId);
            pstmt.setInt(3, Integer.parseInt(age));
            pstmt.executeUpdate();

        } catch (SQLException e) {
            // لو قاعدة البيانات مش شغالة، البرنامج مش هيقفل، بس هيطبع Error في الـ Console
            System.out.println("DB Connection Info: تأكد من تشغيل XAMPP وإنشاء الجدول.");
        }
    }

    private void clearFields() {
        nameField.setText("");
        idField.setText("");
        ageField.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "خطأ في الإدخال", JOptionPane.WARNING_MESSAGE);
    }

    private void styleTable() {
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setSelectionBackground(new Color(173, 216, 230));
    }
}