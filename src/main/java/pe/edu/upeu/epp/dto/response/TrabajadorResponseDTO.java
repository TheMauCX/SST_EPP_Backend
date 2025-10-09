package pe.edu.upeu.epp.dto.response;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.epp.entity.Trabajador.EstadoTrabajador;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrabajadorResponseDTO {

    private Integer trabajadorId;
    private String dni;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private String codigoQrPhotocheck;
    private Integer areaId;
    private String areaNombre;
    private String puesto;
    private java.time.LocalDate fechaIngreso;
    private String telefono;
    private String email;
    private EstadoTrabajador estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}