package es.nfq.mdminterno.dto.baja;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Setter
public class Rol {
    @SerializedName("rol")
    private String rol;
    private String idContrato;

    public void validar() {
        if (rol == null)
            throw new IllegalArgumentException("El campo 'rol' debe estar informado");
        if (idContrato == null)
            throw new IllegalArgumentException("El campo 'idContrato' debe estar informado");
    }
}
