package br.gov.rn.natal.cadpgmapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * CORREÇÃO DE BUG (item 4 do diagnóstico):
     * A anotação @Async usada no AuditLogListener não tinha NENHUM efeito, porque não
     * existia @EnableAsync em lugar nenhum do projeto. Consequências do bug:
     *   1) A auditoria era gravada de forma SÍNCRONA e DENTRO da MESMA transação do
     *      método auditado -> se a operação de negócio fizesse rollback, o log também
     *      era desfeito (contrariando o requisito de "auditar até em falhas").
     *   2) Uma lentidão/falha na gravação do log travava ou derrubava a operação toda.

     * O @EnableAsync acima liga o processamento assíncrono do Spring.
     * O bean abaixo define um EXECUTOR DEDICADO (com pool de threads) em vez do
     * SimpleAsyncTaskExecutor padrão, que criaria uma thread nova a cada evento
     * (sem reuso e com risco de estourar o SO em picos de uso).
     */
    @Bean(name = "asyncExecutor")
    public TaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);       // fila de eventos aguardando execução
        executor.setThreadNamePrefix("async-"); // facilita identificar a thread em logs/dumps
        executor.initialize();
        return executor;
    }
}