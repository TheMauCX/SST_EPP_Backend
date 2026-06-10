-- ============================================================
--  SST EPP - SCRIPT DE DATOS SINTÉTICOS (Tipo Producción)
--  UPEU - Universidad Peruana Unión
--  Compatible con: Spring Boot 3.1.5 + TimescaleDB/PostgreSQL 15
--  Instrucciones:
--    1. Levanta el backend (Spring Boot) para que Hibernate
--       cree el schema epp y todas las tablas.
--    2. Ejecuta este script:
--       docker exec -i epp_postgres psql -U epp_user -d epp_db < seed_produccion.sql
--  Notas:
--    - Links/URLs se dejan en NULL (dev sin Azure).
--    - EPPs consumibles sin tallas.
--    - EPPs duraderos con tallas.
--    - Datos distribuidos en 8 meses para que los reportes
--      muestren tendencias visibles.
-- ============================================================

SET search_path TO epp, public;

-- ============================================================
-- 0. LIMPIEZA (orden inverso de dependencias)
-- ============================================================
TRUNCATE TABLE
    epp.instancia_epp,
    epp.detalle_entrega_epp,
    epp.entrega_epp,
    epp.inventario_area,
    epp.inventario_central,
    epp.detalle_compra,
    epp.compra,
    epp.catalogo_epp_norma,
    epp.epp_talla,
    epp.catalogo_epp,
    epp.norma_epp,
    epp.catalogo_talla,
    epp.trabajador,
    epp.area,
    epp.usuario_rol,
    epp.usuario,
    epp.rol,
    epp.estado_epp
RESTART IDENTITY CASCADE;

-- ============================================================
-- 1. ROLES
-- ============================================================
INSERT INTO epp.rol (nombre_rol, descripcion) VALUES
('SUPERVISOR_SST',       'Supervisor de Seguridad y Salud en el Trabajo'),
('ADMINISTRADOR_SISTEMA','Administrador con acceso total al sistema');

-- ============================================================
-- 2. USUARIOS (pass: Admin1234! en BCrypt)
-- ============================================================
INSERT INTO epp.usuario (nombre_usuario, contrasena_hash, email, nombre_completo, activo, fecha_creacion, intentos_fallidos) VALUES
('supervisor1', '$2a$10$N.zmdr9zkMhJBjyQZcS7uOoFfuuHZY5dglEFGrJE2b2RNXyOqbXzm', 'supervisor1@upeu.edu.pe', 'Carlos Mendoza Huanca',  TRUE, NOW(), 0),
('supervisor2', '$2a$10$N.zmdr9zkMhJBjyQZcS7uOoFfuuHZY5dglEFGrJE2b2RNXyOqbXzm', 'supervisor2@upeu.edu.pe', 'María Quispe Flores',    TRUE, NOW(), 0),
('admin',       '$2a$10$N.zmdr9zkMhJBjyQZcS7uOoFfuuHZY5dglEFGrJE2b2RNXyOqbXzm', 'admin@upeu.edu.pe',       'Administrador del Sistema', TRUE, NOW(), 0);

-- Asignar roles
INSERT INTO epp.usuario_rol (usuario_id, rol_id) VALUES
(1, 1), -- supervisor1 → SUPERVISOR_SST
(2, 1), -- supervisor2 → SUPERVISOR_SST
(3, 2); -- admin       → ADMINISTRADOR_SISTEMA

-- ============================================================
-- 3. ESTADOS EPP
-- ============================================================
INSERT INTO epp.estado_epp (nombre, descripcion, permite_uso, color_hex) VALUES
('NUEVO',           'EPP recién adquirido, sin uso',                TRUE,  '#28a745'),
('EN_STOCK',        'Disponible en almacén para su uso',            TRUE,  '#17a2b8'),
('USADO',           'En uso por un trabajador',                     TRUE,  '#ffc107'),
('DAÑADO',          'Dañado, no apto para uso',                     FALSE, '#dc3545'),
('EN_REPARACION',   'En proceso de revisión o reparación',          FALSE, '#fd7e14'),
('OBSOLETO',        'Fuera de vida útil, retirado',                 FALSE, '#6c757d'),
('ENTREGADO',       'Entregado formalmente a un trabajador',        TRUE,  '#007bff'),
('BAJA',            'Dado de baja definitiva del sistema',          FALSE, '#343a40');

-- ============================================================
-- 4. ÁREAS DE TRABAJO (UPEU)
-- ============================================================
INSERT INTO epp.area (nombre_area, codigo_area, descripcion, ubicacion, activo, fecha_creacion) VALUES
('Mantenimiento General',  'MNT-001', 'Área de mantenimiento de infraestructura y equipos',     'Pabellón Técnico - Piso 1',   TRUE, NOW()),
('Limpieza y Saneamiento', 'LMP-001', 'Área de limpieza, ornato y mantenimiento de ambientes',  'Campus Principal',             TRUE, NOW()),
('Laboratorios',           'LAB-001', 'Laboratorios de ciencias, química y biología',            'Pabellón Ciencias - Piso 2',  TRUE, NOW()),
('Cocina y Comedor',       'COC-001', 'Área de preparación de alimentos y servicio de comedor',  'Edificio Servicios',          TRUE, NOW()),
('Construcción y Obras',   'OBR-001', 'Área de trabajos de construcción y obras civiles',        'Campus Sur - Obra Nueva',     TRUE, NOW());

-- ============================================================
-- 5. TALLAS (catálogo maestro)
-- ============================================================
INSERT INTO epp.catalogo_talla (nombre, descripcion, orden_visualizacion) VALUES
('XS',    'Extra pequeño',      1),
('S',     'Pequeño',            2),
('M',     'Mediano',            3),
('L',     'Grande',             4),
('XL',    'Extra grande',       5),
('XXL',   'Extra extra grande', 6),
('36',    'Calzado talla 36',   10),
('37',    'Calzado talla 37',   11),
('38',    'Calzado talla 38',   12),
('39',    'Calzado talla 39',   13),
('40',    'Calzado talla 40',   14),
('41',    'Calzado talla 41',   15),
('42',    'Calzado talla 42',   16),
('43',    'Calzado talla 43',   17),
('UNICA', 'Talla única',        99);

-- ============================================================
-- 6. NORMAS EPP (22 normas predefinidas)
-- ============================================================
INSERT INTO epp.norma_epp (codigo, nombre_corto, descripcion, organismo, categoria, icono_url, activo) VALUES
('ANSI-Z89.1',    'ANSI Cascos Industriales',     'Cascos de protección industrial Tipo I y II. Clases E (eléctrica), G (general) y C (conductora).', 'ANSI', 'CABEZA',      NULL, TRUE),
('NTP-399.018',   'NTP Cascos de Seguridad',       'Norma Técnica Peruana para cascos de seguridad en trabajos industriales.', 'NTP', 'CABEZA',      NULL, TRUE),
('ANSI-Z87.1',    'ANSI Protección Ocular',        'Práctica para la protección ocular y facial en la industria, construcción y educación.', 'ANSI', 'OJOS',        NULL, TRUE),
('EN-166',        'EN Protección Ocular Personal', 'Especificaciones europeas para equipos de protección individual de los ojos.', 'EN', 'OJOS',        NULL, TRUE),
('ANSI-S3.19',    'ANSI Protectores Auditivos',    'Atenuación de ruido para protectores auditivos. Define el NRR (Noise Reduction Rating).', 'ANSI', 'OIDOS',       NULL, TRUE),
('EN-352-1',      'EN Orejeras de Protección',     'Requisitos generales para orejeras. Ensayo, marcado e información del fabricante.', 'EN', 'OIDOS',       NULL, TRUE),
('NIOSH-42C84',   'NIOSH Respiradores',            'Certificación NIOSH para respiradores de partículas. Incluye N95, N99, P100.', 'NIOSH', 'RESPIRATORIA', NULL, TRUE),
('EN-149',        'EN FFP Respiradores',           'Piezas faciales filtrantes contra partículas. Clases FFP1, FFP2, FFP3.', 'EN', 'RESPIRATORIA', NULL, TRUE),
('EN-374',        'EN Guantes Químicos',           'Guantes de protección contra productos químicos y microorganismos.', 'EN', 'MANOS',        NULL, TRUE),
('EN-388',        'EN Guantes Mecánicos',          'Guantes de protección contra riesgos mecánicos. Escala de 4 niveles por propiedad.', 'EN', 'MANOS',        NULL, TRUE),
('ANSI-Z41',      'ANSI Calzado de Seguridad',     'Calzado de protección con puntera de acero o composite. Resistencia de 75 libras.', 'ANSI', 'PIES',         NULL, TRUE),
('NTP-ISO-20345', 'NTP Calzado de Seguridad',      'Calzado de seguridad para uso profesional. Requisitos básicos y adicionales.', 'NTP', 'PIES',         NULL, TRUE),
('EN-ISO-20345',  'EN Calzado de Seguridad',       'Norma europea para calzado de seguridad. Clases S1-S5 según nivel de protección.', 'EN', 'PIES',         NULL, TRUE),
('EN-361',        'EN Arnés Anticaída',            'Arneses anticaída para sistemas de detención de caídas. Ensayo y marcado.', 'EN', 'CAIDAS',       NULL, TRUE),
('EN-358',        'EN Cinturón de Sujeción',       'Cinturones de sujeción para el posicionamiento en el trabajo y arneses de sujeción.', 'EN', 'CAIDAS',       NULL, TRUE),
('EN-340',        'EN Ropa de Protección General', 'Requisitos generales para ropa de protección. Prestaciones, diseño, inocuidad.', 'EN', 'CUERPO',       NULL, TRUE),
('EN-471',        'EN Ropa Alta Visibilidad',      'Ropa de señalización de alta visibilidad para uso profesional. Clases 1-3.', 'EN', 'CUERPO',       NULL, TRUE),
('NTP-ISO-3873',  'NTP Cascos Industriales',       'Cascos de protección para la industria. Especificaciones y métodos de ensayo.', 'NTP', 'CABEZA',      NULL, TRUE),
('OSHA-1910.136', 'OSHA Calzado Protector',        'Estándar OSHA para calzado protector en entornos de trabajo con riesgos de impacto.', 'OSHA', 'PIES',        NULL, TRUE),
('EN-1731',       'EN Protección Ocular Malla',    'Dispositivos de protección ocular de malla de alambre. Alcance y requisitos.', 'EN', 'OJOS',         NULL, TRUE),
('ANSI-ISEA-105', 'ANSI Guantes de Trabajo',       'Clasificación de rendimiento de guantes de trabajo, protectores de manos y brazos.', 'ANSI', 'MANOS',       NULL, TRUE),
('EN-812',        'EN Gorros de Seguridad',        'Gorros de seguridad para la industria. Protección contra golpes ligeros en la cabeza.', 'EN', 'CABEZA',      NULL, TRUE);

