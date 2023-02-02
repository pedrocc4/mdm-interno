package es.nfq.mdminterno.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cliente<T extends Validar> {
    private List<T> clientes;

    public void validate() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (clientes == null || clientes.isEmpty()) {
            throw new IllegalStateException("No se encontraron clientes");
        }
        for (T t : clientes) {
            Method method = t.getClass().getMethod("validar");
            method.invoke(t);
        }
    }
}
