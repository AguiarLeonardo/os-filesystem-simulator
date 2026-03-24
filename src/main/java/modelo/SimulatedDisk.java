/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/** * Autor: AguiarLeonardo 
 * Subsistema: Simulación de Almacenamiento Secundario
 * Descripción: Administra el espacio de un disco simulado en memoria principal.
 * Implementa un esquema de asignación encadenada.
 */
public class SimulatedDisk {

    // Arreglo primitivo para el almacenamiento continuo en memoria
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
     */
    public synchronized int allocateChain(String fileName, int requiredBlocks) throws Exception {
        if (requiredBlocks > availableBlocks) {
            throw new Exception("Fallo de asignación: Espacio insuficiente en disco para el archivo '" + fileName + "'. Requerido: " + requiredBlocks + ", Disponible: " + availableBlocks);
        }

        int startBlock = -1;
        int previousBlock = -1;
        int blocksAllocated = 0;

        for (int i = 0; i < totalCapacity && blocksAllocated < requiredBlocks; i++) {
            FileBlock current = storage[i];
            
            if (!current.isOccupied()) {
                current.setOccupied(true);
                current.setOwnerFileName(fileName);
                
                if (startBlock == -1) {
                    startBlock = current.getBlockId();
                }
                
                if (previousBlock != -1) {
                    storage[previousBlock].setNextBlockId(current.getBlockId());
                }
                
                previousBlock = current.getBlockId();
                blocksAllocated++;
                availableBlocks--;
            }
        }
        
        if (previousBlock != -1) {
            storage[previousBlock].setNextBlockId(-1);
        }

        return startBlock;
    }

    /**
     * Libera una cadena completa de bloques basándose en su bloque inicial.
     */
    public synchronized void freeChain(int startBlockId) {
        int currentId = startBlockId;
        
        while (currentId != -1 && currentId < totalCapacity) {
            FileBlock block = storage[currentId];
            int nextId = block.getNextBlockId(); 
            
            if (block.isOccupied()) {
                block.clearBlock();
                availableBlocks++;
            }
            
            currentId = nextId; 
        }
    }

    /**
     * Retorna una referencia de lectura directa a un bloque específico.
     */
    public FileBlock getBlock(int index) {
        if (index < 0 || index >= totalCapacity) {
            throw new IndexOutOfBoundsException("Intento de acceso a sector inválido del disco: " + index);
        }
        return storage[index];
    }
    
    /**
     * NUEVO: Formatea el disco completo. Usado en la Recuperación del Sistema.
     */
    public synchronized void formatDisk() {
        for (int i = 0; i < totalCapacity; i++) {
            if (storage[i].isOccupied()) {
                storage[i].clearBlock();
            }
        }
        this.availableBlocks = totalCapacity; // Reseteamos la capacidad
    }

    /**
     * Genera un arreglo de enteros que representa la tabla de asignación (FAT).
     * Esto es lo que usa la Vista para llenar la tabla del disco.
     * @return Arreglo donde cada índice contiene el ID del siguiente bloque (-1 si es fin, 0 si está libre).
     */
    public int[] getFatTable() {
        int[] fatTable = new int[totalCapacity];
        for (int i = 0; i < totalCapacity; i++) {
            if (storage[i].isOccupied()) {
                fatTable[i] = storage[i].getNextBlockId();
            } else {
                fatTable[i] = 0; // Representa bloque libre en la interfaz
            }
        }
        return fatTable;
    }
    
    public void allocateSpecificBlocks(int start, int quantity, String fileName) {
        for (int i = 0; i < quantity; i++) {
            int currentBlock = start + i;
            
            // Verificamos que no nos salgamos del tamaño del disco
            if (currentBlock < storage.length) {
                // 1. Marcamos como ocupado (usando tu método setOccupied)
                storage[currentBlock].setOccupied(true);
                
                // 2. Asignamos el nombre (usando tu método setOwnerFileName)
                storage[currentBlock].setOwnerFileName(fileName);
                
                // 3. Creamos la cadena de bloques (Siguiente bloque)
                if (i < quantity - 1) {
                    storage[currentBlock].setNextBlockId(currentBlock + 1);
                } else {
                    storage[currentBlock].setNextBlockId(-1); // Fin de archivo (EOF)
                }
            }
        }
    }
}
