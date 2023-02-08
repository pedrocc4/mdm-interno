package es.nfq.mdminterno.service;

public interface IValidateService {
    boolean operacionValida(String operacion);

    boolean validarJson(Object datos, String operacion);

    boolean procesoJson(String operacion, Object datos);
}
