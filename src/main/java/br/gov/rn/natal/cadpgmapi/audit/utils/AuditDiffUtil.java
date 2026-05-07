package br.gov.rn.natal.cadpgmapi.audit.utils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class AuditDiffUtil {
    // Construtor privado: impede que alguém faça "new AuditDiffUtil()"
    private AuditDiffUtil() {
        throw new IllegalStateException("Classe utilitária não deve ser instanciada");
    }

    // Compara dois objetos dinamicamente e retorna um texto com as diferenças.
    public static String generateDiff(Object oldObj, Object newObj) {
        if (oldObj == null || newObj == null) return "";

        StringBuilder diffBuilder = new StringBuilder();

        // Pega todos os atributos da classe (do DTO)
        Field[] fields = oldObj.getClass().getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();

            if (fieldName.equalsIgnoreCase("id")
                    || fieldName.equalsIgnoreCase("password")
                    || fieldName.equalsIgnoreCase("senha")
            ) {
                continue;
            }

            // Permite ler atributos privados
            field.setAccessible(true);
            try {
                Object oldValue = field.get(oldObj);
                Object newValue = field.get(newObj);

                // Se os valores forem diferentes, registramos no log
                if (!Objects.equals(oldValue, newValue)) {
                    // Formata para não imprimir "null", mas sim "Vazio"
                    String oldStr = formatValue(oldValue);
                    String newStr = formatValue(newValue);

                    diffBuilder.append("[")
                            .append(field.getName()) // Ex: "emailInstitucional"
                            .append(": de '").append(oldStr)
                            .append("' para '").append(newStr)
                            .append("'] ");
                }
            } catch (IllegalAccessException e) {
                // Ignora campos que o Java de segurança não deixa ler
            }
        }

        return diffBuilder.toString().trim();
    }

    // MÉTODOS AUXILIARES PRIVADOS

    //Define como o valor será impresso no log. Trata listas e objetos únicos.
    private static String formatValue(Object obj) {
        if (obj == null) return "Vazio";

        // Se o atributo for uma Lista ou Set (ex: sistemas, procuradores)
        if (obj instanceof Collection<?> collection) {
            if (collection.isEmpty()) return "Vazio";

            // Extrai o nome de cada item da lista e junta com vírgulas
            String joinedValues = collection.stream()
                    .map(AuditDiffUtil::extractDisplayName)
                    .collect(Collectors.joining(", "));

            return "[" + joinedValues + "]";
        }

        // Se for um objeto único
        String extracted = extractDisplayName(obj);
        return extracted.isBlank() ? "Vazio" : extracted;
    }


    // O "Smart Getter": Tenta extrair um campo legível (nome, descrição) do DTO.
    private static String extractDisplayName(Object obj) {
        if (obj == null) return "Vazio";

        // Se for um tipo nativo do Java (String, Integer, LocalDate, Enum) retorna o toString original
        if (obj.getClass().getPackageName().startsWith("java.") || obj.getClass().isEnum()) {
            return obj.toString();
        }

        // Se for um DTO, tenta buscar o valor de campos descritivos comuns
        String[] commonNameFields = {"nome", "descricao", "email", "userName", "username", "titulo", "name"};

        for (String targetField : commonNameFields) {
            try {
                Field field = obj.getClass().getDeclaredField(targetField);
                field.setAccessible(true);
                Object val = field.get(obj);
                if (val != null) {
                    return val.toString(); // Retorna o valor do "nome" ou "descricao"
                }
            } catch (Exception e) {
                // Se o DTO não tiver esse campo, o catch ignora e o loop tenta o próximo
            }
        }

        // Fallback: se não achar nenhum campo legível, imprime o objeto padrão
        return obj.toString();
    }
}