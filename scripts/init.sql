-- ============================================
-- SCRIPT DE INICIALIZACIÓN - SOLO DATOS
-- Plataforma de Gestión de EPP - UPEU
-- Las tablas las crea Hibernate automáticamente
-- ============================================

-- Crear extensiones y esquema
CREATE EXTENSION IF NOT EXISTS timescaledb;
CREATE SCHEMA IF NOT EXISTS epp;

-- Dar permisos al usuario
GRANT ALL PRIVILEGES ON SCHEMA epp TO epp_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA epp TO epp_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA epp TO epp_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA epp GRANT ALL ON TABLES TO epp_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA epp GRANT ALL ON SEQUENCES TO epp_user;

-- ============================================
-- DATOS INICIALES (SEED DATA)
-- ============================================

-- Roles
INSERT INTO epp.rol (nombre_rol, descripcion) VALUES
                                                  ('ADMINISTRADOR_SISTEMA', 'Gestión total del sistema y configuración'),
                                                  ('SUPERVISOR_SST', 'Supervisión de seguridad, gestión de inventario central'),
                                                  ('JEFE_AREA', 'Gestión de personal y EPP en su área'),
                                                  ('COORDINADOR_SST', 'Coordinación general y acceso a reportes')
    ON CONFLICT (nombre_rol) DO NOTHING;

-- Estados EPP
INSERT INTO epp.estado_epp (nombre, descripcion, permite_uso, color_hex) VALUES
                                                                             ('EN_STOCK', 'Disponible en inventario', FALSE, '#4CAF50'),
                                                                             ('ENTREGADO', 'Asignado a trabajador', TRUE, '#2196F3'),
                                                                             ('EN_MANTENIMIENTO', 'Requiere reparación', FALSE, '#FF9800'),
                                                                             ('BAJA', 'Dado de baja, no utilizable', FALSE, '#F44336'),
                                                                             ('EXTRAVIADO', 'Reportado como perdido', FALSE, '#9E9E9E')
    ON CONFLICT (nombre) DO NOTHING;

-- Áreas iniciales
INSERT INTO epp.area (nombre_area, codigo_area, descripcion, activo, fecha_creacion) VALUES
                                                                                         ('Almacén General', 'ALM', 'Almacén central de EPPs', TRUE, CURRENT_TIMESTAMP),
                                                                                         ('Construcción - Zona A', 'CONST-A', 'Área de construcción zona norte', TRUE, CURRENT_TIMESTAMP),
                                                                                         ('Mantenimiento', 'MANT', 'Área de mantenimiento general', TRUE, CURRENT_TIMESTAMP),
                                                                                         ('Seguridad', 'SEG', 'Personal de seguridad', TRUE, CURRENT_TIMESTAMP)
    ON CONFLICT (nombre_area) DO NOTHING;

