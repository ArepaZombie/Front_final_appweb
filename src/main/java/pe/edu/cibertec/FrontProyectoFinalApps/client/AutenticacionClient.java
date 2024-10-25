package pe.edu.cibertec.FrontProyectoFinalApps.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pe.edu.cibertec.FrontProyectoFinalApps.config.AutenticacionFeingConfig;
import pe.edu.cibertec.FrontProyectoFinalApps.dto.LoginRequestDTO;
import pe.edu.cibertec.FrontProyectoFinalApps.dto.LoginResponseDTO;
import pe.edu.cibertec.FrontProyectoFinalApps.dto.LogoutRequestDTO;
import pe.edu.cibertec.FrontProyectoFinalApps.dto.LogoutResponseDTO;

@FeignClient(name = "autenticacion", url = "https://back-proyecto-final.azurewebsites.net/autenticacion", configuration = AutenticacionFeingConfig.class)
public interface AutenticacionClient {

    @PostMapping("/logout")
    ResponseEntity<LogoutResponseDTO> logout(@RequestBody LogoutRequestDTO logoutRequestDTO);

    @PostMapping("/login")
    ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO);

}
