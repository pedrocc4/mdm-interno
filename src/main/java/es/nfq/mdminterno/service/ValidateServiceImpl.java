package es.nfq.mdminterno.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;
import es.nfq.mdminterno.utils.exception.APIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static es.nfq.mdminterno.utils.Constantes.*;

@Service
@Slf4j
public class ValidateServiceImpl implements IValidateService {
    private final IProcessService processService;

    @Autowired
    public ValidateServiceImpl(IProcessService processService) {
        this.processService = processService;
    }

    @Override
    public boolean operacionValida(String operacion) {
        switch (operacion) {
            case ALTA:
            case BAJA:
            case ALTA_CONSENTIMIENTO:
            case ALTA_ROL:
            case BAJA_MDM:
            case BAJA_CONSENTIMIENTO:
            case BAJA_ROL:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean validarJson(Object datos, String operacion) {
        // Obtener keys del json
        Gson gson = new Gson();
        LinkedHashMap mapa = (LinkedHashMap) datos;
        String jsonMapa = gson.toJson(mapa);
//        jsonMapa = processService.limpiarContenido(jsonMapa); //FIXME problema con los arrays en la request
        List<String> keysInput = new ArrayList<>();
        JsonElement element = JsonParser.parseString(jsonMapa);
        getAllKeys(element, keysInput);

        // Obtener keys de la interfaz
        List<File> interfaces = processService.getFiles(RUTA_INTERFACES);
        File interfaz = interfaces.stream()
                .filter(file -> file.getName().contains(operacion))
                .findFirst().orElseThrow(() -> new APIException("No se encuentra la interfaz: " + operacion));
        List<String> keysInterface = new ArrayList<>();
        FileReader reader;
        try {
            reader = new FileReader(interfaz);
            element = JsonParser.parseReader(reader);
            String elementString = element.toString();
            elementString = processService.limpiarContenido(elementString);
            element = JsonParser.parseString(elementString);
        } catch (Exception e) {
            log.error("Se ha producido un error al leer la interfaz: " + e.getLocalizedMessage());
            throw new APIException(ERROR_INESPERADO);
        }
        getAllKeys(element, keysInterface);

        Set<String> setInput = new HashSet<>(keysInput);
        Set<String> setInterface = new HashSet<>(keysInterface);

        Set<String> onlyInSet2 = new HashSet<>(setInterface);
        onlyInSet2.removeAll(setInput);

        if (!onlyInSet2.isEmpty()) {
            throw new APIException("Debes incluir los siguientes campos: " + onlyInSet2);
        }

        return setInput.containsAll(setInterface);
    }

    @Override
    public boolean procesoJson(String operacion, Object datos) {
        // Creacion fichero
        String ruta = Paths.get("").toAbsolutePath().toString();
        LocalDateTime ldt = LocalDateTime.now();
        String stringLdt = ldt.toString();
        stringLdt = stringLdt.replace(":", "_");
        stringLdt = stringLdt.replace(".", "_");
        String rutaTotal = ruta + RUTA_ARCHIVOS + operacion + "_" + stringLdt + ".json";
        File file = new File(rutaTotal);

        // Escritura
        try {
            FileWriter myWriter = new FileWriter(file);
            LinkedHashMap mapa = (LinkedHashMap) datos;
            Gson gson = new Gson();
            String jsonMapa = gson.toJson(mapa);
            myWriter.write(jsonMapa);
            myWriter.close();
        } catch (IOException e) {
            log.error("Se ha producido un error al escribir el archivo json: " + e.getLocalizedMessage());
            throw new APIException(ERROR_INESPERADO);
        }
        // Ejecutar python y mover fichero
        return processService.procesarFile(file, operacion);
    }

    public static void getAllKeys(JsonElement element, List<String> keys) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();
            entries.forEach(entry -> {
                String key = entry.getKey();
                keys.add(key.toLowerCase());
                JsonElement value = entry.getValue();
                if (value != null && value.isJsonObject()) {
                    getAllKeys(value, keys);
                }
            });
        }
    }

}
