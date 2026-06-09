-- ============================================================================
-- SEED DATA — SST EPP Backend (desarrollo)
-- Ejecutar DESPUÉS de que Spring Boot haya creado las tablas con ddl-auto=update
-- Los archivos (fotos, PDFs, facturas) quedan en NULL en entorno de desarrollo
-- ============================================================================

SET search_path TO epp, public;

-- ── 1. ROLES ─────────────────────────────────────────────────────────────────
INSERT INTO epp.rol (nombre_rol, descripcion) VALUES
('SUPERVISOR_SST',       'Supervisor de Seguridad y Salud en el Trabajo — acceso completo al sistema'),
('ADMINISTRADOR_SISTEMA','Administrador técnico del sistema')
ON CONFLICT (nombre_rol) DO NOTHING;

-- ── 2. USUARIOS (contraseña: Admin1234! hasheada con BCrypt) ─────────────────
-- Hash BCrypt de "Admin1234!" — válido para todos los usuarios de prueba
INSERT INTO epp.usuario (nombre_usuario, contrasena_hash, email, nombre_completo, activo, fecha_creacion, intentos_fallidos) VALUES
('supervisor1', '$2a$10$oiiNYUyZeAGqO3aTmXb4VOLAFZjv2eHDpHHtYluw58X9WyBkIzwt6', 'supervisor1@upeu.edu.pe', 'Carlos Mendoza Ríos',     true, NOW(), 0),
('supervisor2', '$2a$10$oiiNYUyZeAGqO3aTmXb4VOLAFZjv2eHDpHHtYluw58X9WyBkIzwt6', 'supervisor2@upeu.edu.pe', 'Ana Torres Villanueva',   true, NOW(), 0),
('admin',       '$2a$10$oiiNYUyZeAGqO3aTmXb4VOLAFZjv2eHDpHHtYluw58X9WyBkIzwt6', 'admin@upeu.edu.pe',       'Administrador del Sistema', true, NOW(), 0)
ON CONFLICT (nombre_usuario) DO NOTHING;

-- Asignar roles
INSERT INTO epp.usuario_rol (usuario_id, rol_id)
SELECT u.usuario_id, r.rol_id FROM epp.usuario u, epp.rol r
WHERE u.nombre_usuario = 'supervisor1' AND r.nombre_rol = 'SUPERVISOR_SST'
ON CONFLICT DO NOTHING;

INSERT INTO epp.usuario_rol (usuario_id, rol_id)
SELECT u.usuario_id, r.rol_id FROM epp.usuario u, epp.rol r
WHERE u.nombre_usuario = 'supervisor2' AND r.nombre_rol = 'SUPERVISOR_SST'
ON CONFLICT DO NOTHING;

INSERT INTO epp.usuario_rol (usuario_id, rol_id)
SELECT u.usuario_id, r.rol_id FROM epp.usuario u, epp.rol r
WHERE u.nombre_usuario = 'admin' AND r.nombre_rol = 'ADMINISTRADOR_SISTEMA'
ON CONFLICT DO NOTHING;

-- ── 3. ESTADOS EPP ────────────────────────────────────────────────────────────
INSERT INTO epp.estado_epp (nombre, descripcion, permite_uso, color_hex) VALUES
('EN_STOCK',    'Disponible en inventario para uso',      true,  '#28A745'),
('EN_USO',      'Asignado a un trabajador activo',        true,  '#007BFF'),
('DETERIORADO', 'En mal estado, no apto para uso',        false, '#DC3545'),
('VENCIDO',     'Fuera de fecha de vencimiento',          false, '#6C757D'),
('EN_REPARACION','En proceso de mantenimiento',           false, '#FFC107'),
('DADO_DE_BAJA','Retirado definitivamente del inventario',false, '#343A40')
ON CONFLICT (nombre) DO NOTHING;

-- ── 4. ÁREAS ─────────────────────────────────────────────────────────────────
INSERT INTO epp.area (nombre_area, codigo_area, descripcion, ubicacion, activo, fecha_creacion) VALUES
('Producción',          'PROD',  'Área de producción y manufactura',         'Planta Principal - Piso 1',  true, NOW()),
('Mantenimiento',       'MANT',  'Área de mantenimiento de equipos',          'Taller Central',             true, NOW()),
('Limpieza y Servicios','LIMP',  'Área de limpieza y servicios generales',    'Edificio Administrativo',    true, NOW()),
('Laboratorio',         'LAB',   'Área de control de calidad y laboratorio',  'Planta Principal - Piso 2',  true, NOW()),
('Almacén',             'ALM',   'Área de almacén y logística',               'Depósito General',           true, NOW())
ON CONFLICT (nombre_area) DO NOTHING;

