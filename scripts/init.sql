-- ===================================================================
-- SCRIPT DE INICIALIZACIÓN DE DATOS CORREGIDO Y UNIFICADO
-- Plataforma de Gestión de EPP
-- ===================================================================

-- 1. CONFIGURACIÓN INICIAL DEL ESQUEMA Y PERMISOS
---------------------------------------------------------------------
-- Se asume que el usuario 'epp_user' y la base de datos ya existen.
CREATE SCHEMA IF NOT EXISTS epp;

-- Asignar el esquema por defecto para la sesión actual para no tener que usar el prefijo 'epp.'
SET search_path TO epp;

-- ===================================================================
-- 2. INSERCIÓN DE DATOS MAESTROS (SEED DATA)
-- Se usa ON CONFLICT para evitar errores si los datos ya existen.
-- ===================================================================

-- Roles del sistema
INSERT INTO rol (nombre_rol, descripcion) VALUES
                                              ('ADMINISTRADOR_SISTEMA', 'Gestión completa del sistema, usuarios y catálogos.'),
                                              ('SUPERVISOR_SST', 'Gestión de inventario central, aprobaciones y reportes.'),
                                              ('JEFE_AREA', 'Gestión de personal y entregas en su área asignada.'),
                                              ('TRABAJADOR', 'Acceso para solicitar EPP y consultar su historial.')
    ON CONFLICT (nombre_rol) DO NOTHING;

-- Estados posibles para un EPP
INSERT INTO estado_epp (nombre, descripcion, permite_uso, color_hex) VALUES
                                                                         ('Nuevo', 'EPP recién adquirido, nunca usado.', true, '#28a745'),
                                                                         ('En Uso', 'EPP asignado y actualmente en uso por un trabajador.', true, '#007bff'),
                                                                         ('En Mantenimiento', 'EPP retirado temporalmente para reparación o limpieza.', false, '#ffc107'),
                                                                         ('Dañado', 'EPP que ha sufrido un daño y no es seguro para su uso.', false, '#dc3545'),
                                                                         ('Descartado', 'EPP que ha sido dado de baja permanentemente.', false, '#6c757d'),
                                                                         ('Extraviado', 'EPP reportado como perdido por el trabajador.', false, '#9E9E9E')
    ON CONFLICT (nombre) DO NOTHING;

-- Áreas de la empresa
INSERT INTO area (nombre_area, codigo_area, descripcion, ubicacion, activo, fecha_creacion) VALUES
                                                                                                ('Almacén Central', 'ALM-001', 'Almacén principal de EPPs y materiales.', 'Edificio A - Piso 1', true, NOW()),
                                                                                                ('Mantenimiento', 'MNT-001', 'Área de mantenimiento general de maquinaria.', 'Edificio B - Piso 2', true, NOW()),
                                                                                                ('Construcción', 'CON-001', 'Obras y construcción civil en la Zona Norte.', 'Campo - Zona Norte', true, NOW()),
                                                                                                ('Seguridad y Vigilancia', 'SEG-001', 'Personal de seguridad patrimonial.', 'Entrada Principal', true, NOW())
    ON CONFLICT (nombre_area) DO NOTHING;

-- Catálogo de EPP (incluyendo las columnas 'marca' y 'unidad_medida')
INSERT INTO catalogo_epp (nombre_epp, codigo_identificacion, especificaciones_tecnicas, tipo_uso, vida_util_meses, marca, unidad_medida, activo, fecha_creacion) VALUES
                                                                                                                                                                     ('Casco de Seguridad Clase G', 'EPP-CSC-001', 'Casco dieléctrico clase G, resistencia 2200V, norma ANSI Z89.1', 'DURADERO', 36, 'LIBUS', 'UNI', TRUE, NOW()),
                                                                                                                                                                     ('Arnés de Seguridad Full Body', 'EPP-ARN-001', 'Arnés de cuerpo completo con 4 anillos D, capacidad 140kg', 'DURADERO', 60, 'STELLPRO', 'UNI', TRUE, NOW()),
                                                                                                                                                                     ('Zapatos de Seguridad Punta de Acero T-42', 'EPP-ZAP-001', 'Botines dieléctricos, suela antideslizante, norma ASTM F2413', 'DURADERO', 12, 'MANRIQUE', 'PAR', TRUE, NOW()),
                                                                                                                                                                     ('Guantes de Nitrilo (Caja x100)', 'EPP-GNT-001', 'Guantes desechables nitrilo azul, talla M, libre de látex', 'CONSUMIBLE', NULL, '3M', 'CAJA', TRUE, NOW()),
                                                                                                                                                                     ('Mascarilla N95', 'EPP-MAS-001', 'Mascarilla autofiltrante N95, filtración de partículas 95%', 'CONSUMIBLE', NULL, '3M', 'UNI', TRUE, NOW()),
                                                                                                                                                                     ('Lentes de Seguridad Claros', 'EPP-LEN-001', 'Lentes de policarbonato con protección UV y anti-rayaduras', 'CONSUMIBLE', 6, 'STELLPRO', 'UNI', TRUE, NOW())
    ON CONFLICT (codigo_identificacion) DO NOTHING;


