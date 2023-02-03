package es.nfq.mdminterno.dto.plantilla;

import lombok.Data;

import java.util.List;

@Data
public class Plantilla {
    private List<Cliente> clientes;

    @Data
    public static class Cliente {
        private String idInterno;
    }

}