/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import estructuras.CustomLinkedList;

/** * Autor: AguiarLeonardo 
 */

/**
 * Subsistema: Planificación de Almacenamiento Secundario
 * Descripción: Representa la aguja/cabezal del disco simulado. Conserva 
 * la dirección física actual y aplica el patrón de enrutamiento (Switch) 
 * para delegar la selección del próximo bloque a la clase algorítmica.
 */
public class DiskScheduler {

    private int currentHeadPosition;
    private String currentDirection; // "UP" o "DOWN"
    private String activePolicy;

    /**
     * Inicializa el cabezal del disco en un cilindro específico.
     * @param initialHead Posición de inicio del cabezal.
     */
    public DiskScheduler(int initialHead) {
        this.currentHeadPosition = initialHead;
        this.currentDirection = "UP"; // Dirección predeterminada ascendente
        this.activePolicy = "FIFO";   // Política por defecto
    }

    public int getCurrentHeadPosition() {
        return currentHeadPosition;
    }

    public void setCurrentHeadPosition(int position) {
        this.currentHeadPosition = position;
    }

    public String getCurrentDirection() {
        return currentDirection;
    }

    /**
     * Establece la política de planificación con validación estricta de entrada.
     * @param policy Cadena que representa el algoritmo.
     * @throws Exception Si la política solicitada no existe en el sistema.
     */
    public void setPolicy(String policy) throws Exception {
        if (policy == null) {
            throw new Exception("Error: La política de planificación no puede ser nula.");
        }
        
        String formattedPolicy = policy.trim().toUpperCase();
        switch (formattedPolicy) {
            case "FIFO":
            case "SSTF":
            case "SCAN":
            case "C-SCAN":
                this.activePolicy = formattedPolicy;
                break;
            default:
                throw new Exception("Violación de simulación: Algoritmo de planificación no reconocido ('" + policy + "'). Use FIFO, SSTF, SCAN o C-SCAN.");
        }
    }

    /**
     * Enrutador principal. Intercepta la lista de peticiones y decide 
     * qué algoritmo matemático delegar según la política activa.
     * @param pendingRequests Lista enlazada de PCB pendientes.
     * @return El proceso seleccionado para ejecución.
     */
    public ProcessControlBlock getNextRequest(CustomLinkedList<ProcessControlBlock> pendingRequests) {
        if (pendingRequests.isEmpty()) {
            return null;
        }

        return switch (activePolicy) {
            case "FIFO" -> SchedulerAlgorithms.applyFIFO(pendingRequests);
            case "SSTF" -> SchedulerAlgorithms.applySSTF(pendingRequests, currentHeadPosition);
            case "SCAN" -> SchedulerAlgorithms.applySCAN(pendingRequests, currentHeadPosition, currentDirection);
            case "C-SCAN" -> SchedulerAlgorithms.applyCSCAN(pendingRequests, currentHeadPosition, currentDirection);
            default -> null;
        };
    }
}