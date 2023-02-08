package es.nfq.mdminterno.utils.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseAPI {
    @JsonProperty("Mensaje")
    private String mensaje;
}
