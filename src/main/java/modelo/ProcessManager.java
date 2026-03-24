/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import estructuras.CustomLinkedList;
import estructuras.ThreadSafeQueue;
import java.util.concurrent.Semaphore;

/**
 * Autor: AguiarLeonardo 
 * Subsistema: Gestión de Procesos y Concurrencia
 * Descripción: Orquesta el ciclo de vida de los procesos encolados. 
 * Implementa un hilo consumidor continuo que extrae procesos de la cola de E/S 
 * (ThreadSafeQueue) y delega su ejecución, garantizando la exclusión mutua 
 * en las transiciones de estado para evitar condiciones de carrera con la GUI.
 */
public class ProcessManager {

    private final ThreadSafeQueue<ProcessControlBlock> ioQueue;
    private final CustomLinkedList<ProcessControlBlock> allProcesses;
    private DiskScheduler scheduler;
    private boolean isRunning;
    private final Semaphore executionMutex;
    private int processCounter = 1; // Para IDs autoincrementales de procesos automáticos
    private SimulatedDisk discoFisico;
    private FileSystemNode nodoRaiz;
    
    public ProcessManager(DiskScheduler scheduler) {
        this.ioQueue = new ThreadSafeQueue<>();
        this.allProcesses = new CustomLinkedList<>();
        this.scheduler = scheduler;
        this.isRunning = false;
        this.executionMutex = new Semaphore(1);
    }

