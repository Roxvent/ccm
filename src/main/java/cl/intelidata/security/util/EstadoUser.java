package cl.intelidata.security.util;

import lombok.Getter;

@Getter
public enum EstadoUser {
    ENABLE(true),
    DISABLE(false);

    private final Boolean value;

    EstadoUser(Boolean value){
        this.value = value;
    }
}
