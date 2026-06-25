package pe.edu.upeu.epp.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventarioAreaUpdateDTO {

    private Integer estadoId;

    @Size(max = 100)
    private String ubicacion;
}
