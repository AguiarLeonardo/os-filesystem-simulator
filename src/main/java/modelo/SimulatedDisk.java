/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/** * Autor: AguiarLeonardo 
 */
/**
 * Subsistema: Simulación de Almacenamiento Secundario
 * Descripción: Administra el espacio de un disco simulado en memoria principal.
 * Implementa un esquema de asignación encadenada, buscando bloques libres
 * de forma secuencial y enlazándolos lógicamente. Los métodos críticos están
 * sincronizados para garantizar thread-safety durante las peticiones concurrentes de E/S.
 */
public class SimulatedDisk {

    // Arreglo primitivo para el almacenamiento continuo en memoria, O(1) en acceso físico.
    private final FileBlock[] storage; 
    private int totalCapacity;
    private int availableBlocks;

    /**
     * Formatea e inicializa el disco simulado con una capacidad definida.
     * @param capacity Cantidad total de bloques disponibles en el disco.
     */
    public SimulatedDisk(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacidad del disco debe ser mayor a 0.");
        }
        this.totalCapacity = capacity;
        this.availableBlocks = capacity;
        this.storage = new FileBlock[capacity];

        // Creación física de los sectores del disco
        for (int i = 0; i < capacity; i++) {
            this.storage[i] = new FileBlock(i);
        }
    }

    /**
     * Obtiene la cantidad de bloques libres en el disco.
     * @return Número de bloques no asignados.
     */
    public synchronized int getAvailableBlocks() {
        return availableBlocks;
    }

    /**
     * Asigna una cadena de bloques para un archivo nuevo.
     * Lógica: Escanea el arreglo secuencialmente buscando bloques libres.
     * Cuando encuentra uno, lo marca como ocupado y lo enlaza con el bloque anterior.
     * * @param fileName Nombre del archivo a crear.
     * @param requiredBlocks Cantidad de bloques que requiere el archivo.
     * @return El índice del bloque inicial (head) de la cadena.
     * @throws Exception Si el disco no tiene fragmentos libres suficientes.
     */
    public synchronized int allocateChain(String fileName, int requiredBlocks) throws Exception {
        if (requiredBlocks > availableBlocks) {
            throw new Exception("Fallo de asignación: Espacio insuficiente en disco para el archivo '" + fileName + "'. Requerido: " + requiredBlocks + ", Disponible: " + availableBlocks);
        }

        int startBlock = -1;
        int previousBlock = -1;
        int blocksAllocated = 0;

        // Escaneo lineal para asignación (Simulación de búsqueda de sectores)
        for (int i = 0; i < totalCapacity && blocksAllocated < requiredBlocks; i++) {
            FileBlock current = storage[i];
            
            if (!current.isOccupied()) {
                current.setOccupied(true);
                current.setOwnerFileName(fileName);
                
                // Si es el primer bloque encontrado, guardamos el puntero inicial
                if (startBlock == -1) {
                    startBlock = current.getBlockId();
                }
                
                // Enlazar lógicamente el bloque anterior con este nuevo bloque
                if (previousBlock != -1) {
                    storage[previousBlock].setNextBlockId(current.getBlockId());
                }
                
                previousBlock = current.getBlockId();
                blocksAllocated++;
                availableBlocks--;
            }
        }
        
        // Sellar la cadena en el último bloque asignado (EOF)
        if (previousBlock != -1) {
            storage[previousBlock].setNextBlockId(-1);
        }

        return startBlock;
    }

    /**
     * Libera una cadena completa de bloques basándose en su bloque inicial.
     * Lógica: Recorre los punteros lógicos (nextBlockId) limpiando recursivamente
     * la memoria hasta encontrar el marcador de EOF (-1).
     * * @param startBlockId El identificador del primer bloque del archivo.
     */
    public synchronized void freeChain(int startBlockId) {
        int currentId = startBlockId;
        
        while (currentId != -1 && currentId < totalCapacity) {
            FileBlock block = storage[currentId];
            int nextId = block.getNextBlockId(); // Guardar el puntero antes de limpiar
            
            if (block.isOccupied()) {
                block.clearBlock();
                availableBlocks++;
            }
            
            currentId = nextId; // Mover el cabezal de lectura al siguiente bloque
        }
    }

    /**
     * Retorna una referencia de lectura directa a un bloque específico.
     * @param index Índice físico del bloque en el arreglo.
     * @return Referencia al objeto FileBlock.
     * @throws IndexOutOfBoundsException Si el índice no existe físicamente.
     */
    public FileBlock getBlock(int index) {
        if (index < 0 || index >= totalCapacity) {
            throw new IndexOutOfBoundsException("Intento de acceso a sector inválido del disco: " + index);
        }
        return storage[index];
    }
}