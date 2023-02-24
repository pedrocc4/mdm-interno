package es.nfq.mdminterno.controller;

import es.nfq.mdminterno.service.IValidateService;
import es.nfq.mdminterno.utils.response.ResponseAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static es.nfq.mdminterno.utils.Constantes.*;

@RestController
public class Controller {

    private final IValidateService service;

    @Autowired
    public Controller(IValidateService service) {
        this.service = service;
    }

    @PostMapping("/proceso")
    public ResponseEntity<ResponseAPI> proceso(@RequestParam String operacion, @RequestBody Object datos) throws IOException {

        if (!service.operacionValida(operacion))
            return new ResponseEntity<>(new ResponseAPI(
                    "Operación no válida, operaciones soportadas: " + Arrays.toString(OPERACIONES)),
                    HttpStatus.BAD_REQUEST);

        LocalDateTime ldt = LocalDateTime.now();
        String timestamp = ldt.toString().replace(":", "_");
        timestamp = timestamp.replace(".", "_");

        final Logger log = Logger.getLogger(operacion + "_" + timestamp + "·.java");
        String directorio = RUTA_LOG + operacion + "_" + timestamp;

        // Crear el objeto Path con la ruta del directorio
        Path dir = Paths.get(directorio);

        // Crear el directorio si no existe
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileHandler fileHandler = new FileHandler(directorio + "//java.log");
        SimpleFormatter formatter = new SimpleFormatter() {
            private static final String format = "[%1$tF %1$tT] %2$s %3$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(format, new Date(lr.getMillis()), lr.getLevel().getName(), lr.getMessage());
            }
        };
        fileHandler.setFormatter(formatter);
        log.addHandler(fileHandler);
        log.info("-- COMIENZA PROCESO --");

        if (!service.validarJson(datos, operacion, log)) {
            log.info(PROCESO_TERMINADO);
            return new ResponseEntity<>(new ResponseAPI("Json no válido"), HttpStatus.BAD_REQUEST);
        }

        if (!service.procesoJson(operacion, datos, log, timestamp)) {
            log.info(PROCESO_TERMINADO);
            return new ResponseEntity<>(new ResponseAPI("Json no válido"), HttpStatus.BAD_REQUEST);
        }

        log.info(PROCESO_TERMINADO);
        return new ResponseEntity<>(new ResponseAPI("Json válido"), HttpStatus.OK);
    }
}
