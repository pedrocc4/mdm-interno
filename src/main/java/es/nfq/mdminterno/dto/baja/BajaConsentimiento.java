package es.nfq.mdminterno.dto.baja;

import es.nfq.mdminterno.dto.Validar;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BajaConsentimiento extends Validar {
    private String idCliente;
    private List<RolConsentimiento> roles;

    public void validar() {
        if (idCliente == null) {
            throw new IllegalStateException("El campo 'idCliente' es obligatorio");
        }
        roles.forEach(RolConsentimiento::validar);
    }
}
