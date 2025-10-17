package pe.edu.upeu.epp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.epp.dto.response.EstadoEppResponseDTO;
import pe.edu.upeu.epp.service.EstadoEppService;

import java.util.List;

/**
 * Controller REST para gestión de Estados EPP.
 * Endpoints de solo lectura - Los estados son datos maestros.
 *
 * Endpoints disponibles:
 * - GET /api/v1/estados-epp - Listar todos
 * - GET /api/v1/estados-epp/permite-uso - Listar estados utilizables
 * - GET /api/v1/estados-epp/{id} - Obtener por ID
 *
 * @author Sistema EPP
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/estados-epp")
@RequiredArgsConstructor
@Tag(name = "Estados EPP", description = "Gestión de estados de Equipos de Protección Personal")
@SecurityRequirement(name = "Bearer Authentication")
public class EstadoEppController {

    private final EstadoEppService estadoEppService;

    /**
     * Listar todos los estados EPP disponibles.
     * Accesible por todos los roles autenticados.
     *
     * Retorna información de todos los estados configurados en el sistema:
     * - NUEVO
     * - EN_STOCK
     * - USADO
     * - DAÑADO
     * - EN_REPARACION
     * - OBSOLETO
     * - ENTREGADO
     * - BAJA
     *
     * @return Lista de todos los estados EPP
     */
    @GetMapping
    @Operation(
            summary = "Listar todos los estados",
            description = "Retorna la lista completa de estados EPP disponibles en el sistema. " +
                    "Cada estado incluye su ID, nombre, descripción, si permite uso y color para UI."
    )
    public ResponseEntity<List<EstadoEppResponseDTO>> listarTodos() {
        List<EstadoEppResponseDTO> response = estadoEppService.listarTodos();
        return ResponseEntity.ok(response);
    }

    /**
     * Listar solo estados que permiten uso.
     * Útil para combos/selects en formularios de inventario.
     *
     * Retorna solo estados con permiteUso = true:
     * - NUEVO
     * - EN_STOCK
     * - USADO
     * - ENTREGADO
     *
     * Excluye estados que no permiten uso:
     * - DAÑADO
     * - EN_REPARACION
     * - OBSOLETO
     * - BAJA
     *
     * @return Lista de estados utilizables
     */
    @GetMapping("/permite-uso")
    @Operation(
            summary = "Listar estados que permiten uso",
            description = "Retorna solo los estados que tienen permiteUso = true. " +
                    "Son los estados válidos para inventarios activos y entregas a trabajadores."
    )
    public ResponseEntity<List<EstadoEppResponseDTO>> listarPermiteUso() {
        List<EstadoEppResponseDTO> response = estadoEppService.listarPermiteUso();
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener un estado específico por su ID.
     * Accesible por todos los roles autenticados.
     *
     * @param id ID del estado EPP
     * @return Datos completos del estado
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener estado por ID",
            description = "Retorna los detalles completos de un estado EPP específico"
    )
    public ResponseEntity<EstadoEppResponseDTO> obtenerPorId(
            @Parameter(description = "ID del estado EPP", required = true, example = "1")
            @PathVariable Integer id) {
        EstadoEppResponseDTO response = estadoEppService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener un estado por su nombre.
     * Endpoint opcional, útil para integraciones.
     *
     * @param nombre Nombre del estado (ej: NUEVO, USADO, DAÑADO)
     * @return Datos del estado
     */
    @GetMapping("/nombre/{nombre}")
    @Operation(
            summary = "Obtener estado por nombre",
            description = "Busca un estado por su nombre exacto. Útil para validaciones e integraciones."
    )
    public ResponseEntity<EstadoEppResponseDTO> obtenerPorNombre(
            @Parameter(description = "Nombre del estado", required = true, example = "NUEVO")
            @PathVariable String nombre) {
        EstadoEppResponseDTO response = estadoEppService.obtenerPorNombre(nombre);
        return ResponseEntity.ok(response);
    }
}