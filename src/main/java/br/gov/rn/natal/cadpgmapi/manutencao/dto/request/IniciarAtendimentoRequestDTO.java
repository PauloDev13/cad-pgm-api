package br.gov.rn.natal.cadpgmapi.manutencao.dto.request;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.TipoExecutorChamado;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para início do atendimento do chamado")
public record IniciarAtendimentoRequestDTO(

        @NotNull(message = "O tipo de executor é obrigatório ao iniciar o atendimento")
        @Schema(description = "Tipo de executor da demanda", example = "EQUIPE_PROPRIA", requiredMode = Schema.RequiredMode.REQUIRED)
        TipoExecutorChamado tipoExecutor,
        @Size(max = 255, message = "A identificação do executor deve ter no máximo 255 caracteres")
        @Schema(description = "Identificação/descrição do executor caso a opção seja OUTROS", example = "Empresa Terceirizada Alpha")
        String outroExecutor,
        @Schema(description = "Observação opcional ao iniciar a execução", example = "Iniciado reparo técnico no local")
        @Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres")
        String observacao
) {
        @AssertTrue(message = "Informe quem será o executor da demanda quando a opção selecionada for 'Outros'")
        public boolean isOutroExecutorValido() {
                if (tipoExecutor == TipoExecutorChamado.OUTROS) {
                        return outroExecutor != null && !outroExecutor.trim().isEmpty();
                }
                return true;
        }
}
