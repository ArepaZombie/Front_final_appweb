package pe.edu.cibertec.FrontProyectoFinalApps.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.FrontProyectoFinalApps.client.AutenticacionClient;
import pe.edu.cibertec.FrontProyectoFinalApps.dto.LoginRequestDTO;
import pe.edu.cibertec.FrontProyectoFinalApps.dto.LoginResponseDTO;
import pe.edu.cibertec.FrontProyectoFinalApps.dto.LogoutRequestDTO;
import pe.edu.cibertec.FrontProyectoFinalApps.dto.LogoutResponseDTO;
import pe.edu.cibertec.FrontProyectoFinalApps.viewmodel.LoginModel;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:8080")
public class LoginControllerAsync {

    @Autowired
    WebClient webClientAutenticacion;

    @Autowired
    AutenticacionClient autenticacionClient;


    @PostMapping("/autenticar-async")
    public Mono<LoginResponseDTO> autenticar(@RequestBody LoginRequestDTO loginRequestDTO) {

        // validar campos de entrada
        if(loginRequestDTO.codigoIntegrante() == null || loginRequestDTO.codigoIntegrante().trim().length() == 0 ||
            loginRequestDTO.password() == null || loginRequestDTO.password().trim().length() == 0) {

            return Mono.just(new LoginResponseDTO("01", "Error: Debe completar correctamente sus credenciales",  "", "", ""));

        }

        try {

            // consumir servicio de autenticación (Del Backend)
            return webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class)
                    .flatMap(response -> {

                        if(response.codigo().equals("00")) {
                            return Mono.just(new LoginResponseDTO("00", "", response.nombreUsuario(), response.correoUsuario(), response.codigoIntegrante()));
                        } else {
                            return Mono.just(new LoginResponseDTO("02", "Error: Autenticación fallida",  "", "", ""));
                        }


                    });

        } catch(Exception e) {

            System.out.println(e.getMessage());
            return Mono.just(new LoginResponseDTO("99", "Error: Ocurrió un problema en la autenticación", ", ", "", ""));

        }
    }
    @PostMapping("/logout-async")
    public Mono<LogoutResponseDTO> cerrarSesion(@RequestBody LogoutRequestDTO logoutRequestDTO) {
        try {
            return webClientAutenticacion.post()
                    .uri("/logout")
                    .body(Mono.just(logoutRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class)
                    .flatMap(response -> {
                        if (response.codigo().equals("00")) {
                            return Mono.just(new LogoutResponseDTO("00", "Cierre de sesión exitoso"));
                        } else {
                            return Mono.just(new LogoutResponseDTO("02", "Error al cerrar sesión"));
                        }
                    });
        } catch (Exception e) {
            return Mono.just(new LogoutResponseDTO("99", "Error en el proceso de cierre de sesión"));
        }
    }
    @PostMapping("/logout-feign")
    public Mono<LogoutResponseDTO> cerrarSesionFeign(@RequestBody LogoutRequestDTO logoutRequestDTO) {
        try {

            ResponseEntity<LogoutResponseDTO> responseEntity = autenticacionClient.logout(logoutRequestDTO);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {

                LogoutResponseDTO logoutResponseDTO = responseEntity.getBody();

                if(logoutResponseDTO.codigo().equals("00")){
                    return Mono.just(new LogoutResponseDTO("00", "Cierre de sesión exitoso"));
                }else {
                    return Mono.just(new LogoutResponseDTO("02", "Error al cerrar sesión"));
                }


            } else {
                return Mono.just(new LogoutResponseDTO("99", "Error: Ocurrió un problema http"));
            }

        } catch (Exception e) {
            return Mono.just(new LogoutResponseDTO("99", "Error en el proceso de cierre de sesión"));
        }
    }

}
