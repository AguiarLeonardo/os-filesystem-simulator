/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import estructuras.CustomLinkedList;
import java.util.concurrent.Semaphore;

/** * Autor: Macorre21 */

/**
 * Subsistema: Tolerancia a Fallos (Journaling)
 * Descripción: Administra la bitácora de transacciones del sistema de archivos.
 * Permite registrar operaciones y revertirlas (rollback) en caso de interrupción
 * abrupta, garantizando la consistencia de los bloques en el disco simulado.
 */
public class JournalingManager {

    private final CustomLinkedList<JournalEntry> transactionLog;
    private final Semaphore logMutex; // Protege la lista de transacciones concurrentes

    /**
     * Inicializa el gestor de bitácora vacío y asegura su semáforo.
     */
    public JournalingManager() {
        this.transactionLog = new CustomLinkedList<>();
        this.logMutex = new Semaphore(1);
    }

    /**
     * Registra una nueva transacción de forma segura en entornos multihilo.
     * @param entry Objeto de transacción a encolar.
     * @throws InterruptedException Si se interrumpe la espera por el lock.
     */
    public void logTransaction(JournalEntry entry) throws InterruptedException {
        logMutex.acquire();
        try {
            transactionLog.add(entry);
        } finally {
            logMutex.release();
        }
    }

    /**
     * Marca una transacción como "CONFIRMADA", indicando que se escribió con éxito en disco.
     * @param transactionId El identificador de la transacción a confirmar.
     * @throws InterruptedException Si se interrumpe la espera por el lock.
     */
    public void commitTransaction(int transactionId) throws InterruptedException {
        logMutex.acquire();
        try {
            for (int i = 0; i < transactionLog.size(); i++) {
                JournalEntry entry = transactionLog.get(i);
                if (entry.getTransactionId() == transactionId) {
                    try {
                        entry.setState("CONFIRMADA");
                    } catch (Exception e) {
                        // Capturamos silenciosamente ya que el código interno está validado
                        System.err.println("Error al confirmar transacción: " + e.getMessage());
                    }
                    break;
                }
            }
        } finally {
            logMutex.release();
        }
    }

    /**
     * Ejecuta la rutina de recuperación. Recorre el log buscando operaciones no confirmadas
     * y las revierte para evitar fugas de memoria o sectores corruptos en el disco.
     * @param disk Instancia del disco simulado para liberar bloques sucios.
     * @param root Nodo raíz del sistema de archivos (preparación para limpieza de árbol).
     */
    public void rollbackPendingTransactions(SimulatedDisk disk, FileSystemNode root) {
        try {
            logMutex.acquire();
            try {
                for (int i = 0; i < transactionLog.size(); i++) {
                    JournalEntry entry = transactionLog.get(i);
                    
                    if (entry.getState().equals("PENDIENTE")) {
                        // Reversión de creación: Liberar los bloques en el disco
                        if (entry.getOperationType().equalsIgnoreCase("CREATE") && entry.getAllocatedStartBlock() != -1) {
                            disk.freeChain(entry.getAllocatedStartBlock());
                        }
                        
                        // NOTA DE ARQUITECTURA: La reversión de los nodos en el FileSystemNode (árbol) 
                        // dependerá de un método recursivo de búsqueda y eliminación en la clase raíz, 
                        // que se implementará en el controlador de la Interfaz.
                    }
                }
                // Tras el rollback total, el log se limpia para reiniciar el estado
                transactionLog.clear();
            } finally {
                logMutex.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Retorna la referencia a la bitácora actual para su visualización.
     * @return CustomLinkedList con las entradas de la bitácora.
     */
    public CustomLinkedList<JournalEntry> getLog() {
        return transactionLog;
    }
}

