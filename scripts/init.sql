-- ===================================================================
-- SCRIPT DE DATOS SINTÉTICOS COMPLETO (CORREGIDO v3)
-- Plataforma de Gestión de EPP - UPEU
-- Fecha: 2025-10-22
-- ===================================================================
-- IMPORTANTE: Ejecutar DESPUÉS de que Hibernate cree las tablas
-- Este script puebla la BD con datos realistas de prueba
-- ===================================================================

SET search_path TO epp;

-- ===================================================================
-- 1. ROLES DEL SISTEMA
-- ===================================================================
INSERT INTO epp.rol (nombre_rol, descripcion) VALUES
                                                  ('ADMINISTRADOR_SISTEMA', 'Gestión completa del sistema, usuarios y catálogos'),
                                                  ('SUPERVISOR_SST', 'Gestión de inventario central, aprobaciones y reportes'),
                                                  ('JEFE_AREA', 'Gestión de personal y entregas en su área asignada'),
                                                  ('COORDINADOR_SST', 'Acceso de solo lectura a reportes y dashboards')
ON CONFLICT (nombre_rol) DO NOTHING;

-- ===================================================================
-- 2. ESTADOS DE EPP
-- ===================================================================
INSERT INTO epp.estado_epp (nombre, descripcion, permite_uso, color_hex) VALUES
                                                                             ('EN_STOCK', 'Disponible en inventario', FALSE, '#4CAF50'),
                                                                             ('ENTREGADO', 'Asignado a trabajador', TRUE, '#2196F3'),
                                                                             ('EN_MANTENIMIENTO', 'Requiere reparación o limpieza', FALSE, '#FF9800'),
                                                                             ('BAJA', 'Dado de baja permanentemente', FALSE, '#F44336'),
                                                                             ('EXTRAVIADO', 'Reportado como perdido', FALSE, '#9E9E9E')
ON CONFLICT (nombre) DO NOTHING;

-- ===================================================================
-- 3. ÁREAS DE LA EMPRESA (10 áreas)
-- ===================================================================
INSERT INTO epp.area (nombre_area, codigo_area, descripcion, ubicacion, activo, fecha_creacion) VALUES
                                                                                                    ('Almacén Central', 'ALM-001', 'Almacén principal de EPPs y materiales', 'Edificio A - Piso 1', TRUE, NOW()),
                                                                                                    ('Construcción - Zona Norte', 'CONS-N-001', 'Obras de construcción en zona norte', 'Campo - Zona Norte', TRUE, NOW()),
                                                                                                    ('Construcción - Zona Sur', 'CONS-S-001', 'Obras de construcción en zona sur', 'Campo - Zona Sur', TRUE, NOW()),
                                                                                                    ('Mantenimiento Eléctrico', 'MANT-E-001', 'Mantenimiento de sistemas eléctricos', 'Edificio B - Piso 2', TRUE, NOW()),
                                                                                                    ('Mantenimiento Mecánico', 'MANT-M-001', 'Mantenimiento de maquinaria pesada', 'Taller Principal', TRUE, NOW()),
                                                                                                    ('Seguridad y Vigilancia', 'SEG-001', 'Personal de seguridad patrimonial', 'Entrada Principal', TRUE, NOW()),
                                                                                                    ('Laboratorio Químico', 'LAB-Q-001', 'Análisis y pruebas químicas', 'Edificio C - Piso 3', TRUE, NOW()),
                                                                                                    ('Pintura Industrial', 'PINT-001', 'Aplicación de recubrimientos', 'Nave Industrial 2', TRUE, NOW()),
                                                                                                    ('Soldadura', 'SOLD-001', 'Trabajos de soldadura y corte', 'Taller de Soldadura', TRUE, NOW()),
                                                                                                    ('Administración', 'ADM-001', 'Personal administrativo y oficinas', 'Edificio Central', TRUE, NOW())
ON CONFLICT (nombre_area) DO NOTHING;

-- ===================================================================
-- 4. CATÁLOGO DE EPP (20 tipos diferentes)
-- ===================================================================
INSERT INTO epp.catalogo_epp (nombre_epp, codigo_identificacion, especificaciones_tecnicas, tipo_uso, vida_util_meses,
                              nivel_proteccion, marca, unidad_medida, activo, fecha_creacion, fecha_actualizacion) VALUES
