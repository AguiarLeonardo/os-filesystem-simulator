/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Macorre21
 * Subsistema: Persistencia / Tolerancia a fallos
 * Descripción: Mantiene una bitácora física de las operaciones completadas.
 */
public class JournalManager {
    
    private static final String JOURNAL_FILE = "journal.log";

    // Escribe una operación exitosa en el archivo de texto
    public static void logOperation(String operation) {
        try (FileWriter fw = new FileWriter(JOURNAL_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(operation);
        } catch (IOException e) {
            System.err.println("Error al escribir en el Journal: " + e.getMessage());
        }
    }

    // Lee el historial completo para poder recuperar el sistema
    public static List<String> readJournal() {
        List<String> ops = new ArrayList<>();
        File file = new File(JOURNAL_FILE);
        if (!file.exists()) return ops;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                ops.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error al leer el Journal: " + e.getMessage());
        }
        return ops;
    }
    
    // Limpia el historial (útil si formateamos el disco)
    public static void clearJournal() {
        new File(JOURNAL_FILE).delete();
    }
}