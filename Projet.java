import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// ==========================================
// 1. SINGLETON: The Clipboard
// ==========================================
class Clipboard {
    private static Clipboard instance;
    private String content = "";

    private Clipboard() {}

    public static Clipboard getInstance() {
        if (instance == null) instance = new Clipboard();
        return instance;
    }

    public void copy(String text) {
        this.content = text;
        System.out.println("Text copied successfully.");
    }

    public String paste() {
        return this.content;
    }
}

// ==========================================
// 2. STRATEGY: Export Formats
// ==========================================
interface ExportStrategy {
    void save(String content, String filename);
}

// STRATEGY 1: WORD (RTF)
class WordExport implements ExportStrategy {
    @Override
    public void save(String content, String filename) {
        if (!filename.endsWith(".rtf")) filename += ".rtf";
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("{\\rtf1\\ansi\\ansicpg1252\\deff0\\nouicompat\\deflang1036");
            writer.write("{\\fonttbl{\\f0\\fnil\\fcharset0 Arial;}}");
            writer.write("\\viewkind4\\uc1\\pard\\sa200\\sl276\\slmult1\\f0\\fs24\\lang12 ");
            
            StringBuilder rtfContent = new StringBuilder();
            for (char c : content.toCharArray()) {
                if (c == '\n') rtfContent.append("\\par ");
                else if (c > 127) rtfContent.append("\\u").append((int) c).append("?");
                else rtfContent.append(c);
            }
            writer.write(rtfContent.toString());
            writer.write("}");
            JOptionPane.showMessageDialog(null, "✅ Saved WORD File :\n" + filename);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "❌ Error : " + e.getMessage());
        }
    }
}

// STRATEGY 2: HTML
class HtmlExport implements ExportStrategy {
    @Override
    public void save(String content, String filename) {
        if (!filename.endsWith(".html")) filename += ".html";
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + filename + "</title></head>");
            writer.write("<body style='font-family: sans-serif; padding: 40px; background: #f4f4f4;'>");
            writer.write("<div style='background: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 10px rgba(0,0,0,0.1);'>");
            writer.write("<h1 style='color: #2c3e50; border-bottom: 2px solid #eee; padding-bottom: 10px;'>" + filename + "</h1>");
            writer.write("<p style='font-size: 16px; line-height: 1.6; color: #333; white-space: pre-wrap;'>" + content + "</p>");
            writer.write("</div></body></html>");
            JOptionPane.showMessageDialog(null, "✅ Saved HTML File :\n" + filename);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "❌ Error : " + e.getMessage());
        }
    }
}

// ==========================================
// 3. OBSERVER: UI Update
// ==========================================
interface StatsObserver {
    void update(String content);
}

class WordCounterLabel implements StatsObserver {
    private final JLabel labelToUpdate;
    public WordCounterLabel(JLabel label) { this.labelToUpdate = label; }
    @Override
    public void update(String content) {
        int count = content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;
        labelToUpdate.setText("  LIVE STATISTICS : " + count + " words | " + content.length() + " characters  ");
    }
}

// ==========================================
// THE SUBJECT: The Engine
// ==========================================
class TextEditor {
    private String content = "";
    private ExportStrategy exportStrategy;
    private final List<StatsObserver> observers = new ArrayList<>();

    public TextEditor() { this.exportStrategy = new WordExport(); }

    public void setContent(String newContent) {
        this.content = newContent;
        notifyObservers();
    }
    public String getContent() { return content; }
    public void addObserver(StatsObserver o) { observers.add(o); }
    private void notifyObservers() { for (StatsObserver o : observers) o.update(this.content); }
    public void setExportStrategy(ExportStrategy s) { this.exportStrategy = s; }
    public void save(String filename) { exportStrategy.save(this.content, filename); }
    public void copy() { Clipboard.getInstance().copy(this.content); }
    public String paste() { return Clipboard.getInstance().paste(); }
}

// ==========================================
// THE GRAPHICAL INTERFACE (VIEW)
// ==========================================
public class Projet extends JFrame {
    private final TextEditor editor;
    private JTextArea textArea;
    private JComboBox<String> strategySelector;
    
    // THEME COLOR (MIDNIGHT BLUE)
    private final Color THEME_COLOR = new Color(44, 62, 80);

    public Projet() {
        editor = new TextEditor();

        // 1. GLOBAL STYLE
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {}

        // 2. WINDOW CONFIGURATION
        setTitle("Text Editor Software Patterns");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout(0, 0));

        // 3. TEXT AREA
        textArea = new JTextArea();
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        textArea.setForeground(THEME_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(20, 20, 20, 20));
        
        // --- KEY LISTENER ---
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) {
                    e.consume(); 
                    String selectedText = textArea.getSelectedText();
                    if (selectedText == null) selectedText = textArea.getText();
                    editor.setContent(textArea.getText());
                    Clipboard.getInstance().copy(selectedText);
                    JOptionPane.showMessageDialog(null, "Text successfully copied (CTRL+C) !");
                }
                else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V) {
                    e.consume();
                    String textFromSingleton = Clipboard.getInstance().paste();
                    textArea.replaceSelection(textFromSingleton);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // 4. TOOLBAR
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        toolBar.setMargin(new Insets(12, 12, 12, 12));

        // Save Button
        JButton btnSave = createFlatButton("💾  SAVE", THEME_COLOR);

        // Strategy Selector
        String[] strategies = {"📄 WORD FORMAT (.rtf)", "🌐 HTML FORMAT (.html)"};
        strategySelector = new JComboBox<>(strategies);
        
        // --- MODIFICATION HERE: Forcing EMOJI font for the list as well ---
        strategySelector.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        
        strategySelector.setFocusable(false);
        strategySelector.setBackground(Color.WHITE);
        strategySelector.setForeground(THEME_COLOR);

        // Add elements
        JLabel infoLabel = new JLabel(" NOTE : Use CTRL+C and CTRL+V to copy and paste text");
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        toolBar.add(infoLabel);
        
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(new JLabel("RECORDING FORMAT : "));
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(strategySelector);
        toolBar.add(Box.createHorizontalStrut(15));
        toolBar.add(btnSave);
        add(toolBar, BorderLayout.NORTH);

        // 5. STATUS BAR
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(THEME_COLOR);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel statusLabel = new JLabel("Statistics : Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(Color.WHITE);
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        JLabel creditLabel = new JLabel("Project of Software Patterns - Made by ONCINO Alexandre");
        creditLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        creditLabel.setForeground(new Color(200, 200, 200));
        statusPanel.add(creditLabel, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);

        // --- WIRING / LISTENER REGISTRATION ---
        editor.addObserver(new WordCounterLabel(statusLabel));

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateEngine(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateEngine(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateEngine(); }
            void updateEngine() { editor.setContent(textArea.getText()); }
        });

        strategySelector.addActionListener(e -> {
            if (strategySelector.getSelectedIndex() == 0) editor.setExportStrategy(new WordExport());
            else editor.setExportStrategy(new HtmlExport());
        });

        btnSave.addActionListener(e -> {
            String filename = JOptionPane.showInputDialog(this, "File name (without extension) :");
            if (filename != null && !filename.trim().isEmpty()) editor.save(filename);
        });
    }

    // --- BUTTON DESIGN ---
    private JButton createFlatButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setUI(new BasicButtonUI());
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        
        // Use Emoji font for the button
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(color.brighter()); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(color); }
        });
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Projet().setVisible(true));
    }
}