-- ===================================================================
-- 3. CREACIÓN DE USUARIOS DE PRUEBA
-- ===================================================================

-- Crear un trabajador para el usuario Administrador
INSERT INTO trabajador (trabajador_id, dni, nombres, apellidos, area_id, puesto, estado, fecha_creacion)
VALUES (1, '00000001', 'Admin', 'Global', 1, 'Administrador de Sistema', 'ACTIVO', NOW())
    ON CONFLICT (trabajador_id) DO UPDATE SET nombres = EXCLUDED.nombres; -- Actualiza si ya existe

-- Crear el usuario Administrador (contraseña: Admin123!)
-- Hash BCrypt para "Admin123!": $2a$10$X9X77Kshyvn7J6DAHUu/qOgtHMpPdIfofSOFYCAhAsue4tEIPxLAe
INSERT INTO usuario (usuario_id, trabajador_id, nombre_usuario, contrasena_hash, email, activo, fecha_creacion)
VALUES (1, 1, 'admin', '$2a$10$X9X77Kshyvn7J6DAHUu/qOgtHMpPdIfofSOFYCAhAsue4tEIPxLAe', 'admin@empresa.com', TRUE, NOW())
    ON CONFLICT (usuario_id) DO UPDATE SET nombre_usuario = EXCLUDED.nombre_usuario; -- Actualiza si ya existe

-- Asignar el rol de Administrador al usuario admin
INSERT INTO usuario_rol (usuario_id, rol_id)
VALUES (1, (SELECT rol_id FROM rol WHERE nombre_rol = 'ADMINISTRADOR_SISTEMA'))
    ON CONFLICT (usuario_id, rol_id) DO NOTHING;

-- Crear un trabajador para el usuario Supervisor
INSERT INTO trabajador (trabajador_id, dni, nombres, apellidos, area_id, puesto, estado, fecha_creacion)
VALUES (2, '12345678', 'Supervisor', 'SST', 3, 'Supervisor de Seguridad', 'ACTIVO', NOW())
    ON CONFLICT (trabajador_id) DO UPDATE SET nombres = EXCLUDED.nombres;

-- Crear el usuario Supervisor (contraseña: Super123!)
-- Hash BCrypt para "Super123!": $2a$10$ZhO/jZRY9A.H88yJFtH20.OyRsuh8okvoO5E8wzq5g6FfGMgfAp8u
INSERT INTO usuario (usuario_id, trabajador_id, nombre_usuario, contrasena_hash, email, activo, fecha_creacion)
VALUES (2, 2, 'supervisor', '$2a$10$ZhO/jZRY9A.H88yJFtH20.OyRsuh8okvoO5E8wzq5g6FfGMgfAp8u', 'supervisor@empresa.com', TRUE, NOW())
    ON CONFLICT (usuario_id) DO UPDATE SET nombre_usuario = EXCLUDED.nombre_usuario;

-- Asignar el rol de Supervisor
INSERT INTO usuario_rol (usuario_id, rol_id)
VALUES (2, (SELECT rol_id FROM rol WHERE nombre_rol = 'SUPERVISOR_SST'))
    ON CONFLICT (usuario_id, rol_id) DO NOTHING;

-- ===================================================================
-- 4. MENSAJE DE FINALIZACIÓN
-- ===================================================================
DO $$
BEGIN
    RAISE NOTICE '==============================================';
    RAISE NOTICE 'Script de datos iniciales ejecutado.';
    RAISE NOTICE 'Usuario admin creado (Password: Admin123!)';
    RAISE NOTICE 'Usuario supervisor creado (Password: Super123!)';
    RAISE NOTICE '** Recordar cambiar contraseñas en producción **';
    RAISE NOTICE '==============================================';
END $$;