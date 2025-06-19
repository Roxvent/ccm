package cl.intelidata.security.controller;

import cl.intelidata.security.service.IUsuarioService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/external")
public class ExternalController {

    @Autowired
    IUsuarioService service;

    @GetMapping(value = "/identification/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findIdentification(@PathVariable("username") String username) {
        return service.findIdentification(username);
    }
}