    // NUEVO: Método para generar carga de trabajo automática (Para la nota máxima)
    public void crearProcesoAleatorio() {
        try {
            String[] nombres = {"sys_log.txt", "temp_data.bin", "auto_save.bak", "cache.tmp"};
            String nombre = nombres[(int)(Math.random() * nombres.length)];
            int randomBlocks = (int)(Math.random() * 5) + 1; // Entre 1 y 5 bloques
            
            // --- CORRECCIÓN FASE 2: Constructor con 5 parámetros ---
            // ID, Operación, Nombre del archivo, Bloques requeridos, Dueño
            ProcessControlBlock nuevo = new ProcessControlBlock(processCounter++, "CREATE", nombre, randomBlocks, "SYSTEM");
            this.addProcess(nuevo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void setRecursosSistema(SimulatedDisk disco, FileSystemNode raiz) {
        this.discoFisico = disco;
        this.nodoRaiz = raiz;
    }

    public void addProcess(ProcessControlBlock pcb) throws InterruptedException {
        // --- CORRECCIÓN: Usar los estados válidos de tu PCB ("listo" en vez de "READY") ---
        pcb.setState("listo"); 
        allProcesses.add(pcb);
        ioQueue.enqueue(pcb);
    }

    public void startProcessing() {
        if (isRunning) return;
        isRunning = true;

        Thread executionThread = new Thread(() -> {
            while (isRunning) {
                try {
                    // Simular llegada de procesos de sistema cada cierto tiempo
                    // if (Math.random() > 0.8) { crearProcesoAleatorio(); } // Descomenta para probar con procesos automáticos

                    // --- EXTRACCIÓN Y MOVIMIENTO DE CABEZAL ---
                    ProcessControlBlock pcb = ioQueue.extractScheduled(this.scheduler);
                    
                    if (pcb != null) {
                        // ¡MAGIA VISUAL! Actualizamos la posición real del cabezal en el Scheduler
                        if (pcb.getStartBlockId() != -1) {
                            this.scheduler.setCurrentHeadPosition(pcb.getStartBlockId());
                        }
                        executeProcess(pcb);
                    }
                    Thread.sleep(1000); // Pausa entre ciclos del procesador
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    isRunning = false;
                }
            }
        });
        
        executionThread.setName("OS-ProcessManager-Thread");
        executionThread.start();
    }

    public void stopProcessing() {
        this.isRunning = false;
    }

    private void executeProcess(ProcessControlBlock pcb) {
        try {
            executionMutex.acquire();
            try {
                // 1. Pasa a ejecución
                pcb.setState("ejecutando"); 
                
                // Simula el tiempo que tarda el disco/CPU en procesar
                Thread.sleep(1500); 

                // 2. Lógica de operaciones CRUD CON SOPORTE PARA CARPETAS
                if ("CREATE".equals(pcb.getOperationType())) {
                    
                    if (discoFisico != null && pcb.getParentNode() != null) {
                        int bloquesNecesarios = pcb.getRequiredBlocks();
                        
                        try {
                            int startBlock = discoFisico.allocateChain(pcb.getTargetFileName(), bloquesNecesarios);
                            
                            pcb.setStartBlockId(startBlock); 
                            
                            // Añadimos al árbol visual dentro del PARENT NODE
                            FileSystemNode nuevoArchivo = new FileSystemNode(pcb.getTargetFileName(), false, pcb.getTargetOwner());
                            nuevoArchivo.setSizeInBlocks(bloquesNecesarios);
                            nuevoArchivo.setStartBlockId(startBlock);
                            
                            pcb.getParentNode().getChildren().add(nuevoArchivo);
                            
                            // Guardamos el CREATE con sus datos separados por comas
                            util.JournalManager.logOperation("CREATE," + pcb.getTargetFileName() + "," + bloquesNecesarios + "," + pcb.getTargetOwner());
                            
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                            pcb.setState("bloqueado"); // No hay espacio
                            return; 
                        }
                    }
                } 
                // --- NUEVA OPERACIÓN: MKDIR (Crear Carpeta) ---
                else if ("MKDIR".equals(pcb.getOperationType())) {
                    if (pcb.getParentNode() != null) {
                        FileSystemNode nuevaCarpeta = new FileSystemNode(pcb.getTargetFileName(), true, pcb.getTargetOwner());
                        pcb.getParentNode().getChildren().add(nuevaCarpeta);
                        util.JournalManager.logOperation("MKDIR," + pcb.getTargetFileName() + "," + pcb.getTargetOwner());
                    }
                }
                // --- ACTUALIZACIÓN: DELETE RECURSIVO ---
                else if ("DELETE".equals(pcb.getOperationType())) {
                    if (discoFisico != null && pcb.getParentNode() != null) {
                        FileSystemNode parent = pcb.getParentNode();
                        
                        for (int i = 0; i < parent.getChildren().size(); i++) {
                            FileSystemNode objetivo = parent.getChildren().get(i);
                            if (objetivo.getName().equals(pcb.getTargetFileName())) {
                                
                                // Borramos todos los bloques de este nodo y sus hijos (si es carpeta)
                                liberarBloquesRecursivo(objetivo, discoFisico);
                                
                                // Lo quitamos del árbol
                                parent.getChildren().remove(objetivo); 
                                util.JournalManager.logOperation("DELETE," + pcb.getTargetFileName());
                                break;
                            }
                        }
                    }
                }// --- NUEVA OPERACIÓN: UPDATE (Renombrar) ---
                else if ("UPDATE".equals(pcb.getOperationType())) {
                    if (pcb.getParentNode() != null) {
                        FileSystemNode parent = pcb.getParentNode();
                        for (int i = 0; i < parent.getChildren().size(); i++) {
                            FileSystemNode objetivo = parent.getChildren().get(i);
                            if (objetivo.getName().equals(pcb.getTargetFileName())) {
                                try {
                                    objetivo.setName(pcb.getNewFileName());
                                    util.JournalManager.logOperation("UPDATE," + pcb.getTargetFileName() + "," + pcb.getNewFileName());
                                } catch (Exception ex) {
                                    // Si el nombre es inválido
                                    System.out.println(ex.getMessage());
                                }
                                break;
                            }
                        }
                    }
                }
                // --- MANEJO DE LECTURA DE PRUEBA (Para el JSON) ---
                else if ("READ".equals(pcb.getOperationType())) {
                    // Las operaciones de lectura solo hacen que el cabezal viaje y consuma tiempo (sleep).
                }

                // 3. Termina el proceso
                pcb.setState("terminado"); 
                
            } finally {
                executionMutex.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    

    // =========================================================================
    // GETTERS NECESARIOS PARA EL CONTROLADOR
    // =========================================================================
    public CustomLinkedList<ProcessControlBlock> getAllProcesses() {
        return allProcesses;
    }

    public DiskScheduler getScheduler() {
        return scheduler;
    }
    
    // --- MÉTODO: BORRADO RECURSIVO EN CASCADA ---
    private void liberarBloquesRecursivo(FileSystemNode nodo, SimulatedDisk disco) {
        if (!nodo.isDirectory()) {
            if (nodo.getStartBlockId() != -1) {
                disco.freeChain(nodo.getStartBlockId());
            }
        } else {
            // Si es carpeta, entramos a borrar a sus hijos
            if (nodo.getChildren() != null) {
                for (int i = 0; i < nodo.getChildren().size(); i++) {
                    liberarBloquesRecursivo(nodo.getChildren().get(i), disco);
                }
            }
        }
    }
}