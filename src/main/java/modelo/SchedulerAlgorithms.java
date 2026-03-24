/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import estructuras.CustomLinkedList;

/** * Autor: AguiarLeonardo 
 * Subsistema: Matemáticas de Planificación
 * Descripción: Clase de utilidad pura (Utility Class) que aloja la lógica 
 * estática de los algoritmos de disco. Implementa búsqueda heurística e 
 * iteración manual en cumplimiento de la restricción de cero arreglos/sort() nativos.
 */
public class SchedulerAlgorithms {

    /**
     * First-In, First-Out: Atiende por orden de llegada.
     * @param list Cola de procesos.
     * @return El proceso más antiguo en la lista.
     */
    public static ProcessControlBlock applyFIFO(CustomLinkedList<ProcessControlBlock> list) {
        if (list.isEmpty()) return null;
        return list.removeFirst(); // Operación natural O(1) de nuestra estructura
    }

    /**
     * Shortest Seek Time First: Busca el cilindro más cercano al cabezal actual.
     * @param list Lista de procesos pendientes.
     * @param currentHead Posición física actual del cabezal.
     * @return El proceso cuya distancia de búsqueda es mínima.
     */
    public static ProcessControlBlock applySSTF(CustomLinkedList<ProcessControlBlock> list, int currentHead) {
        if (list.isEmpty()) return null;
        
        ProcessControlBlock best = null;
        int minDiff = Integer.MAX_VALUE;

        // Iteración lineal (O(N)) manual requerida por reglas de negocio
        for (int i = 0; i < list.size(); i++) {
            ProcessControlBlock pcb = list.get(i);
            int block = pcb.getStartBlockId(); 
            
            // --- NUEVA REGLA: Si es una creación (-1), se atiende de inmediato ---
            if (block == -1) {
                list.remove(pcb);
                return pcb;
            }
            
            int diff = Math.abs(block - currentHead);
            
            if (diff < minDiff) {
                minDiff = diff;
                best = pcb;
            }
        }
        
        if (best != null) list.remove(best);
        return best;
    }

    /**
     * Algoritmo del Elevador (SCAN): Barre en una dirección hasta el final 
     * y luego retrocede atendiendo las peticiones en el camino inverso.
     * @param list Lista de procesos pendientes.
     * @param currentHead Posición actual.
     * @param direction "UP" o "DOWN".
     * @return El siguiente proceso en el barrido.
     */
    public static ProcessControlBlock applySCAN(CustomLinkedList<ProcessControlBlock> list, int currentHead, String direction) {
        if (list.isEmpty()) return null;
        
        ProcessControlBlock best = null;
        int minDiff = Integer.MAX_VALUE;

        // 1. Buscar en la dirección actual del barrido
        for (int i = 0; i < list.size(); i++) {
            ProcessControlBlock pcb = list.get(i);
            int block = pcb.getStartBlockId();
            
            // --- NUEVA REGLA ---
            if (block == -1) {
                list.remove(pcb);
                return pcb;
            }
            
            if (direction.equals("UP") && block >= currentHead) {
                int diff = block - currentHead;
                if (diff < minDiff) { minDiff = diff; best = pcb; }
            } else if (direction.equals("DOWN") && block <= currentHead) {
                int diff = currentHead - block;
                if (diff < minDiff) { minDiff = diff; best = pcb; }
            }
        }

        // 2. Si no hay más peticiones en la dirección actual, el cabezal rebota
        if (best == null) {
            for (int i = 0; i < list.size(); i++) {
                ProcessControlBlock pcb = list.get(i);
                int block = pcb.getStartBlockId();
                
                if (block == -1) { list.remove(pcb); return pcb; } // Por seguridad
                
                // Si iba "UP", rebotó en el techo y ahora va "DOWN" -> tomamos el cilindro más alto posible
                if (direction.equals("UP")) {
                    if (best == null || block > best.getStartBlockId()) { best = pcb; }
                } 
                // Si iba "DOWN", rebotó en cero y ahora va "UP" -> tomamos el cilindro más bajo posible
                else {
                    if (best == null || block < best.getStartBlockId()) { best = pcb; }
                }
            }
        }

        if (best != null) list.remove(best);
        return best;
    }

    /**
     * Circular SCAN (C-SCAN): Barre en una dirección, pero al terminar, 
     * salta al extremo opuesto sin atender peticiones en el retroceso.
     * @param list Lista de procesos pendientes.
     * @param currentHead Posición actual.
     * @param direction "UP" o "DOWN".
     * @return El siguiente proceso en el barrido circular.
     */
    public static ProcessControlBlock applyCSCAN(CustomLinkedList<ProcessControlBlock> list, int currentHead, String direction) {
        if (list.isEmpty()) return null;
        
        ProcessControlBlock best = null;
        int minDiff = Integer.MAX_VALUE;

        // 1. Buscar en la dirección actual del barrido
        for (int i = 0; i < list.size(); i++) {
            ProcessControlBlock pcb = list.get(i);
            int block = pcb.getStartBlockId();
            
            // --- NUEVA REGLA ---
            if (block == -1) {
                list.remove(pcb);
                return pcb;
            }
            
            if (direction.equals("UP") && block >= currentHead) {
                int diff = block - currentHead;
                if (diff < minDiff) { minDiff = diff; best = pcb; }
            } else if (direction.equals("DOWN") && block <= currentHead) {
                int diff = currentHead - block;
                if (diff < minDiff) { minDiff = diff; best = pcb; }
            }
        }

        // 2. Si se acabó el camino, aplicar salto circular al extremo opuesto
        if (best == null) {
            for (int i = 0; i < list.size(); i++) {
                ProcessControlBlock pcb = list.get(i);
                int block = pcb.getStartBlockId();
                
                if (block == -1) { list.remove(pcb); return pcb; } // Por seguridad
                
                // Iba "UP" -> Salta a 0 y toma el cilindro más bajo
                if (direction.equals("UP")) {
                    if (best == null || block < best.getStartBlockId()) { best = pcb; }
                } 
                // Iba "DOWN" -> Salta al máximo y toma el cilindro más alto
                else {
                    if (best == null || block > best.getStartBlockId()) { best = pcb; }
                }
            }
        }

        if (best != null) list.remove(best);
        return best;
    }
}