package br.gov.rn.natal.cadpgmapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(caffeineCacheBuilder());

        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                // Define que o cache expira 12 horas após ter sido criado/atualizado.
                // Isso garante que se algum evento falhar, o cache não ficará defasado para sempre.
                .expireAfterWrite(12, TimeUnit.HOURS)

                // Define o tamanho máximo de registros por cache (protege a memória RAM).
                // Se chegar no limite, o Caffeine apaga os menos usados automaticamente.
                .maximumSize(5000)

                // Opcional: Registra métricas de uso (Hits/Misses)
                .recordStats();
    }
}
