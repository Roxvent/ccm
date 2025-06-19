package cl.intelidata.security.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.intelidata.ccm2.security.entity.Menu;
import cl.intelidata.ccm2.security.repository.IMenuDAO;
import cl.intelidata.security.model.MenuResponse;
import cl.intelidata.security.model.NavigationResponse;
import cl.intelidata.security.model.api.UsuarioRequest;
import cl.intelidata.security.service.IMenuService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MenuServiceImpl implements IMenuService {

	@Autowired
	private IMenuDAO dao;

	@Override
	public void registrar(Menu menu) {
		dao.save(menu);
	}

	@Override
	public void modificar(Menu menu) {
		dao.save(menu);
	}

	@Override
	public void eliminar(long idMenu) {
		// 2
		dao.deleteById(idMenu);
	}

	@Override
	public Menu listarId(long idMenu) {
		// 2
		Optional<Menu> opt = dao.findById(idMenu);
		return opt.orElseGet(Menu::new);
	}

	@Override
	public List<Menu> listar() {
		return dao.findAll();
	}

	@Override
	public List<NavigationResponse> listarMenuPorUsuario(UsuarioRequest request) {

		log.info("Service Listar usuario");
		List<NavigationResponse> responseList = new ArrayList<>();

		NavigationResponse response = new NavigationResponse();

		List<Menu> nivel1 = dao.listarMenuPorUsuarioNivel(request.getNombre(), request.getApp(), 1);

		nivel1.forEach(menu -> {

			MenuResponse menuResponse = createMenuResponse(menu);

			if (response.getChildren() == null) {
				response.setChildren(new ArrayList<>());
				response.getChildren().add(menuResponse);
			} else if (!response.getChildren().contains(menuResponse)) {
				response.getChildren().add(menuResponse);
			}
		});

		List<Menu> nivel2 = dao.listarMenuPorUsuarioNivel(request.getNombre(), request.getApp(), 2);

		nivel2.forEach(menu -> {
			MenuResponse menuResponse = createMenuResponse(menu);

			response.getChildren().forEach(menuPadre -> {
				if (menu.getPadre().getId().equals(menuPadre.getId())) {
					if (menuPadre.getChildren() == null) {
						menuPadre.setChildren(new ArrayList<>());
						menuPadre.getChildren().add(menuResponse);
					} else if (!menuPadre.getChildren().contains(menuResponse)) {
						menuPadre.getChildren().add(menuResponse);
					}
				}
			});
		});

		List<Menu> nivel3 = dao.listarMenuPorUsuarioNivel(request.getNombre(), request.getApp(), 3);

		nivel3.forEach(menu -> {

			MenuResponse menuResponse = createMenuResponse(menu);

			response.getChildren().forEach(menu1 -> {

				if (menu1.getChildren() != null) {

					menu1.getChildren().forEach(menuPadre -> {

						if (menu.getPadre().getId().equals(menuPadre.getId())) {
							if (menuPadre.getChildren() == null) {
								menuPadre.setChildren(new ArrayList<>());
								menuPadre.getChildren().add(menuResponse);
							} else if (!menuPadre.getChildren().contains(menuResponse)) {
								menuPadre.getChildren().add(menuResponse);
							}
						}
					});
				}
			});
		});

		response.setId("applications");
		response.setTitle("Menu Principal");
		response.setTranslate("NAV.APPLICATIONS");
		response.setType("group");

		System.out.println("FIN LISTAR");
		responseList.add(response);
		return responseList;
	}

	MenuResponse createMenuResponse(Menu menu) {

		MenuResponse menuResponse = new MenuResponse();
		menuResponse.setIdMenu(menu.getIdMenu());
		menuResponse.setId(menu.getId());
		menuResponse.setTitle(menu.getTitle());
		menuResponse.setType(menu.getType());
		menuResponse.setIcon(menu.getIcon());
		menuResponse.setUrl(menu.getUrl());

		return menuResponse;
	}

}
