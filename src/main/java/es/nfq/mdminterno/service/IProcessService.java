package es.nfq.mdminterno.service;

import java.io.File;
import java.util.List;

public interface IProcessService {
    List<File> getFiles(String ruta);

    String[] crearPython(File file, String operacion);

    boolean ejecutarPython(String[] cmd);

    String definirRuta(File file);

    String definirRutaError(File file);

    boolean moverFichero(File file1, String targetDirectory);

    boolean procesarFile(File file, String tipo);

    String limpiarContenido(String contenido);
}
