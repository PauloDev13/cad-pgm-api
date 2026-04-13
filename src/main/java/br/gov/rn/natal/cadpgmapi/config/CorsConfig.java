package br.gov.rn.natal.cadpgmapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplica o CORS apenas aos endpoints da API
                .allowedOrigins("http://localhost:4200") // O endereço exato do Angular
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS") // Métodos permitidos
                .allowedHeaders("*") // Permite o envio de qualquer header (como tokens JWT)
                .allowCredentials(true); // Necessário se for usar cookies ou autenticação baseada em sessão
    }
}
