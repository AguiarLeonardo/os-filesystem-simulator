/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

import java.util.concurrent.Semaphore;

/**
 * Autor: Macorre21
 
 * Subsistema: Gestión de Concurrencia y Estructuras de Datos
 * * Descripción: Implementación de una cola genérica y concurrente (Thread-Safe).
 * Diseñada específicamente para gestionar la cola de procesos "Listos" y 
 * las solicitudes de E/S en el simulador. Se apoya en CustomLinkedList para 
 * la gestión de memoria (nodos) y utiliza Semáforos para el control estricto 
 * de la concurrencia en la lectura y escritura.
 * * @param <T> Tipo genérico de los elementos almacenados en la cola.
 * @param <T>
 */
public class ThreadSafeQueue<T> {

    private final CustomLinkedList<T> queue;
    
    // Semáforo binario para garantizar exclusión mutua sobre la lista enlazada
    private final Semaphore mutex;
    
    // Semáforo contador para suspender hilos si la cola está vacía
    private final Semaphore items;

    /**
     * Construye una nueva cola concurrente vacía.
     */
    public ThreadSafeQueue() {
        this.queue = new CustomLinkedList<>();
        this.mutex = new Semaphore(1);
        this.items = new Semaphore(0);
    }

    /**
     * Inserta un elemento en la parte trasera de la cola de forma segura.
     * * @param item El elemento a encolar.
     * @param item
     * @throws InterruptedException si el hilo es interrumpido mientras espera el mutex.
     */
    public void enqueue(T item) throws InterruptedException {
        mutex.acquire();   // Sección crítica: bloquear acceso a la estructura
        try {
            queue.add(item); // Ajustado al método add() de CustomLinkedList
        } finally {
            mutex.release();   // Salir de la sección crítica asegurando la liberación
        }
        items.release();       // Señalizar a posibles consumidores bloqueados
    }

    /**
     * Extrae y retorna el elemento en el frente de la cola.
     * * @return El elemento extraído del frente de la cola.
     * @return 
     * @throws InterruptedException si el hilo es interrumpido mientras espera.
     */
    public T dequeue() throws InterruptedException {
        items.acquire();       // Bloquear si la cola está vacía, restar 1 ítem si hay disponibles
        mutex.acquire();       // Sección crítica: bloquear acceso a la estructura
        try {
            return queue.removeFirst(); // Acoplado perfectamente a CustomLinkedList
        } finally {
            mutex.release();   // Liberar el acceso a la estructura
        }
    }

    /**
     * Observa el elemento en el frente de la cola sin extraerlo.
     * * @return El elemento en el frente, o null si la estructura está vacía.
     * @return 
     */
    public T peek() {
        try {
            mutex.acquire();
            try {
                if (queue.isEmpty()) {
                    return null;
                }
                return queue.get(0); // Ajustado para usar get(0) de CustomLinkedList
            } finally {
                mutex.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar la bandera de interrupción
            return null;
        }
    }

    /**
     * Verifica si la cola está vacía de forma atómica.
     * * @return true si no contiene elementos, false de lo contrario.
     * @return 
     */
    public boolean isEmpty() {
        try {
            mutex.acquire();
            try {
                return queue.isEmpty();
            } finally {
                mutex.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
    }

    /**
     * Obtiene la cantidad de elementos en la cola asegurando la lectura correcta.
     * * @return El tamaño actual de la cola.
     * @return 
     */
    public int size() {
        try {
            mutex.acquire();
            try {
                return queue.size();
            } finally {
                mutex.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }
}