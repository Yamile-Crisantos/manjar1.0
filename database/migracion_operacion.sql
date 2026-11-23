-- ==========================================================
-- Manjar · migración: comandas, cocina, caja, apartados, sucursales y reportes (HU-08 a HU-21)
--
-- Qué hace:
--   * Agrega a las tablas existentes las columnas que usan las entidades nuevas:
--       establecimiento : TOKENCOCINA (clave de la pantalla de cocina de cada sucursal)
--       comanda         : ESTABLECIMIENTOID, TIPO (MESA o LLEVAR), FECHAHORA, FECHACOBRO
--       detallecomanda  : PRECIOUNITARIO, COSTOUNITARIO (precio y costo al pedir, para los reportes), NOTA
--       detalleapartado : COSTOUNITARIO
--       apartado        : FECHAENTREGA, ENTREGADOPORID
--       caja            : CERRADAPORID
--       movimientocaja  : CLASE (VENTA, APARTADO, RETIRO o GASTO), METODO, USUARIOID, APARTADOID
--       pago            : REGISTRADOPORID
--   * Crea sus llaves foráneas y dos índices para que las consultas del día y los reportes sean rápidas.
--   * Rellena lo que se pueda de las comandas, cajas y apartados que ya existían (ver "Datos anteriores").
--
-- Cómo usarlo: ejecútalo sobre la base "manjar" ya creada con database/manjar.sql y DESPUÉS de
-- database/migracion_personalizacion.sql (esa agrega las columnas de personalización del menú).
-- Se puede ejecutar de nuevo sin problema: solo agrega lo que falta y solo rellena lo que está vacío.
--
-- Datos anteriores: las comandas viejas se asignan a la sucursal de su mesero y quedan como MESA; sus renglones
-- toman el precio y costo ACTUALES del platillo (no se guardaba el de aquel día); los movimientos de caja viejos
-- se consideran ventas o gastos en efectivo. No se borra ni se cambia nada más.
-- ==========================================================

