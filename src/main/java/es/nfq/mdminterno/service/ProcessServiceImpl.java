package es.nfq.mdminterno.service;

import es.nfq.mdminterno.utils.exception.APIException;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static es.nfq.mdminterno.utils.Constantes.*;
import static es.nfq.mdminterno.utils.Constantes.ERROR_INESPERADO;

@Service
@Slf4j
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
            log.error("Se ha producido un error al ejecutar el archivo python: " + e.getLocalizedMessage());
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
    public boolean moverFichero(File file1, String targetDirectory) {
        File file = new File(file1.getAbsolutePath());
        if (file.renameTo(new File(targetDirectory))) {
//            try {
//                Files.delete(file.toPath());
//            } catch (IOException e) {
//                log.error("Se ha producido un problema al borrar el fichero: " + e.getLocalizedMessage());
//                throw new APIException(ERROR_INESPERADO);
//            }
            if (targetDirectory.contains("historico")) {
                log.info("Fichero a directorio historico");
                return true;
            } else {
                log.info("Fichero a directorio de errores");
                return false;
            }
        } else {
            log.error("Problema al mover el fichero");
            throw new APIException(ERROR_INESPERADO);
        }
    }

    @Override
    public String limpiarContenido(String contenido) {
        contenido = contenido.replace("[", "");
        return contenido.replace("]", "");
    }

    @Override
    public boolean procesarFile(File file, String tipo) {
        String[] cmd = crearPython(file, tipo);
        if (ejecutarPython(cmd)) {
            log.info("Fichero " + file.getName() + " correcto");
            return moverFichero(file, definirRuta(file));
        }

        log.info("Fichero " + file.getName() + " incorrecto");
        return moverFichero(file, definirRutaError(file));
    }
}
