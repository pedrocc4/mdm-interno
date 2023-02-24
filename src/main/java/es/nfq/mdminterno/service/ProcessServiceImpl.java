package es.nfq.mdminterno.service;

import es.nfq.mdminterno.utils.exception.APIException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

import static es.nfq.mdminterno.utils.Constantes.*;

@Service
public class ProcessServiceImpl implements IProcessService {
    @Override
    public List<File> getFiles(String ruta) {
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

    @Override
    public String[] crearPython(File file, String operacion) {
        return new String[]{
                "python",
                "src/main/resources/script/test.py",
                operacion,
                file.getAbsolutePath()
        };
    }

    @Override
    public boolean ejecutarPython(String[] cmd, Logger log, String timestamp) {
        try {
            String directorio = RUTA_LOG + cmd[2] + "_" + timestamp;
            final Logger logPython = Logger.getLogger(cmd[2] + "_" + timestamp + "·.python");
            FileHandler fileHandler1 = new FileHandler(directorio + "//pyhton.log");
            SimpleFormatter formatter = new SimpleFormatter() {
                private static final String format = "[%1$tF %1$tT] %2$s %3$s %n";

                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(format, new Date(lr.getMillis()), lr.getLevel().getName(), lr.getMessage());
                }
            };
            fileHandler1.setFormatter(formatter);
            logPython.addHandler(fileHandler1);
            Process process = Runtime.getRuntime().exec(cmd);
            int exitValue = process.waitFor();
            InputStream stdout = process.getInputStream();
            String text = new BufferedReader(
                    new InputStreamReader(stdout, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            logPython.info(text);
            fileHandler1.close();
            return exitValue == 0;
        } catch (IOException | InterruptedException e) {
            log.severe("Se ha producido un error al ejecutar el archivo python: " + e.getLocalizedMessage());
            log.info(PROCESO_TERMINADO);
            Thread.currentThread().interrupt();
            throw new APIException(ERROR_INESPERADO);
        }
    }

    @Override
    public String definirRuta(File file) {
        String ruta = Paths.get("").toAbsolutePath().toString();
        return ruta + RUTA_HISTORICO + file.getName();
    }

    public String definirRutaError(File file) {
        String ruta = Paths.get("").toAbsolutePath().toString();
        return ruta + RUTA_ERROR + file.getName();
    }

    @Override
    public boolean moverFichero(File file1, String targetDirectory, Logger log) {
        File file = new File(file1.getAbsolutePath());
        if (file.renameTo(new File(targetDirectory))) {
            if (targetDirectory.contains("historico")) {
                log.info("Fichero a directorio historico");
                return true;
            } else {
                log.info("Fichero a directorio de errores");
                return false;
            }
        } else {
            log.severe("Problema al mover el fichero");
            throw new APIException(ERROR_INESPERADO);
        }
    }

    @Override
    public String limpiarContenido(String contenido) {
        contenido = contenido.replace("[", "");
        return contenido.replace("]", "");
    }

    @Override
    public boolean procesarFile(File file, String tipo, Logger log, String timestamp) {
        String[] cmd = crearPython(file, tipo);
        if (ejecutarPython(cmd, log, timestamp)) {
            log.info("Fichero " + file.getName() + " correcto");
            return moverFichero(file, definirRuta(file), log);
        }

        log.info("Fichero " + file.getName() + " incorrecto");
        return moverFichero(file, definirRutaError(file), log);
    }
}
