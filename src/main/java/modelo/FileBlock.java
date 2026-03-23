/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/** * Autor: AguiarLeonardo 
 */
/**
 * Subsistema: Simulación de Almacenamiento Secundario
 * Descripción: Representa una unidad atómica de almacenamiento en el disco simulado.
 * Actúa como un nodo en la asignación encadenada de archivos.
 */
public class FileBlock {

    private int blockId;
    private boolean isOccupied;
    private String ownerFileName;
    private int nextBlockId; // Puntero lógico al siguiente bloque. -1 indica EOF (End Of File).

    /**
     * Construye un bloque de disco vacío y listo para ser asignado.
     * @param id Identificador físico del bloque en el arreglo del disco.
     */
    public FileBlock(int id) {
        this.blockId = id;
        this.isOccupied = false;
        this.ownerFileName = null;
        this.nextBlockId = -1;
    }

    /**
     * Retorna el identificador del bloque.
     * @return El ID numérico del bloque.
     */
    public int getBlockId() {
        return blockId;
    }

    /**
     * Verifica si el bloque contiene datos válidos de un archivo.
     * @return true si está en uso, false si está libre.
     */
    public boolean isOccupied() {
        return isOccupied;
    }

    /**
     * Cambia el estado de ocupación del bloque con validación estricta.
     * Lógica: Previene la corrupción de datos simulada al lanzar una excepción
     * si se intenta marcar como ocupado un bloque que ya lo está.
     * @param status Nuevo estado de ocupación.
     */
    public void setOccupied(boolean status) {
        if (status && this.isOccupied) {
            throw new IllegalStateException("Violación de integridad: Intento de sobreescritura accidental en el bloque " + blockId);
        }
        this.isOccupied = status;
    }

    /**
     * Obtiene el nombre del archivo dueño de este bloque.
     * @return Nombre del archivo o null si está libre.
     */
    public String getOwnerFileName() {
        return ownerFileName;
    }

    /**
     * Asigna un archivo como dueño del bloque.
     * @param name Nombre del archivo propietario.
     */
    public void setOwnerFileName(String name) {
        this.ownerFileName = name;
    }

    /**
     * Obtiene el puntero lógico al siguiente bloque del archivo.
     * @return ID del siguiente bloque, o -1 si es el último bloque.
     */
    public int getNextBlockId() {
        return nextBlockId;
    }

    /**
     * Establece el puntero lógico al siguiente bloque en la cadena.
     * @param nextId ID del bloque sucesor.
     */
    public void setNextBlockId(int nextId) {
        this.nextBlockId = nextId;
    }

    /**
     * Libera el bloque por completo, preparándolo para una nueva asignación.
     * Restablece el puntero de cadena a -1 (estado huérfano).
     */
    public void clearBlock() {
        this.isOccupied = false;
        this.ownerFileName = null;
        this.nextBlockId = -1;
    }
}