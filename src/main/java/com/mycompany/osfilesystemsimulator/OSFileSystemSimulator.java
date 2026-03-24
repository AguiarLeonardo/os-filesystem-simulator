/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.osfilesystemsimulator;

import controlador.GUIController;
import estructuras.CustomLinkedList;
import modelo.ConcurrencyManager;
import modelo.CrashSimulator;
import modelo.DiskScheduler;
import modelo.FileSystemNode;
import modelo.JournalingManager;
import modelo.JsonManager;
import modelo.ProcessControlBlock;
import modelo.ProcessManager;
import modelo.SimulatedDisk;
import modelo.UserManager;
import vista.MainSimulatorGUI;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * @author Macorre21
 * Clase Principal: Punto de entrada del simulador con integración MVC completa.
 */
public class OSFileSystemSimulator {

    public static void main(String[] args) {
        
        // Ejecutar en el hilo de despacho de eventos de Swing para evitar bloqueos visuales
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Estilo visual y preparación
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                System.out.println("Iniciando la carga del Kernel Simulado...");

                // 2. Instanciar Hardware (Disco)
                SimulatedDisk discoFisico = new SimulatedDisk(1024);
                
                // Datos de prueba iniciales para el disco
                discoFisico.getFatTable()[0] = 1;  
                discoFisico.getFatTable()[1] = -1; 
                discoFisico.getFatTable()[5] = -1; 

                // 3. Instanciar Estructura de Archivos (Jerarquía inicial)
                FileSystemNode nodoRaiz = new FileSystemNode("C:", true, "SYSTEM");
                nodoRaiz.getChildren().add(new FileSystemNode("Windows", true, "SYSTEM"));
                nodoRaiz.getChildren().add(new FileSystemNode("Users", true, "SYSTEM"));

                // 4. Instanciar Gestores de Sistema y Seguridad
                UserManager userManager = new UserManager();
                userManager.setSession("ADMIN", "Administrador"); 
                
                JournalingManager journal = new JournalingManager();
                ConcurrencyManager concurrency = new ConcurrencyManager();
                
                // El CrashSimulator se encarga de gestionar los fallos de energía
                CrashSimulator crashSimulator = new CrashSimulator(journal);

                // 5. Instanciar Planificadores de Procesos y Disco
                DiskScheduler diskScheduler = new DiskScheduler(0); 
                ProcessManager processManager = new ProcessManager(diskScheduler);

                // 6. Instanciar Motor de Persistencia (JSON)
                JsonManager jsonManager = new JsonManager("sistema_archivos_dump.json");

                // 7. Inicializar Interfaz (Vista) y Controlador
                MainSimulatorGUI vistaPrincipal = new MainSimulatorGUI();
                GUIController controlador = new GUIController(vistaPrincipal, processManager, crashSimulator);

                // --- INTEGRACIÓN CLAVE ---
                // Pasamos la referencia del nodo raíz al controlador para que pueda 
                // añadir archivos nuevos cuando el usuario lo pida en la GUI.
                controlador.setSystemResources(nodoRaiz, discoFisico);
                // --------------------------

                // 8. Cargar datos iniciales en la Vista
                vistaPrincipal.updateFileTree(nodoRaiz);
                vistaPrincipal.updateDiskTable(discoFisico);
                
                // Agregamos un log inicial para confirmar el estado
                vistaPrincipal.appendLog("Kernel: Estructuras de datos inicializadas.");
                vistaPrincipal.appendLog("Seguridad: Sesión activa como ADMIN.");
                vistaPrincipal.appendLog("Persistencia: Archivo JSON vinculado.");

                // 9. Configuración final de la ventana
                vistaPrincipal.setLocationRelativeTo(null); // Centrar en pantalla
                vistaPrincipal.setVisible(true);
                
                // 10. Arrancar el motor del simulador (Timer de refresco y Hilos de procesos)
                controlador.startSimulation();

                System.out.println("Simulador OS en línea. Motor de procesos y GUI sincronizados.");

            } catch (Exception e) {
                System.err.println("Fallo crítico durante la inicialización: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}