-- ── 5. TALLAS ─────────────────────────────────────────────────────────────────
INSERT INTO epp.catalogo_talla (nombre, descripcion, orden_visualizacion) VALUES
('XS',    'Extra Small',   1),
('S',     'Small',         2),
('M',     'Medium',        3),
('L',     'Large',         4),
('XL',    'Extra Large',   5),
('XXL',   'Extra Extra Large', 6),
('UNICA', 'Talla única',   7),
('35',    'Calzado talla 35', 10),
('36',    'Calzado talla 36', 11),
('37',    'Calzado talla 37', 12),
('38',    'Calzado talla 38', 13),
('39',    'Calzado talla 39', 14),
('40',    'Calzado talla 40', 15),
('41',    'Calzado talla 41', 16),
('42',    'Calzado talla 42', 17),
('43',    'Calzado talla 43', 18),
('44',    'Calzado talla 44', 19)
ON CONFLICT (nombre) DO NOTHING;

-- ── 6. CATÁLOGO EPP ───────────────────────────────────────────────────────────
INSERT INTO epp.catalogo_epp (nombre_epp, tipo_uso, fabricante, caracteristicas, aprobaciones_normas, color, cantidad_minima, cantidad_maxima, activo, fecha_creacion, fecha_actualizacion) VALUES
('Casco de Seguridad 3M H-700',         'DURADERO',   '3M',          'Casco tipo II, clase E. Resistente a impactos. Ajuste con trinquete.',     'ANSI/ISEA Z89.1, NTP-ISO 3873', 'Blanco',    5,  50, true, NOW(), NOW()),
('Casco de Seguridad Jyrsa',            'DURADERO',   'Jyrsa',       'Casco tipo I, clase E. Ventilado. Ajuste manual.',                          'ANSI/ISEA Z89.1',               'Amarillo',  5,  50, true, NOW(), NOW()),
('Lentes de Seguridad 3M 11394',        'DURADERO',   '3M',          'Lentes transparentes anti-empañantes. Protección UV.',                      'ANSI Z87.1',                    'Transparente', 10, 100, true, NOW(), NOW()),
('Lentes Oscuros Uvex S3960',           'DURADERO',   'Uvex',        'Lentes oscuros para trabajo en exteriores. Filtro UV 400.',                 'ANSI Z87.1, EN 166',            'Gris',     10, 100, true, NOW(), NOW()),
('Tapones Auditivos 3M 1100',           'CONSUMIBLE', '3M',          'Tapones de espuma de un solo uso. NRR 29 dB.',                             'ANSI S3.19',                    'Naranja',  20, 500, true, NOW(), NOW()),
('Orejeras 3M Peltor X2A',              'DURADERO',   '3M',          'Orejeras de copa. NRR 24 dB. Diadema ajustable.',                           'ANSI S3.19, EN 352-1',          'Amarillo', 5,  30, true, NOW(), NOW()),
('Respirador 3M 6200',                  'DURADERO',   '3M',          'Respirador media cara. Acepta filtros 2091 y 6001. Talla M.',               'NIOSH, NTP 458',                'Gris',     5,  30, true, NOW(), NOW()),
('Mascarilla KN95',                     'CONSUMIBLE', 'MSA Safety',  'Mascarilla desechable KN95. Filtración 95%.',                               'GB2626-2019',                   'Blanco',   30, 300, true, NOW(), NOW()),
('Guantes Nitrilo Talla M',             'CONSUMIBLE', 'Ansell',      'Guantes de nitrilo desechables. Resistentes a químicos básicos.',           'EN 374, ASTM D6319',            'Azul',     20, 200, true, NOW(), NOW()),
('Guantes de Cuero Caña Corta',         'DURADERO',   'Steelpro',    'Guantes de cuero flor. Protección mecánica. Costura reforzada.',            'EN 388',                        'Marrón',   10, 100, true, NOW(), NOW()),
('Botas de Seguridad con Punta Acero',  'DURADERO',   'Caterpillar', 'Botas cuero. Punta de acero. Suela antideslizante y antiestática.',         'ASTM F2413, NTP-ISO 20345',     'Negro',    5,  60, true, NOW(), NOW()),
('Botines Dieléctricos',                'DURADERO',   'Bata',        'Botines sin punta metálica. Aislamiento eléctrico hasta 18kV.',             'ASTM F2413-11 EH',              'Negro',    5,  40, true, NOW(), NOW()),
('Chaleco de Seguridad Reflectivo',     'DURADERO',   'DuPont',      'Chaleco tipo II con bandas reflectivas. Cierre con velcro.',                'ANSI/ISEA 107',                 'Naranja',  10, 80, true, NOW(), NOW()),
('Arnés de Seguridad Full Body',        'DURADERO',   'MSA Safety',  'Arnés cuerpo completo. 5 puntos de sujeción. Carga máx. 140 kg.',           'ANSI Z359.1, EN 361',           'Negro',    3,  20, true, NOW(), NOW()),
('Mameluco Tyvek',                      'CONSUMIBLE', 'DuPont',      'Mameluco desechable Tyvek 400. Protección partículas secas y salpicaduras.','EN 13982-1, EN 13034',          'Blanco',   10, 100, true, NOW(), NOW()),
('Mandil de Cuero para Soldadura',      'DURADERO',   'Steelpro',    'Mandil cuero dividido. Protección calor y chispas. Ajustable.',             'EN ISO 11611',                  'Marrón',   3,  15, true, NOW(), NOW()),
('Careta de Soldadura Fotosensible',    'DURADERO',   '3M',          'Careta oscurecimiento automático. Grado DIN 9-13. Energía solar+batería.', 'ANSI Z87.1, EN 379',            'Negro',    2,  10, true, NOW(), NOW()),
('Cinta de Señalización',               'CONSUMIBLE', 'Brady',       'Cinta de señalización bicolor. 3 pulgadas x 300 metros.',                  'OSHA 1926.200',                 'Amarillo/Negro', 5, 50, true, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ── 7. RELACIÓN EPP ↔ TALLAS ─────────────────────────────────────────────────
-- Cascos (talla única)
INSERT INTO epp.epp_talla (epp_id, talla_id)
SELECT c.epp_id, t.talla_id FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp IN ('Casco de Seguridad 3M H-700', 'Casco de Seguridad Jyrsa')
  AND t.nombre = 'UNICA'
ON CONFLICT DO NOTHING;

-- Lentes (talla única)
INSERT INTO epp.epp_talla (epp_id, talla_id)
SELECT c.epp_id, t.talla_id FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp IN ('Lentes de Seguridad 3M 11394', 'Lentes Oscuros Uvex S3960')
  AND t.nombre = 'UNICA'
ON CONFLICT DO NOTHING;

-- Tapones auditivos (talla única)
INSERT INTO epp.epp_talla (epp_id, talla_id)
SELECT c.epp_id, t.talla_id FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp IN ('Tapones Auditivos 3M 1100', 'Orejeras 3M Peltor X2A')
  AND t.nombre = 'UNICA'
ON CONFLICT DO NOTHING;

-- Respiradores y mascarillas (S, M, L)
INSERT INTO epp.epp_talla (epp_id, talla_id)
SELECT c.epp_id, t.talla_id FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp IN ('Respirador 3M 6200', 'Mascarilla KN95')
  AND t.nombre IN ('S', 'M', 'L')
ON CONFLICT DO NOTHING;

-- Guantes (S, M, L, XL)
INSERT INTO epp.epp_talla (epp_id, talla_id)
SELECT c.epp_id, t.talla_id FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp IN ('Guantes Nitrilo Talla M', 'Guantes de Cuero Caña Corta')
  AND t.nombre IN ('S', 'M', 'L', 'XL')
ON CONFLICT DO NOTHING;

-- Botas y botines (tallas de calzado 38-43)
INSERT INTO epp.epp_talla (epp_id, talla_id)
SELECT c.epp_id, t.talla_id FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp IN ('Botas de Seguridad con Punta Acero', 'Botines Dieléctricos')
  AND t.nombre IN ('38', '39', '40', '41', '42', '43')
ON CONFLICT DO NOTHING;

-- Chalecos, arneses, mamelucos (S, M, L, XL, XXL)
INSERT INTO epp.epp_talla (epp_id, talla_id)
SELECT c.epp_id, t.talla_id FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp IN ('Chaleco de Seguridad Reflectivo', 'Arnés de Seguridad Full Body',
                        'Mameluco Tyvek', 'Mandil de Cuero para Soldadura')
  AND t.nombre IN ('S', 'M', 'L', 'XL', 'XXL')
ON CONFLICT DO NOTHING;

-- Careta y cinta (talla única)
INSERT INTO epp.epp_talla (epp_id, talla_id)
SELECT c.epp_id, t.talla_id FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp IN ('Careta de Soldadura Fotosensible', 'Cinta de Señalización')
  AND t.nombre = 'UNICA'
ON CONFLICT DO NOTHING;

-- ── 8. TRABAJADORES ────────────────────────────────────────────────────────────
INSERT INTO epp.trabajador (nombres, apellidos, dni, cargo, email, telefono, estado, fecha_ingreso, fecha_creacion, fecha_actualizacion, area_id) VALUES
-- Producción
('Juan Carlos',  'Quispe Mamani',    '45123456', 'Operario de Producción',    'jquispe@empresa.pe',    '987654321', 'ACTIVO', '2022-03-01', NOW(), NOW(), (SELECT area_id FROM epp.area WHERE codigo_area = 'PROD')),
('María Elena',  'Huanca Flores',    '45234567', 'Operaria de Producción',    'mhuanca@empresa.pe',    '987654322', 'ACTIVO', '2022-03-15', NOW(), NOW(), (SELECT area_id FROM epp.area WHERE codigo_area = 'PROD')),
('Pedro Luis',   'Condori Apaza',    '45345678', 'Jefe de Línea',             'pcondori@empresa.pe',   '987654323', 'ACTIVO', '2021-01-10', NOW(), NOW(), (SELECT area_id FROM epp.area WHERE codigo_area = 'PROD')),
('Rosa Angela',  'Calisaya Torres',  '45456789', 'Operaria de Producción',    'rcalisaya@empresa.pe',  '987654324', 'ACTIVO', '2023-06-01', NOW(), NOW(), (SELECT area_id FROM epp.area WHERE codigo_area = 'PROD')),
-- Mantenimiento
('Jorge Luis',   'Mamani Ticona',    '46123456', 'Técnico de Mantenimiento',  'jmamani@empresa.pe',    '987654325', 'ACTIVO', '2021-05-20', NOW(), NOW(), (SELECT area_id FROM epp.area WHERE codigo_area = 'MANT')),
('Carlos Alberto','Puma Chura',      '46234567', 'Soldador',                  'cpuma@empresa.pe',      '987654326', 'ACTIVO', '2020-11-01', NOW(), NOW(), (SELECT area_id FROM epp.area WHERE codigo_area = 'MANT')),
('Luis Miguel',  'Ramos Coila',      '46345678', 'Electricista',              'lramos@empresa.pe',     '987654327', 'ACTIVO', '2022-08-15', NOW(), NOW(), (SELECT area_id FROM epp.area WHERE codigo_area = 'MANT')),
-- Limpieza
('Gloria Ruth',  'Ticona Mamani',    '47123456', 'Personal de Limpieza',      'gticona@empresa.pe',    '987654328', 'ACTIVO', '2023-01-10', NOW(), NOW(), (SELECT area_id FROM epp.area WHERE codigo_area = 'LIMP')),
('Juana Rosa',   'Apaza Flores',     '47234567', 'Personal de Limpieza',      'japaza@empresa.pe',     '987654329', 'ACTIVO', '2023-01-10', NOW(), NOW(), (SELECT area_id FROM epp.area WHERE codigo_area = 'LIMP')),
-- Laboratorio
('Roberto César','Larico Quispe',    '48123456', 'Analista de Calidad',       'rlarico@empresa.pe',    '987654330', 'ACTIVO', '2021-09-01', NOW(), NOW(), (SELECT area_id FROM epp.area WHERE codigo_area = 'LAB')),
('Silvia Paola', 'Chura Vilca',      '48234567', 'Técnica de Laboratorio',    'schura@empresa.pe',     '987654331', 'ACTIVO', '2022-04-01', NOW(), NOW(), (SELECT area_id FROM epp.area WHERE codigo_area = 'LAB')),
-- Almacén
('Raúl Ernesto', 'Flores Catacora',  '49123456', 'Operario de Almacén',       'rflores@empresa.pe',    '987654332', 'ACTIVO', '2020-07-01', NOW(), NOW(), (SELECT area_id FROM epp.area WHERE codigo_area = 'ALM')),
('Diana Lucía',  'Vargas Quispe',    '49234567', 'Asistente de Logística',    'dvargas@empresa.pe',    '987654333', 'ACTIVO', '2022-12-01', NOW(), NOW(), (SELECT area_id FROM epp.area WHERE codigo_area = 'ALM'))
ON CONFLICT (dni) DO NOTHING;

-- ── 9. COMPRAS ────────────────────────────────────────────────────────────────
-- Compra 1: Factura F001-2025 — EPPs de cabeza y oídos
INSERT INTO epp.compra (nro_factura, fecha_compra, fecha_registro, proveedor, subtotal, igv, monto_total, ruta_archivo_factura, ruta_archivo_cotizacion) VALUES
('F001-2025', '2025-01-15', NOW(), '3M del Perú SAC',          1271.19, 228.81, 1500.00, NULL, NULL),
('F002-2025', '2025-02-10', NOW(), 'Steelpro Safety Perú',     847.46,  152.54, 1000.00, NULL, NULL),
('F003-2025', '2025-03-05', NOW(), 'MSA Safety Perú',          593.22,  106.78, 700.00,  NULL, NULL),
('F004-2025', '2025-04-20', NOW(), 'Caterpillar Safety Perú',  1059.32, 190.68, 1250.00, NULL, NULL),
('F005-2025', '2025-05-12', NOW(), '3M del Perú SAC',          338.98,  61.02,  400.00,  NULL, NULL)
ON CONFLICT DO NOTHING;

-- ── 10. DETALLE COMPRA ─────────────────────────────────────────────────────────
-- F001-2025: Cascos (talla UNICA) + Tapones
INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal)
SELECT
    (SELECT compra_id FROM epp.compra WHERE nro_factura = 'F001-2025'),
    c.epp_id,
    t.talla_id,
    20,
    35.00,
    700.00
FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp = 'Casco de Seguridad 3M H-700' AND t.nombre = 'UNICA';

INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal)
SELECT
    (SELECT compra_id FROM epp.compra WHERE nro_factura = 'F001-2025'),
    c.epp_id,
    t.talla_id,
    100,
    8.00,
    800.00
FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp = 'Tapones Auditivos 3M 1100' AND t.nombre = 'UNICA';

-- F002-2025: Guantes de cuero (M, L) + Mandil
INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal)
SELECT
    (SELECT compra_id FROM epp.compra WHERE nro_factura = 'F002-2025'),
    c.epp_id,
    t.talla_id,
    15,
    25.00,
    375.00
FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp = 'Guantes de Cuero Caña Corta' AND t.nombre = 'M';

INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal)
SELECT
    (SELECT compra_id FROM epp.compra WHERE nro_factura = 'F002-2025'),
    c.epp_id,
    t.talla_id,
    15,
    25.00,
    375.00
FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp = 'Guantes de Cuero Caña Corta' AND t.nombre = 'L';

INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal)
SELECT
    (SELECT compra_id FROM epp.compra WHERE nro_factura = 'F002-2025'),
    c.epp_id,
    t.talla_id,
    5,
    50.00,
    250.00
FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp = 'Mandil de Cuero para Soldadura' AND t.nombre = 'L';