-- ============================================================
-- 7. CATÁLOGO EPP
--    DURADEROS: cascos, botas, guantes de cuero, arnés, ropa,
--               lentes, orejeras, careta facial
--    CONSUMIBLES: mascarillas, guantes nitrilo, tapones,
--                 guantes latex, cintas adhesivas, EPPs desechables
-- ============================================================
INSERT INTO epp.catalogo_epp
  (nombre_epp, tipo_uso, fabricante, caracteristicas, tiempo_uso_fabricante, tiempo_uso_operacion,
   condiciones_mantenimiento, condiciones_almacenamiento, condiciones_cambio_prematuro,
   foto_referencia, ficha_tecnica_path, color, cantidad_minima, cantidad_maxima, activo, fecha_creacion, fecha_actualizacion)
VALUES
-- DURADEROS (id 1..8)
('Casco de Seguridad 3M H-700',
 'DURADERO', '3M', 'Casco de polietileno de alta densidad PEHD, con ranura para aditamentos. Peso 385g. Resistencia eléctrica clase E (20.000V).',
 '5 años desde fecha de fabricación', '2 años en uso continuo',
 'Limpiar con paño húmedo y jabón neutro. No usar solventes ni pinturas.',
 'Lugar fresco y seco, lejos de luz solar directa. No apilar más de 5 unidades.',
 'Reemplazar si recibe impacto fuerte, muestra grietas, deformaciones o cumple vida útil.',
 NULL, NULL, 'Blanco', 15, 120, TRUE, NOW(), NOW()),

('Bota de Seguridad Punta de Acero',
 'DURADERO', 'Caterpillar', 'Bota con puntera de acero clase 75, suela antideslizante y antiestática. Cuero de alta resistencia. Resistencia eléctrica 18kV.',
 '3 años desde fabricación', '1.5 años en uso continuo',
 'Limpiar con cepillo seco. Aplicar crema protectora cada 2 semanas. Secar a temperatura ambiente.',
 'Almacenar en posición vertical, lejos de humedad y calor excesivo.',
 'Cambiar si la puntera queda expuesta, la suela se desprende o el cuero presenta cortes profundos.',
 NULL, NULL, 'Negro', 10, 80, TRUE, NOW(), NOW()),

('Guante de Cuero para Soldadura',
 'DURADERO', 'Steelpro', 'Guante de cuero vaqueta, forro interior de tela gruesa. Puño largo de 35cm. Resistencia a temperatura hasta 250°C.',
 '18 meses', '6 meses en uso regular',
 'Limpiar con paño seco. No lavar con agua. Guardar extendido para mantener la forma.',
 'Lugar seco. Colgar o extender, nunca doblar o comprimir.',
 'Cambiar si presenta quemaduras, cortes en la palma o endurecimiento extremo del cuero.',
 NULL, NULL, 'Marrón', 20, 200, TRUE, NOW(), NOW()),

('Arnés de Seguridad Anticaída',
 'DURADERO', 'MSA Safety', 'Arnés de cuerpo completo, 5 puntos de ajuste. Carga de rotura 22kN. Punto de anclaje dorsal y esternal. Conforme EN 361.',
 '10 años desde fabricación', '5 años en uso activo',
 'Inspeccionar antes de cada uso. Limpiar con agua limpia y jabón suave. Secar al aire.',
 'Colgar en percha, lugar ventilado, sin exposición a UV ni productos químicos.',
 'Cambiar si soportó una caída, presenta cortes en las correas o los broches fallan.',
 NULL, NULL, 'Naranja', 5, 30, TRUE, NOW(), NOW()),

('Lentes de Seguridad Policarbonato',
 'DURADERO', '3M', 'Lentes de policarbonato con protección lateral. Anti-rayadura, anti-empañante. Filtro UV 99.9%. Conforme ANSI Z87.1.',
 '3 años', '1 año en uso regular',
 'Limpiar con paño de microfibra. No usar telas ásperas ni solventes.',
 'Guardar en funda protectora. Lejos de objetos cortantes.',
 'Cambiar si los lentes presentan rayaduras profundas que reducen visibilidad.',
 NULL, NULL, 'Transparente', 20, 150, TRUE, NOW(), NOW()),

('Orejeras de Protección 3M Optime 98',
 'DURADERO', '3M', 'Orejeras con NRR 25dB. Almohadillas cómodas intercambiables. Compatible con cascos. Peso 198g.',
 '5 años', '2 años en uso regular',
 'Limpiar almohadillas con paño húmedo. Reemplazar almohadillas cada 6 meses.',
 'Guardar en estuche original. No comprimir las almohadillas.',
 'Cambiar si las almohadillas están endurecidas, rotas o el arco presenta fracturas.',
 NULL, NULL, 'Amarillo', 10, 80, TRUE, NOW(), NOW()),

('Careta Facial de Policarbonato',
 'DURADERO', 'Steelpro', 'Careta facial con visor de policarbonato anti-rayadura. Protección ante proyecciones y salpicaduras. Bandas ajustables.',
 '3 años', '1.5 años',
 'Limpiar el visor con paño suave y agua. No frotar con materiales abrasivos.',
 'Guardar en bolsa protectora colgada, lejos de solventes.',
 'Cambiar si el visor tiene rayaduras profundas o el sistema de sujeción falla.',
 NULL, NULL, 'Transparente', 8, 50, TRUE, NOW(), NOW()),

('Chaleco Reflectante Alta Visibilidad',
 'DURADERO', 'Deltaplus', 'Chaleco con 2 bandas reflectantes de 5cm. Clase 2 EN-471. Material poliéster fluorescente amarillo. Cierre de velcro.',
 '3 años', '1 año en uso regular',
 'Lavar a máquina 30°C, no usar blanqueador. Inspeccionar reflectantes regularmente.',
 'Guardar limpio y seco. No doblar sobre las bandas reflectantes.',
 'Cambiar si las bandas reflectantes pierden efectividad o el material presenta rasgaduras.',
 NULL, NULL, 'Amarillo fluorescente', 12, 100, TRUE, NOW(), NOW()),

-- CONSUMIBLES (id 9..15) — SIN TALLAS
('Mascarilla N95 Desechable 3M 8210',
 'CONSUMIBLE', '3M', 'Mascarilla filtrante de partículas N95. Filtra ≥95% partículas no aceitunas. Certificación NIOSH. Ajuste nasal flexible. Caja x20 unidades.',
 'Hasta fecha de vencimiento impresa (generalmente 5 años)', 'Un solo uso o máximo 8 horas de exposición continua',
 'No aplicable (desechable). Inspeccionar antes del uso.',
 'Lugar seco, limpio y fresco. No exponer a UV ni humedad. Temperatura 10-30°C.',
 'Desechar si está húmedo, dañado, contaminado o dificulta la respiración.',
 NULL, NULL, 'Blanco', 100, 2000, TRUE, NOW(), NOW()),

