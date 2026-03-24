/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package controlador;

import modelo.CrashSimulator;
import modelo.ProcessControlBlock;
import modelo.ProcessManager;
import modelo.SimulatedDisk; 
import modelo.FileSystemNode; 
import util.InputValidator;
import vista.MainSimulatorGUI;

import javax.swing.*;
import javax.swing.tree.TreePath; // Importación para manejar el árbol
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Macorre21
 * Subsistema: Controlador Principal (MVC)
 * Descripción: Puente entre la interfaz gráfica de Swing y la lógica de negocio concurrente.
 */
public class GUIController {

    private final MainSimulatorGUI view;
    private final ProcessManager processManager;
    private final CrashSimulator crashSimulator;
    private final Timer refreshTimer;
    private FileSystemNode rootNode; 
    
    // --- NUEVAS VARIABLES GLOBALES ---
    private int nextProcessId = 1000; 
    private SimulatedDisk discoFisico; 
    private int cantidadArchivosAnterior = -1; // <-- CONTROL DE PARPADEO DEL ÁRBOL

    public GUIController(MainSimulatorGUI view, ProcessManager pm, CrashSimulator cs) {
        this.view = view;
        this.processManager = pm;
        this.crashSimulator = cs;

        // Configurar el Timer para refrescar la GUI cada 500ms
        this.refreshTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshViews();
            }
        });

        // --- VINCULACIÓN DE EVENTOS ---
        this.view.getBtnCrash().addActionListener(e -> handleCrashEvent());
        this.view.getBtnLoadJSON().addActionListener(e -> handleLoadJSONAction());
        this.view.getBtnRename().addActionListener(e -> handleRenameAction());
        
        this.view.getBtnCrash().setOpaque(true);
        this.view.getBtnCrash().setContentAreaFilled(true);
        
        // Escuchador para el botón de crear Archivo
        this.view.getBtnCreate().addActionListener(e -> {
            String nombre = this.view.getTxtFileName().getText();
            String tamano = this.view.getTxtFileSize().getText();
            handleCreateAction(nombre, tamano);
        });
        
        // --- NUEVO: Escuchador para crear CARPETA ---
        this.view.getBtnCreateFolder().addActionListener(e -> {
            String nombre = this.view.getTxtFileName().getText();
            handleCreateFolderAction(nombre);
        });

        this.view.getBtnDelete().addActionListener(e -> handleDeleteAction());
        this.view.getBtnRecover().addActionListener(e -> handleRecoverAction());

        // --- EVENTOS FASE 3 (Planificación de Disco) ---
        this.view.getCmbPoliticas().addActionListener(e -> {
            String selectedPolicy = (String) this.view.getCmbPoliticas().getSelectedItem();
            try {
                this.processManager.getScheduler().setPolicy(selectedPolicy);
                this.view.appendLog("Sistema: Política de disco cambiada a " + selectedPolicy);
            } catch (Exception ex) {
                this.view.showErrorDialog(ex.getMessage());
            }
        });

        this.view.getTxtCabezalInicial().addActionListener(e -> {
            try {
                int nuevoCabezal = Integer.parseInt(this.view.getTxtCabezalInicial().getText());
                this.processManager.getScheduler().setCurrentHeadPosition(nuevoCabezal);
                this.view.appendLog("Sistema: Cabezal movido manualmente al bloque " + nuevoCabezal);
            } catch (NumberFormatException ex) {
                this.view.showErrorDialog("Error: Ingrese un número válido para el cabezal.");
            }
        });
    }

    public void setSystemResources(FileSystemNode root, SimulatedDisk disco) {
        this.rootNode = root;
        this.discoFisico = disco;
        this.processManager.setRecursosSistema(disco, root); 
    }

    public void startSimulation() {
        processManager.startProcessing();
        refreshTimer.start();
        view.appendLog("Sistema Operativo iniciado. Motor de procesos en línea.");
    }

    // =========================================================================
    // MÉTODOS DE BÚSQUEDA EN EL ÁRBOL (NUEVOS PARA SOPORTAR CARPETAS)
    // =========================================================================
    
    // Convierte el path visual del JTree en el nodo real de tu modelo
    private FileSystemNode findNodeByPath(TreePath path) {
        if (path == null) return rootNode;
        Object[] elements = path.getPath();
        FileSystemNode current = rootNode;
        
        for (int i = 1; i < elements.length; i++) {
            String nodeText = elements[i].toString();
            String name = nodeText.split(" \\[")[0]; // Quitamos el [ADMIN] o [USER]
            
            boolean found = false;
            if (current.getChildren() != null) {
                for (int j = 0; j < current.getChildren().size(); j++) {
                    FileSystemNode child = current.getChildren().get(j);
                    if (child.getName().equals(name)) {
                        current = child;
                        found = true;
                        break;
                    }
                }
            }
            if (!found) return rootNode; 
        }
        return current;
    }

    // Obtiene la carpeta destino según lo que el usuario haya seleccionado
    private FileSystemNode getSelectedFolder() {
        TreePath selectedPath = view.getFileTree().getSelectionPath();
        if (selectedPath == null) return rootNode;
        
        FileSystemNode selectedNode = findNodeByPath(selectedPath);
        if (selectedNode.isDirectory()) {
            return selectedNode;
        } else {
            // Si seleccionó un archivo, devolvemos su carpeta padre
            return findNodeByPath(selectedPath.getParentPath());
        }
    }
    
    // --- NUEVO: Detecta cambios en cantidad y en NOMBRES ---
    private int getTreeStateHash(FileSystemNode node) {
        if (node == null) return 0;
        // Usamos el código hash del nombre para detectar si cambia
        int hash = node.getName().hashCode(); 
        if (node.getChildren() != null) {
            for (int i = 0; i < node.getChildren().size(); i++) {
                hash += getTreeStateHash(node.getChildren().get(i));
            }
        }
        return hash;
    }

    // =========================================================================
    // ACCIONES DE BOTONES
    // =========================================================================

    public void handleCreateAction(String name, String sizeStr) {
        if (crashSimulator.isSystemCrashed()) {
            view.showErrorDialog("Operación denegada: El sistema está en estado de fallo (Crashed)."); return;
        }
        try {
            String validName = InputValidator.validateFileName(name);
            int validSize = InputValidator.validateIntegerInput(sizeStr, 1, 1024);
            String usuarioActual = view.getUserSelector().getSelectedItem().toString().toUpperCase().contains("ADMIN") ? "ADMIN" : "USER";

            FileSystemNode parentFolder = getSelectedFolder(); // MAGIA: Obtenemos la carpeta actual

            ProcessControlBlock newProcess = new ProcessControlBlock(nextProcessId++, "CREATE", validName, validSize, usuarioActual);
            newProcess.setParentNode(parentFolder); // Le pasamos la carpeta al proceso
            
            processManager.addProcess(newProcess);
            view.appendLog("Proceso [" + newProcess.getProcessId() + "] encolado: Crear Archivo '" + validName + "' en /" + parentFolder.getName());
            
            view.getTxtFileName().setText(""); view.getTxtFileSize().setText("");
        } catch (Exception ex) { view.showErrorDialog("Error: " + ex.getMessage()); }
    }

    // --- NUEVO: CREAR CARPETA ---
    public void handleCreateFolderAction(String name) {
        if (crashSimulator.isSystemCrashed()) {
            view.showErrorDialog("Operación denegada: El sistema está en estado de fallo."); return;
        }
        try {
            String validName = InputValidator.validateFileName(name);
            String usuarioActual = view.getUserSelector().getSelectedItem().toString().toUpperCase().contains("ADMIN") ? "ADMIN" : "USER";

            FileSystemNode parentFolder = getSelectedFolder(); // MAGIA: Obtenemos la carpeta actual

            // Para las carpetas enviamos el tamaño como 1 bloque virtual (o 0), usamos MKDIR
            ProcessControlBlock newProcess = new ProcessControlBlock(nextProcessId++, "MKDIR", validName, 1, usuarioActual);
            newProcess.setParentNode(parentFolder);
            
            processManager.addProcess(newProcess);
            view.appendLog("Proceso [" + newProcess.getProcessId() + "] encolado: Crear Carpeta '" + validName + "' en /" + parentFolder.getName());
            
            view.getTxtFileName().setText(""); view.getTxtFileSize().setText("");
        } catch (Exception ex) { view.showErrorDialog("Error: " + ex.getMessage()); }
    }
    
    public void handleDeleteAction() {
        if (crashSimulator.isSystemCrashed()) {
            view.showErrorDialog("Operación denegada: Sistema caído."); return;
        }

        TreePath selectedPath = view.getFileTree().getSelectionPath();
        if (selectedPath == null || selectedPath.getPathCount() == 1) {
            view.showErrorDialog("Seleccione un archivo o carpeta válida (no puede borrar la raíz)."); return;
        }

        FileSystemNode nodeToDelete = findNodeByPath(selectedPath);
        FileSystemNode parentNode = findNodeByPath(selectedPath.getParentPath());

        String usuarioActual = view.getUserSelector().getSelectedItem().toString().toUpperCase().contains("ADMIN") ? "ADMIN" : "USER";
        if (usuarioActual.equals("USER") && nodeToDelete.getOwner().equals("ADMIN")) {
            view.showErrorDialog("Acceso Denegado: Un usuario estándar no puede eliminar archivos/carpetas del Sistema/Administrador."); return;
        }
        
        ProcessControlBlock newProcess = new ProcessControlBlock(nextProcessId++, "DELETE", nodeToDelete.getName(), nodeToDelete.getSizeInBlocks(), nodeToDelete.getOwner());
        newProcess.setParentNode(parentNode); // Le decimos en qué carpeta buscarlo para borrarlo
        newProcess.setStartBlockId(nodeToDelete.getStartBlockId()); 

        try {
            processManager.addProcess(newProcess);
            view.appendLog("Proceso [" + newProcess.getProcessId() + "] encolado: Eliminar '" + nodeToDelete.getName() + "'");
        } catch (Exception ex) { view.showErrorDialog("Error interno al encolar: " + ex.getMessage()); }
    }

    public void handleCrashEvent() {
        crashSimulator.triggerCrash();
        view.appendLog("¡ALERTA! Se ha simulado un fallo catastrófico.");
        processManager.stopProcessing(); 
    }
    
    public void handleRecoverAction() {
        if (!crashSimulator.isSystemCrashed()) {
            view.showErrorDialog("El sistema no está caído. No es necesario recuperar."); return;
        }
        view.appendLog("INICIANDO RECUPERACIÓN DEL SISTEMA DESDE EL JOURNAL...");

        discoFisico.formatDisk(); 
        rootNode.getChildren().clear(); 

        java.util.List<String> historial = util.JournalManager.readJournal();
        
        if (historial.isEmpty()) {
            view.appendLog("El Journal está vacío. Sistema recuperado en estado de fábrica.");
        } else {
            for (String linea : historial) {
                String[] partes = linea.split(",");
                String operacion = partes[0];
                try {
                    // Para simplificar la recuperación post-crash, recreamos todo en la raíz visual
                    if (operacion.equals("CREATE") && partes.length == 4) {
                        int startBlock = discoFisico.allocateChain(partes[1], Integer.parseInt(partes[2]));
                        FileSystemNode recuperado = new FileSystemNode(partes[1], false, partes[3]);
                        recuperado.setSizeInBlocks(Integer.parseInt(partes[2]));
                        recuperado.setStartBlockId(startBlock);
                        rootNode.getChildren().add(recuperado);
                    } else if (operacion.equals("MKDIR") && partes.length == 3) {
                        FileSystemNode recuperado = new FileSystemNode(partes[1], true, partes[2]);
                        rootNode.getChildren().add(recuperado);
                    } else if (operacion.equals("DELETE") && partes.length == 2) {
                        for (int i = 0; i < rootNode.getChildren().size(); i++) {
                            FileSystemNode archivo = rootNode.getChildren().get(i);
                            if (archivo.getName().equals(partes[1])) {
                                discoFisico.freeChain(archivo.getStartBlockId());
                                rootNode.getChildren().remove(archivo); break;
                            }
                        }
                    }
                } catch (Exception e) { view.appendLog("Error recuperando operación: " + linea); }
            }
            view.appendLog("Se han recuperado " + rootNode.getChildren().size() + " elementos del Journal.");
        }

        crashSimulator.recoverSystem(discoFisico, rootNode);
        processManager.startProcessing(); 
        view.appendLog("¡Sistema Operativo restaurado y en línea!");
        
        view.updateDiskTable(discoFisico);
        view.updateFileTree(rootNode);
        cantidadArchivosAnterior = getTreeStateHash(rootNode);
    }

   public void handleLoadJSONAction() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccione el archivo JSON de prueba");
        
        if (fileChooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // 1. Leer el archivo tal cual (SIN borrar los espacios)
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line).append(" ");
                }
                String jsonStr = sb.toString(); 

                // 2. Extraer "initial_head"
                int initialHead = 50; 
                Pattern headPattern = Pattern.compile("\"initial_head\"\\s*:\\s*(\\d+)");
                Matcher headMatcher = headPattern.matcher(jsonStr);
                if (headMatcher.find()) {
                    initialHead = Integer.parseInt(headMatcher.group(1));
                    this.processManager.getScheduler().setCurrentHeadPosition(initialHead);
                    this.view.getTxtCabezalInicial().setText(String.valueOf(initialHead));
                }

                // 3. Preparar el entorno (Limpiamos para cargar el escenario del JSON)
                discoFisico.formatDisk();
                rootNode.getChildren().clear();

                // 4. Extraer "system_files" para poblar el árbol
                // Captura: "11": {"name": "boot_sect.bin", "blocks": 2}
                Pattern filePattern = Pattern.compile("\"(\\d+)\"\\s*:\\s*\\{\\s*\"name\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"blocks\"\\s*:\\s*(\\d+)\\s*\\}");
                Matcher fileMatcher = filePattern.matcher(jsonStr);
                
                // Mapa temporal para recordar qué archivo está en qué bloque
                java.util.Map<Integer, String> posToNameMap = new java.util.HashMap<>();

                while (fileMatcher.find()) {
                    int pos = Integer.parseInt(fileMatcher.group(1));
                    String name = fileMatcher.group(2);
                    int blocks = Integer.parseInt(fileMatcher.group(3));

                    posToNameMap.put(pos, name);

                    // Recreamos el archivo en el sistema visual para que existan antes de las operaciones
                    FileSystemNode fileNode = new FileSystemNode(name, false, "SYSTEM");
                    fileNode.setSizeInBlocks(blocks);
                    fileNode.setStartBlockId(pos); 
                    rootNode.getChildren().add(fileNode);
                    
                    // Le decimos al disco: "Ocupa desde el bloque 'pos', 'n' cantidad de bloques con este nombre"
                    discoFisico.allocateSpecificBlocks(pos, blocks, name);
                }

                // 5. Extraer "requests" (Las tareas a encolar)
                // Captura: {"pos": 11, "op": "READ"}
                Pattern reqPattern = Pattern.compile("\\{\\s*\"pos\"\\s*:\\s*(\\d+)\\s*,\\s*\"op\"\\s*:\\s*\"([^\"]+)\"\\s*\\}");
                Matcher reqMatcher = reqPattern.matcher(jsonStr);
                
                int requestCount = 0;
                while (reqMatcher.find()) {
                    int pos = Integer.parseInt(reqMatcher.group(1));
                    String op = reqMatcher.group(2).toUpperCase(); // READ, UPDATE, DELETE
                    
                    // Buscamos el nombre del archivo usando la posición. Si no existe, le ponemos un nombre genérico
                    String fileName = posToNameMap.getOrDefault(pos, "Archivo_Desconocido_" + pos);

                    ProcessControlBlock process = new ProcessControlBlock(nextProcessId++, op, fileName, 1, "SYSTEM");
                    process.setStartBlockId(pos); // <-- CLAVE: Le decimos al disco a dónde moverse
                    process.setParentNode(rootNode); 

                    // Si es un UPDATE (renombrar), le damos un nombre de prueba automático
                    if (op.equals("UPDATE")) {
                        process.setNewFileName(fileName.split("\\.")[0] + "_modificado"); 
                    }

                    processManager.addProcess(process);
                    requestCount++;
                }
                
                // 6. Refrescar la GUI con el nuevo entorno
                view.updateFileTree(rootNode);
                view.updateDiskTable(discoFisico);
                cantidadArchivosAnterior = getTreeStateHash(rootNode);
                
                view.appendLog("======================================");
                view.appendLog("✓ Escenario JSON cargado exitosamente.");
                view.appendLog("→ Cabezal en bloque: " + initialHead);
                view.appendLog("→ Archivos de sistema precargados: " + posToNameMap.size());
                view.appendLog("→ Peticiones encoladas: " + requestCount);
                view.appendLog("======================================");

            } catch (Exception ex) { 
                view.showErrorDialog("Error al parsear el archivo JSON: " + ex.getMessage()); 
            }
        }
    }

    private void refreshViews() {
        if (crashSimulator.isSystemCrashed()) return; 

        try {
            view.updateProcessTable(processManager.getAllProcesses());
            
            if (discoFisico != null && rootNode != null) {
                view.updateDiskTable(discoFisico); 
                
                // --- ARREGLO DEL ÁRBOL RECURSIVO ---
                int estadoActual = getTreeStateHash(rootNode); // <-- USAMOS EL NUEVO MÉTODO
                if (estadoActual != cantidadArchivosAnterior) {
                    view.updateFileTree(rootNode);     
                    cantidadArchivosAnterior = estadoActual; 
                }
            }
            
            if (processManager.getScheduler() != null) {
                int cabezalActual = processManager.getScheduler().getCurrentHeadPosition();
                view.getLblPosicionCabezal().setText("  >>> Cabezal Actual: " + cabezalActual + " <<<");
            }
            
        } catch (Exception e) {}
    }
    
    // --- NUEVO MÉTODO: RENOMBRAR (UPDATE) ---
    public void handleRenameAction() {
        if (crashSimulator.isSystemCrashed()) {
            view.showErrorDialog("Operación denegada: Sistema caído."); return;
        }

        TreePath selectedPath = view.getFileTree().getSelectionPath();
        if (selectedPath == null || selectedPath.getPathCount() == 1) {
            view.showErrorDialog("Seleccione un archivo o carpeta para renombrar."); return;
        }

        // --- VALIDACIÓN ESTRICTA DE LA RÚBRICA ---
        String modoSeleccionado = view.getUserSelector().getSelectedItem().toString().toUpperCase();
        if (!modoSeleccionado.contains("ADMIN")) {
            view.showErrorDialog("Acceso Denegado: Solo los administradores pueden modificar nombres (Actualizar).");
            return;
        }

        FileSystemNode nodeToRename = findNodeByPath(selectedPath);
        FileSystemNode parentNode = findNodeByPath(selectedPath.getParentPath());

        // Pedimos el nuevo nombre con una ventana emergente
        String nuevoNombre = JOptionPane.showInputDialog(view, "Ingrese el nuevo nombre para '" + nodeToRename.getName() + "':");
        
        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            try {
                String validName = InputValidator.validateFileName(nuevoNombre);
                
                ProcessControlBlock newProcess = new ProcessControlBlock(nextProcessId++, "UPDATE", nodeToRename.getName(), 0, "ADMIN");
                newProcess.setParentNode(parentNode);
                newProcess.setNewFileName(validName); // Le pasamos el nuevo nombre
                
                processManager.addProcess(newProcess);
                view.appendLog("Proceso [" + newProcess.getProcessId() + "] encolado: Renombrar '" + nodeToRename.getName() + "' a '" + validName + "'");
            } catch (Exception ex) {
                view.showErrorDialog("Error: " + ex.getMessage());
            }
        }
    }
}