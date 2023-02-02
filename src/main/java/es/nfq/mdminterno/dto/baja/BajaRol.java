package es.nfq.mdminterno.dto.baja;

import es.nfq.mdminterno.dto.Validar;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BajaRol extends Validar {
    private String idCliente;
    private List<Rol> roles;

    public void validar() {
        if (idCliente == null) {
            throw new IllegalStateException("El campo 'idCliente' es obligatorio");
        }
        roles.forEach(Rol::validar);
    }
}
