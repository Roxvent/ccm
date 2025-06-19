package cl.intelidata.security.model.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GrupoModel implements Serializable {
    private Long id;
    private String codigo;
}
