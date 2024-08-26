package fastSearch;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FileSearchApp extends JFrame {

    private JTextField filenameField;
    private JTextField contentField;
    private JCheckBox caseSensitiveCheckBox;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JLabel directoryLabel;
    private TableRowSorter<DefaultTableModel> sorter;

    public FileSearchApp() {
        setTitle("Dateisuche");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        
       
    }

    private void initComponents() {
        // Panel für die Eingabefelder
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Ordnerauswahl
        JButton directoryButton = new JButton("Ordner wählen");
        directoryLabel = new JLabel("Kein Ordner ausgewählt");
        directoryButton.addActionListener(e -> chooseDirectory());

        // Eingabefelder und Checkbox
        filenameField = new JTextField(15);
        contentField = new JTextField(15);
        caseSensitiveCheckBox = new JCheckBox("Groß-/Kleinschreibung berücksichtigen");

        // Hinzufügen der Komponenten zum Input-Panel
        inputPanel.add(new JLabel("Dateiname:"));
        inputPanel.add(filenameField);
        inputPanel.add(new JLabel("Dateiinhalt:"));
        inputPanel.add(contentField);
        inputPanel.add(caseSensitiveCheckBox);
        inputPanel.add(directoryButton);
        inputPanel.add(directoryLabel);

        // Tabelle für Suchergebnisse
        tableModel = new DefaultTableModel(new Object[]{"Dateipfad", "Größe (MB)"}, 0);
        resultTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        resultTable.setRowSorter(sorter);
        JScrollPane tableScrollPane = new JScrollPane(resultTable);

        // Mouse Listener für Doppelklick und Kontextmenü
        resultTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Doppelklick
                    int row = resultTable.convertRowIndexToModel(resultTable.getSelectedRow());
                    String filePath = tableModel.getValueAt(row, 0).toString();
                    openFile(new File(filePath));
                } else if (SwingUtilities.isRightMouseButton(e)) { // Rechtsklick
                    int row = resultTable.rowAtPoint(e.getPoint());
                    resultTable.setRowSelectionInterval(row, row);
                    String filePath = tableModel.getValueAt(resultTable.convertRowIndexToModel(row), 0).toString();
                    showContextMenu(e, new File(filePath));
                }
            }
        });

        // Button-Panel für die Schaltflächen "Suchen" und "Schließen"
        JPanel buttonPanel = new JPanel(new BorderLayout());

        // Rechtsbündige Buttons in einem separaten Panel
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton searchButton = new JButton("Suchen");
        searchButton.addActionListener(e -> searchFiles());

        JButton closeButton = new JButton("Schließen");
        closeButton.addActionListener(e -> dispose());

        rightButtonPanel.add(searchButton);
        rightButtonPanel.add(closeButton);

        // Hinzufügen der Such-Schaltfläche unterhalb des Ergebnisbereiches
        buttonPanel.add(rightButtonPanel, BorderLayout.SOUTH);

        // Layout der Hauptoberfläche
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(inputPanel, BorderLayout.NORTH);
        getContentPane().add(tableScrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        

        // Setze den Suchbutton als Default Button
        getRootPane().setDefaultButton(searchButton);
    }

    private void chooseDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            directoryLabel.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private void searchFiles() {
        String directoryPath = directoryLabel.getText();
        if ("Kein Ordner ausgewählt".equals(directoryPath)) {
            JOptionPane.showMessageDialog(this, "Bitte wählen Sie einen Ordner aus.", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String filenamePattern = filenameField.getText();
        String content = contentField.getText();
        boolean caseSensitive = caseSensitiveCheckBox.isSelected();

        File directory = new File(directoryPath);
        FileSearcher fileSearcher = new FileSearcher();
        List<File> foundFiles = fileSearcher.searchByName(directory, filenamePattern);

        if (!content.isEmpty()) {
            ContentSearcher contentSearcher = new ContentSearcher(Runtime.getRuntime().availableProcessors() * 2);
            try {
                foundFiles = contentSearcher.searchInFiles(foundFiles, content, caseSensitive);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                contentSearcher.shutdown();
            }
        }

        updateTable(foundFiles);
    }

    private void updateTable(List<File> foundFiles) {
        tableModel.setRowCount(0);
        for (File file : foundFiles) {
            long fileSize = file.length();
            tableModel.addRow(new Object[]{file.getAbsolutePath(), formatFileSize(fileSize)});
        }
    }

    private String formatFileSize(long size) {
        double sizeInMB = size / (1024.0 * 1024.0);
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(sizeInMB);
    }

    private void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showContextMenu(MouseEvent e, File file) {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem openItem = new JMenuItem("Öffnen");
        openItem.addActionListener(ae -> openFile(file));
        contextMenu.add(openItem);

        JMenuItem exploreItem = new JMenuItem("Im Explorer anzeigen");
        exploreItem.addActionListener(ae -> showInExplorer(file));
        contextMenu.add(exploreItem);

        contextMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void showInExplorer(File file) {
        try {
            Desktop.getDesktop().open(file.getParentFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            FileSearchApp app = new FileSearchApp();
            // Icon laden und setzen
            ImageIcon icon = new ImageIcon(FileSearchApp.class.getResource("/resources/app-icon.png"));
            app.setIconImage(icon.getImage());
            app.setVisible(true);
        });
    }
}
