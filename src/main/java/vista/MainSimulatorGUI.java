/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import estructuras.CustomLinkedList;
import modelo.FileSystemNode;
import modelo.ProcessControlBlock;
import modelo.SimulatedDisk;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

/**
 * @author Macorre21
 * Subsistema: Presentación Visual
 * Descripción: JFrame principal que aloja los componentes Swing.
 */
public class MainSimulatorGUI extends JFrame {

    private final JTree fileTree;
    private final JTable processTable;
    private final JTable diskTable;
    private final JTextArea logArea;
    
    // --- COMPONENTES FASE 1 (Seguridad y Crash) ---
    private final JComboBox<String> userSelector;
    private final JButton btnCrash;
    
    // --- COMPONENTES FASE 2 (Creación de Archivos) ---
    private final JTextField txtFileName;
    private final JTextField txtFileSize;
    private final JButton btnCreate;
    private final JButton btnDelete;
    private final JButton btnRecover;
    private final JButton btnCreateFolder;
    private final JButton btnRename;

    // --- COMPONENTES FASE 3 (Planificación de Disco) ---
    private final JComboBox<String> cmbPoliticas;
    private final JTextField txtCabezalInicial;
    private final JLabel lblPosicionCabezal;
    private final JButton btnLoadJSON;

    public MainSimulatorGUI() {
        setTitle("Simulador de Sistema Operativo");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Inicialización de componentes básicos
        fileTree = new JTree(new DefaultMutableTreeNode("Root"));
        processTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Estado", "Bloque Inicio"}, 0));
        diskTable = new JTable(new DefaultTableModel(new Object[]{"Bloque", "Ocupado", "Siguiente"}, 0));
        logArea = new JTextArea();
        logArea.setEditable(false);

        // --- CONTENEDOR DE PANELES SUPERIORES ---
        JPanel headerContainer = new JPanel(new GridLayout(2, 1)); // Dos filas para organizar mejor

        // --- PANEL SUPERIOR 1: Controles de Archivos y Sistema ---
        JPanel topPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        topPanel1.add(new JLabel("Usuario:"));
        userSelector = new JComboBox<>(new String[]{"Modo Administrador", "Modo Usuario"});
        topPanel1.add(userSelector);
        
        topPanel1.add(new JLabel(" | Archivo:"));
        txtFileName = new JTextField(8);
        topPanel1.add(txtFileName);
        
        topPanel1.add(new JLabel(" Tamaño:"));
        txtFileSize = new JTextField(4);
        topPanel1.add(txtFileSize);
        
        btnCreate = new JButton("Crear");
        topPanel1.add(btnCreate);
        
        btnCreateFolder = new JButton("Crear Carpeta");
        topPanel1.add(btnCreateFolder);
        
        btnDelete = new JButton("Eliminar");
        topPanel1.add(btnDelete);
        
        btnRecover = new JButton("Recuperar");
        topPanel1.add(btnRecover);
        
        btnCrash = new JButton("Simular Falla");
        btnCrash.setBackground(Color.RED);
        btnCrash.setForeground(Color.BLACK);
        btnCrash.setContentAreaFilled(true);
        topPanel1.add(btnCrash);
        
        btnRename = new JButton("Renombrar");
        topPanel1.add(btnRename);
        
        headerContainer.add(topPanel1);

        // --- PANEL SUPERIOR 2: Controles de Planificación de Disco ---
        JPanel topPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel2.setBackground(new Color(230, 240, 255)); // Un color azul clarito para diferenciarlo
        
        topPanel2.add(new JLabel("Política de Disco:"));
        cmbPoliticas = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"});
        topPanel2.add(cmbPoliticas);
        
        topPanel2.add(new JLabel("  Cabezal Inicial:"));
        txtCabezalInicial = new JTextField("50", 4); // Por defecto en 50, como el PDF
        topPanel2.add(txtCabezalInicial);
        
        lblPosicionCabezal = new JLabel("  >>> Cabezal Actual: 50 <<<");
        lblPosicionCabezal.setFont(new Font("Arial", Font.BOLD, 14));
        lblPosicionCabezal.setForeground(Color.BLUE);
        topPanel2.add(lblPosicionCabezal);
        
        btnLoadJSON = new JButton("Cargar Prueba JSON");
        topPanel2.add(btnLoadJSON);

        headerContainer.add(topPanel2);
        
        // Añadimos el contenedor completo arriba
        add(headerContainer, BorderLayout.NORTH);
        // ---------------------------------------------

        // Paneles centrales y laterales
        add(new JScrollPane(fileTree), BorderLayout.WEST);
        
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.add(new JScrollPane(processTable));
        centerPanel.add(new JScrollPane(diskTable));
        add(centerPanel, BorderLayout.CENTER);
        
        add(new JScrollPane(logArea), BorderLayout.SOUTH);
    }