-- Catálogo de EPP básico
INSERT INTO epp.catalogo_epp (nombre_epp, codigo_identificacion, tipo_uso, vida_util_meses, nivel_proteccion, activo, fecha_creacion, fecha_actualizacion) VALUES
                                                                                                                                                               ('Casco de Seguridad Clase G', 'CAS-G-001', 'DURADERO', 36, 'Cabeza', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                                               ('Guantes de Nitrilo Talla L', 'GUA-NIT-L', 'CONSUMIBLE', 1, 'Manos', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                                               ('Botas de Seguridad Punta de Acero', 'BOT-SEG-01', 'DURADERO', 12, 'Pies', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                                               ('Arnés de Cuerpo Completo', 'ARN-CC-001', 'DURADERO', 24, 'Caídas', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                                               ('Mascarilla N95', 'MAS-N95', 'CONSUMIBLE', NULL, 'Respiratoria', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (codigo_identificacion) DO NOTHING;

-- Usuario administrador inicial (password: Admin123!)
-- Hash BCrypt para "Admin123!": $2a$10$N9qo8uLOickgx2ZMRZoMye1J8iYLfkYvmH5wJjXJqYvqLqCQmW7rK
INSERT INTO epp.trabajador (dni, nombres, apellidos, area_id, puesto, estado, fecha_creacion, fecha_actualizacion)
VALUES ('00000001', 'Administrador', 'Sistema', 1, 'Administrador de Sistema', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (dni) DO NOTHING;

INSERT INTO epp.usuario (nombre_usuario, contrasena_hash, email, trabajador_id, activo, fecha_creacion)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8iYLfkYvmH5wJjXJqYvqLqCQmW7rK', 'admin@upeu.edu.pe', 1, TRUE, CURRENT_TIMESTAMP)
    ON CONFLICT (nombre_usuario) DO NOTHING;

INSERT INTO epp.usuario_rol (usuario_id, rol_id, fecha_asignacion)
VALUES (1, 1, CURRENT_TIMESTAMP)
    ON CONFLICT DO NOTHING;

-- Mensaje de confirmación
DO $
BEGIN
    RAISE NOTICE '==============================================';
    RAISE NOTICE 'Esquema EPP inicializado correctamente';
    RAISE NOTICE 'Usuario admin creado';
    RAISE NOTICE 'Password: Admin123!';
    RAISE NOTICE '** CAMBIAR EN PRODUCCIÓN **';
    RAISE NOTICE '==============================================';
END $;edor

-- Extensión TimescaleDB (ya viene con la imagen)
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- ====================
-- DATOS INICIALES: ROLES
-- ====================
INSERT INTO rol (nombre_rol, descripcion, fecha_creacion, fecha_actualizacion) VALUES
                                                                                   ('ADMINISTRADOR_SISTEMA', 'Gestión completa del sistema, usuarios y catálogos', NOW(), NOW()),
                                                                                   ('SUPERVISOR_SST', 'Gestión de inventario central, aprobaciones y reportes', NOW(), NOW()),
                                                                                   ('JEFE_AREA', 'Gestión de personal y entregas en su área asignada', NOW(), NOW()),
                                                                                   ('COORDINADOR_SST', 'Acceso de solo lectura a reportes y dashboards', NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- ====================
-- DATOS INICIALES: ESTADOS EPP
-- ====================
INSERT INTO estado_epp (nombre_estado, descripcion) VALUES
                                                        ('En Stock', 'EPP disponible en almacén'),
                                                        ('Entregado', 'EPP entregado a trabajador'),
                                                        ('En Uso', 'EPP actualmente en uso por trabajador'),
                                                        ('En Mantenimiento', 'EPP en proceso de mantenimiento o reparación'),
                                                        ('Dado de Baja', 'EPP fuera de servicio permanentemente'),
                                                        ('Extraviado', 'EPP reportado como perdido'),
                                                        ('Dañado', 'EPP con daños que requieren evaluación')
    ON CONFLICT DO NOTHING;

-- ====================
-- DATOS INICIALES: ÁREAS (Ejemplos)
-- ====================
INSERT INTO area (nombre_area, codigo_area, descripcion, ubicacion, activo, fecha_creacion, fecha_actualizacion) VALUES
                                                                                                                     ('Almacén Central', 'ALM-001', 'Almacén principal de EPPs', 'Edificio A - Piso 1', true, NOW(), NOW()),
                                                                                                                     ('Mantenimiento', 'MNT-001', 'Área de mantenimiento general', 'Edificio B - Piso 2', true, NOW(), NOW()),
                                                                                                                     ('Construcción', 'CON-001', 'Obras y construcción civil', 'Campo - Zona Norte', true, NOW(), NOW()),
                                                                                                                     ('Electricidad', 'ELE-001', 'Instalaciones eléctricas', 'Edificio C - Piso 3', true, NOW(), NOW()),
                                                                                                                     ('SST Central', 'SST-001', 'Oficina de Seguridad y Salud en el Trabajo', 'Edificio Administrativo', true, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- ====================
-- DATOS INICIALES: CATÁLOGO EPP (Ejemplos)
-- ====================
INSERT INTO catalogo_epp (nombre_epp, codigo_identificacion, especificaciones_tecnicas, tipo_uso) VALUES
-- EPPs Durables (con instancias individuales)
('Casco de Seguridad Clase G', 'EPP-CSC-001', 'Casco dieléctrico clase G, resistencia 2200V, norma ANSI Z89.1', 'Duradero'),
('Arnés de Seguridad Full Body', 'EPP-ARN-001', 'Arnés de cuerpo completo con anillos D, capacidad 140kg, norma ANSI Z359.11', 'Duradero'),
('Lentes de Seguridad Antiimpacto', 'EPP-LEN-001', 'Lentes policarbonato con protección UV, antiempañante, norma ANSI Z87.1', 'Duradero'),
('Zapatos de Seguridad Punta de Acero', 'EPP-ZAP-001', 'Botines dieléctricos punta de acero, suela antideslizante, norma ASTM F2413', 'Duradero'),
('Respirador Media Cara', 'EPP-RES-001', 'Respirador elastomérico reutilizable con filtros P100, norma NIOSH', 'Duradero'),

-- EPPs Consumibles (por cantidad)
('Guantes de Nitrilo (Par)', 'EPP-GNT-001', 'Guantes desechables nitrilo azul, talla M, libre de látex', 'Consumible'),
('Mascarilla N95', 'EPP-MAS-001', 'Mascarilla respirador N95, filtración 95%, uso único', 'Consumible'),
('Tapones Auditivos', 'EPP-TAP-001', 'Tapones auditivos espuma PVC, reducción 32dB, desechables', 'Consumible'),
('Guantes de Cuero (Par)', 'EPP-GCU-001', 'Guantes reforzados cuero/lona para trabajo pesado', 'Consumible'),
('Protector Solar Factor 50+', 'EPP-PSO-001', 'Bloqueador solar industrial factor 50+, 120ml, resistente al agua', 'Consumible')
    ON CONFLICT DO NOTHING;

-- ====================
-- ÍNDICES PARA OPTIMIZACIÓN
-- ====================
CREATE INDEX IF NOT EXISTS idx_trabajador_dni ON trabajador(dni);
CREATE INDEX IF NOT EXISTS idx_trabajador_area ON trabajador(area_id);
CREATE INDEX IF NOT EXISTS idx_usuario_nombre ON usuario(nombre_usuario);
CREATE INDEX IF NOT EXISTS idx_inventario_area_area ON inventario_area(area_id);
CREATE INDEX IF NOT EXISTS idx_instancia_estado ON instancia_epp(estado_id);
CREATE INDEX IF NOT EXISTS idx_solicitud_estado ON solicitud_reposicion(estado_solicitud);
CREATE INDEX IF NOT EXISTS idx_entrega_trabajador ON entrega_epp(trabajador_id);
CREATE INDEX IF NOT EXISTS idx_entrega_fecha ON entrega_epp(fecha_entrega);

-- ====================
-- FUNCIÓN PARA AUDITORÍA
-- ====================
CREATE OR REPLACE FUNCTION actualizar_fecha_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar trigger a tablas principales
CREATE TRIGGER trigger_actualizar_trabajador
    BEFORE UPDATE ON trabajador
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_fecha_modificacion();

CREATE TRIGGER trigger_actualizar_usuario
    BEFORE UPDATE ON usuario
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_fecha_modificacion();

-- ====================
-- USUARIO ADMINISTRADOR POR DEFECTO
-- Contraseña: Admin123! (debe cambiarse en producción)
-- Hash generado con BCrypt
-- ====================
-- NOTA: Este INSERT lo haremos desde la aplicación Spring Boot
-- en un componente @PostConstruct para usar BCryptPasswordEncoder

COMMIT;