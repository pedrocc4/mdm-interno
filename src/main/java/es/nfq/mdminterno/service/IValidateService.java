package es.nfq.mdminterno.service;

import java.util.logging.Logger;

public interface IValidateService {
    boolean operacionValida(String operacion);

    boolean validarJson(Object datos, String operacion, Logger log);

    boolean procesoJson(String operacion, Object datos, Logger log, String timestamp);
}
