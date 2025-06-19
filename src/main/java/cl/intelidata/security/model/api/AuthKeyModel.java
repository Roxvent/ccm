package cl.intelidata.security.model.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthKeyModel implements Serializable {
    private String tenantId;
    private String clientId;
    private String clientSecret;
}