('Guante de Nitrilo Desechable (caja x100)',
 'CONSUMIBLE', 'Kimberly-Clark', 'Guante desechable de nitrilo sin polvo. Texturizado en dedos y palma. Ambidiestro. Apto para alimentos. Caja x100 unidades talla única.',
 'Hasta fecha de vencimiento impresa', 'Un solo uso',
 'No aplicable.',
 'Lugar fresco y seco, lejos de luz solar y calor. Temperatura 5-30°C.',
 'Desechar si presenta perforaciones, desgarros o contaminación.',
 NULL, NULL, 'Azul', 50, 3000, TRUE, NOW(), NOW()),

('Tapón de Oído Desechable Par (caja x200)',
 'CONSUMIBLE', 'MSA Safety', 'Tapón de espuma de poliuretano. NRR 32dB. Forma cilíndrica con cordón. Caja x200 pares.',
 'Hasta fecha de vencimiento (3 años)', 'Un solo uso',
 'No aplicable.',
 'Almacén limpio y seco. Temperatura 5-35°C.',
 'Desechar después de cada turno de uso o si el tapón pierde forma.',
 NULL, NULL, 'Naranja', 200, 5000, TRUE, NOW(), NOW()),

('Mascarilla Quirúrgica Desechable (caja x50)',
 'CONSUMIBLE', 'Kimberly-Clark', 'Mascarilla de 3 capas. Barrera ante fluidos. BFE ≥99%. Tiras elásticas. Caja x50 unidades.',
 'Hasta fecha de vencimiento (5 años)', 'Un solo uso o máximo 4 horas',
 'No aplicable.',
 'Lugar seco y limpio. Temperatura 10-30°C.',
 'Desechar si está húmedo, roto o al terminar la jornada.',
 NULL, NULL, 'Celeste', 200, 5000, TRUE, NOW(), NOW()),

('Guante de Látex para Limpieza (par)',
 'CONSUMIBLE', 'Deltaplus', 'Guante de látex natural con forro de algodón interior. Puño largo 30cm. Resistente a productos de limpieza y detergentes. Par.',
 '18 meses', 'Hasta 30 usos o cuando presente perforaciones',
 'Enjuagar con agua después de usar. Secar al aire invertido.',
 'Lugar fresco y seco. No exponer a calor excesivo.',
 'Cambiar si presenta perforaciones, pérdida de elasticidad o coloración.',
 NULL, NULL, 'Amarillo', 100, 2000, TRUE, NOW(), NOW()),

('Cinta Señalizadora Peligro (rollo 200m)',
 'CONSUMIBLE', 'Brady', 'Cinta de polietileno bicolor rojo/blanco. Leyenda PELIGRO / DANGER. Ancho 7.5cm. Rollo de 200 metros.',
 '3 años', 'Uso único por demarcación',
 'No aplicable.',
 'Lugar seco. No comprimir el rollo.',
 'Desechar al terminar la demarcación o si está rota.',
 NULL, NULL, 'Rojo/Blanco', 10, 200, TRUE, NOW(), NOW());

-- ============================================================
-- 8. ASOCIAR TALLAS A EPPs DURADEROS
--    Consumibles (9-15) no llevan tallas
-- ============================================================
-- Casco (id=1): XS, S, M, L, XL
INSERT INTO epp.epp_talla (epp_id, talla_id) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5);

-- Bota (id=2): 36,37,38,39,40,41,42,43
INSERT INTO epp.epp_talla (epp_id, talla_id) VALUES
(2,7),(2,8),(2,9),(2,10),(2,11),(2,12),(2,13),(2,14);

-- Guante cuero (id=3): S, M, L, XL
INSERT INTO epp.epp_talla (epp_id, talla_id) VALUES
(3,2),(3,3),(3,4),(3,5);

-- Arnés (id=4): S, M, L, XL
INSERT INTO epp.epp_talla (epp_id, talla_id) VALUES
(4,2),(4,3),(4,4),(4,5);

-- Lentes (id=5): UNICA
INSERT INTO epp.epp_talla (epp_id, talla_id) VALUES (5,15);

-- Orejeras (id=6): UNICA
INSERT INTO epp.epp_talla (epp_id, talla_id) VALUES (6,15);

-- Careta (id=7): UNICA
INSERT INTO epp.epp_talla (epp_id, talla_id) VALUES (7,15);

-- Chaleco (id=8): S, M, L, XL, XXL
INSERT INTO epp.epp_talla (epp_id, talla_id) VALUES
(8,2),(8,3),(8,4),(8,5),(8,6);

-- ============================================================
-- 9. ASOCIAR NORMAS A EPPs
-- ============================================================
-- Casco (id=1): ANSI-Z89.1, NTP-399.018, NTP-ISO-3873
INSERT INTO epp.catalogo_epp_norma (epp_id, norma_id) VALUES (1,1),(1,2),(1,18);
-- Bota (id=2): ANSI-Z41, NTP-ISO-20345, EN-ISO-20345, OSHA-1910.136
INSERT INTO epp.catalogo_epp_norma (epp_id, norma_id) VALUES (2,11),(2,12),(2,13),(2,19);
-- Guante cuero (id=3): EN-388, ANSI-ISEA-105
INSERT INTO epp.catalogo_epp_norma (epp_id, norma_id) VALUES (3,10),(3,21);
-- Arnés (id=4): EN-361, EN-358
INSERT INTO epp.catalogo_epp_norma (epp_id, norma_id) VALUES (4,14),(4,15);
-- Lentes (id=5): ANSI-Z87.1, EN-166
INSERT INTO epp.catalogo_epp_norma (epp_id, norma_id) VALUES (5,3),(5,4);
-- Orejeras (id=6): ANSI-S3.19, EN-352-1
INSERT INTO epp.catalogo_epp_norma (epp_id, norma_id) VALUES (6,5),(6,6);
-- Careta (id=7): ANSI-Z87.1, EN-166
INSERT INTO epp.catalogo_epp_norma (epp_id, norma_id) VALUES (7,3),(7,4);
-- Chaleco (id=8): EN-340, EN-471
INSERT INTO epp.catalogo_epp_norma (epp_id, norma_id) VALUES (8,16),(8,17);
-- Mascarilla N95 (id=9): NIOSH-42C84, EN-149
INSERT INTO epp.catalogo_epp_norma (epp_id, norma_id) VALUES (9,7),(9,8);
-- Guante nitrilo (id=10): EN-374
INSERT INTO epp.catalogo_epp_norma (epp_id, norma_id) VALUES (10,9);
-- Tapones (id=11): ANSI-S3.19, EN-352-1
INSERT INTO epp.catalogo_epp_norma (epp_id, norma_id) VALUES (11,5),(11,6);
-- Mascarilla quirúrgica (id=12): NIOSH-42C84
INSERT INTO epp.catalogo_epp_norma (epp_id, norma_id) VALUES (12,7);
-- Guante látex (id=13): EN-374
INSERT INTO epp.catalogo_epp_norma (epp_id, norma_id) VALUES (13,9);

