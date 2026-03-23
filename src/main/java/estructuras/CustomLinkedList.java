/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

/**
 *
 * @author  Autor: AguiarLeonardo
 */
/**
 * * Estructura de datos dinámica personalizada que gestiona una secuencia de nodos.
 * Esta clase es la base para la asignación encadenada del sistema de archivos
 * y la gestión de procesos en el planificador.
 * * @param <T> El tipo de elementos contenidos en la lista.
 * @param <T>
 */
public class CustomLinkedList<T> {

    private Node<T> head;
    private Node<T> tail;
    private int size;

    /**
     * Inicializa una lista enlazada vacía.
     */
    public CustomLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Añade un elemento al final de la lista (Operación O(1)).
     * @param item El elemento a añadir.
     */
    public void add(T item) {
        Node<T> newNode = new Node<>(item);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            tail.setNext(newNode);
            newNode.setPrev(tail);
            tail = newNode;
        }
        size++;
    }

    /**
     * Añade un elemento al inicio de la lista (Operación O(1)).
     * Útil para estructuras tipo pila o prioridad alta.
     * @param item El elemento a añadir.
     */
    public void addFirst(T item) {
        Node<T> newNode = new Node<>(item);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            newNode.setNext(head);
            head.setPrev(newNode);
            head = newNode;
        }
        size++;
    }

    /**
     * Elimina y retorna el primer elemento de la lista.
     * @return El dato del nodo eliminado o null si la lista está vacía.
     */
    public T removeFirst() {
        if (isEmpty()) return null;
        
        T data = head.getData();
        head = head.getNext();
        
        if (head != null) {
            head.setPrev(null);
        } else {
            tail = null; // La lista quedó vacía
        }
        
        size--;
        return data;
    }

    /**
     * Busca y elimina la primera ocurrencia del objeto especificado.
     * @param item El objeto a buscar para eliminar.
     * @return true si se encontró y eliminó, false en caso contrario.
     */
    public boolean remove(T item) {
        Node<T> current = head;
        while (current != null) {
            if (current.getData().equals(item)) {
                Node<T> prev = current.getPrev();
                Node<T> next = current.getNext();

                if (prev != null) prev.setNext(next);
                else head = next;

                if (next != null) next.setPrev(prev);
                else tail = prev;

                size--;
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    /**
     * Obtiene el elemento en una posición específica (Operación O(n)).
     * @param index Índice base cero.
     * @return El dato en la posición o lanza IndexOutOfBoundsException.
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        return current.getData();
    }

    /**
     * Retorna la cantidad de elementos en la lista.
     * @return Entero con el tamaño.
     */
    public int size() {
        return size;
    }

    /**
     * Verifica si la lista carece de elementos.
     * @return true si el tamaño es 0.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Limpia la lista eliminando todas las referencias para ayudar al GC.
     */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
}