-- EPPs DURABLES
('Casco de Seguridad Clase G', 'CAS-G-001', 'Casco dieléctrico clase G, resistencia 2200V, norma ANSI Z89.1', 'DURADERO', 36, 'Cabeza - Impactos y Eléctrico', 'LIBUS', 'UNI', TRUE, NOW(), NOW()),
('Casco de Seguridad Clase E', 'CAS-E-001', 'Casco dieléctrico clase E, resistencia 20000V, norma ANSI Z89.1', 'DURADERO', 36, 'Cabeza - Alto Voltaje', 'MSA', 'UNI', TRUE, NOW(), NOW()),
('Arnés de Seguridad Full Body', 'ARN-FB-001', 'Arnés de cuerpo completo con 4 anillos D, capacidad 140kg, norma ANSI Z359.11', 'DURADERO', 60, 'Caídas desde Altura', 'STELLPRO', 'UNI', TRUE, NOW(), NOW()),
('Arnés de Seguridad con Línea de Vida', 'ARN-LV-001', 'Arnés full body + línea de vida retráctil 3m, norma ANSI Z359', 'DURADERO', 48, 'Caídas - Trabajos en Altura', 'MILLER', 'UNI', TRUE, NOW(), NOW()),
('Zapatos de Seguridad Punta de Acero T-42', 'ZAP-PA-42', 'Botines dieléctricos punta de acero, suela antideslizante, norma ASTM F2413', 'DURADERO', 12, 'Pies - Impactos y Perforación', 'MANRIQUE', 'PAR', TRUE, NOW(), NOW()),
('Zapatos de Seguridad Punta de Acero T-40', 'ZAP-PA-40', 'Botines dieléctricos punta de acero, talla 40, norma ASTM F2413', 'DURADERO', 12, 'Pies - Impactos y Perforación', 'CAT', 'PAR', TRUE, NOW(), NOW()),
('Lentes de Seguridad Claros', 'LEN-CL-001', 'Lentes policarbonato con protección UV, antiempañante, norma ANSI Z87.1', 'DURADERO', 6, 'Ojos - Impactos y UV', 'STEELPRO', 'UNI', TRUE, NOW(), NOW()),
('Lentes de Seguridad para Soldadura', 'LEN-SO-001', 'Lentes con filtro shade 5 para soldadura, norma ANSI Z87.1', 'DURADERO', 6, 'Ojos - Radiación Soldadura', '3M', 'UNI', TRUE, NOW(), NOW()),
('Respirador Media Cara con Filtros', 'RES-MC-001', 'Respirador elastomérico reutilizable con filtros P100, norma NIOSH', 'DURADERO', 24, 'Vías Respiratorias - Partículas', 'MOLDEX', 'UNI', TRUE, NOW(), NOW()),
('Protector Facial Completo', 'PRO-FC-001', 'Careta facial policarbonato, ajustable, resistente a químicos', 'DURADERO', 12, 'Cara - Salpicaduras Químicas', 'MSA', 'UNI', TRUE, NOW(), NOW()),
-- EPPs CONSUMIBLES
('Guantes de Nitrilo Talla M (Caja x100)', 'GUA-NIT-M', 'Guantes desechables nitrilo azul, talla M, libre de látex', 'CONSUMIBLE', NULL, 'Manos - Químicos Leves', '3M', 'CAJA', TRUE, NOW(), NOW()),
('Guantes de Nitrilo Talla L (Caja x100)', 'GUA-NIT-L', 'Guantes desechables nitrilo azul, talla L, libre de látex', 'CONSUMIBLE', NULL, 'Manos - Químicos Leves', '3M', 'CAJA', TRUE, NOW(), NOW()),
('Guantes de Cuero Reforzado (Par)', 'GUA-CUE-001', 'Guantes carnaza reforzados para trabajo pesado', 'CONSUMIBLE', NULL, 'Manos - Trabajo Pesado', 'TRUPER', 'PAR', TRUE, NOW(), NOW()),
('Guantes para Soldadura (Par)', 'GUA-SOL-001', 'Guantes cuero alta temperatura, manga larga 35cm', 'CONSUMIBLE', NULL, 'Manos - Altas Temperaturas', 'LINCOLN', 'PAR', TRUE, NOW(), NOW()),
('Mascarilla N95 (Unidad)', 'MAS-N95-001', 'Mascarilla respirador N95, filtración 95%, uso único', 'CONSUMIBLE', NULL, 'Vías Respiratorias - Partículas', '3M', 'UNI', TRUE, NOW(), NOW()),
('Tapones Auditivos de Espuma (Par)', 'TAP-ESP-001', 'Tapones auditivos espuma PVC, reducción 32dB, desechables', 'CONSUMIBLE', NULL, 'Oídos - Ruido Industrial', 'HOWARD LEIGHT', 'PAR', TRUE, NOW(), NOW()),
('Tapones Auditivos con Cordón (Par)', 'TAP-COR-001', 'Tapones silicona con cordón, reutilizables, reducción 27dB', 'CONSUMIBLE', NULL, 'Oídos - Ruido Moderado', 'HONEYWELL', 'PAR', TRUE, NOW(), NOW()),
('Protector Solar Factor 50+ (120ml)', 'PRO-SOL-001', 'Bloqueador solar industrial factor 50+, resistente al agua', 'CONSUMIBLE', NULL, 'Piel - Radiación UV', 'LA ROCHE-POSAY', 'UNI', TRUE, NOW(), NOW()),
('Overol Tyvek Desechable', 'OVE-TYV-001', 'Traje protector Tyvek, resistente a químicos y partículas', 'CONSUMIBLE', NULL, 'Cuerpo - Químicos y Partículas', 'DUPONT', 'UNI', TRUE, NOW(), NOW()),
('Mandil de Cuero para Soldadura', 'MAN-CUE-001', 'Mandil cuero cromo, resistente a chispas y calor', 'CONSUMIBLE', NULL, 'Cuerpo - Soldadura', 'LINCOLN', 'UNI', TRUE, NOW(), NOW())
ON CONFLICT (codigo_identificacion) DO NOTHING;

