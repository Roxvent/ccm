package cl.intelidata.security.model.api;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserModelResponse {
    private long total;
    private List<UserModel> usuarios;
}