    public void updateFileTree(FileSystemNode root) {
        SwingUtilities.invokeLater(() -> {
            DefaultMutableTreeNode guiRoot = buildGuiTree(root);
            fileTree.setModel(new DefaultTreeModel(guiRoot));
            for (int i = 0; i < fileTree.getRowCount(); i++) {
                fileTree.expandRow(i);
            }
        });
    }

    private DefaultMutableTreeNode buildGuiTree(FileSystemNode node) {
        String displayName = node.getName() + (node.getOwner() != null ? " [" + node.getOwner() + "]" : "");
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(displayName);
        
        if (node.isDirectory() && node.getChildren() != null) {
            CustomLinkedList<FileSystemNode> children = node.getChildren();
            for (int i = 0; i < children.size(); i++) {
                treeNode.add(buildGuiTree(children.get(i)));
            }
        }
        return treeNode;
    }

    public void updateProcessTable(CustomLinkedList<ProcessControlBlock> processes) {
        SwingUtilities.invokeLater(() -> {
            DefaultTableModel model = (DefaultTableModel) processTable.getModel();
            model.setRowCount(0); 
            
            for (int i = 0; i < processes.size(); i++) {
                ProcessControlBlock pcb = processes.get(i);
                model.addRow(new Object[]{
                    pcb.getProcessId(), 
                    pcb.getState(), 
                    pcb.getStartBlockId()
                });
            }
        });
    }

    public void updateDiskTable(SimulatedDisk disk) {
        SwingUtilities.invokeLater(() -> {
            DefaultTableModel model = (DefaultTableModel) diskTable.getModel();
            model.setRowCount(0);
            
            int[] fat = disk.getFatTable();
            for (int i = 0; i < fat.length; i++) {
                String status = (fat[i] == 0) ? "Libre" : "Ocupado";
                model.addRow(new Object[]{i, status, fat[i]});
            }
        });
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength()); 
        });
    }

    public void showErrorDialog(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    // =========================================================================
    // GETTERS PARA EL CONTROLADOR
    // =========================================================================
    public JComboBox<String> getUserSelector() { return userSelector; }
    public JButton getBtnCrash() { return btnCrash; }
    public JTree getFileTree() { return fileTree; }
    
    public JTextField getTxtFileName() { return txtFileName; }
    public JTextField getTxtFileSize() { return txtFileSize; }
    public JButton getBtnCreate() { return btnCreate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnRecover() { return btnRecover; }

    // --- NUEVOS GETTERS FASE 3 ---
    public JComboBox<String> getCmbPoliticas() { return cmbPoliticas; }
    public JTextField getTxtCabezalInicial() { return txtCabezalInicial; }
    public JLabel getLblPosicionCabezal() { return lblPosicionCabezal; }
    public JButton getBtnLoadJSON() { return btnLoadJSON; }
    public JButton getBtnCreateFolder() { return btnCreateFolder; }
    public JButton getBtnRename() { return btnRename; }
}