-- ===================================================================
-- 5. TRABAJADORES (30 trabajadores)
-- ===================================================================
INSERT INTO epp.trabajador (dni, codigo_qr_photocheck, nombres, apellidos, area_id, puesto, fecha_ingreso, telefono, email, estado, fecha_creacion, fecha_actualizacion) VALUES
                                                                                                                                                                             ('00000001', 'QR-20250101-000001', 'Carlos', 'Administrador', 10, 'Administrador de Sistema', '2024-01-15', '987654321', 'admin@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('12345678', 'QR-20250102-000002', 'María', 'Supervisor', 1, 'Supervisor de SST', '2024-02-01', '987654322', 'maria.supervisor@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('23456789', 'QR-20250103-000003', 'Juan', 'Coordinador', 10, 'Coordinador SST', '2024-02-15', '987654323', 'juan.coordinador@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('34567890', 'QR-20250104-000004', 'Ana', 'Rodríguez', 1, 'Jefe de Almacén', '2024-03-01', '987654324', 'ana.rodriguez@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('45678901', 'QR-20250105-000005', 'Pedro', 'García', 2, 'Jefe de Obra', '2024-03-15', '987654325', 'pedro.garcia@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('56789012', 'QR-20250106-000006', 'Luis', 'Martínez', 2, 'Maestro de Obra', '2024-04-01', '987654326', 'luis.martinez@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('67890123', 'QR-20250107-000007', 'Roberto', 'López', 2, 'Operario de Construcción', '2024-04-15', '987654327', 'roberto.lopez@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('78901234', 'QR-20250108-000008', 'Miguel', 'Fernández', 2, 'Operario de Construcción', '2024-05-01', '987654328', 'miguel.fernandez@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('89012345', 'QR-20250109-000009', 'Jorge', 'Sánchez', 2, 'Ayudante de Obra', '2024-05-15', '987654329', 'jorge.sanchez@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('90123456', 'QR-20250110-000010', 'Carlos', 'Ramírez', 3, 'Jefe de Obra', '2024-06-01', '987654330', 'carlos.ramirez@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('01234567', 'QR-20250111-000011', 'Diego', 'Torres', 3, 'Maestro de Obra', '2024-06-15', '987654331', 'diego.torres@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('11234567', 'QR-20250112-000012', 'Fernando', 'Flores', 3, 'Operario de Construcción', '2024-07-01', '987654332', 'fernando.flores@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('21234567', 'QR-20250113-000013', 'Ricardo', 'Castro', 3, 'Operario de Construcción', '2024-07-15', '987654333', 'ricardo.castro@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('31234567', 'QR-20250114-000014', 'Andrés', 'Mendoza', 3, 'Ayudante de Obra', '2024-08-01', '987654334', 'andres.mendoza@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('41234567', 'QR-20250115-000015', 'Alberto', 'Vargas', 4, 'Jefe Eléctrico', '2024-08-15', '987654335', 'alberto.vargas@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('51234567', 'QR-20250116-000016', 'Raúl', 'Herrera', 4, 'Técnico Electricista', '2024-09-01', '987654336', 'raul.herrera@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('61234567', 'QR-20250117-000017', 'Oscar', 'Vega', 4, 'Técnico Electricista', '2024-09-15', '987654337', 'oscar.vega@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('71234567', 'QR-20250118-000018', 'Javier', 'Medina', 4, 'Ayudante Eléctrico', '2024-10-01', '987654338', 'javier.medina@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('81234567', 'QR-20250119-000019', 'Manuel', 'Rojas', 5, 'Jefe Mecánico', '2024-10-15', '987654339', 'manuel.rojas@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('91234567', 'QR-20250120-000020', 'Sergio', 'Paredes', 5, 'Mecánico Industrial', '2024-11-01', '987654340', 'sergio.paredes@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('10234567', 'QR-20250121-000021', 'Gustavo', 'Quispe', 5, 'Mecánico Industrial', '2024-11-15', '987654341', 'gustavo.quispe@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('20234567', 'QR-20250122-000022', 'Víctor', 'Chávez', 5, 'Ayudante Mecánico', '2024-12-01', '987654342', 'victor.chavez@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('30234567', 'QR-20250123-000023', 'Eduardo', 'Silva', 9, 'Maestro Soldador', '2024-12-15', '987654343', 'eduardo.silva@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('40234567', 'QR-20250124-000024', 'Héctor', 'Morales', 9, 'Soldador', '2025-01-01', '987654344', 'hector.morales@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('50234567', 'QR-20250125-000025', 'Daniel', 'Ramos', 9, 'Soldador', '2025-01-15', '987654345', 'daniel.ramos@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('60234567', 'QR-20250126-000026', 'Lucía', 'Campos', 7, 'Química Analista', '2025-02-01', '987654346', 'lucia.campos@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('70234567', 'QR-20250127-000027', 'Carmen', 'Navarro', 7, 'Asistente de Laboratorio', '2025-02-15', '987654347', 'carmen.navarro@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('80234567', 'QR-20250128-000028', 'Antonio', 'Córdova', 8, 'Jefe de Pintura', '2025-03-01', '987654348', 'antonio.cordova@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('90234567', 'QR-20250129-000029', 'Francisco', 'Aguilar', 8, 'Pintor Industrial', '2025-03-15', '987654349', 'francisco.aguilar@upeu.edu.pe', 'ACTIVO', NOW(), NOW()),
                                                                                                                                                                             ('10334567', 'QR-20250130-000030', 'Julio', 'Benavides', 6, 'Jefe de Seguridad', '2025-04-01', '987654350', 'julio.benavides@upeu.edu.pe', 'ACTIVO', NOW(), NOW())
ON CONFLICT (dni) DO NOTHING;

-- ===================================================================
-- 6. USUARIOS DEL SISTEMA (5 usuarios)
-- ===================================================================
INSERT INTO epp.usuario (nombre_usuario, contrasena_hash, email, trabajador_id, activo, intentos_fallidos, bloqueado_hasta, fecha_creacion) VALUES
                                                                                                                                                ('admin', '$2a$10$TkJlBC87P4JkcvHAn0UyHebwyTXGHL7iH6.LZq2vTyjrB4x6WjoQi', 'admin@upeu.edu.pe', 1, TRUE, 0, NULL, NOW()),
                                                                                                                                                ('supervisor', '$2a$10$TkJlBC87P4JkcvHAn0UyHebwyTXGHL7iH6.LZq2vTyjrB4x6WjoQi', 'maria.supervisor@upeu.edu.pe', 2, TRUE, 0, NULL, NOW()),
                                                                                                                                                ('jefe_zona_norte', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8iYLfkYvmH5wJjXJqYvqLqCQmW7rK', 'pedro.garcia@upeu.edu.pe', 5, TRUE, 0, NULL, NOW()),
                                                                                                                                                ('jefe_zona_sur', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8iYLfkYvmH5wJjXJqYvqLqCQmW7rK', 'carlos.ramirez@upeu.edu.pe', 10, TRUE, 0, NULL, NOW()),
                                                                                                                                                ('coordinador', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8iYLfkYvmH5wJjXJqYvqLqCQmW7rK', 'juan.coordinador@upeu.edu.pe', 3, TRUE, 0, NULL, NOW())
ON CONFLICT (nombre_usuario) DO NOTHING;

-- ===================================================================
-- 7. ASIGNACIÓN DE ROLES A USUARIOS
-- ===================================================================
INSERT INTO epp.usuario_rol (usuario_id, rol_id) VALUES
                                                     (1, 1), (2, 2), (3, 3), (4, 3), (5, 4)
ON CONFLICT (usuario_id, rol_id) DO NOTHING;

-- ===================================================================
-- 8. INVENTARIO CENTRAL
-- ===================================================================
INSERT INTO epp.inventario_central (epp_id, estado_id, cantidad_actual, cantidad_minima, cantidad_maxima,
                                    ubicacion_bodega, lote, fecha_adquisicion, costo_unitario, proveedor, fecha_vencimiento,
                                    observaciones, fecha_creacion, fecha_actualizacion, ultima_actualizacion) VALUES
                                                                                                                  (1, 1, 50, 10, 100, 'Estante A-01', 'LOT-CAS-G-2025-001', '2025-01-15', 45.50, 'Seguridad Industrial SAC', '2028-01-15', 'Stock inicial', NOW(), NOW(), NOW()),
                                                                                                                  (2, 1, 30, 8, 50, 'Estante A-02', 'LOT-CAS-E-2025-001', '2025-01-20', 85.00, 'Equipos de Protección Perú', '2028-01-20', 'Para alta tensión', NOW(), NOW(), NOW()),
                                                                                                                  (3, 1, 40, 10, 80, 'Estante B-01', 'LOT-ARN-FB-2025-001', '2025-02-01', 180.00, 'Protección Altura EIRL', '2030-02-01', 'Con certificación', NOW(), NOW(), NOW()),
                                                                                                                  (4, 1, 25, 5, 50, 'Estante B-02', 'LOT-ARN-LV-2025-001', '2025-02-10', 320.00, 'Seguridad Vertical SRL', '2029-02-10', 'Incluye línea de vida', NOW(), NOW(), NOW()),
                                                                                                                  (5, 1, 60, 15, 120, 'Estante C-01', 'LOT-ZAP-42-2025-001', '2025-03-01', 95.00, 'Calzado Industrial Lima', '2026-03-01', 'Talla 42', NOW(), NOW(), NOW()),
                                                                                                                  (6, 1, 55, 15, 120, 'Estante C-02', 'LOT-ZAP-40-2025-001', '2025-03-05', 95.00, 'Calzado Industrial Lima', '2026-03-05', 'Talla 40', NOW(), NOW(), NOW()),
                                                                                                                  (7, 1, 100, 20, 200, 'Estante D-01', 'LOT-LEN-CL-2025-001', '2025-03-15', 12.50, 'Óptica Industrial SAC', '2025-09-15', 'Uso general', NOW(), NOW(), NOW()),
                                                                                                                  (8, 1, 80, 15, 150, 'Estante D-02', 'LOT-LEN-SO-2025-001', '2025-03-20', 28.00, 'Equipos Soldadura Perú', '2025-09-20', 'Para soldadores', NOW(), NOW(), NOW()),
                                                                                                                  (9, 1, 35, 8, 70, 'Estante E-01', 'LOT-RES-MC-2025-001', '2025-04-01', 125.00, '3M Perú SAC', '2027-04-01', 'Con filtros P100', NOW(), NOW(), NOW()),
                                                                                                                  (10, 1, 45, 10, 90, 'Estante E-02', 'LOT-PRO-FC-2025-001', '2025-04-10', 35.00, 'Protección Química EIRL', '2026-04-10', 'Resistente a químicos', NOW(), NOW(), NOW()),
                                                                                                                  (11, 1, 150, 30, 300, 'Estante F-01', 'LOT-GNT-M-2025-001', '2025-05-01', 45.00, '3M Perú SAC', '2027-05-01', 'Caja x100 - Talla M', NOW(), NOW(), NOW()),
                                                                                                                  (12, 1, 140, 30, 300, 'Estante F-02', 'LOT-GNT-L-2025-001', '2025-05-05', 45.00, '3M Perú SAC', '2027-05-05', 'Caja x100 - Talla L', NOW(), NOW(), NOW()),
                                                                                                                  (13, 1, 200, 40, 400, 'Estante F-03', 'LOT-GUA-CUE-2025-001', '2025-05-10', 8.50, 'Ferretería Industrial', '2026-11-10', 'Guantes carnaza', NOW(), NOW(), NOW()),
                                                                                                                  (14, 1, 120, 25, 250, 'Estante F-04', 'LOT-GUA-SOL-2025-001', '2025-05-15', 22.00, 'Equipos Soldadura Perú', '2026-11-15', 'Alta temperatura', NOW(), NOW(), NOW()),
                                                                                                                  (15, 1, 500, 100, 1000, 'Estante G-01', 'LOT-MAS-N95-2025-001', '2025-06-01', 2.80, '3M Perú SAC', '2026-06-01', 'Uso único', NOW(), NOW(), NOW()),
                                                                                                                  (16, 1, 800, 150, 1500, 'Estante G-02', 'LOT-TAP-ESP-2025-001', '2025-06-05', 0.50, 'Protección Auditiva SAC', '2027-06-05', 'Tapones desechables', NOW(), NOW(), NOW()),
                                                                                                                  (17, 1, 300, 60, 600, 'Estante G-03', 'LOT-TAP-COR-2025-001', '2025-06-10', 1.20, 'Honeywell Perú', '2027-06-10', 'Reutilizables', NOW(), NOW(), NOW()),
                                                                                                                  (18, 1, 200, 40, 400, 'Estante H-01', 'LOT-PRO-SOL-2025-001', '2025-07-01', 18.50, 'Farmacia Industrial', '2026-07-01', 'Factor 50+', NOW(), NOW(), NOW()),
                                                                                                                  (19, 1, 180, 35, 350, 'Estante H-02', 'LOT-OVE-TYV-2025-001', '2025-07-10', 15.00, 'DuPont Perú', '2027-07-10', 'Overol Tyvek', NOW(), NOW(), NOW()),
                                                                                                                  (20, 1, 90, 20, 180, 'Estante H-03', 'LOT-MAN-CUE-2025-001', '2025-07-15', 35.00, 'Equipos Soldadura Perú', '2026-07-15', 'Mandil cuero', NOW(), NOW(), NOW())
ON CONFLICT ON CONSTRAINT uk_inventario_central_epp_lote_estado DO NOTHING;

-- ===================================================================
-- 9. INVENTARIO POR ÁREA
-- ===================================================================
INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, cantidad_actual, cantidad_minima, cantidad_maxima, ubicacion, ultima_actualizacion) VALUES
                                                                                                                                                     (1, 2, 2, 12, 5, 25, 'Almacén Obra Norte', NOW()),
                                                                                                                                                     (3, 2, 2, 10, 5, 20, 'Almacén Obra Norte', NOW()),
                                                                                                                                                     (5, 2, 2, 15, 8, 30, 'Almacén Obra Norte', NOW()),
                                                                                                                                                     (7, 2, 2, 20, 10, 40, 'Almacén Obra Norte', NOW()),
                                                                                                                                                     (11, 2, 2, 30, 15, 60, 'Almacén Obra Norte', NOW()),
                                                                                                                                                     (13, 2, 2, 50, 20, 100, 'Almacén Obra Norte', NOW()),
                                                                                                                                                     (1, 3, 2, 10, 5, 25, 'Almacén Obra Sur', NOW()),
                                                                                                                                                     (3, 3, 2, 8, 5, 20, 'Almacén Obra Sur', NOW()),
                                                                                                                                                     (6, 3, 2, 12, 8, 30, 'Almacén Obra Sur', NOW()),
                                                                                                                                                     (7, 3, 2, 18, 10, 40, 'Almacén Obra Sur', NOW()),
                                                                                                                                                     (12, 3, 2, 28, 15, 60, 'Almacén Obra Sur', NOW()),
                                                                                                                                                     (13, 3, 2, 45, 20, 100, 'Almacén Obra Sur', NOW()),
                                                                                                                                                     (2, 4, 2, 8, 3, 15, 'Taller Eléctrico', NOW()),
                                                                                                                                                     (5, 4, 2, 10, 5, 20, 'Taller Eléctrico', NOW()),
                                                                                                                                                     (7, 4, 2, 12, 6, 25, 'Taller Eléctrico', NOW()),
                                                                                                                                                     (11, 4, 2, 20, 10, 40, 'Taller Eléctrico', NOW()),
                                                                                                                                                     (16, 4, 2, 50, 25, 100, 'Taller Eléctrico', NOW()),
                                                                                                                                                     (1, 5, 2, 8, 4, 16, 'Taller Mecánico', NOW()),
                                                                                                                                                     (6, 5, 2, 10, 5, 20, 'Taller Mecánico', NOW()),
                                                                                                                                                     (7, 5, 2, 15, 8, 30, 'Taller Mecánico', NOW()),
                                                                                                                                                     (13, 5, 2, 40, 20, 80, 'Taller Mecánico', NOW()),
                                                                                                                                                     (16, 5, 2, 60, 30, 120, 'Taller Mecánico', NOW()),
                                                                                                                                                     (1, 9, 2, 6, 3, 12, 'Taller Soldadura', NOW()),
                                                                                                                                                     (8, 9, 2, 10, 5, 20, 'Taller Soldadura', NOW()),
                                                                                                                                                     (14, 9, 2, 25, 12, 50, 'Taller Soldadura', NOW()),
                                                                                                                                                     (20, 9, 2, 15, 8, 30, 'Taller Soldadura', NOW()),
                                                                                                                                                     (16, 9, 2, 40, 20, 80, 'Taller Soldadura', NOW()),
                                                                                                                                                     (10, 7, 2, 8, 4, 15, 'Laboratorio', NOW()),
                                                                                                                                                     (11, 7, 2, 25, 12, 50, 'Laboratorio', NOW()),
                                                                                                                                                     (19, 7, 2, 20, 10, 40, 'Laboratorio', NOW()),
                                                                                                                                                     (15, 7, 2, 50, 25, 100, 'Laboratorio', NOW())
ON CONFLICT ON CONSTRAINT uk_inventario_area_epp_area_estado DO NOTHING;

-- ===================================================================
-- 10. INSTANCIAS EPP
-- ===================================================================
INSERT INTO epp.instancia_epp (instancia_epp_id, epp_id, codigo_serie, estado_id, area_actual_id, trabajador_actual_id, fecha_adquisicion, fecha_vencimiento, lote, fecha_ultima_inspeccion, fecha_proxima_inspeccion, observaciones, fecha_creacion, fecha_actualizacion) VALUES
                                                                                                                                                                                                                                                                               (1, 1, 'CAS-G-001-0001', 2, 2, 6, '2025-01-15', '2028-01-15', 'LOT-CAS-G-2025-001', '2025-09-01', '2025-12-01', 'Asignado a construcción', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (2, 1, 'CAS-G-001-0002', 2, 2, 7, '2025-01-15', '2028-01-15', 'LOT-CAS-G-2025-001', '2025-09-01', '2025-12-01', 'Asignado a construcción', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (3, 1, 'CAS-G-001-0003', 2, 2, 8, '2025-01-15', '2028-01-15', 'LOT-CAS-G-2025-001', '2025-09-01', '2025-12-01', 'Asignado a construcción', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (4, 1, 'CAS-G-001-0004', 2, 3, 11, '2025-01-15', '2028-01-15', 'LOT-CAS-G-2025-001', '2025-09-01', '2025-12-01', 'Asignado a obra sur', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (5, 1, 'CAS-G-001-0005', 2, 3, 12, '2025-01-15', '2028-01-15', 'LOT-CAS-G-2025-001', '2025-09-01', '2025-12-01', 'Asignado a obra sur', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (6, 1, 'CAS-G-001-0006', 2, 5, 20, '2025-01-15', '2028-01-15', 'LOT-CAS-G-2025-001', '2025-09-01', '2025-12-01', 'Mantenimiento mecánico', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (7, 1, 'CAS-G-001-0007', 2, 5, 21, '2025-01-15', '2028-01-15', 'LOT-CAS-G-2025-001', '2025-09-01', '2025-12-01', 'Mantenimiento mecánico', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (8, 1, 'CAS-G-001-0008', 2, 9, 23, '2025-01-15', '2028-01-15', 'LOT-CAS-G-2025-001', '2025-09-01', '2025-12-01', 'Soldadura', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (9, 1, 'CAS-G-001-0009', 2, 9, 24, '2025-01-15', '2028-01-15', 'LOT-CAS-G-2025-001', '2025-09-01', '2025-12-01', 'Soldadura', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (10, 1, 'CAS-G-001-0010', 1, 1, NULL, '2025-01-15', '2028-01-15', 'LOT-CAS-G-2025-001', NULL, '2025-12-01', 'En stock', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (11, 2, 'CAS-E-001-0001', 2, 4, 16, '2025-01-20', '2028-01-20', 'LOT-CAS-E-2025-001', '2025-09-01', '2025-12-01', 'Electricista principal', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (12, 2, 'CAS-E-001-0002', 2, 4, 17, '2025-01-20', '2028-01-20', 'LOT-CAS-E-2025-001', '2025-09-01', '2025-12-01', 'Electricista', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (13, 2, 'CAS-E-001-0003', 2, 4, 18, '2025-01-20', '2028-01-20', 'LOT-CAS-E-2025-001', '2025-09-01', '2025-12-01', 'Ayudante eléctrico', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (14, 2, 'CAS-E-001-0004', 1, 1, NULL, '2025-01-20', '2028-01-20', 'LOT-CAS-E-2025-001', NULL, '2025-12-01', 'En stock', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (15, 2, 'CAS-E-001-0005', 1, 1, NULL, '2025-01-20', '2028-01-20', 'LOT-CAS-E-2025-001', NULL, '2025-12-01', 'En stock', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (16, 3, 'ARN-FB-001-0001', 2, 2, 5, '2025-02-01', '2030-02-01', 'LOT-ARN-FB-2025-001', '2025-09-15', '2025-11-15', 'Jefe obra norte', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (17, 3, 'ARN-FB-001-0002', 2, 2, 6, '2025-02-01', '2030-02-01', 'LOT-ARN-FB-2025-001', '2025-09-15', '2025-11-15', 'Maestro norte', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (18, 3, 'ARN-FB-001-0003', 2, 2, 7, '2025-02-01', '2030-02-01', 'LOT-ARN-FB-2025-001', '2025-09-15', '2025-11-15', 'Operario norte', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (19, 3, 'ARN-FB-001-0004', 2, 3, 10, '2025-02-01', '2030-02-01', 'LOT-ARN-FB-2025-001', '2025-09-15', '2025-11-15', 'Jefe obra sur', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (20, 3, 'ARN-FB-001-0005', 2, 3, 11, '2025-02-01', '2030-02-01', 'LOT-ARN-FB-2025-001', '2025-09-15', '2025-11-15', 'Maestro sur', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (21, 3, 'ARN-FB-001-0006', 2, 3, 12, '2025-02-01', '2030-02-01', 'LOT-ARN-FB-2025-001', '2025-09-15', '2025-11-15', 'Operario sur', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (22, 3, 'ARN-FB-001-0007', 1, 1, NULL, '2025-02-01', '2030-02-01', 'LOT-ARN-FB-2025-001', NULL, '2025-11-15', 'En stock', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (23, 3, 'ARN-FB-001-0008', 1, 1, NULL, '2025-02-01', '2030-02-01', 'LOT-ARN-FB-2025-001', NULL, '2025-11-15', 'En stock', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (24, 9, 'RES-MC-001-0001', 2, 7, 26, '2025-04-01', '2027-04-01', 'LOT-RES-MC-2025-001', '2025-10-01', '2026-01-01', 'Laboratorio', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (25, 9, 'RES-MC-001-0002', 2, 7, 27, '2025-04-01', '2027-04-01', 'LOT-RES-MC-2025-001', '2025-10-01', '2026-01-01', 'Laboratorio', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (26, 9, 'RES-MC-001-0003', 2, 8, 28, '2025-04-01', '2027-04-01', 'LOT-RES-MC-2025-001', '2025-10-01', '2026-01-01', 'Pintura', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (27, 9, 'RES-MC-001-0004', 2, 8, 29, '2025-04-01', '2027-04-01', 'LOT-RES-MC-2025-001', '2025-10-01', '2026-01-01', 'Pintura', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (28, 9, 'RES-MC-001-0005', 1, 1, NULL, '2025-04-01', '2027-04-01', 'LOT-RES-MC-2025-001', NULL, '2026-01-01', 'En stock', NOW(), NOW()),
                                                                                                                                                                                                                                                                               (29, 9, 'RES-MC-001-0006', 1, 1, NULL, '2025-04-01', '2027-04-01', 'LOT-RES-MC-2025-001', NULL, '2026-01-01', 'En stock', NOW(), NOW())
ON CONFLICT (codigo_serie) DO NOTHING;

-- ===================================================================
-- 11. SOLICITUDES DE REPOSICIÓN
-- ===================================================================
INSERT INTO epp.solicitud_reposicion (epp_id, area_id, solicitante_id, supervisor_id, cantidad_solicitada, cantidad_aprobada, estado_solicitud, prioridad, justificacion, comentarios_supervisor, fecha_solicitud, fecha_aprobacion, fecha_rechazo) VALUES
                                                                                                                                                                                                                                                        (1, 2, 5, 2, 10, 10, 'APROBADA', 'ALTA', 'Nuevo personal ingresante requiere cascos', 'Aprobado. Stock disponible', '2025-10-01', '2025-10-02', NULL),
                                                                                                                                                                                                                                                        (13, 3, 10, 2, 50, 50, 'APROBADA', 'MEDIA', 'Reposición mensual guantes trabajo pesado', 'Aprobado según cronograma', '2025-10-05', '2025-10-06', NULL),
                                                                                                                                                                                                                                                        (15, 7, 26, 2, 100, 80, 'APROBADA', 'ALTA', 'Incremento de actividades requiere más mascarillas', 'Aprobado 80 unidades por disponibilidad actual', '2025-10-08', '2025-10-09', NULL),
                                                                                                                                                                                                                                                        (3, 2, 5, 2, 5, 5, 'APROBADA', 'URGENTE', 'Arneses con fecha de vencimiento próxima', 'Aprobado urgente. Renovación crítica', '2025-10-10', '2025-10-10', NULL),
                                                                                                                                                                                                                                                        (8, 9, 23, 2, 15, 15, 'APROBADA', 'MEDIA', 'Lentes para nuevos soldadores', 'Aprobado', '2025-10-12', '2025-10-13', NULL),
                                                                                                                                                                                                                                                        (11, 4, 15, NULL, 30, NULL, 'PENDIENTE', 'MEDIA', 'Reposición mensual guantes nitrilo', NULL, '2025-10-20', NULL, NULL),
                                                                                                                                                                                                                                                        (5, 5, 19, NULL, 12, NULL, 'PENDIENTE', 'ALTA', 'Zapatos desgastados por uso intensivo', NULL, '2025-10-21', NULL, NULL),
                                                                                                                                                                                                                                                        (16, 9, 23, NULL, 100, NULL, 'PENDIENTE', 'BAJA', 'Stock preventivo tapones auditivos', NULL, '2025-10-22', NULL, NULL),
                                                                                                                                                                                                                                                        (20, 8, 28, 2, 30, NULL, 'RECHAZADA', 'BAJA', 'Solicitud de mandiles adicionales', 'Stock actual suficiente. Revisar uso', '2025-10-15', NULL, '2025-10-16'),
                                                                                                                                                                                                                                                        (7, 3, 10, NULL, 25, NULL, 'CANCELADA', 'MEDIA', 'Lentes de seguridad adicionales', 'Solicitante canceló por cambio de prioridades', '2025-10-18', NULL, NULL)
ON CONFLICT DO NOTHING;

-- ===================================================================
-- 12. ENTREGAS DE EPP
-- ===================================================================
INSERT INTO epp.entrega_epp (entrega_id, trabajador_id, jefe_area_id, fecha_entrega, tipo_entrega, observaciones, firma_digital, status) VALUES
                                                                                                                                             (1, 6, 5, '2025-09-01', 'PRIMERA_ENTREGA', 'Kit completo trabajador nuevo', 'FIRMA-TRB-006', 'COMPLETADA'),
                                                                                                                                             (2, 7, 5, '2025-09-01', 'PRIMERA_ENTREGA', 'Kit completo trabajador nuevo', 'FIRMA-TRB-007', 'COMPLETADA'),
                                                                                                                                             (3, 8, 5, '2025-09-02', 'PRIMERA_ENTREGA', 'Kit completo trabajador nuevo', 'FIRMA-TRB-008', 'COMPLETADA'),
                                                                                                                                             (4, 9, 5, '2025-09-15', 'REPOSICION', 'Reposición guantes desgastados', 'FIRMA-TRB-009', 'COMPLETADA'),
                                                                                                                                             (5, 11, 10, '2025-09-05', 'PRIMERA_ENTREGA', 'Kit completo trabajador nuevo', 'FIRMA-TRB-011', 'COMPLETADA'),
                                                                                                                                             (6, 12, 10, '2025-09-05', 'PRIMERA_ENTREGA', 'Kit completo trabajador nuevo', 'FIRMA-TRB-012', 'COMPLETADA'),
                                                                                                                                             (7, 13, 10, '2025-09-06', 'PRIMERA_ENTREGA', 'Kit completo trabajador nuevo', 'FIRMA-TRB-013', 'COMPLETADA'),
                                                                                                                                             (8, 16, 15, '2025-09-10', 'PRIMERA_ENTREGA', 'Kit electricista', 'FIRMA-TRB-016', 'COMPLETADA'),
                                                                                                                                             (9, 17, 15, '2025-09-10', 'PRIMERA_ENTREGA', 'Kit electricista', 'FIRMA-TRB-017', 'COMPLETADA'),
                                                                                                                                             (10, 18, 15, '2025-09-11', 'PRIMERA_ENTREGA', 'Kit ayudante eléctrico', 'FIRMA-TRB-018', 'COMPLETADA'),
                                                                                                                                             (11, 23, 23, '2025-09-12', 'PRIMERA_ENTREGA', 'Kit soldador completo', 'FIRMA-TRB-023', 'COMPLETADA'),
                                                                                                                                             (12, 24, 23, '2025-09-12', 'PRIMERA_ENTREGA', 'Kit soldador completo', 'FIRMA-TRB-024', 'COMPLETADA'),
                                                                                                                                             (13, 25, 23, '2025-09-13', 'PRIMERA_ENTREGA', 'Kit soldador completo', 'FIRMA-TRB-025', 'COMPLETADA'),
                                                                                                                                             (14, 26, 26, '2025-09-14', 'PRIMERA_ENTREGA', 'Kit protección química', 'FIRMA-TRB-026', 'COMPLETADA'),
                                                                                                                                             (15, 27, 26, '2025-09-14', 'PRIMERA_ENTREGA', 'Kit protección química', 'FIRMA-TRB-027', 'COMPLETADA')
ON CONFLICT (entrega_id) DO NOTHING;

-- ===================================================================
-- 13. DETALLE DE ENTREGAS EPP
-- ===================================================================
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, instancia_epp_id, cantidad, motivo) VALUES
                                                                                                 (1, 1, 1, NULL, 'Asignación inicial - Casco'), (1, 3, 16, NULL, 'Asignación inicial - Arnés'), (1, 5, NULL, 1, 'Asignación inicial - Zapatos'), (1, 7, NULL, 1, 'Asignación inicial - Lentes'), (1, 11, NULL, 2, 'Asignación inicial - Guantes'), (1, 13, NULL, 5, 'Asignación inicial - Guantes cuero'),
                                                                                                 (2, 1, 2, NULL, 'Asignación inicial - Casco'), (2, 3, 17, NULL, 'Asignación inicial - Arnés'), (2, 5, NULL, 1, 'Asignación inicial - Zapatos'), (2, 7, NULL, 1, 'Asignación inicial - Lentes'), (2, 11, NULL, 2, 'Asignación inicial - Guantes'), (2, 13, NULL, 5, 'Asignación inicial - Guantes cuero'),
                                                                                                 (3, 1, 3, NULL, 'Asignación inicial - Casco'), (3, 3, 18, NULL, 'Asignación inicial - Arnés'), (3, 5, NULL, 1, 'Asignación inicial - Zapatos'), (3, 7, NULL, 1, 'Asignación inicial - Lentes'), (3, 11, NULL, 2, 'Asignación inicial - Guantes'), (3, 13, NULL, 5, 'Asignación inicial - Guantes cuero'),
                                                                                                 (4, 13, NULL, 10, 'Reposición por desgaste'),
                                                                                                 (5, 1, 4, NULL, 'Asignación inicial - Casco'), (5, 3, 19, NULL, 'Asignación inicial - Arnés'), (5, 6, NULL, 1, 'Asignación inicial - Zapatos T-40'), (5, 7, NULL, 1, 'Asignación inicial - Lentes'), (5, 12, NULL, 2, 'Asignación inicial - Guantes L'), (5, 13, NULL, 5, 'Asignación inicial - Guantes cuero'),
                                                                                                 (6, 1, 5, NULL, 'Asignación inicial - Casco'), (6, 3, 20, NULL, 'Asignación inicial - Arnés'), (6, 6, NULL, 1, 'Asignación inicial - Zapatos T-40'), (6, 7, NULL, 1, 'Asignación inicial - Lentes'), (6, 12, NULL, 2, 'Asignación inicial - Guantes L'), (6, 13, NULL, 5, 'Asignación inicial - Guantes cuero'),
                                                                                                 (7, 1, 6, NULL, 'Asignación inicial - Casco'), (7, 3, 21, NULL, 'Asignación inicial - Arnés'), (7, 6, NULL, 1, 'Asignación inicial - Zapatos'), (7, 7, NULL, 1, 'Asignación inicial - Lentes'),
                                                                                                 (8, 2, 11, NULL, 'Asignación inicial - Casco Clase E'), (8, 5, NULL, 1, 'Asignación inicial - Zapatos'), (8, 7, NULL, 1, 'Asignación inicial - Lentes'), (8, 11, NULL, 3, 'Asignación inicial - Guantes'), (8, 16, NULL, 10, 'Asignación inicial - Tapones'),
                                                                                                 (9, 2, 12, NULL, 'Asignación inicial - Casco Clase E'), (9, 5, NULL, 1, 'Asignación inicial - Zapatos'), (9, 7, NULL, 1, 'Asignación inicial - Lentes'), (9, 11, NULL, 3, 'Asignación inicial - Guantes'), (9, 16, NULL, 10, 'Asignación inicial - Tapones'),
                                                                                                 (10, 2, 13, NULL, 'Asignación inicial - Casco Clase E'), (10, 5, NULL, 1, 'Asignación inicial - Zapatos'), (10, 7, NULL, 1, 'Asignación inicial - Lentes'), (10, 11, NULL, 2, 'Asignación inicial - Guantes'),
                                                                                                 (11, 1, 8, NULL, 'Asignación inicial - Casco'), (11, 8, NULL, 1, 'Asignación inicial - Lentes sold.'), (11, 14, NULL, 5, 'Asignación inicial - Guantes sold.'), (11, 20, NULL, 1, 'Asignación inicial - Mandil'), (11, 16, NULL, 10, 'Asignación inicial - Tapones'),
                                                                                                 (12, 1, 9, NULL, 'Asignación inicial - Casco'), (12, 8, NULL, 1, 'Asignación inicial - Lentes sold.'), (12, 14, NULL, 5, 'Asignación inicial - Guantes sold.'), (12, 20, NULL, 1, 'Asignación inicial - Mandil'), (12, 16, NULL, 10, 'Asignación inicial - Tapones'),
                                                                                                 (13, 1, 7, NULL, 'Asignación inicial - Casco'), (13, 8, NULL, 1, 'Asignación inicial - Lentes sold.'), (13, 14, NULL, 5, 'Asignación inicial - Guantes sold.'), (13, 20, NULL, 1, 'Asignación inicial - Mandil'),
                                                                                                 (14, 10, NULL, 1, 'Asignación inicial - Prot. facial'), (14, 9, 24, NULL, 'Asignación inicial - Respirador'), (14, 11, NULL, 5, 'Asignación inicial - Guantes'), (14, 19, NULL, 3, 'Asignación inicial - Overol'), (14, 15, NULL, 20, 'Asignación inicial - Mascarillas'),
                                                                                                 (15, 10, NULL, 1, 'Asignación inicial - Prot. facial'), (15, 9, 25, NULL, 'Asignación inicial - Respirador'), (15, 11, NULL, 5, 'Asignación inicial - Guantes'), (15, 19, NULL, 3, 'Asignación inicial - Overol'), (15, 15, NULL, 20, 'Asignación inicial - Mascarillas')
ON CONFLICT DO NOTHING;

-- ===================================================================
-- 14. INSPECCIONES
-- ===================================================================
INSERT INTO epp.inspeccion (instancia_epp_id, inspector_id, fecha_inspeccion, resultado,
                            observaciones, url_foto, accion_correctiva, fecha_proxima_inspeccion) VALUES
                                                                                                      (1, 2, '2025-09-01', 'APTO', 'Casco en buen estado, sin fisuras', 'https://storage.upeu.pe/inspecciones/CAS-G-001-0001.jpg', NULL, '2025-12-01'),
                                                                                                      (2, 2, '2025-09-01', 'APTO', 'Casco sin daños visibles', 'https://storage.upeu.pe/inspecciones/CAS-G-001-0002.jpg', NULL, '2025-12-01'),
                                                                                                      (16, 5, '2025-09-15', 'APTO', 'Arnés con todos los puntos de anclaje funcionales', 'https://storage.upeu.pe/inspecciones/ARN-FB-001-0001.jpg', NULL, '2025-11-15'),
                                                                                                      (17, 5, '2025-09-15', 'APTO', 'Arnés en perfecto estado', 'https://storage.upeu.pe/inspecciones/ARN-FB-001-0002.jpg', NULL, '2025-11-15'),
                                                                                                      (3, 2, '2025-09-01', 'REQUIERE_MANTENIMIENTO', 'Suspensión del casco con desgaste leve', 'https://storage.upeu.pe/inspecciones/CAS-G-001-0003.jpg', 'Cambiar sistema de suspensión', '2025-10-25'),
                                                                                                      (19, 10, '2025-09-15', 'REQUIERE_MANTENIMIENTO', 'Arnés con costuras desgastadas en un punto', 'https://storage.upeu.pe/inspecciones/ARN-FB-001-0004.jpg', 'Reforzar costuras o reemplazar', '2025-10-30'),
                                                                                                      (10, 2, '2025-09-20', 'NO_APTO', 'Casco con fisura en la parte superior', 'https://storage.upeu.pe/inspecciones/CAS-G-001-0010.jpg', 'Dar de baja - reemplazar inmediatamente', NULL),
                                                                                                      (24, 2, '2025-10-01', 'APTO', 'Respirador con filtros en buen estado', 'https://storage.upeu.pe/inspecciones/RES-MC-001-0001.jpg', NULL, '2026-01-01'),
                                                                                                      (25, 2, '2025-10-01', 'APTO', 'Respirador funcional, válvulas operativas', 'https://storage.upeu.pe/inspecciones/RES-MC-001-0002.jpg', NULL, '2026-01-01'),
                                                                                                      (26, 8, '2025-10-01', 'APTO', 'Respirador en buenas condiciones', 'https://storage.upeu.pe/inspecciones/RES-MC-001-0003.jpg', NULL, '2026-01-01')
ON CONFLICT DO NOTHING;

-- ===================================================================
-- 15. AUDITORÍA
-- ===================================================================
INSERT INTO epp.auditoria (tabla_afectada, registro_id, usuario_id, ip_origen, user_agent, datos_anteriores, datos_nuevos, operacion, fecha_operacion) VALUES
                                                                                                                                                           ('usuario', 1, 1, '192.168.1.100', 'Mozilla/5.0', NULL, '{"nombre_usuario": "admin"}', 'UPDATE', '2025-10-22 08:00:00'),
                                                                                                                                                           ('trabajador', 6, 2, '192.168.1.101', 'Mozilla/5.0', NULL, '{"dni": "56789012", "nombres": "Luis", "apellidos": "Martínez"}', 'INSERT', '2025-09-01 09:30:00'),
                                                                                                                                                           ('entrega_epp', 1, 5, '192.168.1.102', 'Mozilla/5.0', NULL, '{"trabajador_id": 6, "tipo_entrega": "PRIMERA_ENTREGA"}', 'INSERT', '2025-09-01 10:15:00'),
                                                                                                                                                           ('solicitud_reposicion', 1, 5, '192.168.1.102', 'Mozilla/5.0', NULL, '{"epp_id": 1, "cantidad_solicitada": 10, "estado": "PENDIENTE"}', 'INSERT', '2025-10-01 11:00:00'),
                                                                                                                                                           ('solicitud_reposicion', 1, 2, '192.168.1.101', 'Mozilla/5.0', '{"estado_solicitud": "PENDIENTE"}', '{"estado_solicitud": "APROBADA", "cantidad_aprobada": 10}', 'UPDATE', '2025-10-02 14:30:00'),
                                                                                                                                                           ('inventario_central', 1, 2, '192.168.1.101', 'Mozilla/5.0', '{"cantidad_actual": 50}', '{"cantidad_actual": 40}', 'UPDATE', '2025-10-02 15:00:00'),
                                                                                                                                                           ('inventario_area', 1, 5, '192.168.1.102', 'Mozilla/5.0', '{"cantidad_actual": 12}', '{"cantidad_actual": 22}', 'UPDATE', '2025-10-02 15:05:00'),
                                                                                                                                                           ('instancia_epp', 10, 2, '192.168.1.101', 'Mozilla/5.0', '{"estado_id": 1}', '{"estado_id": 4}', 'UPDATE', '2025-09-20 16:00:00'),
                                                                                                                                                           ('usuario', 1, 1, '192.168.1.100', 'Mozilla/5.0', NULL, '{"nombre_usuario": "admin"}', 'UPDATE', '2025-10-22 18:00:00')
ON CONFLICT DO NOTHING;

-- ===================================================================
-- FIN DEL SCRIPT DE DATOS SINTÉTICOS
-- ===================================================================

DO $$
    BEGIN
        RAISE NOTICE '========================================================';
        RAISE NOTICE 'SCRIPT DE DATOS SINTÉTICOS EJECUTADO CORRECTAMENTE';
        RAISE NOTICE '========================================================';
        RAISE NOTICE 'Datos creados:';
        RAISE NOTICE '  - 4 Roles del sistema';
        RAISE NOTICE '  - 5 Estados de EPP';
        RAISE NOTICE '  - 10 Áreas de trabajo';
        RAISE NOTICE '  - 20 Tipos de EPP en catálogo';
        RAISE NOTICE '  - 30 Trabajadores';
        RAISE NOTICE '  - 5 Usuarios del sistema';
        RAISE NOTICE '  - Inventario central completo';
        RAISE NOTICE '  - Inventario distribuido por áreas';
        RAISE NOTICE '  - 29 Instancias EPP individualizadas';
        RAISE NOTICE '  - 10 Solicitudes de reposición';
        RAISE NOTICE '  - 15 Entregas completadas con detalle';
        RAISE NOTICE '  - 10 Inspecciones realizadas';
        RAISE NOTICE '  - 9 Registros de auditoría';
        RAISE NOTICE '========================================================';
        RAISE NOTICE 'Credenciales de acceso:';
        RAISE NOTICE '  Usuario: admin | Password: Admin123!';
        RAISE NOTICE '  Usuario: supervisor | Password: Admin123!';
        RAISE NOTICE '  Usuario: jefe_zona_norte | Password: Admin123!';
        RAISE NOTICE '  Usuario: jefe_zona_sur | Password: Admin123!';
        RAISE NOTICE '  Usuario: coordinador | Password: Admin123!';
        RAISE NOTICE '========================================================';
        RAISE NOTICE '⚠️  IMPORTANTE: Cambiar contraseñas en producción';
        RAISE NOTICE '========================================================';
    END $$;