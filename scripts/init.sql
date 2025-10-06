-- ============================================
-- SCRIPT DE INICIALIZACIÓN BASE DE DATOS
-- Plataforma de Gestión de EPP - UPEU
-- Versión: 1.0.0
-- ============================================

-- Crear extensiones
CREATE EXTENSION IF NOT EXISTS timescaledb;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Crear esquema
CREATE SCHEMA IF NOT EXISTS epp;
SET search_path TO epp, public;

-- Asegurar que el usuario epp_user existe y tiene permisos
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'epp_user') THEN
        CREATE USER epp_user WITH PASSWORD 'dev_password';
END IF;
END
$$;

-- Dar permisos completos al usuario
GRANT ALL PRIVILEGES ON DATABASE epp_db TO epp_user;
GRANT ALL PRIVILEGES ON SCHEMA epp TO epp_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA epp TO epp_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA epp TO epp_user;

-- ============================================
-- TABLAS MAESTRAS
-- ============================================

-- Tabla: CATALOGO_EPP
CREATE TABLE catalogo_epp (
                              epp_id SERIAL PRIMARY KEY,
                              nombre_epp VARCHAR(100) NOT NULL,
                              codigo_identificacion VARCHAR(50) UNIQUE,
                              especificaciones_tecnicas TEXT,
                              tipo_uso VARCHAR(20) NOT NULL CHECK (tipo_uso IN ('CONSUMIBLE', 'DURADERO')),
                              vida_util_meses INTEGER,
                              nivel_proteccion VARCHAR(50),
                              fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              fecha_actualizacion TIMESTAMP,
                              activo BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_catalogo_epp_tipo_uso ON catalogo_epp(tipo_uso);
CREATE INDEX idx_catalogo_epp_activo ON catalogo_epp(activo);

-- Tabla: AREA
CREATE TABLE area (
                      area_id SERIAL PRIMARY KEY,
                      nombre_area VARCHAR(100) NOT NULL UNIQUE,
                      codigo_area VARCHAR(20) UNIQUE,
                      descripcion TEXT,
                      ubicacion VARCHAR(200),
                      responsable_id INTEGER,
                      activo BOOLEAN DEFAULT TRUE,
                      fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_area_activo ON area(activo);

-- Tabla: ROL
CREATE TABLE rol (
                     rol_id SERIAL PRIMARY KEY,
                     nombre_rol VARCHAR(50) NOT NULL UNIQUE,
                     descripcion TEXT,
                     permisos JSONB
);

-- Insertar roles predefinidos
INSERT INTO rol (nombre_rol, descripcion) VALUES
                                              ('ADMINISTRADOR_SISTEMA', 'Gestión total del sistema y configuración'),
                                              ('SUPERVISOR_SST', 'Supervisión de seguridad, gestión de inventario central'),
                                              ('JEFE_AREA', 'Gestión de personal y EPP en su área'),
                                              ('COORDINADOR_SST', 'Coordinación general y acceso a reportes');

-- Tabla: ESTADO_EPP
CREATE TABLE estado_epp (
                            estado_id SERIAL PRIMARY KEY,
                            nombre VARCHAR(50) NOT NULL UNIQUE,
                            descripcion TEXT,
                            permite_uso BOOLEAN DEFAULT FALSE,
                            color_hex VARCHAR(7)
);

INSERT INTO estado_epp (nombre, descripcion, permite_uso, color_hex) VALUES
                                                                         ('EN_STOCK', 'Disponible en inventario', FALSE, '#4CAF50'),
                                                                         ('ENTREGADO', 'Asignado a trabajador', TRUE, '#2196F3'),
                                                                         ('EN_MANTENIMIENTO', 'Requiere reparación', FALSE, '#FF9800'),
                                                                         ('BAJA', 'Dado de baja, no utilizable', FALSE, '#F44336'),
                                                                         ('EXTRAVIADO', 'Reportado como perdido', FALSE, '#9E9E9E');

-- ============================================
-- TABLAS DE PERSONAL
-- ============================================

-- Tabla: TRABAJADOR
CREATE TABLE trabajador (
                            trabajador_id SERIAL PRIMARY KEY,
                            dni VARCHAR(10) UNIQUE NOT NULL,
                            nombres VARCHAR(100) NOT NULL,
                            apellidos VARCHAR(100) NOT NULL,
                            codigo_qr_photocheck VARCHAR(50) UNIQUE,
                            area_id INTEGER NOT NULL REFERENCES area(area_id),
                            puesto VARCHAR(100),
                            fecha_ingreso DATE,
                            telefono VARCHAR(20),
                            email VARCHAR(100),
                            estado VARCHAR(20) DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO')),
                            fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            fecha_actualizacion TIMESTAMP
);

CREATE INDEX idx_trabajador_dni ON trabajador(dni);
CREATE INDEX idx_trabajador_qr ON trabajador(codigo_qr_photocheck);
CREATE INDEX idx_trabajador_area ON trabajador(area_id);
CREATE INDEX idx_trabajador_estado ON trabajador(estado);

-- Tabla: USUARIO
CREATE TABLE usuario (
                         usuario_id SERIAL PRIMARY KEY,
                         nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
                         contrasena_hash VARCHAR(255) NOT NULL,
                         email VARCHAR(100) UNIQUE,
                         trabajador_id INTEGER UNIQUE REFERENCES trabajador(trabajador_id),
                         activo BOOLEAN DEFAULT TRUE,
                         fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         ultimo_acceso TIMESTAMP,
                         intentos_fallidos INTEGER DEFAULT 0,
                         bloqueado_hasta TIMESTAMP
);

CREATE INDEX idx_usuario_nombre ON usuario(nombre_usuario);
CREATE INDEX idx_usuario_activo ON usuario(activo);

-- Tabla: USUARIO_ROL (Many-to-Many)
CREATE TABLE usuario_rol (
                             usuario_id INTEGER NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
                             rol_id INTEGER NOT NULL REFERENCES rol(rol_id) ON DELETE CASCADE,
                             fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (usuario_id, rol_id)
);

-- ============================================
-- DATOS INICIALES (SEED DATA)
-- ============================================

-- Áreas
INSERT INTO area (nombre_area, codigo_area, descripcion) VALUES
                                                             ('Construcción - Zona A', 'CONST-A', 'Área de construcción zona norte'),
                                                             ('Mantenimiento', 'MANT', 'Área de mantenimiento general'),
                                                             ('Almacén General', 'ALM', 'Almacén central'),
                                                             ('Seguridad', 'SEG', 'Personal de seguridad');

-- Catálogo de EPP básico
INSERT INTO catalogo_epp (nombre_epp, codigo_identificacion, tipo_uso, vida_util_meses, nivel_proteccion) VALUES
                                                                                                              ('Casco de Seguridad Clase G', 'CAS-G-001', 'DURADERO', 36, 'Cabeza'),
                                                                                                              ('Guantes de Nitrilo Talla L', 'GUA-NIT-L', 'CONSUMIBLE', 1, 'Manos'),
                                                                                                              ('Botas de Seguridad Punta de Acero', 'BOT-SEG-01', 'DURADERO', 12, 'Pies'),
                                                                                                              ('Arnés de Cuerpo Completo', 'ARN-CC-001', 'DURADERO', 24, 'Caídas'),
                                                                                                              ('Mascarilla N95', 'MAS-N95', 'CONSUMIBLE', NULL, 'Respiratoria');

-- Usuario administrador inicial (password: Admin123!)
-- Hash BCrypt para "Admin123!" = $2a$10$N9qo8uLOickgx2ZMRZoMye1J8iYLfkYvmH5wJjXJqYvqLqCQmW7rK
INSERT INTO trabajador (dni, nombres, apellidos, area_id, puesto, estado) VALUES
    ('00000001', 'Administrador', 'Sistema', 3, 'Administrador de Sistema', 'ACTIVO');

INSERT INTO usuario (nombre_usuario, contrasena_hash, email, trabajador_id, activo) VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8iYLfkYvmH5wJjXJqYvqLqCQmW7rK', 'admin@upeu.edu.pe', 1, TRUE);

INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (1, 1);

-- ============================================
-- FINALIZACIÓN
-- ============================================

-- Vacuum y análisis inicial
VACUUM ANALYZE;

-- Mensaje de confirmación
DO $$
BEGIN
    RAISE NOTICE '==============================================';
    RAISE NOTICE 'Base de datos EPP inicializada exitosamente';
    RAISE NOTICE '==============================================';
    RAISE NOTICE 'Tablas creadas: 6';
    RAISE NOTICE 'Usuarios creados: 1 (admin)';
    RAISE NOTICE 'Password inicial: Admin123!';
    RAISE NOTICE '** CAMBIAR PASSWORD EN PRODUCCIÓN **';
    RAISE NOTICE '==============================================';
END $$;