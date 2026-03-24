/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.concurrent.Semaphore;

/** * Autor: AguiarLeonardo */

/**
 * Subsistema: Gestión de Concurrencia (I/O)
 * Descripción: Implementa el clásico problema de los Lectores-Escritores.
 * Permite múltiples lecturas simultáneas (compartiendo el recurso) pero
 * garantiza exclusión mutua estricta cuando un proceso requiere escribir,
 * previniendo deadlocks y condiciones de carrera en el sistema de archivos.
 */
public class ConcurrencyManager {

    private final Semaphore mutex;     // Protege la sección crítica de la variable readCount
    private final Semaphore writeLock; // Garantiza exclusión mutua para los escritores
    private int readCount;

    /**
     * Inicializa los semáforos y el contador de lectores.
     */
    public ConcurrencyManager() {
        this.mutex = new Semaphore(1);
        this.writeLock = new Semaphore(1);
        this.readCount = 0;
    }

    /**
     * Adquiere el bloqueo compartido para operaciones de lectura.
     * Lógica: El primer lector que llega bloquea el acceso a los escritores.
     * Los lectores subsiguientes entran directamente.
     * @throws InterruptedException Si el hilo es interrumpido en la espera.
     */
    public void acquireReadLock() throws InterruptedException {
        mutex.acquire();
        readCount++;
        if (readCount == 1) {
            writeLock.acquire(); // El primer lector bloquea a los escritores
        }
        mutex.release();
    }

    /**
     * Libera el bloqueo compartido tras una operación de lectura.
     * Lógica: El último lector en salir es el responsable de despertar/desbloquear
     * a los escritores que puedan estar esperando.
     * @throws InterruptedException Si el hilo es interrumpido.
     */
    public void releaseReadLock() throws InterruptedException {
        mutex.acquire();
        readCount--;
        if (readCount == 0) {
            writeLock.release(); // El último lector libera a los escritores
        }
        mutex.release();
    }

    /**
     * Adquiere el bloqueo exclusivo para operaciones de escritura/modificación.
     * Solo un hilo puede pasar esta barrera, y solo si no hay lectores activos.
     * @throws InterruptedException Si el hilo es interrumpido.
     */
    public void acquireWriteLock() throws InterruptedException {
        writeLock.acquire();
    }

    /**
     * Libera el bloqueo exclusivo tras finalizar la escritura.
     */
    public void releaseWriteLock() {
        writeLock.release();
    }
}