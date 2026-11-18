-- ==========================================================
-- Manjar · datos de prueba (SOLO DESARROLLO)
--
-- Qué hace:
--   * Crea 2 negocios (para probar que cada uno ve solo lo suyo)
--   * Crea cuentas de prueba de los 4 roles
--   * Crea categorías y platillos de ejemplo
--
-- Cómo usarlo: ejecútalo UNA vez sobre la base "manjar" ya creada con
-- database/manjar.sql. Se puede ejecutar de nuevo sin duplicar nada
-- (cada INSERT revisa si el dato ya existe).
--
-- Contraseña de TODAS las cuentas de prueba: Manjar123
-- (en la base solo se guarda el hash PBKDF2, nunca la contraseña)
--
-- Códigos que usa el sistema:
--   Rol:    1 = Cliente, 2 = Dueño, 3 = Gerente, 4 = Mesero
--   Estado: 1 = activo, 0 = inactivo
--   Platillo.Estado: 1 = disponible, 0 = agotado
--
-- No modifica la estructura de las tablas.
-- ==========================================================

USE manjar;
SET NAMES utf8mb4;
SET @sql_safe_anterior = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- ---------- 1) Cuentas ----------
INSERT INTO usuario (Nombre, Correo, Contrasenahash, Rol, Estado)
SELECT 'Lupe Hernández', 'dueno@manjar.test', '65000:1a2b3c4d5e6f708192a3b4c5d6e7f801:96e15afb509b70240cdb83347c5b79ee5dfc689ebe304b65e0ac701efdeaab4f', 2, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE Correo = 'dueno@manjar.test');

INSERT INTO usuario (Nombre, Correo, Contrasenahash, Rol, Estado)
SELECT 'Rosa Martínez', 'gerente@manjar.test', '65000:2b3c4d5e6f708192a3b4c5d6e7f80112:4c2ba0e9c318889ddb8498225076bdfc6115cfe241d44a9d33ec825ee3016d3c', 3, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE Correo = 'gerente@manjar.test');

INSERT INTO usuario (Nombre, Correo, Contrasenahash, Rol, Estado)
SELECT 'Carlos Ruiz', 'mesero@manjar.test', '65000:3c4d5e6f708192a3b4c5d6e7f8011223:3286f8cae750addc340be93116a33f43719c0f84f897c684793b209d3e4f9a8f', 4, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE Correo = 'mesero@manjar.test');

INSERT INTO usuario (Nombre, Correo, Contrasenahash, Rol, Estado)
SELECT 'Pedro Cuenta Baja', 'inactivo@manjar.test', '65000:4d5e6f708192a3b4c5d6e7f801122334:ef9016cbec8a93939ba44e4de3c399e8a57b548be5486eadb4c19fef30da7702', 4, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE Correo = 'inactivo@manjar.test');

INSERT INTO usuario (Nombre, Correo, Contrasenahash, Rol, Estado)
SELECT 'Ana López', 'cliente@manjar.test', '65000:5e6f708192a3b4c5d6e7f80112233445:79c93d08fea6a9db7ff97bde8a302b4f932e06f5b6677ba370168c7e3bf2c68f', 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE Correo = 'cliente@manjar.test');

INSERT INTO usuario (Nombre, Correo, Contrasenahash, Rol, Estado)
SELECT 'Mario Gómez', 'dueno2@manjar.test', '65000:6f708192a3b4c5d6e7f8011223344556:82e1f307eb33b84038f21922af2d3d10e8ff43e050c71c5585dd2b9602774fb5', 2, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE Correo = 'dueno2@manjar.test');

INSERT INTO usuario (Nombre, Correo, Contrasenahash, Rol, Estado)
SELECT 'Julia Vega', 'mesero2@manjar.test', '65000:708192a3b4c5d6e7f80112233445566a:2a9f91400ee1ca6500952d7830df5e38bab9370fca9b50231daa1aa53f17cb68', 4, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE Correo = 'mesero2@manjar.test');