-- ============================================================
-- 10. TRABAJADORES (30 trabajadores distribuidos en 5 áreas)
-- ============================================================
INSERT INTO epp.trabajador (dni, nombres, apellidos, codigo_qr_photocheck, area_id, cargo, fecha_ingreso, telefono, email, estado, fecha_creacion, fecha_actualizacion) VALUES
-- Área 1: Mantenimiento (6 trabajadores)
('45123678','Pedro','Mamani Quispe',    'QR-20240301-000001',1,'Técnico Electricista',   '2022-03-01','987001001','p.mamani@upeu.edu.pe',    'ACTIVO',NOW(),NOW()),
('45234789','Luis','Condori Apaza',     'QR-20240301-000002',1,'Técnico Mecánico',       '2021-06-15','987001002','l.condori@upeu.edu.pe',   'ACTIVO',NOW(),NOW()),
('45345890','José','Huanca Turpo',      'QR-20240301-000003',1,'Técnico de Plomería',    '2023-01-10','987001003','j.huanca@upeu.edu.pe',    'ACTIVO',NOW(),NOW()),
('45456901','Edwin','Ccoa Huayhua',     'QR-20240301-000004',1,'Auxiliar Mantenimiento', '2022-08-20','987001004','e.ccoa@upeu.edu.pe',      'ACTIVO',NOW(),NOW()),
('45567012','Wilfredo','Puma Larico',   'QR-20240301-000005',1,'Técnico Soldador',       '2020-11-05','987001005','w.puma@upeu.edu.pe',      'ACTIVO',NOW(),NOW()),
('45678123','Roberto','Alave Flores',   'QR-20240301-000006',1,'Técnico Electricista Jr','2023-09-01','987001006','r.alave@upeu.edu.pe',     'ACTIVO',NOW(),NOW()),
-- Área 2: Limpieza (6 trabajadores)
('46123678','Ana','Churata Mamani',     'QR-20240301-000007',2,'Operaria de Limpieza',   '2021-04-01','987002001','a.churata@upeu.edu.pe',   'ACTIVO',NOW(),NOW()),
('46234789','Rosa','Ticona Vargas',     'QR-20240301-000008',2,'Operaria de Limpieza',   '2022-02-15','987002002','r.ticona@upeu.edu.pe',    'ACTIVO',NOW(),NOW()),
('46345890','Carmen','Villca Quenta',   'QR-20240301-000009',2,'Operaria Senior',        '2019-07-20','987002003','c.villca@upeu.edu.pe',    'ACTIVO',NOW(),NOW()),
('46456901','Silvia','Apaza Cruz',      'QR-20240301-000010',2,'Operaria de Limpieza',   '2023-03-10','987002004','s.apaza@upeu.edu.pe',     'ACTIVO',NOW(),NOW()),
('46567012','Juan','Quispe Layme',      'QR-20240301-000011',2,'Operario de Limpieza',   '2022-10-01','987002005','j.quispe@upeu.edu.pe',    'ACTIVO',NOW(),NOW()),
('46678123','María','Cutipa Soncco',    'QR-20240301-000012',2,'Supervisora de Limpieza','2020-05-15','987002006','m.cutipa@upeu.edu.pe',    'ACTIVO',NOW(),NOW()),
-- Área 3: Laboratorios (5 trabajadores)
('47123678','Carlos','Lupaca Chura',    'QR-20240301-000013',3,'Técnico de Laboratorio', '2021-08-01','987003001','c.lupaca@upeu.edu.pe',    'ACTIVO',NOW(),NOW()),
('47234789','Sandra','Flores Pilco',    'QR-20240301-000014',3,'Asistente de Lab',       '2022-05-20','987003002','s.flores@upeu.edu.pe',    'ACTIVO',NOW(),NOW()),
('47345890','Marco','Calla Maquera',    'QR-20240301-000015',3,'Técnico Químico',        '2020-12-10','987003003','m.calla@upeu.edu.pe',     'ACTIVO',NOW(),NOW()),
('47456901','Diana','Sucasaca Pari',    'QR-20240301-000016',3,'Asistente de Lab',       '2023-04-05','987003004','d.sucasaca@upeu.edu.pe',  'ACTIVO',NOW(),NOW()),
('47567012','Victor','Tito Huanca',     'QR-20240301-000017',3,'Técnico Senior',         '2019-09-15','987003005','v.tito@upeu.edu.pe',      'ACTIVO',NOW(),NOW()),
-- Área 4: Cocina (7 trabajadores)
('48123678','Elena','Catacora Mamani',  'QR-20240301-000018',4,'Cocinera',               '2021-01-10','987004001','e.catacora@upeu.edu.pe',  'ACTIVO',NOW(),NOW()),
('48234789','Gloria','Callata Quispe',  'QR-20240301-000019',4,'Cocinera',               '2022-07-01','987004002','g.callata@upeu.edu.pe',   'ACTIVO',NOW(),NOW()),
('48345890','Patricia','Humpiri Aguilar','QR-20240301-000020',4,'Auxiliar de Cocina',    '2023-02-15','987004003','p.humpiri@upeu.edu.pe',   'ACTIVO',NOW(),NOW()),
('48456901','Nancy','Sánchez Torres',   'QR-20240301-000021',4,'Auxiliar de Cocina',     '2020-08-20','987004004','n.sanchez@upeu.edu.pe',   'ACTIVO',NOW(),NOW()),
('48567012','Lucía','Vargas Benítez',   'QR-20240301-000022',4,'Cocinera Senior',        '2018-11-01','987004005','l.vargas@upeu.edu.pe',    'ACTIVO',NOW(),NOW()),
('48678123','Hugo','Pinto Quecaño',     'QR-20240301-000023',4,'Ayudante de Cocina',     '2023-06-10','987004006','h.pinto@upeu.edu.pe',     'ACTIVO',NOW(),NOW()),
('48789234','Elsa','Ramos Villegas',    'QR-20240301-000024',4,'Auxiliar de Cocina',     '2022-04-05','987004007','e.ramos@upeu.edu.pe',     'ACTIVO',NOW(),NOW()),
-- Área 5: Construcción (6 trabajadores)
('49123678','Máximo','Calizaya Yucra',  'QR-20240301-000025',5,'Operario de Construcción','2022-01-15','987005001','m.calizaya@upeu.edu.pe', 'ACTIVO',NOW(),NOW()),
('49234789','Julio','Tapia Cohaila',    'QR-20240301-000026',5,'Operario de Construcción','2021-10-01','987005002','j.tapia@upeu.edu.pe',    'ACTIVO',NOW(),NOW()),
('49345890','Fredy','Mamani Roque',     'QR-20240301-000027',5,'Albañil Oficial',        '2020-06-20','987005003','f.mamani@upeu.edu.pe',    'ACTIVO',NOW(),NOW()),
('49456901','Raúl','Huanca Lima',       'QR-20240301-000028',5,'Albañil Ayudante',       '2023-05-01','987005004','r.huanca@upeu.edu.pe',    'ACTIVO',NOW(),NOW()),
('49567012','Bruno','Apaza Ramos',      'QR-20240301-000029',5,'Operario Senior',        '2019-03-10','987005005','b.apaza@upeu.edu.pe',     'ACTIVO',NOW(),NOW()),
('49678123','Nelly','Quispe Zarate',    'QR-20240301-000030',5,'Auxiliar de Obra',       '2023-08-01','987005006','n.quispe@upeu.edu.pe',    'ACTIVO',NOW(),NOW());

-- ============================================================
-- 11. COMPRAS (8 facturas distribuidas en 8 meses)
--     Diseñadas para mostrar tendencias en estadísticas
-- ============================================================

-- COMPRA 1: Octubre 2024 — Dotación inicial masiva
INSERT INTO epp.compra (nro_factura, fecha_compra, proveedor, subtotal, igv, monto_total, ruta_archivo_factura, ruta_archivo_cotizacion, usuario_registro_id, fecha_registro) VALUES
('F001-2024-001', '2024-10-01', '3M del Perú SAC', 8898.31, 1601.69, 10500.00, NULL, NULL, 3, '2024-10-01 09:00:00');

INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal) VALUES
(1, 1, 3,  20, 118.00, 2360.00),  -- Casco talla M ×20
(1, 1, 4,  15, 118.00, 1770.00),  -- Casco talla L ×15
(1, 2, 10, 10, 190.00, 1900.00),  -- Bota talla 40 ×10
(1, 2, 11, 10, 190.00, 1900.00),  -- Bota talla 41 ×10
(1, 5, 15, 30,  85.00, 2550.00);  -- Lentes talla única ×30

-- COMPRA 2: Octubre 2024 — Consumibles primera dotación
INSERT INTO epp.compra (nro_factura, fecha_compra, proveedor, subtotal, igv, monto_total, ruta_archivo_factura, ruta_archivo_cotizacion, usuario_registro_id, fecha_registro) VALUES
('F001-2024-002', '2024-10-05', 'SegurProt SAC', 2779.66, 500.34, 3280.00, NULL, NULL, 1, '2024-10-05 10:30:00');

INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal) VALUES
(2,  9, NULL, 50,  28.00, 1400.00),  -- Mascarilla N95 ×50 cajas
(2, 10, NULL, 80,  12.00,  960.00),  -- Guante nitrilo ×80 cajas
(2, 11, NULL, 30,   8.00,  240.00),  -- Tapones ×30 cajas
(2, 14, NULL, 20,  34.00,  680.00);  -- Cinta señalizadora ×20 rollos

-- COMPRA 3: Noviembre 2024 — Refuerzo EPPs duraderos
INSERT INTO epp.compra (nro_factura, fecha_compra, proveedor, subtotal, igv, monto_total, ruta_archivo_factura, ruta_archivo_cotizacion, usuario_registro_id, fecha_registro) VALUES
('F001-2024-003', '2024-11-10', 'Kimberly-Clark Perú', 4728.81, 851.19, 5580.00, NULL, NULL, 1, '2024-11-10 08:45:00');

INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal) VALUES
(3, 3, 3,  15, 75.00, 1125.00),   -- Guante cuero talla M ×15
(3, 3, 4,  15, 75.00, 1125.00),   -- Guante cuero talla L ×15
(3, 6, 15, 20, 95.00, 1900.00),   -- Orejeras ×20
(3, 8, 3,  10, 58.00,  580.00),   -- Chaleco talla M ×10
(3, 8, 4,  10, 58.00,  580.00),   -- Chaleco talla L ×10
(3, 8, 5,   5, 58.00,  290.00);   -- Chaleco talla XL ×5

-- COMPRA 4: Diciembre 2024 — Consumibles reposición
INSERT INTO epp.compra (nro_factura, fecha_compra, proveedor, subtotal, igv, monto_total, ruta_archivo_factura, ruta_archivo_cotizacion, usuario_registro_id, fecha_registro) VALUES
('F001-2024-004', '2024-12-03', 'SegurProt SAC', 2050.85, 369.15, 2420.00, NULL, NULL, 2, '2024-12-03 09:15:00');

INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal) VALUES
(4,  9, NULL, 40,  28.00, 1120.00),  -- Mascarilla N95
(4, 12, NULL, 60,  10.00,  600.00),  -- Mascarilla quirúrgica
(4, 13, NULL, 40,  18.00,  720.00);  -- Guante látex limpieza

-- COMPRA 5: Enero 2025 — Arneses y equipo especializado
INSERT INTO epp.compra (nro_factura, fecha_compra, proveedor, subtotal, igv, monto_total, ruta_archivo_factura, ruta_archivo_cotizacion, usuario_registro_id, fecha_registro) VALUES
('F001-2025-001', '2025-01-08', 'MSA Safety Perú', 5932.20, 1067.80, 7000.00, NULL, NULL, 3, '2025-01-08 08:00:00');

INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal) VALUES
(5, 4, 3, 5, 320.00, 1600.00),  -- Arnés talla M ×5
(5, 4, 4, 5, 320.00, 1600.00),  -- Arnés talla L ×5
(5, 7, 15, 10, 140.00, 1400.00), -- Careta facial ×10
(5, 2, 9,  8, 195.00, 1560.00),  -- Bota talla 39 ×8 (precio subió)
(5, 2, 12, 7, 195.00, 1365.00);  -- Bota talla 42 ×7

-- COMPRA 6: Febrero 2025 — Consumibles alta demanda cocina
INSERT INTO epp.compra (nro_factura, fecha_compra, proveedor, subtotal, igv, monto_total, ruta_archivo_factura, ruta_archivo_cotizacion, usuario_registro_id, fecha_registro) VALUES
('F001-2025-002', '2025-02-14', 'Kimberly-Clark Perú', 1847.46, 332.54, 2180.00, NULL, NULL, 1, '2025-02-14 10:00:00');

INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal) VALUES
(6, 10, NULL, 60,  12.50,  750.00),  -- Guante nitrilo (precio subió)
(6, 12, NULL, 50,  10.50,  525.00),  -- Mascarilla quirúrgica
(6, 13, NULL, 30,  18.50,  555.00),  -- Guante látex
(6, 14, NULL, 10,  35.00,  350.00);  -- Cinta señalizadora

-- COMPRA 7: Marzo 2025 — Cascos y lentes reposición + nuevo precio
INSERT INTO epp.compra (nro_factura, fecha_compra, proveedor, subtotal, igv, monto_total, ruta_archivo_factura, ruta_archivo_cotizacion, usuario_registro_id, fecha_registro) VALUES
('F001-2025-003', '2025-03-05', '3M del Perú SAC', 3677.97, 662.03, 4340.00, NULL, NULL, 2, '2025-03-05 09:30:00');

INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal) VALUES
(7, 1, 3,  10, 125.00, 1250.00),  -- Casco talla M (precio subió a 125)
(7, 1, 4,  10, 125.00, 1250.00),  -- Casco talla L
(7, 5, 15, 20,  92.00, 1840.00);  -- Lentes (precio subió a 92)

-- COMPRA 8: Abril 2025 — Consumibles obras + EPPs especiales
INSERT INTO epp.compra (nro_factura, fecha_compra, proveedor, subtotal, igv, monto_total, ruta_archivo_factura, ruta_archivo_cotizacion, usuario_registro_id, fecha_registro) VALUES
('F001-2025-004', '2025-04-02', 'SegurProt SAC', 3186.44, 573.56, 3760.00, NULL, NULL, 1, '2025-04-02 08:30:00');

INSERT INTO epp.detalle_compra (compra_id, epp_id, talla_id, cantidad, precio_unitario, subtotal) VALUES
(8,  9, NULL, 40,  29.00, 1160.00),  -- Mascarilla N95 (precio 29)
(8, 11, NULL, 30,   8.50,  255.00),  -- Tapones
(8, 14, NULL, 15,  36.00,  540.00),  -- Cinta señalizadora
(8,  8, 5,   10,  60.00,  600.00),   -- Chaleco XL (precio 60)
(8,  6, 15,  10,  98.00,  980.00);   -- Orejeras (precio 98)

-- ============================================================
-- 12. INVENTARIO CENTRAL
--     Refleja el resultado de las compras (lote = nro_factura)
--     Estado EN_STOCK = id 2
-- ============================================================

-- De compra 1: F001-2024-001
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, fecha_adquisicion, costo_unitario, proveedor, ubicacion_bodega, fecha_creacion, ultima_actualizacion) VALUES
(1, 2, 3,  12, 'F001-2024-001', '2024-10-01', 118.00, '3M del Perú SAC',      'Estante A-1', NOW(), NOW()),
(1, 2, 4,   8, 'F001-2024-001', '2024-10-01', 118.00, '3M del Perú SAC',      'Estante A-1', NOW(), NOW()),
(2, 2, 10,  5, 'F001-2024-001', '2024-10-01', 190.00, '3M del Perú SAC',      'Estante B-1', NOW(), NOW()),
(2, 2, 11,  4, 'F001-2024-001', '2024-10-01', 190.00, '3M del Perú SAC',      'Estante B-1', NOW(), NOW()),
(5, 2, 15, 18, 'F001-2024-001', '2024-10-01',  85.00, '3M del Perú SAC',      'Estante C-1', NOW(), NOW());

-- De compra 2: F001-2024-002
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, fecha_adquisicion, costo_unitario, proveedor, ubicacion_bodega, fecha_creacion, ultima_actualizacion) VALUES
( 9, 2, NULL, 20, 'F001-2024-002', '2024-10-05',  28.00, 'SegurProt SAC', 'Estante D-1', NOW(), NOW()),
(10, 2, NULL, 30, 'F001-2024-002', '2024-10-05',  12.00, 'SegurProt SAC', 'Estante D-2', NOW(), NOW()),
(11, 2, NULL, 15, 'F001-2024-002', '2024-10-05',   8.00, 'SegurProt SAC', 'Estante D-3', NOW(), NOW()),
(14, 2, NULL, 10, 'F001-2024-002', '2024-10-05',  34.00, 'SegurProt SAC', 'Estante D-4', NOW(), NOW());

-- De compra 3: F001-2024-003
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, fecha_adquisicion, costo_unitario, proveedor, ubicacion_bodega, fecha_creacion, ultima_actualizacion) VALUES
(3, 2, 3, 10, 'F001-2024-003', '2024-11-10',  75.00, 'Kimberly-Clark Perú', 'Estante A-2', NOW(), NOW()),
(3, 2, 4,  8, 'F001-2024-003', '2024-11-10',  75.00, 'Kimberly-Clark Perú', 'Estante A-2', NOW(), NOW()),
(6, 2, 15,12, 'F001-2024-003', '2024-11-10',  95.00, 'Kimberly-Clark Perú', 'Estante C-2', NOW(), NOW()),
(8, 2, 3,  8, 'F001-2024-003', '2024-11-10',  58.00, 'Kimberly-Clark Perú', 'Estante E-1', NOW(), NOW()),
(8, 2, 4,  7, 'F001-2024-003', '2024-11-10',  58.00, 'Kimberly-Clark Perú', 'Estante E-1', NOW(), NOW()),
(8, 2, 5,  3, 'F001-2024-003', '2024-11-10',  58.00, 'Kimberly-Clark Perú', 'Estante E-1', NOW(), NOW());

-- De compra 4: F001-2024-004
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, fecha_adquisicion, costo_unitario, proveedor, ubicacion_bodega, fecha_creacion, ultima_actualizacion) VALUES
( 9, 2, NULL, 15, 'F001-2024-004', '2024-12-03',  28.00, 'SegurProt SAC', 'Estante D-1', NOW(), NOW()),
(12, 2, NULL, 30, 'F001-2024-004', '2024-12-03',  10.00, 'SegurProt SAC', 'Estante D-5', NOW(), NOW()),
(13, 2, NULL, 20, 'F001-2024-004', '2024-12-03',  18.00, 'SegurProt SAC', 'Estante D-6', NOW(), NOW());

-- De compra 5: F001-2025-001
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, fecha_adquisicion, costo_unitario, proveedor, ubicacion_bodega, fecha_creacion, ultima_actualizacion) VALUES
(4, 2, 3, 4, 'F001-2025-001', '2025-01-08', 320.00, 'MSA Safety Perú', 'Estante F-1', NOW(), NOW()),
(4, 2, 4, 4, 'F001-2025-001', '2025-01-08', 320.00, 'MSA Safety Perú', 'Estante F-1', NOW(), NOW()),
(7, 2, 15,7, 'F001-2025-001', '2025-01-08', 140.00, 'MSA Safety Perú', 'Estante C-3', NOW(), NOW()),
(2, 2, 9, 5, 'F001-2025-001', '2025-01-08', 195.00, 'MSA Safety Perú', 'Estante B-2', NOW(), NOW()),
(2, 2, 12,4, 'F001-2025-001', '2025-01-08', 195.00, 'MSA Safety Perú', 'Estante B-2', NOW(), NOW());

