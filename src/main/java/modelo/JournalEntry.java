/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/** * Autor: AguiarLeonardo */

/**
 * Subsistema: Tolerancia a Fallos (Journaling)
 * Descripción: Representa una operación atómica (transacción) en el sistema.
 * Mantiene el metadato necesario para auditar o revertir (rollback) cambios
 * en caso de que el sistema colapse (crash) antes de persistirlos definitivamente.
 */
public class JournalEntry {

    private int transactionId;
    private String operationType;
    private String targetPath;
    private String state;
    private int allocatedStartBlock;

    /**
     * Crea un nuevo registro de transacción asumiendo un estado inicial "PENDIENTE".
     * @param id ID numérico único de la transacción.
     * @param opType Tipo de operación ("CREATE", "DELETE", "UPDATE").
     * @param path Ruta del archivo o directorio afectado.
     * @param startBlock Bloque inicial en disco (usado para rollbacks de creación).
     */
    public JournalEntry(int id, String opType, String path, int startBlock) {
        this.transactionId = id;
        this.operationType = opType;
        this.targetPath = path;
        this.state = "PENDIENTE";
        this.allocatedStartBlock = startBlock;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public String getState() {
        return state;
    }

    /**
     * Actualiza el estado de la transacción con validación estricta de integridad.
     * @param state El nuevo estado ("PENDIENTE" o "CONFIRMADA").
     * @throws Exception Si el estado proporcionado corrompe el dominio de valores permitidos.
     */
    public void setState(String state) throws Exception {
        if (state == null || (!state.equalsIgnoreCase("PENDIENTE") && !state.equalsIgnoreCase("CONFIRMADA"))) {
            throw new Exception("Corrupción de Journaling: Estado de transacción inválido ('" + state + "'). Solo se permite 'PENDIENTE' o 'CONFIRMADA'.");
        }
        this.state = state.toUpperCase(); // Normalización a prueba de balas
    }

    public int getAllocatedStartBlock() {
        return allocatedStartBlock;
    }
}