-- ---------- 2) Negocios (cada uno con su dueño) ----------
INSERT INTO establecimiento (Nombre, TenantId, SystemId, DIRECCION, DUENOID)
SELECT 'Fonda Doña Lupe', 'fonda-lupe', 'SYS-0001', 'Av. Juárez 120, Centro, Oaxaca', u.ID FROM usuario u
WHERE u.Correo = 'dueno@manjar.test'
AND NOT EXISTS (SELECT 1 FROM establecimiento WHERE Nombre = 'Fonda Doña Lupe');

INSERT INTO establecimiento (Nombre, TenantId, SystemId, DIRECCION, DUENOID)
SELECT 'Taquería El Güero', 'taqueria-guero', 'SYS-0002', 'Calle Morelos 45, Col. Reforma, Oaxaca', u.ID FROM usuario u
WHERE u.Correo = 'dueno2@manjar.test'
AND NOT EXISTS (SELECT 1 FROM establecimiento WHERE Nombre = 'Taquería El Güero');

-- Enlace relativo del menú público de cada negocio de prueba (lo que lleva el código QR)
UPDATE establecimiento SET CodigoQR = CONCAT('/vistas/menu.xhtml?e=', ID)
WHERE CodigoQR IS NULL AND Nombre IN ('Fonda Doña Lupe', 'Taquería El Güero');

-- ---------- 3) Personal ligado a su negocio ----------
UPDATE usuario SET ESTABLECIMIENTOID = (SELECT ID FROM establecimiento WHERE Nombre = 'Fonda Doña Lupe')
WHERE Correo = 'dueno@manjar.test';
UPDATE usuario SET ESTABLECIMIENTOID = (SELECT ID FROM establecimiento WHERE Nombre = 'Fonda Doña Lupe')
WHERE Correo = 'gerente@manjar.test';
UPDATE usuario SET ESTABLECIMIENTOID = (SELECT ID FROM establecimiento WHERE Nombre = 'Fonda Doña Lupe')
WHERE Correo = 'mesero@manjar.test';
UPDATE usuario SET ESTABLECIMIENTOID = (SELECT ID FROM establecimiento WHERE Nombre = 'Fonda Doña Lupe')
WHERE Correo = 'inactivo@manjar.test';
UPDATE usuario SET ESTABLECIMIENTOID = (SELECT ID FROM establecimiento WHERE Nombre = 'Taquería El Güero')
WHERE Correo = 'dueno2@manjar.test';
UPDATE usuario SET ESTABLECIMIENTOID = (SELECT ID FROM establecimiento WHERE Nombre = 'Taquería El Güero')
WHERE Correo = 'mesero2@manjar.test';
UPDATE establecimiento SET GERENTEID = (SELECT ID FROM usuario WHERE Correo = 'gerente@manjar.test')
WHERE Nombre = 'Fonda Doña Lupe';

-- ---------- 4) Categorías ----------
INSERT INTO categoria (ESTABLECIMIENTOID, Nombre, Descripcion, Estado)
SELECT e.ID, 'Platos fuertes', 'Comida del día', 1 FROM establecimiento e
WHERE e.Nombre = 'Fonda Doña Lupe'
AND NOT EXISTS (SELECT 1 FROM categoria c WHERE c.ESTABLECIMIENTOID = e.ID AND c.Nombre = 'Platos fuertes');

INSERT INTO categoria (ESTABLECIMIENTOID, Nombre, Descripcion, Estado)
SELECT e.ID, 'Antojitos', 'Para empezar', 1 FROM establecimiento e
WHERE e.Nombre = 'Fonda Doña Lupe'
AND NOT EXISTS (SELECT 1 FROM categoria c WHERE c.ESTABLECIMIENTOID = e.ID AND c.Nombre = 'Antojitos');

INSERT INTO categoria (ESTABLECIMIENTOID, Nombre, Descripcion, Estado)
SELECT e.ID, 'Bebidas', 'Aguas y café', 1 FROM establecimiento e
WHERE e.Nombre = 'Fonda Doña Lupe'
AND NOT EXISTS (SELECT 1 FROM categoria c WHERE c.ESTABLECIMIENTOID = e.ID AND c.Nombre = 'Bebidas');