-- F003-2025: Mascarillas KN95 (S, M, L) + Arnés
INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal)
SELECT
    (SELECT compra_id FROM epp.compra WHERE nro_factura = 'F003-2025'),
    c.epp_id,
    t.talla_id,
    50,
    3.50,
    175.00
FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp = 'Mascarilla KN95' AND t.nombre IN ('S', 'M', 'L');

INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal)
SELECT
    (SELECT compra_id FROM epp.compra WHERE nro_factura = 'F003-2025'),
    c.epp_id,
    t.talla_id,
    5,
    75.00,
    375.00
FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp = 'Arnés de Seguridad Full Body' AND t.nombre IN ('M', 'L');

-- F004-2025: Botas de seguridad (tallas 39-42)
INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal)
SELECT
    (SELECT compra_id FROM epp.compra WHERE nro_factura = 'F004-2025'),
    c.epp_id,
    t.talla_id,
    5,
    125.00,
    625.00
FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp = 'Botas de Seguridad con Punta Acero' AND t.nombre IN ('39', '40', '41', '42');

-- F005-2025: Lentes + Chalecos
INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal)
SELECT
    (SELECT compra_id FROM epp.compra WHERE nro_factura = 'F005-2025'),
    c.epp_id,
    t.talla_id,
    20,
    8.50,
    170.00
FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp = 'Lentes de Seguridad 3M 11394' AND t.nombre = 'UNICA';

INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal)
SELECT
    (SELECT compra_id FROM epp.compra WHERE nro_factura = 'F005-2025'),
    c.epp_id,
    t.talla_id,
    10,
    23.00,
    230.00
FROM epp.catalogo_epp c, epp.catalogo_talla t
WHERE c.nombre_epp = 'Chaleco de Seguridad Reflectivo' AND t.nombre IN ('M', 'L', 'XL');

-- ── 11. INVENTARIO CENTRAL ────────────────────────────────────────────────────
-- (lote = nro_factura, reflejando las compras registradas)
-- Estado EN_STOCK para todos los lotes

-- Cascos 3M — F001-2025
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, proveedor, costo_unitario, fecha_adquisicion, ubicacion_bodega, fecha_creacion, ultima_actualizacion)
SELECT c.epp_id, e.estado_id, t.talla_id, 18, 'F001-2025', '3M del Perú SAC', 35.00, '2025-01-15', 'Estante A1', NOW(), NOW()
FROM epp.catalogo_epp c, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Casco de Seguridad 3M H-700' AND e.nombre = 'EN_STOCK' AND t.nombre = 'UNICA'
ON CONFLICT ON CONSTRAINT uk_inventario_central_epp_lote_estado_talla DO NOTHING;

