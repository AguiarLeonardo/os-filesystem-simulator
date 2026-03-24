/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 * Autor: Macorre21
 * Subsistema: Gestión de Procesos (Simulador de Sistema de Archivos Concurrente)
 * * Descripción: Representa el Bloque de Control de Proceso (PCB).
 * Mantiene el contexto de un proceso de usuario que solicita operaciones
 * de E/S sobre el sistema de archivos. Está diseñado para ser thread-safe,
 * asegurando la consistencia del estado en un entorno multihilo.
 */
public class ProcessControlBlock {

    private int processId;
    private String state;
    private String operationType;
    private String targetFileName;
    private int requiredBlocks;
    private int startBlockId; // Nuevo atributo necesario para los algoritmos
    /**
     * Construye un nuevo Bloque de Control de Proceso.
     * Todo proceso recién creado ingresa automáticamente en el estado "nuevo".
     *
     * @param id Identificador único del proceso.
     * @param operation Tipo de operación (ej. "CREATE", "READ").
     * @param target Nombre del archivo objetivo.
     * @param blocks Cantidad de bloques requeridos para la operación.
     */
    public ProcessControlBlock(int id, String operation, String target, int blocks) {
        this.processId = id;
        this.operationType = operation;
        this.targetFileName = target;
        this.requiredBlocks = blocks;
        this.state = "nuevo"; // Estado inicial por defecto en el modelo de transición
    }

    /**
     * Obtiene el estado actual del proceso de forma sincronizada.
     * * @return El estado actual del proceso ("nuevo", "listo", "ejecutando", "bloqueado", "terminado").
     * @return 
     */
    public synchronized String getState() {
        return state;
    }

    /**
     * Transiciona el proceso a un nuevo estado.
     * Se sincroniza para evitar condiciones de carrera si el planificador y
     * el manejador de E/S intentan alterar el estado simultáneamente.
     * * @param newState El nuevo estado al que transiciona el proceso.
     * @param newState
     * @throws IllegalArgumentException si el estado no pertenece a los 5 válidos.
     */
    public synchronized void setState(String newState) {
        if (isValidState(newState)) {
            this.state = newState;
        } else {
            throw new IllegalArgumentException("Transición de estado inválida. Estado no reconocido: " + newState);
        }
    }

    /**
     * Verifica lógicamente si el estado proporcionado pertenece al modelo
     * de 5 estados del sistema.
     * * @param stateToCheck El estado a evaluar.
     * @return true si es válido, false en caso contrario.
     */
    private boolean isValidState(String stateToCheck) {
        if (stateToCheck == null) return false;
        
        return switch (stateToCheck) {
            case "nuevo", "listo", "ejecutando", "bloqueado", "terminado" -> true;
            default -> false;
        };
    }

    // =========================================================================
    // Getters y Setters Adicionales (Sincronizados para consistencia en memoria)
    // =========================================================================

    public synchronized int getProcessId() {
        return processId;
    }

    public synchronized void setProcessId(int processId) {
        this.processId = processId;
    }

    public synchronized String getOperationType() {
        return operationType;
    }

    public synchronized void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public synchronized String getTargetFileName() {
        return targetFileName;
    }

    public synchronized void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    public synchronized int getRequiredBlocks() {
        return requiredBlocks;
    }

    public synchronized void setRequiredBlocks(int requiredBlocks) {
        this.requiredBlocks = requiredBlocks;
    }
    public synchronized int getStartBlockId() {
    return startBlockId;
    }

    public synchronized void setStartBlockId(int startBlockId) {
        this.startBlockId = startBlockId;
    }
}