USE manjar;
SET NAMES utf8mb4;
SET @sql_safe_anterior = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- ---------- establecimiento.TOKENCOCINA ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'establecimiento' AND COLUMN_NAME = 'TOKENCOCINA');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `establecimiento` ADD COLUMN `TOKENCOCINA` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL',
    'SELECT ''establecimiento.TOKENCOCINA ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- comanda.ESTABLECIMIENTOID ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'comanda' AND COLUMN_NAME = 'ESTABLECIMIENTOID');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `comanda` ADD COLUMN `ESTABLECIMIENTOID` int DEFAULT NULL AFTER `MESEROID`',
    'SELECT ''comanda.ESTABLECIMIENTOID ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- comanda.TIPO ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'comanda' AND COLUMN_NAME = 'TIPO');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `comanda` ADD COLUMN `TIPO` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL',
    'SELECT ''comanda.TIPO ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- comanda.FECHAHORA ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'comanda' AND COLUMN_NAME = 'FECHAHORA');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `comanda` ADD COLUMN `FECHAHORA` timestamp NULL DEFAULT NULL',
    'SELECT ''comanda.FECHAHORA ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- comanda.FECHACOBRO ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'comanda' AND COLUMN_NAME = 'FECHACOBRO');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `comanda` ADD COLUMN `FECHACOBRO` timestamp NULL DEFAULT NULL',
    'SELECT ''comanda.FECHACOBRO ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- detallecomanda.PRECIOUNITARIO ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'detallecomanda' AND COLUMN_NAME = 'PRECIOUNITARIO');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `detallecomanda` ADD COLUMN `PRECIOUNITARIO` decimal(10,2) DEFAULT NULL',
    'SELECT ''detallecomanda.PRECIOUNITARIO ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- detallecomanda.COSTOUNITARIO ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'detallecomanda' AND COLUMN_NAME = 'COSTOUNITARIO');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `detallecomanda` ADD COLUMN `COSTOUNITARIO` decimal(10,2) DEFAULT NULL',
    'SELECT ''detallecomanda.COSTOUNITARIO ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- detallecomanda.NOTA ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'detallecomanda' AND COLUMN_NAME = 'NOTA');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `detallecomanda` ADD COLUMN `NOTA` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL',
    'SELECT ''detallecomanda.NOTA ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- detalleapartado.COSTOUNITARIO ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'detalleapartado' AND COLUMN_NAME = 'COSTOUNITARIO');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `detalleapartado` ADD COLUMN `COSTOUNITARIO` decimal(10,2) DEFAULT NULL',
    'SELECT ''detalleapartado.COSTOUNITARIO ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- apartado.FECHAENTREGA ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'apartado' AND COLUMN_NAME = 'FECHAENTREGA');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `apartado` ADD COLUMN `FECHAENTREGA` timestamp NULL DEFAULT NULL',
    'SELECT ''apartado.FECHAENTREGA ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- apartado.ENTREGADOPORID ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'apartado' AND COLUMN_NAME = 'ENTREGADOPORID');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `apartado` ADD COLUMN `ENTREGADOPORID` int DEFAULT NULL',
    'SELECT ''apartado.ENTREGADOPORID ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- caja.CERRADAPORID ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'caja' AND COLUMN_NAME = 'CERRADAPORID');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `caja` ADD COLUMN `CERRADAPORID` int DEFAULT NULL',
    'SELECT ''caja.CERRADAPORID ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- movimientocaja.CLASE ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'movimientocaja' AND COLUMN_NAME = 'CLASE');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `movimientocaja` ADD COLUMN `CLASE` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL',
    'SELECT ''movimientocaja.CLASE ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- movimientocaja.METODO ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'movimientocaja' AND COLUMN_NAME = 'METODO');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `movimientocaja` ADD COLUMN `METODO` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL',
    'SELECT ''movimientocaja.METODO ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- movimientocaja.USUARIOID ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'movimientocaja' AND COLUMN_NAME = 'USUARIOID');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `movimientocaja` ADD COLUMN `USUARIOID` int DEFAULT NULL',
    'SELECT ''movimientocaja.USUARIOID ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- movimientocaja.APARTADOID ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'movimientocaja' AND COLUMN_NAME = 'APARTADOID');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `movimientocaja` ADD COLUMN `APARTADOID` int DEFAULT NULL',
    'SELECT ''movimientocaja.APARTADOID ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- pago.REGISTRADOPORID ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'pago' AND COLUMN_NAME = 'REGISTRADOPORID');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `pago` ADD COLUMN `REGISTRADOPORID` int DEFAULT NULL',
    'SELECT ''pago.REGISTRADOPORID ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ==========================================================
-- Llaves foráneas
-- ==========================================================

