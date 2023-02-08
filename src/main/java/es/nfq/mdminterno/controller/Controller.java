package es.nfq.mdminterno.controller;

import es.nfq.mdminterno.service.IValidateService;
import es.nfq.mdminterno.utils.response.ResponseAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static es.nfq.mdminterno.utils.Constantes.PROCESO_TERMINADO;

@RestController
@Slf4j
public class Controller {
    private final IValidateService service;

    @Autowired
    public Controller(IValidateService service) {
        this.service = service;
    }

    @PostMapping("/proceso")
    public ResponseEntity<ResponseAPI> proceso(@RequestParam String operacion, @RequestBody Object datos) {
        log.info("-- COMIENZA PROCESO --");
        if (!service.operacionValida(operacion)) {
            log.info(PROCESO_TERMINADO);
            return new ResponseEntity<>(new ResponseAPI(
                    "Nombre de operacion no válido"), HttpStatus.BAD_REQUEST);
        }

        if (!service.validarJson(datos, operacion)) {
            log.info(PROCESO_TERMINADO);
            return new ResponseEntity<>(new ResponseAPI("Json no válido"), HttpStatus.BAD_REQUEST);
        }

        if (!service.procesoJson(operacion, datos)) {
            log.info(PROCESO_TERMINADO);
            return new ResponseEntity<>(new ResponseAPI("Json no válido"), HttpStatus.BAD_REQUEST);
        }

        log.info(PROCESO_TERMINADO);
        return new ResponseEntity<>(new ResponseAPI("Json válido"), HttpStatus.OK);
    }
}
