/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

/**
 *
 * @author Macorre21
 * Subsistema: Seguridad y Validación
 * Descripción: Motor centralizado estático para sanear las entradas de la GUI.
 */
public class InputValidator {

    public static int validateIntegerInput(String input, int min, int max) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo numérico no puede estar vacío.");
        }
        try {
            int value = Integer.parseInt(input.trim());
            if (value < min || value > max) {
                throw new IllegalArgumentException("El valor " + value + " está fuera del rango permitido [" + min + " - " + max + "].");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Entrada inválida: '" + input + "' no es un número entero válido.");
        }
    }

    public static String validateFileName(String input) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío.");
        }
        
        char[] invalidChars = {'/', '\\', ':', '*', '?', '"', '<', '>', '|'};
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            for (int j = 0; j < invalidChars.length; j++) {
                if (c == invalidChars[j]) {
                    throw new IllegalArgumentException("El nombre contiene caracteres prohibidos por el OS ('" + c + "').");
                }
            }
        }
        return input.trim();
    }

    public static boolean isAlphanumeric(String input) {
        if (input == null || input.isEmpty()) return false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