-- ---------- fk_comanda_establecimiento ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'comanda' AND CONSTRAINT_NAME = 'fk_comanda_establecimiento');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `comanda` ADD CONSTRAINT `fk_comanda_establecimiento` FOREIGN KEY (`ESTABLECIMIENTOID`) REFERENCES `establecimiento` (`ID`)',
    'SELECT ''fk_comanda_establecimiento ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- fk_apartado_entregadopor ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'apartado' AND CONSTRAINT_NAME = 'fk_apartado_entregadopor');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `apartado` ADD CONSTRAINT `fk_apartado_entregadopor` FOREIGN KEY (`ENTREGADOPORID`) REFERENCES `usuario` (`ID`)',
    'SELECT ''fk_apartado_entregadopor ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- fk_caja_cerradapor ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'caja' AND CONSTRAINT_NAME = 'fk_caja_cerradapor');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `caja` ADD CONSTRAINT `fk_caja_cerradapor` FOREIGN KEY (`CERRADAPORID`) REFERENCES `usuario` (`ID`)',
    'SELECT ''fk_caja_cerradapor ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- fk_movimientocaja_usuario ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'movimientocaja' AND CONSTRAINT_NAME = 'fk_movimientocaja_usuario');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `movimientocaja` ADD CONSTRAINT `fk_movimientocaja_usuario` FOREIGN KEY (`USUARIOID`) REFERENCES `usuario` (`ID`)',
    'SELECT ''fk_movimientocaja_usuario ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- fk_movimientocaja_apartado ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'movimientocaja' AND CONSTRAINT_NAME = 'fk_movimientocaja_apartado');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `movimientocaja` ADD CONSTRAINT `fk_movimientocaja_apartado` FOREIGN KEY (`APARTADOID`) REFERENCES `apartado` (`ID`)',
    'SELECT ''fk_movimientocaja_apartado ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- fk_pago_registradopor ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'pago' AND CONSTRAINT_NAME = 'fk_pago_registradopor');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `pago` ADD CONSTRAINT `fk_pago_registradopor` FOREIGN KEY (`REGISTRADOPORID`) REFERENCES `usuario` (`ID`)',
    'SELECT ''fk_pago_registradopor ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ==========================================================
-- Índices
-- ==========================================================

-- ---------- ix_comanda_est_cobro ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'comanda' AND INDEX_NAME = 'ix_comanda_est_cobro');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `comanda` ADD INDEX `ix_comanda_est_cobro` (`ESTABLECIMIENTOID`, `FECHACOBRO`)',
    'SELECT ''ix_comanda_est_cobro ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ---------- ix_apartado_est_estado ----------
SET @existe = (SELECT COUNT(*) FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'apartado' AND INDEX_NAME = 'ix_apartado_est_estado');
SET @sql = IF(@existe = 0,
    'ALTER TABLE `apartado` ADD INDEX `ix_apartado_est_estado` (`ESTABLECIMIENTOID`, `ESTADO`)',
    'SELECT ''ix_apartado_est_estado ya existe: sin cambios'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- ==========================================================
-- Datos anteriores (solo rellena lo que está vacío)
-- ==========================================================
UPDATE `comanda` c JOIN `usuario` u ON u.`ID` = c.`MESEROID`
   SET c.`ESTABLECIMIENTOID` = u.`ESTABLECIMIENTOID`
 WHERE c.`ESTABLECIMIENTOID` IS NULL AND u.`ESTABLECIMIENTOID` IS NOT NULL;

UPDATE `comanda` SET `TIPO` = 'MESA' WHERE `TIPO` IS NULL;

UPDATE `detallecomanda` d JOIN `platillo` p ON p.`ID` = d.`PLATILLOID`
   SET d.`PRECIOUNITARIO` = p.`Precio`
 WHERE d.`PRECIOUNITARIO` IS NULL;

UPDATE `detallecomanda` d JOIN `platillo` p ON p.`ID` = d.`PLATILLOID`
   SET d.`COSTOUNITARIO` = p.`COSTO`
 WHERE d.`COSTOUNITARIO` IS NULL;

UPDATE `detalleapartado` d JOIN `platillo` p ON p.`ID` = d.`PLATILLOID`
   SET d.`COSTOUNITARIO` = p.`COSTO`
 WHERE d.`COSTOUNITARIO` IS NULL;

UPDATE `movimientocaja`
   SET `CLASE` = IF(`TIPO` = 'INGRESO', 'VENTA', 'GASTO')
 WHERE `CLASE` IS NULL;

UPDATE `movimientocaja` SET `METODO` = 'EFECTIVO' WHERE `METODO` IS NULL;

UPDATE `movimientocaja` m JOIN `caja` c ON c.`ID` = m.`CAJAID`
   SET m.`USUARIOID` = c.`USUARIOID`
 WHERE m.`USUARIOID` IS NULL;

SET SQL_SAFE_UPDATES = @sql_safe_anterior;

SELECT 'Migración de operación aplicada' AS resultado;
