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
 * * Clase que representa un nodo genérico para estructuras de datos enlazadas.
 * Proporciona soporte para punteros hacia adelante y hacia atrás, optimizando
 * operaciones en listas y colas personalizadas.
 * * @param <T> El tipo de dato que almacenará el nodo.
 * @param <T>
 */
public class Node<T> {

    private T data;
    private Node<T> next;
    private Node<T> prev;

    /**
     * Constructor principal del nodo.
     * @param data El objeto o dato de tipo T a almacenar.
     */
    public Node(T data) {
        this.data = data;
        this.next = null;
        this.prev = null;
    }

    /**
     * Obtiene el dato almacenado en el nodo.
     * @return El dato de tipo T.
     */
    public T getData() {
        return data;
    }

    /**
     * Actualiza el dato almacenado en el nodo.
     * @param data Nuevo dato a establecer.
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Obtiene la referencia al siguiente nodo.
     * @return Referencia de tipo Node o null si es el final.
     */
    public Node<T> getNext() {
        return next;
    }

    /**
     * Establece la referencia al siguiente nodo.
     * @param next Nodo sucesor.
     */
    public void setNext(Node<T> next) {
        this.next = next;
    }

    /**
     * Obtiene la referencia al nodo anterior.
     * @return Referencia de tipo Node o null si es el inicio.
     */
    public Node<T> getPrev() {
        return prev;
    }

    /**
     * Establece la referencia al nodo anterior.
     * @param prev Nodo antecesor.
     */
    public void setPrev(Node<T> prev) {
        this.prev = prev;
    }
}