-- Tapones — F001-2025
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, proveedor, costo_unitario, fecha_adquisicion, ubicacion_bodega, fecha_creacion, ultima_actualizacion)
SELECT c.epp_id, e.estado_id, t.talla_id, 80, 'F001-2025', '3M del Perú SAC', 8.00, '2025-01-15', 'Estante B1', NOW(), NOW()
FROM epp.catalogo_epp c, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Tapones Auditivos 3M 1100' AND e.nombre = 'EN_STOCK' AND t.nombre = 'UNICA'
ON CONFLICT ON CONSTRAINT uk_inventario_central_epp_lote_estado_talla DO NOTHING;

-- Guantes cuero M — F002-2025
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, proveedor, costo_unitario, fecha_adquisicion, ubicacion_bodega, fecha_creacion, ultima_actualizacion)
SELECT c.epp_id, e.estado_id, t.talla_id, 12, 'F002-2025', 'Steelpro Safety Perú', 25.00, '2025-02-10', 'Estante C1', NOW(), NOW()
FROM epp.catalogo_epp c, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Guantes de Cuero Caña Corta' AND e.nombre = 'EN_STOCK' AND t.nombre = 'M'
ON CONFLICT ON CONSTRAINT uk_inventario_central_epp_lote_estado_talla DO NOTHING;

-- Guantes cuero L — F002-2025
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, proveedor, costo_unitario, fecha_adquisicion, ubicacion_bodega, fecha_creacion, ultima_actualizacion)
SELECT c.epp_id, e.estado_id, t.talla_id, 10, 'F002-2025', 'Steelpro Safety Perú', 25.00, '2025-02-10', 'Estante C1', NOW(), NOW()
FROM epp.catalogo_epp c, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Guantes de Cuero Caña Corta' AND e.nombre = 'EN_STOCK' AND t.nombre = 'L'
ON CONFLICT ON CONSTRAINT uk_inventario_central_epp_lote_estado_talla DO NOTHING;

-- Mandil L — F002-2025
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, proveedor, costo_unitario, fecha_adquisicion, ubicacion_bodega, fecha_creacion, ultima_actualizacion)
SELECT c.epp_id, e.estado_id, t.talla_id, 4, 'F002-2025', 'Steelpro Safety Perú', 50.00, '2025-02-10', 'Estante D1', NOW(), NOW()
FROM epp.catalogo_epp c, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Mandil de Cuero para Soldadura' AND e.nombre = 'EN_STOCK' AND t.nombre = 'L'
ON CONFLICT ON CONSTRAINT uk_inventario_central_epp_lote_estado_talla DO NOTHING;

-- Mascarillas KN95 (S, M, L) — F003-2025
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, proveedor, costo_unitario, fecha_adquisicion, ubicacion_bodega, fecha_creacion, ultima_actualizacion)
SELECT c.epp_id, e.estado_id, t.talla_id, 45, 'F003-2025', 'MSA Safety Perú', 3.50, '2025-03-05', 'Estante B2', NOW(), NOW()
FROM epp.catalogo_epp c, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Mascarilla KN95' AND e.nombre = 'EN_STOCK' AND t.nombre IN ('S', 'M', 'L')
ON CONFLICT ON CONSTRAINT uk_inventario_central_epp_lote_estado_talla DO NOTHING;

-- Arnés M y L — F003-2025
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, proveedor, costo_unitario, fecha_adquisicion, ubicacion_bodega, fecha_creacion, ultima_actualizacion)
SELECT c.epp_id, e.estado_id, t.talla_id, 4, 'F003-2025', 'MSA Safety Perú', 75.00, '2025-03-05', 'Estante E1', NOW(), NOW()
FROM epp.catalogo_epp c, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Arnés de Seguridad Full Body' AND e.nombre = 'EN_STOCK' AND t.nombre IN ('M', 'L')
ON CONFLICT ON CONSTRAINT uk_inventario_central_epp_lote_estado_talla DO NOTHING;

