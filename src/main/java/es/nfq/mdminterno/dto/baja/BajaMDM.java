package es.nfq.mdminterno.dto.baja;

import es.nfq.mdminterno.dto.Validar;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BajaMDM extends Validar {
    private String idInterno;

    public void validar() {
        if (idInterno == null) {
            throw new IllegalStateException("El campo 'idInterno' es obligatorio");
        }
    }
}
