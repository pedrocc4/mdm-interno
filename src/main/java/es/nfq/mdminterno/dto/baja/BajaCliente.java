package es.nfq.mdminterno.dto.baja;

import es.nfq.mdminterno.dto.Validar;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BajaCliente extends Validar {
    private String idCliente;

    public void validar() {
        if (idCliente == null) {
            throw new IllegalStateException("El campo 'idCliente' es obligatorio");
        }
    }
}
