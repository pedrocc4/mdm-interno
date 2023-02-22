package es.nfq.mdminterno.utils.exception.handle;

import es.nfq.mdminterno.utils.Constantes;
import es.nfq.mdminterno.utils.exception.APIException;
import es.nfq.mdminterno.utils.response.ResponseAPI;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static es.nfq.mdminterno.utils.Constantes.PROCESO_TERMINADO;

@RestControllerAdvice
public class CustomErrors {

    @ExceptionHandler(APIException.class)
    public final ResponseEntity<ResponseAPI> handleAPIException(APIException ex) {
        Logger log = Logger.getLogger("java");
        log.info(PROCESO_TERMINADO);
        return new ResponseEntity<>(new ResponseAPI(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
    }
}
