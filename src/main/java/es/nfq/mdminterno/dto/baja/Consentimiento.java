package es.nfq.mdminterno.dto.baja;

public class Consentimiento {
    private String coConsentimientoMarca;

    public void validar() {
        if (coConsentimientoMarca == null)
            throw new IllegalArgumentException("El campo 'coConsentimientoMarca' debe estar informado");
    }
}
