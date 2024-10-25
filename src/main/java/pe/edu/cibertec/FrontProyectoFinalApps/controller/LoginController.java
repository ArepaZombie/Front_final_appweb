package pe.edu.cibertec.FrontProyectoFinalApps.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.FrontProyectoFinalApps.client.AutenticacionClient;
import pe.edu.cibertec.FrontProyectoFinalApps.dto.LoginRequestDTO;
import pe.edu.cibertec.FrontProyectoFinalApps.dto.LoginResponseDTO;
import pe.edu.cibertec.FrontProyectoFinalApps.viewmodel.LoginModel;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequestMapping("/")
public class LoginController {

    @Autowired
    private WebClient webClientAutenticacion;

    @Autowired
  AutenticacionClient autenticacionClient;

    @GetMapping("/")
    public String inicio(Model model) {
        LoginModel loginModel = new LoginModel("00", "", "");
        model.addAttribute("loginModel", loginModel);
        return "inicio";
    }

    @PostMapping("/autenticar-wc")
    public String autenticar(@RequestParam("codigoIntegrante") String codigoIntegrante,
                             @RequestParam("password") String password,
                             Model model) {

        // Validar campos de entrada
        if (codigoIntegrante == null || codigoIntegrante.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            model.addAttribute("loginModel", new LoginModel("01", "Error: Debe completar correctamente sus credenciales", ""));
            return "inicio";
        }

        try {
            // Invocar servicio de autenticación
            LoginRequestDTO loginRequestDTO = new LoginRequestDTO(codigoIntegrante, password);
            Mono<LoginResponseDTO> monoLoginResponseDTO = webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class);

            // Recuperar resultado en modo bloqueante (Sincrónico)
            LoginResponseDTO loginResponseDTO = monoLoginResponseDTO.block();

            // Verificar resultado y redirigir según el código
            String codigo = loginResponseDTO.codigo();
            LoginModel loginModel;

            if ("00".equals(codigo)) {
                loginModel = new LoginModel("00", "", loginResponseDTO.nombreUsuario());
                model.addAttribute("loginModel", loginModel);
                return listarIntegrantes(model);
            } else if ("01".equals(codigo)) {
                loginModel = new LoginModel("02", "Error: Usuario no encontrado", "");
            } else {
                loginModel = new LoginModel("02", "Error: Autenticación fallida", "");
            }
            model.addAttribute("loginModel", loginModel);
            return "inicio";

        } catch (Exception e) {
            model.addAttribute("loginModel", new LoginModel("99", "Error: Ocurrió un problema en la autenticación", ""));
            System.err.println("Error de autenticación: " + e.getMessage());
            return "inicio";
        }
    }

    @PostMapping("/autenticar")
    public String autenticarFeign(@RequestParam("codigoIntegrante") String codigoIntegrante,
                                  @RequestParam("password") String password,
                                  Model model){
      LoginModel loginModel;
      // Validar campos de entrada
      if (codigoIntegrante == null || codigoIntegrante.trim().isEmpty() ||
        password == null || password.trim().isEmpty()) {
        model.addAttribute("loginModel", new LoginModel("01", "Error: Debe completar correctamente sus credenciales", ""));
        return "inicio";
      }

      try{
        LoginRequestDTO request = new LoginRequestDTO(codigoIntegrante, password);

        ResponseEntity<LoginResponseDTO> response = autenticacionClient.login(request);

        if(response.getStatusCode().is2xxSuccessful()){
          LoginResponseDTO loginResponseDTO = response.getBody();
          loginModel = new LoginModel("00", "", loginResponseDTO.nombreUsuario());
          model.addAttribute("loginModel", loginModel);
          return listarIntegrantes(model);
        }
        else {
          loginModel = new LoginModel("02", "Error: Autenticación fallida", "");
          return "inicio";
        }
      }catch (Exception e){
        model.addAttribute("loginModel", new LoginModel("99", "Error: Ocurrió un problema en la autenticación", ""));
        System.err.println("Error de autenticación: " + e.getMessage());
        return "inicio";
      }

    }

    public String listarIntegrantes(Model model) {
        // Llama al servicio backend para obtener la lista de integrantes
        Mono<List> integrantesMono = webClientAutenticacion.get()
                .uri("/listar-integrantes")
                .retrieve()
                .bodyToMono(List.class);

        // Recupera la lista en modo bloqueante
        List<String> integrantesList = integrantesMono.block();

        // Agrega la lista al modelo para que Thymeleaf la use en la vista
        model.addAttribute("integrantesList", integrantesList);
        return "principal";  // Nombre del template HTML
    }

    @GetMapping("/cerrar-sesion")
    public String cerrarSesion(Model model) {
        // Limpiar la sesión (aquí puedes agregar lógica para invalidar la sesión si es necesario)
        model.addAttribute("loginModel", new LoginModel("00", "", ""));
        return "inicio"; // Redirigir a la página de inicio
    }


}


