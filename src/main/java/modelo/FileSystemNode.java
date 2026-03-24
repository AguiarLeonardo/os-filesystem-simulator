/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import estructuras.CustomLinkedList;

/** * Autor: Macorre21 */

/**
 * Subsistema: Modelo de Sistema de Archivos Jerárquico
 * Descripción: Representa un nodo dentro del árbol de directorios y archivos.
 * Soporta anidamiento mediante CustomLinkedList y mantiene los metadatos
 * fundamentales para la tabla de asignación (FAT) y el control de disco.
 */
public class FileSystemNode {

    private String owner; // "ADMIN" o "USER"
    private boolean isPublic; // true si todos pueden leerlo, false si es privado
    private String name;
    private final boolean isDirectory;
    private int sizeInBlocks;
    private int startBlockId;
    
    // Lista enlazada personalizada para mantener los subdirectorios/archivos hijos
    private final CustomLinkedList<FileSystemNode> children;

    /**
     * Construye un nuevo nodo para el sistema de archivos.
     * @param name Nombre del archivo o directorio.
     * @param isDirectory Flag que determina si es un contenedor (true) o un archivo hoja (false).
     * @param owner Nombre del usuario creador/propietario.
     */
    public FileSystemNode(String name, boolean isDirectory, String owner) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.owner = owner;
        this.isPublic = true; // Por defecto los archivos nacen siendo públicos
        this.sizeInBlocks = 0;
        this.startBlockId = -1; // -1 indica que no tiene bloques asignados aún
        
        if (this.isDirectory) {
            this.children = new CustomLinkedList<>();
        } else {
            this.children = null; // Un archivo no puede tener hijos
        }
    }

    /**
     * @return El nombre actual del nodo.
     */
    public String getName() {
        return name;
    }

    /**
     * Actualiza el nombre del nodo aplicando validaciones estrictas de sistema de archivos.
     * Lógica: Previene cadenas vacías y caracteres reservados a nivel de OS que podrían
     * corromper las rutas o la visualización en el JTree.
     * @param newName El nuevo nombre propuesto.
     * @throws Exception Si el nombre es nulo, vacío o contiene caracteres prohibidos.
     */
    public void setName(String newName) throws Exception {
        if (newName == null || newName.trim().isEmpty()) {
            throw new Exception("Operación rechazada: El nombre no puede ser nulo o estar vacío.");
        }
        
        // Validación de caracteres inválidos típicos en sistemas jerárquicos
        char[] invalidChars = {'/', '\\', ':', '*', '?', '"', '<', '>', '|'};
        for (int i = 0; i < newName.length(); i++) {
            char currentChar = newName.charAt(i);
            for (int j = 0; j < invalidChars.length; j++) {
                if (currentChar == invalidChars[j]) {
                    throw new Exception("Operación rechazada: El nombre contiene un carácter inválido ('" + currentChar + "').");
                }
            }
        }
        this.name = newName;
    }

    /**
     * @return true si el nodo actúa como directorio.
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * @return El tamaño del archivo representado en cantidad de bloques.
     */
    public int getSizeInBlocks() {
        return sizeInBlocks;
    }

    /**
     * @param size Establece la huella de memoria del archivo en bloques.
     */
    public void setSizeInBlocks(int size) {
        this.sizeInBlocks = size;
    }

    /**
     * @return El ID del primer bloque en el disco simulado (inicio de la cadena).
     */
    public int getStartBlockId() {
        return startBlockId;
    }

    /**
     * @param id Establece el puntero de inicio hacia el disco simulado.
     */
    public void setStartBlockId(int id) {
        this.startBlockId = id;
    }

    /**
     * Inserta un nuevo nodo hijo dentro de este directorio.
     * @param node El nodo a insertar.
     * @throws IllegalStateException Si se intenta agregar un hijo a un archivo.
     */
    public void addChild(FileSystemNode node) {
        if (!this.isDirectory) {
            throw new IllegalStateException("Violación de jerarquía: No se pueden agregar hijos a un archivo.");
        }
        this.children.add(node);
    }

    /**
     * @return La lista de nodos hijos.
     */
    public CustomLinkedList<FileSystemNode> getChildren() {
        return children;
    }

    // --- MÉTODOS DE SEGURIDAD (FASE 1) ---

    /**
     * @return El usuario propietario del archivo (ADMIN o USER).
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner El nuevo propietario del archivo.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return true si es de acceso público, false si es privado.
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * @param isPublic Establece si el archivo puede ser leído por otros usuarios.
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}