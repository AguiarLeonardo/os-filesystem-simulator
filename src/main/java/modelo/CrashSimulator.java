/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/** * Autor: Macorre21*/

/**
 * Subsistema: Pruebas y Simulaciones de Fallos
 * Descripción: Permite interrumpir la ejecución normal del sistema de archivos,
 * simulando un apagón o kernel panic. Orquesta la recuperación a través del 
 * JournalingManager.
 */
public class CrashSimulator {

    private JournalingManager journal;
    private boolean systemCrashed;

    /**
     * Inicializa el simulador de fallos enlazándolo a la bitácora.
     * @param journal Instancia activa del gestor de journaling.
     */
    public CrashSimulator(JournalingManager journal) {
        this.journal = journal;
        this.systemCrashed = false;
    }

    /**
     * Dispara un fallo crítico en el sistema.
     * Los planificadores y gestores deberán leer esta bandera para detener sus rutinas.
     */
    public void triggerCrash() {
        this.systemCrashed = true;
        System.out.println("CRITICAL FAULT: El sistema ha colapsado de forma abrupta.");
    }

    /**
     * Restablece el sistema invocando la rutina de reversión de transacciones pendientes.
     * @param disk El disco donde se liberarán los bloques corruptos.
     * @param root La raíz del árbol de directorios.
     */
    public void recoverSystem(SimulatedDisk disk, FileSystemNode root) {
        if (!systemCrashed) {
            return; // No hay nada que recuperar
        }
        
        System.out.println("Iniciando rutina de recuperación mediante Journaling...");
        journal.rollbackPendingTransactions(disk, root);
        
        // Levantar el sistema de nuevo
        this.systemCrashed = false;
        System.out.println("Recuperación exitosa. Sistema operativo en línea.");
    }

    /**
     * Consulta el estado de salud del sistema operativo.
     * @return true si está caído, false si opera normalmente.
     */
    public boolean isSystemCrashed() {
        return systemCrashed;
    }
}
