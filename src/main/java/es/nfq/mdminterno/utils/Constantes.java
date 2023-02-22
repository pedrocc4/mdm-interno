package es.nfq.mdminterno.utils;

import org.apache.log4j.Logger;

public class Constantes {
    private static final Logger log = null;
    private Constantes() {
    }

    // TODO log java + phyton (ALTACONSENTIMIENTO_TIMESTAMP)
    // TODO carpeta por cada timestamp (ejecucion)
    // TODO sistema de limpieza de logs
    // TODO validar que pkOrigen sea un Array

    public static final String RUTA_HISTORICO = "\\src\\main\\resources\\historico\\";
    public static final String RUTA_ERROR = "\\src\\main\\resources\\errores\\";
    public static final String RUTA_ARCHIVOS = "\\src\\main\\resources\\archivos\\";
    public static final String RUTA_INTERFACES = "src/main/resources/interfaces";
    public static final String ALTA = "AltaCliente";
    public static final String ALTA_CONSENTIMIENTO = "AltaConsentimiento";
    public static final String ALTA_ROL = "AltaRol";
    public static final String BAJA = "BajaCliente";
    public static final String BAJA_MDM = "BajaMDM";
    public static final String BAJA_ROL = "BajaRol";
    public static final String BAJA_CONSENTIMIENTO = "BajaConsentimiento";
    public static final String DESUNIFICACIONMANUAL = "DesunificacionManual";
    public static final String UNIFICACIONMANUAL = "UnificacionManual";
    public static final String PROCESO_TERMINADO = "-- PROCESO TERMINADO --";
    public static final String ERROR_INESPERADO = "Se ha producido un error inesperado. Consulta el log de java.";
    public static final String[] OPERACIONES = {
            ALTA, ALTA_ROL, ALTA_CONSENTIMIENTO,
            BAJA, BAJA_CONSENTIMIENTO, BAJA_MDM, BAJA_ROL,
            DESUNIFICACIONMANUAL, UNIFICACIONMANUAL
    };
}