INSERT INTO categoria (ESTABLECIMIENTOID, Nombre, Descripcion, Estado)
SELECT e.ID, 'Postres', 'Algo dulce', 1 FROM establecimiento e
WHERE e.Nombre = 'Fonda Doña Lupe'
AND NOT EXISTS (SELECT 1 FROM categoria c WHERE c.ESTABLECIMIENTOID = e.ID AND c.Nombre = 'Postres');

INSERT INTO categoria (ESTABLECIMIENTOID, Nombre, Descripcion, Estado)
SELECT e.ID, 'Tacos', 'Órdenes de tacos', 1 FROM establecimiento e
WHERE e.Nombre = 'Taquería El Güero'
AND NOT EXISTS (SELECT 1 FROM categoria c WHERE c.ESTABLECIMIENTOID = e.ID AND c.Nombre = 'Tacos');

INSERT INTO categoria (ESTABLECIMIENTOID, Nombre, Descripcion, Estado)
SELECT e.ID, 'Bebidas', 'Aguas y refrescos', 1 FROM establecimiento e
WHERE e.Nombre = 'Taquería El Güero'
AND NOT EXISTS (SELECT 1 FROM categoria c WHERE c.ESTABLECIMIENTOID = e.ID AND c.Nombre = 'Bebidas');

-- ---------- 5) Platillos ----------
INSERT INTO platillo (CATEGORIAID, ESTABLECIMIENTOID, Nombre, Descripcion, Precio, CantidadEstimada, CantidadRestante, Estado, COSTO)
SELECT c.ID, c.ESTABLECIMIENTOID, 'Mole negro con pollo', 'Pollo en mole negro oaxaqueño con arroz', 95.00, 20, 3, 1, 42.00
FROM categoria c JOIN establecimiento e ON e.ID = c.ESTABLECIMIENTOID
WHERE e.Nombre = 'Fonda Doña Lupe' AND c.Nombre = 'Platos fuertes'
AND NOT EXISTS (SELECT 1 FROM platillo p WHERE p.ESTABLECIMIENTOID = c.ESTABLECIMIENTOID AND p.Nombre = 'Mole negro con pollo');

INSERT INTO platillo (CATEGORIAID, ESTABLECIMIENTOID, Nombre, Descripcion, Precio, CantidadEstimada, CantidadRestante, Estado, COSTO)
SELECT c.ID, c.ESTABLECIMIENTOID, 'Caldo de res', 'Con verduras, arroz y tortillas', 85.00, 15, 15, 1, 38.00
FROM categoria c JOIN establecimiento e ON e.ID = c.ESTABLECIMIENTOID
WHERE e.Nombre = 'Fonda Doña Lupe' AND c.Nombre = 'Platos fuertes'
AND NOT EXISTS (SELECT 1 FROM platillo p WHERE p.ESTABLECIMIENTOID = c.ESTABLECIMIENTOID AND p.Nombre = 'Caldo de res');

INSERT INTO platillo (CATEGORIAID, ESTABLECIMIENTOID, Nombre, Descripcion, Precio, CantidadEstimada, CantidadRestante, Estado, COSTO)
SELECT c.ID, c.ESTABLECIMIENTOID, 'Enchiladas verdes', 'Rellenas de pollo, con crema y queso', 80.00, 25, 25, 1, 33.00
FROM categoria c JOIN establecimiento e ON e.ID = c.ESTABLECIMIENTOID
WHERE e.Nombre = 'Fonda Doña Lupe' AND c.Nombre = 'Platos fuertes'
AND NOT EXISTS (SELECT 1 FROM platillo p WHERE p.ESTABLECIMIENTOID = c.ESTABLECIMIENTOID AND p.Nombre = 'Enchiladas verdes');

