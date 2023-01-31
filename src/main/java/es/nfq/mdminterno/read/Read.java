package es.nfq.mdminterno.read;

import org.python.util.PythonInterpreter;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
public class Read {

    public final static String ruta = "src/main/resources/archivos";
    public final static String ALTA = "AltaCliente";
    public final static String ALTA_CONSENTIMIENTO = "AltaConsentimiento";
    public final static String ALTA_ROL = "AltaRol";
    public final static String BAJA = "BajaCliente";
    public final static String BAJA_MDM = "BajaClienteMDM";
    public final static String BAJA_ROL = "BajaRol";
    public final static String BAJA_CONSENTIMIENTO = "BajaConsentimiento";

    public static List<String> getFiles(String ruta) {
        final File folder = new File(ruta);
        List<String> fileName = new ArrayList<>();
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                getFiles(fileEntry.getPath());
            } else {
                fileName.add(fileEntry.getName());
            }
        }
        return fileName.stream().sorted().collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 300000)
    public void proceso() {
        PythonInterpreter pythonInterpreter = new PythonInterpreter();
        List<String> nombres = getFiles("src/main/resources/archivos");
        nombres.forEach(
                s -> {
                    if (s.contains(ALTA)) {
                        String[] cmd = {
                                "python",
                                "text.py",
                                "AltaCliente",
                                ruta + s
                        };
                        try {
                            Process process = Runtime.getRuntime().exec(cmd);
//                            if(process.)
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
//  Para los ficheros "Alta_Cliente_<numero>.json": python <nombre_script>.py AltaCliente <ruta_completa_del_fichero_json>.

                    // python test.py AltaCliente ruta + s
                }
        );
    }
}
