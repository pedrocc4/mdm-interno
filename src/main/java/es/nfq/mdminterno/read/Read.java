package es.nfq.mdminterno.read;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
public class Read {

    public final static String ruta = "src/main/resources/archivos";
    public final static String rutaHistorico = "\\src\\main\\resources\\historico\\";
    public final static String ALTA = "AltaCliente";
    public final static String ALTA_CONSENTIMIENTO = "AltaConsentimiento";
    public final static String ALTA_ROL = "AltaRol";
    public final static String BAJA = "BajaCliente";
    public final static String BAJA_MDM = "BajaClienteMDM";
    public final static String BAJA_ROL = "BajaRol";
    public final static String BAJA_CONSENTIMIENTO = "BajaConsentimiento";

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
        String[] cmd = {
                "python",
                "src/main/resources/script/test.py",
                operacion,
                file.getAbsolutePath()
        };
        return cmd;
    }

    public String[] crearPythonError(File file, String operacion) {
        String[] cmd = {
                "python",
                "src/main/resources/script/test_error.py",
                operacion,
                file.getAbsolutePath()
        };
        return cmd;
    }

    public String ejecutarPython(String[] cmd) {
        String text = "";
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            InputStream stdout = process.getInputStream();

            text = new BufferedReader(
                    new InputStreamReader(stdout, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            System.out.println(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return text;
    }

    public void moverFichero(File file1) {
        File file = new File(file1.getAbsolutePath());
        String ruta = System.getProperty("user.dir");
        String targetDirectory = ruta + rutaHistorico + file.getName();
        System.out.println(targetDirectory);
        if (file.renameTo(new File(targetDirectory))) {
            file.delete();
            System.out.println("File moved successfully");
        } else {
            System.out.println("Failed to move the file");
        }
    }

    @Scheduled(fixedRate = 300000)
    public void proceso() {
        List<File> nombres = getFiles("src/main/resources/archivos");
        nombres.forEach(
                s -> {
                    if (s.getName().contains(ALTA)) {
                        String[] cmd = crearPython(s, ALTA);
                        System.out.println("Ejecución: " + ejecutarPython(cmd));
                        moverFichero(s);
                    } else if (s.getName().contains(ALTA_ROL)) {
                        String[] cmd = crearPython(s, ALTA_ROL);
                        System.out.println("Ejecución: " + ejecutarPython(cmd));
                        moverFichero(s);
                    } else if (s.getName().contains(ALTA_CONSENTIMIENTO)) {
                        String[] cmd = crearPython(s, ALTA_CONSENTIMIENTO);
                        System.out.println("Ejecución: " + ejecutarPython(cmd));
                        moverFichero(s);
                    } else if (s.getName().contains(BAJA)) {
                        String[] cmd = crearPython(s, BAJA);
                        System.out.println("Ejecución: " + ejecutarPython(cmd));
                        moverFichero(s);
                    } else if (s.getName().contains(BAJA_MDM)) {
                        String[] cmd = crearPython(s, BAJA_MDM);
                        System.out.println("Ejecución: " + ejecutarPython(cmd));
                        moverFichero(s);
                    } else if (s.getName().contains(BAJA_CONSENTIMIENTO)) {
                        String[] cmd = crearPython(s, BAJA_CONSENTIMIENTO);
                        System.out.println("Ejecución: " + ejecutarPython(cmd));
                        moverFichero(s);
                    } else if (s.getName().contains(BAJA_ROL)) {
                        String[] cmd = crearPython(s, BAJA_ROL);
                        System.out.println("Ejecución: " + ejecutarPython(cmd));
                        moverFichero(s);
                    }
                }
        );
    }
}
