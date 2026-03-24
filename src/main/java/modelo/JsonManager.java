/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import estructuras.CustomLinkedList;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author User
 *
 * Subsistema: Persistencia de Datos
 * Descripción: Serializa y deserializa el estado del sistema en formato JSON.
 */
public class JsonManager {

    private final String defaultFilePath;
    private final Gson gson;

    public JsonManager(String path) {
        this.defaultFilePath = path;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void saveState(FileSystemNode root, SimulatedDisk disk) throws Exception {
        try (FileWriter writer = new FileWriter(defaultFilePath)) {
            JsonObject rootObject = new JsonObject();
            rootObject.add("fileSystem", gson.toJsonTree(root));
            rootObject.add("disk", gson.toJsonTree(disk));
            gson.toJson(rootObject, writer);
        } catch (IOException e) {
            throw new Exception("Error crítico de I/O al guardar el estado: " + e.getMessage());
        }
    }

    public FileSystemNode loadFileSystem() throws Exception {
        try (FileReader reader = new FileReader(defaultFilePath)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            // Deserialización básica del nodo raíz
            return gson.fromJson(jsonObject.get("fileSystem"), FileSystemNode.class);
            // IMPORTANTE: Si la librería Gson instancia ArrayLists internamente por defecto, 
            // se debe usar un TypeAdapter personalizado de Gson o parsear el JsonArray manualmente 
            // iterando y haciendo .add() en la CustomLinkedList para cumplir al 100% la regla del arquitecto.
        } catch (Exception e) {
            throw new Exception("Archivo JSON corrupto o no encontrado al cargar FileSystem: " + e.getMessage());
        }
    }

    public SimulatedDisk loadDiskState() throws Exception {
        try (FileReader reader = new FileReader(defaultFilePath)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            return gson.fromJson(jsonObject.get("disk"), SimulatedDisk.class);
        } catch (Exception e) {
            throw new Exception("Archivo JSON corrupto o no encontrado al cargar Disco: " + e.getMessage());
        }
    }
}