INSERT INTO platillo (CATEGORIAID, ESTABLECIMIENTOID, Nombre, Descripcion, Precio, CantidadEstimada, CantidadRestante, Estado, COSTO)
SELECT c.ID, c.ESTABLECIMIENTOID, 'Tamal oaxaqueño', 'Tamal de mole en hoja de plátano', 35.00, 40, 40, 1, 14.00
FROM categoria c JOIN establecimiento e ON e.ID = c.ESTABLECIMIENTOID
WHERE e.Nombre = 'Fonda Doña Lupe' AND c.Nombre = 'Antojitos'
AND NOT EXISTS (SELECT 1 FROM platillo p WHERE p.ESTABLECIMIENTOID = c.ESTABLECIMIENTOID AND p.Nombre = 'Tamal oaxaqueño');

INSERT INTO platillo (CATEGORIAID, ESTABLECIMIENTOID, Nombre, Descripcion, Precio, CantidadEstimada, CantidadRestante, Estado, COSTO)
SELECT c.ID, c.ESTABLECIMIENTOID, 'Quesadilla de flor de calabaza', 'Tortilla de maíz con quesillo', 45.00, 0, 0, 1, 18.00
FROM categoria c JOIN establecimiento e ON e.ID = c.ESTABLECIMIENTOID
WHERE e.Nombre = 'Fonda Doña Lupe' AND c.Nombre = 'Antojitos'
AND NOT EXISTS (SELECT 1 FROM platillo p WHERE p.ESTABLECIMIENTOID = c.ESTABLECIMIENTOID AND p.Nombre = 'Quesadilla de flor de calabaza');

INSERT INTO platillo (CATEGORIAID, ESTABLECIMIENTOID, Nombre, Descripcion, Precio, CantidadEstimada, CantidadRestante, Estado, COSTO)
SELECT c.ID, c.ESTABLECIMIENTOID, 'Agua de horchata', 'Vaso grande', 25.00, 0, 0, 1, 7.00
FROM categoria c JOIN establecimiento e ON e.ID = c.ESTABLECIMIENTOID
WHERE e.Nombre = 'Fonda Doña Lupe' AND c.Nombre = 'Bebidas'
AND NOT EXISTS (SELECT 1 FROM platillo p WHERE p.ESTABLECIMIENTOID = c.ESTABLECIMIENTOID AND p.Nombre = 'Agua de horchata');

INSERT INTO platillo (CATEGORIAID, ESTABLECIMIENTOID, Nombre, Descripcion, Precio, CantidadEstimada, CantidadRestante, Estado, COSTO)
SELECT c.ID, c.ESTABLECIMIENTOID, 'Café de olla', 'Con canela y piloncillo', 22.00, 0, 0, 1, 6.00
FROM categoria c JOIN establecimiento e ON e.ID = c.ESTABLECIMIENTOID
WHERE e.Nombre = 'Fonda Doña Lupe' AND c.Nombre = 'Bebidas'
AND NOT EXISTS (SELECT 1 FROM platillo p WHERE p.ESTABLECIMIENTOID = c.ESTABLECIMIENTOID AND p.Nombre = 'Café de olla');

INSERT INTO platillo (CATEGORIAID, ESTABLECIMIENTOID, Nombre, Descripcion, Precio, CantidadEstimada, CantidadRestante, Estado, COSTO)
SELECT c.ID, c.ESTABLECIMIENTOID, 'Flan napolitano', 'Rebanada individual', 40.00, 12, 12, 1, 15.00
FROM categoria c JOIN establecimiento e ON e.ID = c.ESTABLECIMIENTOID
WHERE e.Nombre = 'Fonda Doña Lupe' AND c.Nombre = 'Postres'
AND NOT EXISTS (SELECT 1 FROM platillo p WHERE p.ESTABLECIMIENTOID = c.ESTABLECIMIENTOID AND p.Nombre = 'Flan napolitano');