-- De compra 6: F001-2025-002
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, fecha_adquisicion, costo_unitario, proveedor, ubicacion_bodega, fecha_creacion, ultima_actualizacion) VALUES
(10, 2, NULL, 40, 'F001-2025-002', '2025-02-14',  12.50, 'Kimberly-Clark Perú', 'Estante D-2', NOW(), NOW()),
(12, 2, NULL, 30, 'F001-2025-002', '2025-02-14',  10.50, 'Kimberly-Clark Perú', 'Estante D-5', NOW(), NOW()),
(13, 2, NULL, 20, 'F001-2025-002', '2025-02-14',  18.50, 'Kimberly-Clark Perú', 'Estante D-6', NOW(), NOW()),
(14, 2, NULL,  8, 'F001-2025-002', '2025-02-14',  35.00, 'Kimberly-Clark Perú', 'Estante D-4', NOW(), NOW());

-- De compra 7: F001-2025-003
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, fecha_adquisicion, costo_unitario, proveedor, ubicacion_bodega, fecha_creacion, ultima_actualizacion) VALUES
(1, 2, 3,  8, 'F001-2025-003', '2025-03-05', 125.00, '3M del Perú SAC', 'Estante A-1', NOW(), NOW()),
(1, 2, 4,  7, 'F001-2025-003', '2025-03-05', 125.00, '3M del Perú SAC', 'Estante A-1', NOW(), NOW()),
(5, 2, 15,15, 'F001-2025-003', '2025-03-05',  92.00, '3M del Perú SAC', 'Estante C-1', NOW(), NOW());

-- De compra 8: F001-2025-004
INSERT INTO epp.inventario_central (epp_id, estado_id, talla_id, cantidad_actual, lote, fecha_adquisicion, costo_unitario, proveedor, ubicacion_bodega, fecha_creacion, ultima_actualizacion) VALUES
( 9, 2, NULL, 25, 'F001-2025-004', '2025-04-02',  29.00, 'SegurProt SAC', 'Estante D-1', NOW(), NOW()),
(11, 2, NULL, 20, 'F001-2025-004', '2025-04-02',   8.50, 'SegurProt SAC', 'Estante D-3', NOW(), NOW()),
(14, 2, NULL,  8, 'F001-2025-004', '2025-04-02',  36.00, 'SegurProt SAC', 'Estante D-4', NOW(), NOW()),
( 8, 2, 5,    8, 'F001-2025-004', '2025-04-02',  60.00, 'SegurProt SAC', 'Estante E-1', NOW(), NOW()),
( 6, 2, 15,   8, 'F001-2025-004', '2025-04-02',  98.00, 'SegurProt SAC', 'Estante C-2', NOW(), NOW());

-- ============================================================
-- 13. INVENTARIO DE ÁREA
--     Cada área recibe stock apropiado para sus actividades
-- ============================================================

-- ÁREA 1: MANTENIMIENTO — Necesita cascos, botas, guantes cuero, lentes, arneses, orejeras
INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion) VALUES
(1, 1, 2, 3,  5, 'Armario Seguridad M-1', NOW()),  -- Casco M
(1, 1, 2, 4,  5, 'Armario Seguridad M-1', NOW()),  -- Casco L
(2, 1, 2, 10, 4, 'Armario Seguridad M-1', NOW()),  -- Bota 40
(2, 1, 2, 11, 4, 'Armario Seguridad M-1', NOW()),  -- Bota 41
(3, 1, 2, 3,  5, 'Armario Seguridad M-1', NOW()),  -- Guante cuero M
(3, 1, 2, 4,  5, 'Armario Seguridad M-1', NOW()),  -- Guante cuero L
(4, 1, 2, 3,  2, 'Armario Seguridad M-1', NOW()),  -- Arnés M
(4, 1, 2, 4,  2, 'Armario Seguridad M-1', NOW()),  -- Arnés L
(5, 1, 2, 15, 6, 'Armario Seguridad M-1', NOW()),  -- Lentes
(6, 1, 2, 15, 5, 'Armario Seguridad M-1', NOW()),  -- Orejeras
(9, 1, 2, NULL,8,'Almacén M-2',           NOW()),  -- Mascarillas N95
(11,1, 2, NULL,10,'Almacén M-2',          NOW());  -- Tapones

-- ÁREA 2: LIMPIEZA — Guantes látex, mascarillas, cinta, chalecos
INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion) VALUES
(8, 2, 2, 3,  3, 'Depósito L-1', NOW()),   -- Chaleco M
(8, 2, 2, 4,  3, 'Depósito L-1', NOW()),   -- Chaleco L
(12,2, 2, NULL,15,'Depósito L-2', NOW()),  -- Mascarilla quirúrgica
(13,2, 2, NULL,10,'Depósito L-2', NOW()),  -- Guante látex
(14,2, 2, NULL, 5,'Depósito L-3', NOW());  -- Cinta señalizadora

-- ÁREA 3: LABORATORIOS — Lentes, mascarillas N95, guantes nitrilo, careta, guantes látex
INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion) VALUES
(5, 3, 2, 15, 6, 'Vitrina Lab-1', NOW()),  -- Lentes
(7, 3, 2, 15, 4, 'Vitrina Lab-1', NOW()),  -- Careta facial
(9, 3, 2, NULL,8,'Depósito Lab-2', NOW()), -- Mascarilla N95
(10,3, 2, NULL,15,'Depósito Lab-2',NOW()), -- Guante nitrilo
(13,3, 2, NULL,8,'Depósito Lab-2', NOW()); -- Guante látex

-- ÁREA 4: COCINA — Guantes nitrilo, guantes látex, mascarilla quirúrgica
INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion) VALUES
(10,4, 2, NULL,20,'Cocina Armario C-1', NOW()),  -- Guante nitrilo
(12,4, 2, NULL,15,'Cocina Armario C-1', NOW()),  -- Mascarilla quirúrgica
(13,4, 2, NULL,10,'Cocina Armario C-1', NOW());  -- Guante látex

-- ÁREA 5: CONSTRUCCIÓN — Cascos, botas, arnés, chaleco, lentes, orejeras, cinta
INSERT INTO epp.inventario_area (epp_id, area_id, estado_id, talla_id, cantidad_actual, ubicacion, ultima_actualizacion) VALUES
(1, 5, 2, 3,  4, 'Container Obra-1', NOW()),  -- Casco M
(1, 5, 2, 4,  4, 'Container Obra-1', NOW()),  -- Casco L
(2, 5, 2, 9,  3, 'Container Obra-1', NOW()),  -- Bota 39
(2, 5, 2, 12, 3, 'Container Obra-1', NOW()),  -- Bota 42
(4, 5, 2, 4,  2, 'Container Obra-2', NOW()),  -- Arnés L
(5, 5, 2, 15, 5, 'Container Obra-1', NOW()),  -- Lentes
(6, 5, 2, 15, 4, 'Container Obra-2', NOW()),  -- Orejeras
(8, 5, 2, 5,  4, 'Container Obra-1', NOW()),  -- Chaleco XL
(9, 5, 2, NULL,6,'Container Obra-2', NOW()),  -- Mascarilla N95
(14,5, 2, NULL,3,'Container Obra-3', NOW());  -- Cinta señalizadora

-- ============================================================
-- 14. ENTREGAS EPP
--     Distribuidas entre Oct 2024 y Abr 2025 para tendencias
--     Supervisor 1 (usuario_id=1) y Supervisor 2 (usuario_id=2)
-- ============================================================

-- ── OCTUBRE 2024 ────────────────────────────────────────────

