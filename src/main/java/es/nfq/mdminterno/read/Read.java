package es.nfq.mdminterno.read;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import es.nfq.mdminterno.dto.Cliente;
import es.nfq.mdminterno.dto.baja.BajaCliente;
import es.nfq.mdminterno.dto.baja.BajaConsentimiento;
import es.nfq.mdminterno.dto.baja.BajaMDM;
import es.nfq.mdminterno.dto.baja.BajaRol;
import es.nfq.mdminterno.dto.plantilla.Plantilla;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class Read {
    public static final String RUTA_HISTORICO = "\\src\\main\\resources\\historico\\";
    public static final String RUTA_ERROR = "\\src\\main\\resources\\errores\\";
    public static final String RUTA = "src/main/resources/archivos";
    public static final String RUTA_INTERFACES = "src/main/resources/interfaces";
    public static final String ALTA = "AltaCliente";
    public static final String ALTA_CONSENTIMIENTO = "AltaConsentimiento";
    public static final String ALTA_ROL = "AltaRol";
    public static final String BAJA = "BajaCliente";
    public static final String BAJA_MDM = "BajaMDM";
    public static final String BAJA_ROL = "BajaRol";
    public static final String BAJA_CONSENTIMIENTO = "BajaConsentimiento";

    public static List<File> getFiles(String ruta) {
        final File folder = new File(ruta);
        List<File> fileName = new ArrayList<>();
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                getFiles(fileEntry.getPath());
            } else {
                fileName.add(fileEntry);
            }
        }
        return fileName.stream().sorted().collect(Collectors.toList());
    }

    public String[] crearPython(File file, String operacion) {
        return new String[]{
                "python",
                "src/main/resources/script/test.py",
                operacion,
                file.getAbsolutePath()
        };
    }

    public boolean ejecutarPython(String[] cmd) {
        try {
            Logger log = Logger.getLogger("python." + cmd[2]);
            Process process = Runtime.getRuntime().exec(cmd);
            int exitValue = process.waitFor();
            InputStream stdout = process.getInputStream();
            String text = new BufferedReader(
                    new InputStreamReader(stdout, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            log.info(text);
            return exitValue == 0;
        } catch (IOException | InterruptedException e) {
            log.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    public String definirRuta(File file) {
        String ruta = Paths.get("").toAbsolutePath().toString();
        return ruta + RUTA_HISTORICO + file.getName();
    }

    public String definirRutaError(File file) {
        String ruta = Paths.get("").toAbsolutePath().toString();
        return ruta + RUTA_ERROR + file.getName();
    }

    public void moverFichero(File file1, String targetDirectory) {
        File file = new File(file1.getAbsolutePath());
        if (file.renameTo(new File(targetDirectory))) {
            file.delete();
            if (targetDirectory.contains("historico"))
                log.info("Fichero a directorio historico");
            else log.info("Fichero a directorio de errores");
        } else {
            log.error("Failed to move the file");
        }
    }

    public String limpiarContenido(String contenido) {
        contenido = contenido.replace("[", "");
        return contenido.replace("]", "");
    }

    public void obtenerParametros(List<String> parametrosPlantilla, JsonNode plantillaType, ObjectMapper mapper)
            throws JsonProcessingException {
        String contenido = plantillaType.fields().hasNext() ? plantillaType.fields().next().getValue().toString() : "";
        while (!contenido.isEmpty()) {
            contenido = limpiarContenido(contenido);
            JsonNode plantillaContent = mapper.readTree(contenido);
            plantillaContent.fieldNames().forEachRemaining(s -> parametrosPlantilla.add(s));
            int size = Iterators.size(plantillaContent.fields());
            for (int i = 0; i < size; i++) {
                contenido = plantillaContent.fields().next().getValue().toString();
                contenido = limpiarContenido(contenido);
                JsonNode plantillaContentInside = mapper.readTree(contenido);
                obtenerParametros(parametrosPlantilla, plantillaContentInside, mapper);
            }
        }
    }

    public boolean jsonValido(File interfaz, File file, Cliente dto, String tipo) {
        Gson gson = new Gson();
        try {
//            JsonReader reader = new JsonReader(new FileReader(file));
            List<String> parametrosPlantilla = new ArrayList<>();
            List<String> parametrosArchivo = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode plantillaType = mapper.readTree(interfaz);
            JsonNode aValidar = mapper.readTree(file);
//            obtenerParametros(parametrosPlantilla, plantillaType, mapper);
            parametrosPlantilla.forEach(s -> log.info(s));
//            plantillaType.fields().forEachRemaining(s -> s.getValue().elements()
//                    .forEachRemaining(jsonNode -> log.info(jsonNode.asText())));
//            aValidar.fields().forEachRemaining(s -> s.getValue().fields().forEachRemaining(
//                    p -> log.info(p.toString())));
            if (!plantillaType.equals(aValidar)) {
                throw new IllegalArgumentException("El archivo a validar no coincide con la estructura de la plantilla");
            }
//            JsonNode schemaNode = JsonLoader.fromFile(interfaz);
//            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
//            JsonSchema schema = factory.getJsonSchema(schemaNode);
//
//            JsonNode data = JsonLoader.fromFile(file);
//
//            try {
//                ProcessingReport report = schema.validate(data);
//                if (report.isSuccess()) {
//                    // El objeto JSON cumple con la estructura esperada
//                    log.info("El JSON tiene estructura correcta");
//                } else {
//                    // El objeto JSON no cumple con la estructura esperada
//                    // Puedes imprimir los errores de validación en el report
//                    log.error("El JSON no tiene la estructura correcta");
//                }
//            } catch (ProcessingException e) {
//                log.error(e.getLocalizedMessage());
//                return false;
//            }
//            switch (tipo) {
//                case BAJA:
//                    dto = gson.fromJson(reader, new TypeToken<Cliente<BajaCliente>>() {
//                    }.getType());
//                    break;
//                case BAJA_ROL:
//                    dto = gson.fromJson(reader, new TypeToken<Cliente<BajaRol>>() {
//                    }.getType());
//                    break;
//                case BAJA_CONSENTIMIENTO:
//                    dto = gson.fromJson(reader, new TypeToken<Cliente<BajaConsentimiento>>() {
//                    }.getType());
//                    break;
//                case BAJA_MDM:
//                    dto = gson.fromJson(reader, new TypeToken<Cliente<BajaMDM>>() {
//                    }.getType());
//                    break;
//            }
//            reader.close();
//            dto.validate();
        } catch (IllegalArgumentException e) {
            log.error(e.getLocalizedMessage());
            return false;
        } catch (IllegalStateException e) {
            log.error(e.getLocalizedMessage());
            return false;
        } catch (FileNotFoundException e) {
            log.error(e.getLocalizedMessage());
            return false;
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            return false;
//        } catch (InvocationTargetException e) {
//            log.error(e.getTargetException().getLocalizedMessage());
//            return false;
//        } catch (IllegalAccessException e) {
//            log.error(e.getLocalizedMessage());
//            return false;
//        } catch (NoSuchMethodException e) {
//            log.error(e.getLocalizedMessage());
//            return false;
//        } catch (ProcessingException e) {
//            log.error(e.getLocalizedMessage());
//            return false;
//        }
        }
        return true;
    }

    public void procesarFile(List<File> interfaces, File file, Cliente dto, String tipo) {
//        if (jsonValido(obtenerInterfaz(interfaces, tipo), file, dto, tipo)) {
        String[] cmd = crearPython(file, tipo);
        if (ejecutarPython(cmd)) {
            log.info("Fichero " + file.getName() + " correcto");
            moverFichero(file, definirRuta(file));
        } else {
            log.info("Fichero " + file.getName() + " incorrecto");
            moverFichero(file, definirRutaError(file));
        }
//        } else {
//            log.info("Fichero " + file.getName() + " inválido");
//            moverFichero(file, definirRutaError(file));
//        }
    }

    public File obtenerInterfaz(List<File> interfaces, String tipo) {
        return interfaces.stream()
                .filter(f -> f.getName().contains(tipo))
                .findFirst().orElse(null);
    }

    // TODO log

    @Scheduled(fixedRate = 300000)
    public void proceso() throws InterruptedException {
        Thread.sleep(1000);
        log.info("-- COMIENZA PROCESO --");
        List<File> nombres = getFiles(RUTA);
        List<File> interfaces = getFiles(RUTA_INTERFACES);
        nombres.forEach(
                s -> {
                    log.info("-- Procesando " + s.getName() + " --");
                    Cliente dto = new Cliente();
                    if (s.getName().contains(ALTA)) {
                        procesarFile(interfaces, s, dto, ALTA);
                    } else if (s.getName().contains(ALTA_ROL)) {
                        procesarFile(interfaces, s, dto, ALTA_ROL);
                    } else if (s.getName().contains(ALTA_CONSENTIMIENTO)) {
                        procesarFile(interfaces, s, dto, ALTA_CONSENTIMIENTO);
                    } else if (s.getName().contains(BAJA)) {
                        procesarFile(interfaces, s, dto, BAJA);
                    } else if (s.getName().contains(BAJA_MDM)) {
                        procesarFile(interfaces, s, dto, BAJA_MDM);
                    } else if (s.getName().contains(BAJA_CONSENTIMIENTO)) {
                        procesarFile(interfaces, s, dto, BAJA_CONSENTIMIENTO);
                    } else if (s.getName().contains(BAJA_ROL)) {
                        procesarFile(interfaces, s, dto, BAJA_ROL);
                    } else {
                        log.error("Nombre de fichero incorrecto");
                        moverFichero(s, definirRutaError(s));
                    }
                    log.info("-- Terminado de procesar " + s.getName() + " --\n");
                }
        );
        log.info("-- PROCESO TERMINADO --");
        // TODO: Actualizar al terminar
        // FIXME: Como comentario adicional, aunque la ejecución de la aplicación sea cada cierto periodo de tiempo,
        // hasta que no terminen de ejecutarse los comandos de la anterior ejecución no debería volver a ejecutarse.
    }
}
