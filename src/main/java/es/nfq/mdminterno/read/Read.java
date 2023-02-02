package es.nfq.mdminterno.read;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import es.nfq.mdminterno.dto.Cliente;
import es.nfq.mdminterno.dto.baja.BajaCliente;
import es.nfq.mdminterno.dto.baja.BajaConsentimiento;
import es.nfq.mdminterno.dto.baja.BajaMDM;
import es.nfq.mdminterno.dto.baja.BajaRol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

    public String[] crearPythonError(File file, String operacion) {
        return new String[]{
                "python",
                "src/main/resources/script/test_error.py",
                operacion,
                file.getAbsolutePath()
        };
    }

    public boolean ejecutarPython(String[] cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            int exitValue = process.waitFor();
            return exitValue == 0;
        } catch (IOException | InterruptedException e) {
            log.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    public String definirRuta(File file) {
        String ruta = System.getProperty("user.dir");
        return ruta + RUTA_HISTORICO + file.getName();
    }

    public String definirRutaError(File file) {
        String ruta = System.getProperty("user.dir");
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

    public boolean jsonValido(File file, Cliente dto, String tipo) {
        Gson gson = new Gson();
        try {
            JsonReader reader = new JsonReader(new FileReader(file));
            switch (tipo) {
                case BAJA:
                    dto = gson.fromJson(reader, new TypeToken<Cliente<BajaCliente>>() {
                    }.getType());
                    break;
                case BAJA_ROL:
                    dto = gson.fromJson(reader, new TypeToken<Cliente<BajaRol>>() {
                    }.getType());
                    break;
                case BAJA_CONSENTIMIENTO:
                    dto = gson.fromJson(reader, new TypeToken<Cliente<BajaConsentimiento>>() {
                    }.getType());
                    break;
                case BAJA_MDM:
                    dto = gson.fromJson(reader, new TypeToken<Cliente<BajaMDM>>() {
                    }.getType());
                    break;
            }
            reader.close();
            dto.validate();
        } catch (IllegalStateException e) {
            log.error(e.getLocalizedMessage());
            return false;
        } catch (FileNotFoundException e) {
            log.error(e.getLocalizedMessage());
            return false;
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            return false;
        } catch (InvocationTargetException e) {
            log.error(e.getTargetException().getLocalizedMessage());
            return false;
        } catch (IllegalAccessException e) {
            log.error(e.getLocalizedMessage());
            return false;
        } catch (NoSuchMethodException e) {
            log.error(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    public void procesarFile(File file, Cliente dto, String tipo) {
        if (jsonValido(file, dto, tipo)) {
            String[] cmd = crearPython(file, tipo);
            if (ejecutarPython(cmd)) {
                log.info("Fichero " + file.getName() + " correcto");
                moverFichero(file, definirRuta(file));
            } else {
                log.info("Fichero " + file.getName() + " incorrecto");
                moverFichero(file, definirRutaError(file));
            }
        } else {
            log.info("Fichero " + file.getName() + " inválido");
            moverFichero(file, definirRutaError(file));
        }
    }

    public void procesarFileFallo(File file, Cliente dto, String tipo) {
        if (jsonValido(file, dto, tipo)) {
            String[] cmd = crearPythonError(file, tipo);
            if (ejecutarPython(cmd)) {
                log.info("Fichero " + file.getName() + " correcto");
                moverFichero(file, definirRuta(file));
            } else {
                log.info("Fichero " + file.getName() + " incorrecto");
                moverFichero(file, definirRutaError(file));
            }
        } else {
            log.info("Fichero " + file.getName() + " inválido");
            moverFichero(file, definirRutaError(file));
        }
    }

    @Scheduled(fixedRate = 300000)
    public void proceso() throws InterruptedException {
        Thread.sleep(1000);
        List<File> nombres = getFiles(RUTA);
        nombres.forEach(
                s -> {
                    log.info("-- Procesando " + s.getName() + " --");
                    Cliente dto = new Cliente(); //TODO dto alta
                    if (s.getName().contains(ALTA)) {
                        String[] cmd = crearPythonError(s, ALTA);
                        if (ejecutarPython(cmd)) {
                            moverFichero(s, definirRuta(s));
                        } else moverFichero(s, definirRutaError(s));
                    } else if (s.getName().contains(ALTA_ROL)) {
                        String[] cmd = crearPythonError(s, ALTA_ROL);
                        if (ejecutarPython(cmd)) {
                            moverFichero(s, definirRuta(s));
                        } else moverFichero(s, definirRutaError(s));
                    } else if (s.getName().contains(ALTA_CONSENTIMIENTO)) {
                        String[] cmd = crearPythonError(s, ALTA_CONSENTIMIENTO);
                        if (ejecutarPython(cmd)) {
                            moverFichero(s, definirRuta(s));
                        } else moverFichero(s, definirRutaError(s));
                    } else if (s.getName().contains(BAJA)) {
                        procesarFile(s, dto, BAJA);
                    } else if (s.getName().contains(BAJA_MDM)) {
                        procesarFile(s, dto, BAJA_MDM);
                    } else if (s.getName().contains(BAJA_CONSENTIMIENTO)) {
                        procesarFile(s, dto, BAJA_CONSENTIMIENTO);
                    } else if (s.getName().contains(BAJA_ROL)) {
                        procesarFile(s, dto, BAJA_ROL);
                    }
                    log.info("-- Terminado de procesar " + s.getName() + " --\n");
                }
        );
        // TODO: Actualizar al terminar
        // FIXME: Como comentario adicional, aunque la ejecución de la aplicación sea cada cierto periodo de tiempo,
        // hasta que no terminen de ejecutarse los comandos de la anterior ejecución no debería volver a ejecutarse.
    }
}
