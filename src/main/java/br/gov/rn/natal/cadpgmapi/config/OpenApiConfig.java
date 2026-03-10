package br.gov.rn.natal.cadpgmapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestão de Servidores - PGM")
                        .version("v1.0.0")
                        .description("Backend RESTful para o banco cad_serv_pgm_db")
                        .contact(new Contact().name("Arquiteto de Software").email("arquitetura@example.com")
                        )
                );
    }
}
