-- ==========================================================
-- Manjar · migración: experiencia del cliente y del mesero
--   (apartado con anticipo, "para llevar / comer aquí" e imagen de los platillos)
--
-- Qué hace:
--   * apartado.TIPOENTREGA : PARA LLEVAR (LLEVAR) o COMER AQUÍ (AQUI). Comer aquí no asigna mesa.
--   * apartado.ANTICIPO    : lo que el cliente paga al apartar (por omisión el 50 % del total). El resto
--                            (TOTAL - ANTICIPO) se cobra en caja al entregar el pedido.
--   * pago.CONCEPTO        : ANTICIPO o SALDO. Un apartado puede tener dos pagos: el anticipo y, al entregar, el saldo.
--   * platillo.IMAGENURL   : foto del platillo. Es un enlace http/https, o la ruta interna "imagen/platillo/<id>?v=<n>"
--                            cuando el dueño sube su propia foto.
--   * Tabla imagenplatillo : guarda los bytes de la foto subida por el dueño (una por platillo).
--
-- Cómo usarlo: ejecútalo sobre la base "manjar" ya creada con database/manjar.sql y DESPUÉS de
-- database/migracion_personalizacion.sql y database/migracion_operacion.sql.
-- Se puede ejecutar de nuevo sin problema: solo agrega lo que falta y solo rellena lo que está vacío.
--
-- Datos anteriores: los apartados que ya existían se pagaban completos, así que quedan con ANTICIPO = TOTAL
-- (no deben nada al entregar), como PARA LLEVAR, y sus pagos como ANTICIPO. No se borra ni se cambia nada más.
-- ==========================================================

USE manjar;
SET NAMES utf8mb4;
SET @sql_safe_anterior = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- ---------- apartado.TIPOENTREGA ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'apartado' AND COLUMN_NAME = 'TIPOENTREGA');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `apartado` ADD COLUMN `TIPOENTREGA` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT ''LLEVAR''',
    'SELECT ''apartado.TIPOENTREGA ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- apartado.ANTICIPO ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'apartado' AND COLUMN_NAME = 'ANTICIPO');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `apartado` ADD COLUMN `ANTICIPO` decimal(10,2) DEFAULT NULL',
    'SELECT ''apartado.ANTICIPO ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- Los apartados anteriores se pagaban completos: no deben saldo
UPDATE `apartado` SET `ANTICIPO` = `TOTAL` WHERE `ANTICIPO` IS NULL;

-- Una vez rellenada, la columna ya no admite vacíos (el sistema siempre la llena)
ALTER TABLE `apartado` MODIFY COLUMN `ANTICIPO` decimal(10,2) NOT NULL;

-- ---------- pago.CONCEPTO ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'pago' AND COLUMN_NAME = 'CONCEPTO');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `pago` ADD COLUMN `CONCEPTO` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT ''ANTICIPO''',
    'SELECT ''pago.CONCEPTO ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- platillo.IMAGENURL ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'platillo' AND COLUMN_NAME = 'IMAGENURL');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `platillo` ADD COLUMN `IMAGENURL` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL',
    'SELECT ''platillo.IMAGENURL ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- tabla imagenplatillo ----------
CREATE TABLE IF NOT EXISTS `imagenplatillo` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `PLATILLOID` int NOT NULL,
  `TIPOMIME` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `DATOS` mediumblob NOT NULL,
  `TAMANO` int NOT NULL,
  `FECHA` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `ux_imagenplatillo_platillo` (`PLATILLOID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- fk_imagenplatillo_platillo ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'imagenplatillo' AND CONSTRAINT_NAME = 'fk_imagenplatillo_platillo');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `imagenplatillo` ADD CONSTRAINT `fk_imagenplatillo_platillo` FOREIGN KEY (`PLATILLOID`) REFERENCES `platillo` (`ID`)',
    'SELECT ''fk_imagenplatillo_platillo ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

SET SQL_SAFE_UPDATES = @sql_safe_anterior;

SELECT 'Migración de experiencia aplicada' AS resultado;