INSERT INTO platillo (CATEGORIAID, ESTABLECIMIENTOID, Nombre, Descripcion, Precio, CantidadEstimada, CantidadRestante, Estado, COSTO)
SELECT c.ID, c.ESTABLECIMIENTOID, 'Pan de elote', 'Se acabó por hoy', 30.00, 10, 0, 0, 11.00
FROM categoria c JOIN establecimiento e ON e.ID = c.ESTABLECIMIENTOID
WHERE e.Nombre = 'Fonda Doña Lupe' AND c.Nombre = 'Postres'
AND NOT EXISTS (SELECT 1 FROM platillo p WHERE p.ESTABLECIMIENTOID = c.ESTABLECIMIENTOID AND p.Nombre = 'Pan de elote');

INSERT INTO platillo (CATEGORIAID, ESTABLECIMIENTOID, Nombre, Descripcion, Precio, CantidadEstimada, CantidadRestante, Estado, COSTO)
SELECT c.ID, c.ESTABLECIMIENTOID, 'Orden de tacos al pastor', '5 tacos con piña y cebolla', 65.00, 60, 60, 1, 28.00
FROM categoria c JOIN establecimiento e ON e.ID = c.ESTABLECIMIENTOID
WHERE e.Nombre = 'Taquería El Güero' AND c.Nombre = 'Tacos'
AND NOT EXISTS (SELECT 1 FROM platillo p WHERE p.ESTABLECIMIENTOID = c.ESTABLECIMIENTOID AND p.Nombre = 'Orden de tacos al pastor');

INSERT INTO platillo (CATEGORIAID, ESTABLECIMIENTOID, Nombre, Descripcion, Precio, CantidadEstimada, CantidadRestante, Estado, COSTO)
SELECT c.ID, c.ESTABLECIMIENTOID, 'Orden de tacos de suadero', '5 tacos con cilantro y salsa', 60.00, 50, 50, 1, 26.00
FROM categoria c JOIN establecimiento e ON e.ID = c.ESTABLECIMIENTOID
WHERE e.Nombre = 'Taquería El Güero' AND c.Nombre = 'Tacos'
AND NOT EXISTS (SELECT 1 FROM platillo p WHERE p.ESTABLECIMIENTOID = c.ESTABLECIMIENTOID AND p.Nombre = 'Orden de tacos de suadero');

INSERT INTO platillo (CATEGORIAID, ESTABLECIMIENTOID, Nombre, Descripcion, Precio, CantidadEstimada, CantidadRestante, Estado, COSTO)
SELECT c.ID, c.ESTABLECIMIENTOID, 'Agua de jamaica', 'Vaso grande', 22.00, 0, 0, 1, 6.00
FROM categoria c JOIN establecimiento e ON e.ID = c.ESTABLECIMIENTOID
WHERE e.Nombre = 'Taquería El Güero' AND c.Nombre = 'Bebidas'
AND NOT EXISTS (SELECT 1 FROM platillo p WHERE p.ESTABLECIMIENTOID = c.ESTABLECIMIENTOID AND p.Nombre = 'Agua de jamaica');

INSERT INTO platillo (CATEGORIAID, ESTABLECIMIENTOID, Nombre, Descripcion, Precio, CantidadEstimada, CantidadRestante, Estado, COSTO)
SELECT c.ID, c.ESTABLECIMIENTOID, 'Refresco de botella', 'Marcado como agotado a mano', 20.00, 0, 0, 0, 12.00
FROM categoria c JOIN establecimiento e ON e.ID = c.ESTABLECIMIENTOID
WHERE e.Nombre = 'Taquería El Güero' AND c.Nombre = 'Bebidas'
AND NOT EXISTS (SELECT 1 FROM platillo p WHERE p.ESTABLECIMIENTOID = c.ESTABLECIMIENTOID AND p.Nombre = 'Refresco de botella');

SET SQL_SAFE_UPDATES = @sql_safe_anterior;

-- Revisión rápida (con la base vacía antes de ejecutar: 7 cuentas, 2 negocios y 13 platillos):
SELECT (SELECT COUNT(*) FROM usuario WHERE Correo LIKE '%@manjar.test') AS cuentas_prueba,
       (SELECT COUNT(*) FROM establecimiento) AS negocios,
       (SELECT COUNT(*) FROM platillo) AS platillos;