-- Botas (39-42) — F004-2025
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, proveedor, costo_unitario, fecha_adquisicion, ubicacion_bodega, fecha_creacion, ultima_actualizacion)
SELECT c.epp_id, e.estado_id, t.talla_id, 4, 'F004-2025', 'Caterpillar Safety Perú', 125.00, '2025-04-20', 'Estante F1', NOW(), NOW()
FROM epp.catalogo_epp c, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Botas de Seguridad con Punta Acero' AND e.nombre = 'EN_STOCK' AND t.nombre IN ('39', '40', '41', '42')
ON CONFLICT ON CONSTRAINT uk_inventario_central_epp_lote_estado_talla DO NOTHING;

-- Lentes — F005-2025
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, proveedor, costo_unitario, fecha_adquisicion, ubicacion_bodega, fecha_creacion, ultima_actualizacion)
SELECT c.epp_id, e.estado_id, t.talla_id, 18, 'F005-2025', '3M del Perú SAC', 8.50, '2025-05-12', 'Estante A2', NOW(), NOW()
FROM epp.catalogo_epp c, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Lentes de Seguridad 3M 11394' AND e.nombre = 'EN_STOCK' AND t.nombre = 'UNICA'
ON CONFLICT ON CONSTRAINT uk_inventario_central_epp_lote_estado_talla DO NOTHING;

-- Chalecos (M, L, XL) — F005-2025
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, proveedor, costo_unitario, fecha_adquisicion, ubicacion_bodega, fecha_creacion, ultima_actualizacion)
SELECT c.epp_id, e.estado_id, t.talla_id, 8, 'F005-2025', '3M del Perú SAC', 23.00, '2025-05-12', 'Estante G1', NOW(), NOW()
FROM epp.catalogo_epp c, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Chaleco de Seguridad Reflectivo' AND e.nombre = 'EN_STOCK' AND t.nombre IN ('M', 'L', 'XL')
ON CONFLICT ON CONSTRAINT uk_inventario_central_epp_lote_estado_talla DO NOTHING;

-- ── 12. INVENTARIO ÁREA ────────────────────────────────────────────────────────
-- Stock transferido desde central a cada área (cantidades menores)

-- Producción: cascos, tapones, guantes, chalecos, lentes
INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion)
SELECT c.epp_id, a.area_id, e.estado_id, t.talla_id, 5, 'Armario Producción', NOW()
FROM epp.catalogo_epp c, epp.area a, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Casco de Seguridad 3M H-700' AND a.codigo_area = 'PROD'
  AND e.nombre = 'EN_STOCK' AND t.nombre = 'UNICA'
ON CONFLICT ON CONSTRAINT uk_inventario_area_epp_area_estado_talla DO NOTHING;

INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion)
SELECT c.epp_id, a.area_id, e.estado_id, t.talla_id, 30, 'Armario Producción', NOW()
FROM epp.catalogo_epp c, epp.area a, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Tapones Auditivos 3M 1100' AND a.codigo_area = 'PROD'
  AND e.nombre = 'EN_STOCK' AND t.nombre = 'UNICA'
ON CONFLICT ON CONSTRAINT uk_inventario_area_epp_area_estado_talla DO NOTHING;

INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion)
SELECT c.epp_id, a.area_id, e.estado_id, t.talla_id, 5, 'Armario Producción', NOW()
FROM epp.catalogo_epp c, epp.area a, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Lentes de Seguridad 3M 11394' AND a.codigo_area = 'PROD'
  AND e.nombre = 'EN_STOCK' AND t.nombre = 'UNICA'
ON CONFLICT ON CONSTRAINT uk_inventario_area_epp_area_estado_talla DO NOTHING;

INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion)
SELECT c.epp_id, a.area_id, e.estado_id, t.talla_id, 3, 'Armario Producción', NOW()
FROM epp.catalogo_epp c, epp.area a, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Guantes de Cuero Caña Corta' AND a.codigo_area = 'PROD'
  AND e.nombre = 'EN_STOCK' AND t.nombre = 'M'
ON CONFLICT ON CONSTRAINT uk_inventario_area_epp_area_estado_talla DO NOTHING;

INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion)
SELECT c.epp_id, a.area_id, e.estado_id, t.talla_id, 3, 'Armario Producción', NOW()
FROM epp.catalogo_epp c, epp.area a, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Chaleco de Seguridad Reflectivo' AND a.codigo_area = 'PROD'
  AND e.nombre = 'EN_STOCK' AND t.nombre IN ('M', 'L')
ON CONFLICT ON CONSTRAINT uk_inventario_area_epp_area_estado_talla DO NOTHING;