-- Entrega 1: Dotación inicial Mantenimiento (Oct 2024)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(1, 1, '2024-10-05 09:00:00', 'PRIMERA_ENTREGA', 'Dotación inicial - Área Mantenimiento', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(1, 1, 3, 1, 'PRIMERA_ENTREGA'),   -- Casco M
(1, 2, 11, 1, 'PRIMERA_ENTREGA'),  -- Bota 41
(1, 5, 15, 1, 'PRIMERA_ENTREGA'),  -- Lentes
(1, 6, 15, 1, 'PRIMERA_ENTREGA');  -- Orejeras

-- Entrega 2: Pedro Mamani
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(2, 1, '2024-10-05 09:30:00', 'PRIMERA_ENTREGA', 'Dotación inicial', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(2, 1, 4, 1, 'PRIMERA_ENTREGA'),
(2, 2, 10, 1, 'PRIMERA_ENTREGA'),
(2, 3, 3, 1, 'PRIMERA_ENTREGA'),
(2, 5, 15, 1, 'PRIMERA_ENTREGA');

-- Entrega 3: José Huanca
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(3, 1, '2024-10-06 08:00:00', 'PRIMERA_ENTREGA', 'Dotación inicial técnico plomería', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(3, 1, 3, 1, 'PRIMERA_ENTREGA'),
(3, 5, 15, 1, 'PRIMERA_ENTREGA'),
(3, 9, NULL, 2, 'PRIMERA_ENTREGA');

-- Entrega 4: Dotación inicial Limpieza
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(7, 2, '2024-10-08 08:00:00', 'PRIMERA_ENTREGA', 'Dotación inicial - Área Limpieza', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(4, 8, 3, 1, 'PRIMERA_ENTREGA'),   -- Chaleco M
(4, 12, NULL, 5, 'PRIMERA_ENTREGA'), -- Mascarillas quirúrgicas
(4, 13, NULL, 2, 'PRIMERA_ENTREGA'); -- Guantes látex

-- Entrega 5: Rosa Ticona
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(8, 2, '2024-10-08 08:30:00', 'PRIMERA_ENTREGA', 'Dotación inicial', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(5, 8, 4, 1, 'PRIMERA_ENTREGA'),
(5, 12, NULL, 5, 'PRIMERA_ENTREGA'),
(5, 13, NULL, 2, 'PRIMERA_ENTREGA');

-- Entrega 6: Laboratorios dotación inicial Carlos Lupaca
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(13, 1, '2024-10-10 10:00:00', 'PRIMERA_ENTREGA', 'Dotación inicial laboratorios', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(6, 5, 15, 1, 'PRIMERA_ENTREGA'),  -- Lentes
(6, 7, 15, 1, 'PRIMERA_ENTREGA'),  -- Careta
(6, 9, NULL, 3, 'PRIMERA_ENTREGA'), -- Mascarilla N95
(6, 10, NULL, 5, 'PRIMERA_ENTREGA'); -- Guante nitrilo

-- Entrega 7: Cocina Elena Catacora
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(18, 2, '2024-10-12 07:30:00', 'PRIMERA_ENTREGA', 'Dotación inicial cocina', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(7, 10, NULL, 5, 'PRIMERA_ENTREGA'),  -- Guante nitrilo
(7, 12, NULL, 5, 'PRIMERA_ENTREGA'),  -- Mascarilla quirúrgica
(7, 13, NULL, 2, 'PRIMERA_ENTREGA');  -- Guante látex

-- Entrega 8: Construcción Máximo Calizaya
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(25, 1, '2024-10-15 07:00:00', 'PRIMERA_ENTREGA', 'Dotación inicial obra', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(8, 1, 3, 1, 'PRIMERA_ENTREGA'),   -- Casco M
(8, 2, 12, 1, 'PRIMERA_ENTREGA'),  -- Bota 42
(8, 5, 15, 1, 'PRIMERA_ENTREGA'),  -- Lentes
(8, 9, NULL, 3, 'PRIMERA_ENTREGA'); -- Mascarilla N95

-- ── NOVIEMBRE 2024 ──────────────────────────────────────────

-- Entrega 9: Wilfredo Puma (soldador) — primera dotación
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(5, 1, '2024-11-05 08:00:00', 'PRIMERA_ENTREGA', 'Kit soldador completo', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(9, 1, 4, 1, 'PRIMERA_ENTREGA'),   -- Casco L
(9, 3, 4, 1, 'PRIMERA_ENTREGA'),   -- Guante cuero L
(9, 6, 15, 1, 'PRIMERA_ENTREGA'),  -- Orejeras
(9, 7, 15, 1, 'PRIMERA_ENTREGA');  -- Careta facial

-- Entrega 10: Carmen Villca (limpieza senior)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(9, 2, '2024-11-08 08:00:00', 'PRIMERA_ENTREGA', 'Dotación supervisora limpieza', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(10, 8, 3, 1, 'PRIMERA_ENTREGA'),
(10, 12, NULL, 10, 'PRIMERA_ENTREGA'),
(10, 13, NULL, 3, 'PRIMERA_ENTREGA');

-- Entrega 11: Reposición Mantenimiento Edwin Ccoa
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(4, 1, '2024-11-10 09:00:00', 'PRIMERA_ENTREGA', 'Primera dotación auxiliar', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(11, 1, 3, 1, 'PRIMERA_ENTREGA'),
(11, 5, 15, 1, 'PRIMERA_ENTREGA'),
(11, 11, NULL, 5, 'PRIMERA_ENTREGA');

-- Entrega 12: Sandra Flores (laboratorio)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(14, 1, '2024-11-12 10:00:00', 'PRIMERA_ENTREGA', 'Asistente lab nueva dotación', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(12, 5, 15, 1, 'PRIMERA_ENTREGA'),
(12, 9, NULL, 3, 'PRIMERA_ENTREGA'),
(12, 10, NULL, 5, 'PRIMERA_ENTREGA');

-- Entrega 13: Gloria Callata (cocina)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(19, 2, '2024-11-15 07:00:00', 'PRIMERA_ENTREGA', 'Dotación cocinera', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(13, 10, NULL, 5, 'PRIMERA_ENTREGA'),
(13, 12, NULL, 5, 'PRIMERA_ENTREGA');

-- ── DICIEMBRE 2024 ──────────────────────────────────────────

-- Entrega 14: Julio Tapia (construcción)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(26, 1, '2024-12-03 07:00:00', 'PRIMERA_ENTREGA', 'Operario construcción dotación', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(14, 1, 4, 1, 'PRIMERA_ENTREGA'),
(14, 2, 9, 1, 'PRIMERA_ENTREGA'),
(14, 8, 4, 1, 'PRIMERA_ENTREGA'),
(14, 9, NULL, 3, 'PRIMERA_ENTREGA');

-- Entrega 15: Patricia Humpiri (cocina)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(20, 2, '2024-12-05 07:30:00', 'PRIMERA_ENTREGA', 'Nueva auxiliar cocina', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(15, 10, NULL, 5, 'PRIMERA_ENTREGA'),
(15, 12, NULL, 5, 'PRIMERA_ENTREGA'),
(15, 13, NULL, 2, 'PRIMERA_ENTREGA');

-- Entrega 16: Reposición mascarillas Laboratorio Marco Calla
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(15, 1, '2024-12-10 09:00:00', 'REPOSICION', 'Reposición mensual consumibles lab', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(16, 9, NULL, 3, 'REPOSICION'),
(16, 10, NULL, 10, 'REPOSICION');

-- Entrega 17: Reposición guantes limpieza Silvia Apaza
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(10, 2, '2024-12-12 08:00:00', 'PRIMERA_ENTREGA', 'Nueva operaria limpieza', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(17, 8, 3, 1, 'PRIMERA_ENTREGA'),
(17, 12, NULL, 5, 'PRIMERA_ENTREGA'),
(17, 13, NULL, 2, 'PRIMERA_ENTREGA');

-- ── ENERO 2025 ──────────────────────────────────────────────

-- Entrega 18: Fredy Mamani (albañil) — kit obra
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(27, 1, '2025-01-10 07:00:00', 'PRIMERA_ENTREGA', 'Albañil oficial dotación completa', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(18, 1, 3, 1, 'PRIMERA_ENTREGA'),
(18, 2, 12, 1, 'PRIMERA_ENTREGA'),
(18, 4, 4, 1, 'PRIMERA_ENTREGA'),  -- Arnés L
(18, 5, 15, 1, 'PRIMERA_ENTREGA'),
(18, 8, 5, 1, 'PRIMERA_ENTREGA');  -- Chaleco XL

-- Entrega 19: Roberto Alave (nuevo electricista)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(6, 1, '2025-01-12 08:30:00', 'PRIMERA_ENTREGA', 'Nuevo técnico electricista jr', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(19, 1, 3, 1, 'PRIMERA_ENTREGA'),
(19, 5, 15, 1, 'PRIMERA_ENTREGA'),
(19, 6, 15, 1, 'PRIMERA_ENTREGA'),
(19, 9, NULL, 2, 'PRIMERA_ENTREGA');

-- Entrega 20: Reposición mensual cocina — Elena Catacora
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(18, 2, '2025-01-15 07:00:00', 'REPOSICION', 'Reposición mensual consumibles cocina', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(20, 10, NULL, 5, 'REPOSICION'),
(20, 12, NULL, 5, 'REPOSICION');

-- Entrega 21: Victor Tito (lab senior) reposición
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(17, 1, '2025-01-20 10:00:00', 'REPOSICION', 'Reposición consumibles técnico senior', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(21, 9, NULL, 3, 'REPOSICION'),
(21, 10, NULL, 10, 'REPOSICION');

-- ── FEBRERO 2025 ────────────────────────────────────────────

-- Entrega 22: Nancy Sánchez (cocina)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(21, 2, '2025-02-03 07:30:00', 'PRIMERA_ENTREGA', 'Dotación auxiliar cocina', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(22, 10, NULL, 5, 'PRIMERA_ENTREGA'),
(22, 12, NULL, 5, 'PRIMERA_ENTREGA'),
(22, 13, NULL, 2, 'PRIMERA_ENTREGA');

-- Entrega 23: Raúl Huanca (obra)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(28, 1, '2025-02-05 07:00:00', 'PRIMERA_ENTREGA', 'Nuevo ayudante albañil', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(23, 1, 3, 1, 'PRIMERA_ENTREGA'),
(23, 8, 4, 1, 'PRIMERA_ENTREGA'),
(23, 9, NULL, 3, 'PRIMERA_ENTREGA');

-- Entrega 24: Reposición mascarillas limpieza — Carmen Villca
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(9, 2, '2025-02-10 08:00:00', 'REPOSICION', 'Reposición mensual limpieza', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(24, 12, NULL, 10, 'REPOSICION'),
(24, 13, NULL, 3, 'REPOSICION');

-- Entrega 25: Diana Sucasaca (lab)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(16, 1, '2025-02-12 10:00:00', 'PRIMERA_ENTREGA', 'Nueva asistente laboratorio', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(25, 5, 15, 1, 'PRIMERA_ENTREGA'),
(25, 9, NULL, 3, 'PRIMERA_ENTREGA'),
(25, 10, NULL, 5, 'PRIMERA_ENTREGA');

-- ── MARZO 2025 ──────────────────────────────────────────────

-- Entrega 26: Reposición Juan Quispe (limpieza)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(11, 2, '2025-03-05 08:00:00', 'REPOSICION', 'Reposición guantes desgastados', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(26, 12, NULL, 10, 'REPOSICION'),
(26, 13, NULL, 3, 'REPOSICION');

-- Entrega 27: Bruno Apaza (obra senior) reposición
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(29, 1, '2025-03-08 07:00:00', 'REPOSICION', 'Reposición cascos desgastados obra', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(27, 1, 4, 1, 'REPOSICION'),
(27, 5, 15, 1, 'REPOSICION'),
(27, 9, NULL, 3, 'REPOSICION');

-- Entrega 28: Reposición mascarillas N95 Mantenimiento Luis Condori
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(2, 1, '2025-03-10 09:00:00', 'REPOSICION', 'Reposición trimestral mantenimiento', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(28, 9, NULL, 3, 'REPOSICION'),
(28, 11, NULL, 5, 'REPOSICION');

-- Entrega 29: Reposición cocina Gloria Callata
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(19, 2, '2025-03-15 07:30:00', 'REPOSICION', 'Reposición mensual', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(29, 10, NULL, 5, 'REPOSICION'),
(29, 12, NULL, 5, 'REPOSICION');

-- Entrega 30: Nelly Quispe (obra nueva trabajadora)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(30, 1, '2025-03-20 07:00:00', 'PRIMERA_ENTREGA', 'Nueva auxiliar de obra', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(30, 1, 3, 1, 'PRIMERA_ENTREGA'),
(30, 8, 3, 1, 'PRIMERA_ENTREGA'),
(30, 9, NULL, 3, 'PRIMERA_ENTREGA');

-- ── ABRIL 2025 ──────────────────────────────────────────────

-- Entrega 31: Reposición completa laboratorio Victor Tito
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(17, 1, '2025-04-05 10:00:00', 'REPOSICION', 'Reposición trimestral laboratorio', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(31, 9, NULL, 3, 'REPOSICION'),
(31, 10, NULL, 10, 'REPOSICION');

-- Entrega 32: Hugo Pinto (ayudante cocina)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(23, 2, '2025-04-08 07:30:00', 'PRIMERA_ENTREGA', 'Nuevo ayudante cocina', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(32, 10, NULL, 5, 'PRIMERA_ENTREGA'),
(32, 12, NULL, 5, 'PRIMERA_ENTREGA'),
(32, 13, NULL, 2, 'PRIMERA_ENTREGA');

-- Entrega 33: Emergencia — Obra Civil
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(27, 1, '2025-04-10 07:00:00', 'EMERGENCIA', 'Reposición urgente por deterioro acelerado', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(33, 1, 3, 1, 'REPOSICION'),
(33, 6, 15, 1, 'REPOSICION'),
(33, 8, 5, 1, 'REPOSICION');

-- Entrega 34: Elsa Ramos (cocina)
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(24, 2, '2025-04-12 07:30:00', 'PRIMERA_ENTREGA', 'Nueva auxiliar cocina', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(34, 10, NULL, 5, 'PRIMERA_ENTREGA'),
(34, 12, NULL, 5, 'PRIMERA_ENTREGA');

-- Entrega 35: Reposición mascarillas cocina Lucía Vargas
INSERT INTO epp.entrega_epp (trabajador_id, supervisor_usuario_id, fecha_entrega, tipo_entrega, observaciones, status) VALUES
(22, 2, '2025-04-15 07:00:00', 'REPOSICION', 'Reposición cocinera senior', 'COMPLETADA');
INSERT INTO epp.detalle_entrega_epp (entrega_id, epp_id, talla_id, cantidad, motivo) VALUES
(35, 10, NULL, 5, 'REPOSICION'),
(35, 12, NULL, 5, 'REPOSICION'),
(35, 13, NULL, 2, 'REPOSICION');

-- ============================================================
-- 15. VERIFICACIÓN FINAL
-- ============================================================
DO $$
DECLARE
  v_usuarios      INTEGER;
  v_areas         INTEGER;
  v_tallas        INTEGER;
  v_normas        INTEGER;
  v_epps          INTEGER;
  v_trabajadores  INTEGER;
  v_compras       INTEGER;
  v_det_compras   INTEGER;
  v_inv_central   INTEGER;
  v_inv_area      INTEGER;
  v_entregas      INTEGER;
  v_det_entregas  INTEGER;
BEGIN
  SELECT COUNT(*) INTO v_usuarios     FROM epp.usuario;
  SELECT COUNT(*) INTO v_areas        FROM epp.area;
  SELECT COUNT(*) INTO v_tallas       FROM epp.catalogo_talla;
  SELECT COUNT(*) INTO v_normas       FROM epp.norma_epp;
  SELECT COUNT(*) INTO v_epps         FROM epp.catalogo_epp;
  SELECT COUNT(*) INTO v_trabajadores FROM epp.trabajador;
  SELECT COUNT(*) INTO v_compras      FROM epp.compra;
  SELECT COUNT(*) INTO v_det_compras  FROM epp.detalle_compra;
  SELECT COUNT(*) INTO v_inv_central  FROM epp.inventario_central;
  SELECT COUNT(*) INTO v_inv_area     FROM epp.inventario_area;
  SELECT COUNT(*) INTO v_entregas     FROM epp.entrega_epp;
  SELECT COUNT(*) INTO v_det_entregas FROM epp.detalle_entrega_epp;

  RAISE NOTICE '╔══════════════════════════════════════╗';
  RAISE NOTICE '║  SST EPP — SEED PRODUCCIÓN COMPLETO  ║';
  RAISE NOTICE '╠══════════════════════════════════════╣';
  RAISE NOTICE '║  Usuarios:            %              ║', LPAD(v_usuarios::TEXT, 3, ' ');
  RAISE NOTICE '║  Áreas:               %              ║', LPAD(v_areas::TEXT, 3, ' ');
  RAISE NOTICE '║  Tallas:              %              ║', LPAD(v_tallas::TEXT, 3, ' ');
  RAISE NOTICE '║  Normas EPP:          %              ║', LPAD(v_normas::TEXT, 3, ' ');
  RAISE NOTICE '║  EPPs catálogo:       %              ║', LPAD(v_epps::TEXT, 3, ' ');
  RAISE NOTICE '║  Trabajadores:        %              ║', LPAD(v_trabajadores::TEXT, 3, ' ');
  RAISE NOTICE '║  Compras (facturas):  %              ║', LPAD(v_compras::TEXT, 3, ' ');
  RAISE NOTICE '║  Detalles de compra:  %              ║', LPAD(v_det_compras::TEXT, 3, ' ');
  RAISE NOTICE '║  Inv. Central (lotes):%              ║', LPAD(v_inv_central::TEXT, 3, ' ');
  RAISE NOTICE '║  Inv. Área (registros):%             ║', LPAD(v_inv_area::TEXT, 3, ' ');
  RAISE NOTICE '║  Entregas:            %              ║', LPAD(v_entregas::TEXT, 3, ' ');
  RAISE NOTICE '║  Detalles entrega:    %              ║', LPAD(v_det_entregas::TEXT, 3, ' ');
  RAISE NOTICE '╠══════════════════════════════════════╣';
  RAISE NOTICE '║  Credenciales (todos): Admin1234!    ║';
  RAISE NOTICE '║  Usuarios: supervisor1, supervisor2, ║';
  RAISE NOTICE '║            admin                     ║';
  RAISE NOTICE '╚══════════════════════════════════════╝';
END $$;
