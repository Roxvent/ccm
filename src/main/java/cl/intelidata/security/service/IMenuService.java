package cl.intelidata.security.service;

import java.util.List;

import cl.intelidata.ccm2.security.entity.Menu;
import cl.intelidata.security.model.NavigationResponse;
import cl.intelidata.security.model.api.UsuarioRequest;

public interface IMenuService {

	void registrar(Menu menu);

	void modificar(Menu menu);

	void eliminar(long idMenu);

	Menu listarId(long idMenu);

	List<Menu> listar();

	List<NavigationResponse> listarMenuPorUsuario(UsuarioRequest request);
}
