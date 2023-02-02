package es.nfq.mdminterno.dto.baja;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Setter
public class RolConsentimiento {
    private String rol;
    private String idContrato;
    private List<Consentimiento> consentimiento;

    public void validar() {
        if (rol == null)
            throw new IllegalArgumentException("El campo 'rol' debe estar informado");
        if (idContrato == null)
            throw new IllegalArgumentException("El campo 'idContrato' debe estar informado");
        consentimiento.forEach(Consentimiento::validar);
    }
}
