package cl.intelidata.security.controller;

import cl.intelidata.security.dto.AuthenticationRequest;
import cl.intelidata.security.dto.AzureRedirectResponse;
import cl.intelidata.security.service.IUsuarioService;
import cl.intelidata.security.service.IEmpresaService;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/external")
public class ExternalController {

    @Autowired
    IUsuarioService service;

    @Autowired
    IEmpresaService empresaService;

    @GetMapping(value = "/identification/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findIdentification(@PathVariable("username") String username) {
        return service.findIdentification(username);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request, HttpServletResponse response) {
        try {
            String token = service.authenticate(request.getUsername(), request.getPassword());

            Cookie cookie = new Cookie("jwttoken", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true); // Only send over HTTPS
            cookie.setPath("/");

            if (request.isRememberMe()) {
                // 8 hours for 'remember me'
                cookie.setMaxAge(8 * 60 * 60);
            } else {
                // Session cookie, expires when the browser is closed
                cookie.setMaxAge(-1);
            }

            response.addCookie(cookie);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping(value = "/azure-login/{idEmpresa}", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<?> azureLogin(@PathVariable("idEmpresa") Long idEmpresa) {
        ResponseEntity<?> responseEntity = empresaService.getAzureLoginUrl(idEmpresa);
        if (responseEntity.getBody() instanceof AzureRedirectResponse) {
            AzureRedirectResponse redirectResponse = (AzureRedirectResponse) responseEntity.getBody();
            if (redirectResponse != null && redirectResponse.getRedirectUrl() != null) {
                String url = redirectResponse.getRedirectUrl() + "&state=" + idEmpresa;
                return ResponseEntity.ok(new AzureRedirectResponse(url));
            }
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not construct Azure redirect URL.");
    }

        @GetMapping(value = "/azure-callback")
    public void azureCallback(@RequestParam("code") String code, @RequestParam("state") String state, HttpServletResponse httpResponse) {
        try {
            String token = service.handleAzureCallback(code, state);

            Cookie cookie = new Cookie("jwttoken", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true); // Solo enviar a través de HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(8 * 60 * 60); // 8 horas
            httpResponse.addCookie(cookie);

            // Redirigir a la página de perfil del usuario después de un inicio de sesión exitoso
            httpResponse.sendRedirect("/ccm-security/user-profile.html");
        } catch (Exception e) {
            // Manejar la excepción, por ejemplo, redirigiendo a una página de error
            try {
                                String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString());
                httpResponse.sendRedirect("/ccm-security/error.html?message=" + errorMessage);
            } catch (java.io.IOException ex) {
                // Fallback si la redirección falla
            }
        }
    }
}
