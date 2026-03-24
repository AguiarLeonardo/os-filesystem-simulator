/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import estructuras.CustomLinkedList;
import estructuras.ThreadSafeQueue;
import java.util.concurrent.Semaphore;

/** * Autor: AguiarLeonardo 
 */

/**
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

    /**
     * Construye el orquestador de procesos.
     * @param scheduler Referencia al planificador de disco que gestionará las políticas.
     */
    public ProcessManager(DiskScheduler scheduler) {
        this.ioQueue = new ThreadSafeQueue<>();
        this.allProcesses = new CustomLinkedList<>();
        this.scheduler = scheduler;
        this.isRunning = false;
        // Semáforo binario nativo (Mutex) para blindar la ejecución y transición de estados
        this.executionMutex = new Semaphore(1);
    }

    /**
     * Encola un nuevo proceso generado por el usuario/sistema de forma segura.
     * @param pcb Bloque de Control de Proceso a encolar.
     * @throws InterruptedException Si el hilo es interrumpido durante la espera.
     */
    public void addProcess(ProcessControlBlock pcb) throws InterruptedException {
        pcb.setState("listo");
        allProcesses.add(pcb);
        ioQueue.enqueue(pcb);
    }

    /**
     * Dispara el hilo en segundo plano que consume continuamente la cola de procesos.
     * Se instancia un Thread nativo de Java según las restricciones.
     */
    public void startProcessing() {
        if (isRunning) return;
        isRunning = true;

        Thread executionThread = new Thread(() -> {
            while (isRunning) {
                try {
                    // Extrae el siguiente proceso asegurando concurrencia
                    ProcessControlBlock pcb = ioQueue.dequeue();
                    if (pcb != null) {
                        executeProcess(pcb);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    isRunning = false;
                }
            }
        });
        
        executionThread.setName("OS-ProcessManager-Thread");
        executionThread.start();
    }

    /**
     * Detiene la bandera de ejecución del motor de procesos de forma segura.
     */
    public void stopProcessing() {
        this.isRunning = false;
    }

    /**
     * Ejecuta atómicamente la transición de estado de un proceso utilizando locks nativos.
     * @param pcb Proceso a ejecutar.
     */
    private void executeProcess(ProcessControlBlock pcb) {
        try {
            executionMutex.acquire(); // Lock: Entrando a zona crítica de estado
            try {
                // Transición: Listo -> Ejecutando
                pcb.setState("ejecutando");
                
                // Simulación de ráfaga de E/S (Operación contra el disco simulado)
                Thread.sleep(500); // Pausa estratégica para permitir visualización en la GUI
                
                // Transición: Ejecutando -> Terminado
                pcb.setState("terminado");
                
            } finally {
                executionMutex.release(); // Unlock: Liberación incondicional del recurso
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}