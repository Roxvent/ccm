package cl.intelidata.security.controller;

import cl.intelidata.security.model.api.AuthDTO;
import cl.intelidata.security.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1")
public class ProfileController {

    @Autowired
    private IUsuarioService usuarioService;

    @GetMapping("/profile")
    public ResponseEntity<AuthDTO> getUserProfile(Principal principal) {
        if (principal == null) {
            // This should not be reached if Spring Security is configured correctly
            return ResponseEntity.status(401).build();
        }

        // Reuse the existing service method to get user identification data
        AuthDTO authDTO = usuarioService.getIdentification(principal.getName());

        return ResponseEntity.ok(authDTO);
    }

}