-- Mantenimiento: cascos, guantes, mandil, botas, arnés
INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion)
SELECT c.epp_id, a.area_id, e.estado_id, t.talla_id, 4, 'Armario Mantenimiento', NOW()
FROM epp.catalogo_epp c, epp.area a, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Casco de Seguridad 3M H-700' AND a.codigo_area = 'MANT'
  AND e.nombre = 'EN_STOCK' AND t.nombre = 'UNICA'
ON CONFLICT ON CONSTRAINT uk_inventario_area_epp_area_estado_talla DO NOTHING;

INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion)
SELECT c.epp_id, a.area_id, e.estado_id, t.talla_id, 3, 'Armario Mantenimiento', NOW()
FROM epp.catalogo_epp c, epp.area a, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Guantes de Cuero Caña Corta' AND a.codigo_area = 'MANT'
  AND e.nombre = 'EN_STOCK' AND t.nombre IN ('M', 'L')
ON CONFLICT ON CONSTRAINT uk_inventario_area_epp_area_estado_talla DO NOTHING;

INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion)
SELECT c.epp_id, a.area_id, e.estado_id, t.talla_id, 2, 'Armario Mantenimiento', NOW()
FROM epp.catalogo_epp c, epp.area a, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Mandil de Cuero para Soldadura' AND a.codigo_area = 'MANT'
  AND e.nombre = 'EN_STOCK' AND t.nombre = 'L'
ON CONFLICT ON CONSTRAINT uk_inventario_area_epp_area_estado_talla DO NOTHING;

INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion)
SELECT c.epp_id, a.area_id, e.estado_id, t.talla_id, 2, 'Armario Mantenimiento', NOW()
FROM epp.catalogo_epp c, epp.area a, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Botas de Seguridad con Punta Acero' AND a.codigo_area = 'MANT'
  AND e.nombre = 'EN_STOCK' AND t.nombre IN ('40', '41')
ON CONFLICT ON CONSTRAINT uk_inventario_area_epp_area_estado_talla DO NOTHING;

-- Limpieza: guantes nitrilo, mascarillas
INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion)
SELECT c.epp_id, a.area_id, e.estado_id, t.talla_id, 20, 'Armario Limpieza', NOW()
FROM epp.catalogo_epp c, epp.area a, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Mascarilla KN95' AND a.codigo_area = 'LIMP'
  AND e.nombre = 'EN_STOCK' AND t.nombre IN ('S', 'M')
ON CONFLICT ON CONSTRAINT uk_inventario_area_epp_area_estado_talla DO NOTHING;

-- Laboratorio: lentes, mascarillas, guantes nitrilo
INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion)
SELECT c.epp_id, a.area_id, e.estado_id, t.talla_id, 5, 'Armario Laboratorio', NOW()
FROM epp.catalogo_epp c, epp.area a, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Lentes de Seguridad 3M 11394' AND a.codigo_area = 'LAB'
  AND e.nombre = 'EN_STOCK' AND t.nombre = 'UNICA'
ON CONFLICT ON CONSTRAINT uk_inventario_area_epp_area_estado_talla DO NOTHING;

INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion)
SELECT c.epp_id, a.area_id, e.estado_id, t.talla_id, 15, 'Armario Laboratorio', NOW()
FROM epp.catalogo_epp c, epp.area a, epp.estado_epp e, epp.catalogo_talla t
WHERE c.nombre_epp = 'Mascarilla KN95' AND a.codigo_area = 'LAB'
  AND e.nombre = 'EN_STOCK' AND t.nombre = 'M'
ON CONFLICT ON CONSTRAINT uk_inventario_area_epp_area_estado_talla DO NOTHING;

-- ── 13. VERIFICACIÓN FINAL ────────────────────────────────────────────────────
SELECT 'usuarios'          AS tabla, COUNT(*) AS registros FROM epp.usuario
UNION ALL SELECT 'roles',            COUNT(*) FROM epp.rol
UNION ALL SELECT 'areas',            COUNT(*) FROM epp.area
UNION ALL SELECT 'trabajadores',     COUNT(*) FROM epp.trabajador
UNION ALL SELECT 'catalogo_epp',     COUNT(*) FROM epp.catalogo_epp
UNION ALL SELECT 'catalogo_talla',   COUNT(*) FROM epp.catalogo_talla
UNION ALL SELECT 'epp_talla',        COUNT(*) FROM epp.epp_talla
UNION ALL SELECT 'compras',          COUNT(*) FROM epp.compra
UNION ALL SELECT 'detalle_compra',   COUNT(*) FROM epp.detalle_compra
UNION ALL SELECT 'inventario_central', COUNT(*) FROM epp.inventario_central
UNION ALL SELECT 'inventario_area',  COUNT(*) FROM epp.inventario_area;
