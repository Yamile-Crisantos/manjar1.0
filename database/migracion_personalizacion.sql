-- ==========================================================
-- Manjar · migración: personalización del menú y código de QR por negocio
--
-- Qué hace (sobre la tabla `establecimiento`):
--   * Agrega las columnas de personalización del menú público:
--       ColorMenu  -> color de acento (#RRGGBB)           Lema      -> frase corta bajo el nombre
--       DisenoMenu -> "tarjetas" o "lista"                Tipografia-> "clasica", "moderna" o "calida"
--       Esquinas   -> "redondas" o "rectas"               FondoMenu -> "claro" u "oscuro"
--       Telefono, Whatsapp, Instagram, Facebook, Horario  -> contacto que se muestra en el menú
--       Aviso      -> mensaje destacado del día
--   * Asigna un código de QR (CodigoQR) a los negocios que no lo tengan.
--     El menú público de cada negocio se abre con  /vistas/menu.xhtml?q=<CodigoQR>
--
-- Cómo usarlo: ejecútalo sobre la base "manjar" ya creada con database/manjar.sql.
-- Se puede ejecutar de nuevo sin problema: solo agrega lo que falta.
-- Si ya ejecutaste la versión nueva de database/manjar.sql, no cambia nada.
--
-- No borra ni modifica datos existentes (solo rellena los que estén vacíos).
-- ==========================================================

USE manjar;
SET NAMES utf8mb4;
SET @sql_safe_anterior = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- ---------- Columna ColorMenu ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'establecimiento' AND COLUMN_NAME = 'ColorMenu');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `establecimiento` ADD COLUMN `ColorMenu` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `DIRECCION`',
    'SELECT ''ColorMenu ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- Columna Lema ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'establecimiento' AND COLUMN_NAME = 'Lema');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `establecimiento` ADD COLUMN `Lema` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `ColorMenu`',
    'SELECT ''Lema ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- Columna DisenoMenu ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'establecimiento' AND COLUMN_NAME = 'DisenoMenu');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `establecimiento` ADD COLUMN `DisenoMenu` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `Lema`',
    'SELECT ''DisenoMenu ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- Columna Tipografia ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'establecimiento' AND COLUMN_NAME = 'Tipografia');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `establecimiento` ADD COLUMN `Tipografia` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `DisenoMenu`',
    'SELECT ''Tipografia ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- Columna Esquinas ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'establecimiento' AND COLUMN_NAME = 'Esquinas');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `establecimiento` ADD COLUMN `Esquinas` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `Tipografia`',
    'SELECT ''Esquinas ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- Columna FondoMenu ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'establecimiento' AND COLUMN_NAME = 'FondoMenu');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `establecimiento` ADD COLUMN `FondoMenu` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `Esquinas`',
    'SELECT ''FondoMenu ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- Columna Telefono ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'establecimiento' AND COLUMN_NAME = 'Telefono');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `establecimiento` ADD COLUMN `Telefono` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `FondoMenu`',
    'SELECT ''Telefono ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- Columna Whatsapp ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'establecimiento' AND COLUMN_NAME = 'Whatsapp');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `establecimiento` ADD COLUMN `Whatsapp` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `Telefono`',
    'SELECT ''Whatsapp ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- Columna Horario ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'establecimiento' AND COLUMN_NAME = 'Horario');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `establecimiento` ADD COLUMN `Horario` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `Whatsapp`',
    'SELECT ''Horario ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- Columna Instagram ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'establecimiento' AND COLUMN_NAME = 'Instagram');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `establecimiento` ADD COLUMN `Instagram` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `Horario`',
    'SELECT ''Instagram ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- Columna Facebook ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'establecimiento' AND COLUMN_NAME = 'Facebook');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `establecimiento` ADD COLUMN `Facebook` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `Instagram`',
    'SELECT ''Facebook ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- Columna Aviso ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'establecimiento' AND COLUMN_NAME = 'Aviso');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `establecimiento` ADD COLUMN `Aviso` varchar(160) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `Facebook`',
    'SELECT ''Aviso ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- Datos que faltan ----------
-- Código de QR: minúsculas, números y guiones (si no tenía, o tenía el formato anterior /vistas/menu.xhtml?e=1)
UPDATE establecimiento
SET CodigoQR = CONCAT('menu-', SUBSTRING(MD5(CONCAT(ID, '-', UUID())), 1, 8))
WHERE CodigoQR IS NULL OR CodigoQR = '' OR CodigoQR NOT REGEXP '^[a-z0-9]+(-[a-z0-9]+)*$';

-- Estilo predeterminado (terracota, tarjetas, tipografía clásica, esquinas redondas, fondo claro)
UPDATE establecimiento SET ColorMenu = '#C44A24' WHERE ColorMenu IS NULL OR ColorMenu = '';
UPDATE establecimiento SET DisenoMenu = 'tarjetas' WHERE DisenoMenu IS NULL OR DisenoMenu = '';
UPDATE establecimiento SET Tipografia = 'clasica' WHERE Tipografia IS NULL OR Tipografia = '';
UPDATE establecimiento SET Esquinas = 'redondas' WHERE Esquinas IS NULL OR Esquinas = '';
UPDATE establecimiento SET FondoMenu = 'claro' WHERE FondoMenu IS NULL OR FondoMenu = '';

SET SQL_SAFE_UPDATES = @sql_safe_anterior;

SELECT ID, Nombre, CodigoQR, ColorMenu, DisenoMenu, Tipografia, Esquinas, FondoMenu FROM establecimiento ORDER BY ID;
