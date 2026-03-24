/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/** * Autor: Macorre21 */

/**
 * Subsistema: Seguridad y Control de Acceso
 * Descripción: Gestiona el contexto de seguridad actual del simulador.
 * Controla los privilegios de ejecución basándose en el modo de sesión
 * (Administrador o Usuario) y la propiedad de los archivos en el árbol.
 */
public class UserManager {

    private String currentMode;
    private String currentUserName;

    /**
     * Inicializa el gestor de usuarios sin ninguna sesión activa.
     */
    public UserManager() {
        this.currentMode = null;
        this.currentUserName = null;
    }

    /**
     * Autentica y establece el contexto de ejecución actual.
     * Lógica: Exige un nivel de acceso estricto ("ADMIN" o "USER"), ignorando
     * mayúsculas y minúsculas para mayor robustez frente a la entrada de la GUI.
     * * @param mode Nivel de acceso.
     * @param userName Identificador del usuario.
     * @throws Exception Si las credenciales de formato son inválidas.
     */
    public void setSession(String mode, String userName) throws Exception {
        // Corrección: ignorar mayúsculas/minúsculas con equalsIgnoreCase
        if (mode == null || (!mode.equalsIgnoreCase("ADMIN") && !mode.equalsIgnoreCase("USER"))) {
            throw new Exception("Fallo de autenticación: El modo de sesión debe ser estrictamente 'ADMIN' o 'USER'.");
        }
        if (userName == null || userName.trim().isEmpty()) {
            throw new Exception("Fallo de autenticación: El nombre de usuario no puede estar vacío.");
        }
        
        // Normalizamos a mayúscula por consistencia en las validaciones posteriores
        this.currentMode = mode.toUpperCase(); 
        this.currentUserName = userName;
    }

    /**
     * Verifica si la sesión actual posee privilegios de superusuario.
     * @return true si es administrador.
     */
    public boolean isAdmin() {
        return "ADMIN".equals(this.currentMode);
    }

    /**
     * Determina si la sesión actual tiene permisos de creación.
     * @return true si hay una sesión activa.
     */
    public boolean canCreate() {
        return this.currentMode != null;
    }

    /**
     * Evalúa si la sesión actual puede alterar un archivo existente.
     * Lógica: Un administrador tiene acceso irrestricto (Override). Un usuario
     * regular solo puede modificar archivos que le pertenecen.
     * * @param fileOwner El propietario original del archivo.
     * @return true si la modificación está permitida.
     */
    public boolean canModify(String fileOwner) {
        if (isAdmin()) {
            return true;
        }
        return this.currentUserName != null && this.currentUserName.equals(fileOwner);
    }

    /**
     * Evalúa si la sesión actual puede destruir un archivo existente.
     * Lógica: Mismas reglas de validación que la modificación.
     * * @param fileOwner El propietario original del archivo.
     * @return true si la eliminación está permitida.
     */
    public boolean canDelete(String fileOwner) {
        if (isAdmin()) {
            return true;
        }
        return this.currentUserName != null && this.currentUserName.equals(fileOwner);
    }
}