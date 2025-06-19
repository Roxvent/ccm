package cl.intelidata.security.model.api;

import java.util.List;

import cl.intelidata.ccm2.security.entity.Rol;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RolListResponse {
	private long total;
	private List<Rol> roles;
}
