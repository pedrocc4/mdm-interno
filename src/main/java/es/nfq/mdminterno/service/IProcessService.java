package es.nfq.mdminterno.service;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public interface IProcessService {
    List<File> getFiles(String ruta);

    String[] crearPython(File file, String operacion);

    boolean ejecutarPython(String[] cmd, Logger log, String timestamp);

    String definirRuta(File file);

    String definirRutaError(File file);

    boolean moverFichero(File file1, String targetDirectory, Logger log);

    boolean procesarFile(File file, String tipo, Logger log, String timestamp);

    String limpiarContenido(String contenido);
}
