package pe.edu.cibertec.FrontProyectoFinalApps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FrontProyectoFinalAppsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrontProyectoFinalAppsApplication.class, args);
	}

}
