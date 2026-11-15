CREATE DATABASE  IF NOT EXISTS `manjar` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `manjar`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: manjar
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `apartado`
--

DROP TABLE IF EXISTS `apartado`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `apartado` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `CLIENTEID` int NOT NULL,
  `ESTABLECIMIENTOID` int NOT NULL,
  `FECHACREACION` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `HORARECOLECCION` timestamp NOT NULL,
  `ESTADO` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'PENDIENTE',
  `TOTAL` decimal(10,2) NOT NULL DEFAULT '0.00',
  `CODIGOCOMPROBANTE` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uq_apartado_comprobante` (`CODIGOCOMPROBANTE`),
  KEY `fk_apartado_cliente` (`CLIENTEID`),
  KEY `fk_apartado_establecimiento` (`ESTABLECIMIENTOID`),
  CONSTRAINT `fk_apartado_cliente` FOREIGN KEY (`CLIENTEID`) REFERENCES `usuario` (`ID`),
  CONSTRAINT `fk_apartado_establecimiento` FOREIGN KEY (`ESTABLECIMIENTOID`) REFERENCES `establecimiento` (`ID`),
  CONSTRAINT `chk_apartado_total` CHECK ((`TOTAL` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `apartado`
--

LOCK TABLES `apartado` WRITE;
/*!40000 ALTER TABLE `apartado` DISABLE KEYS */;
/*!40000 ALTER TABLE `apartado` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `caja`
--

DROP TABLE IF EXISTS `caja`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `caja` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `ESTABLECIMIENTOID` int NOT NULL,
  `USUARIOID` int NOT NULL,
  `FECHAAPERTURA` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `MONTOINICIAL` decimal(10,2) NOT NULL,
  `ESTADO` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'ABIERTA',
  `FECHACIERRE` timestamp NULL DEFAULT NULL,
  `MONTOESPERADO` decimal(10,2) DEFAULT NULL,
  `MONTOCONTADO` decimal(10,2) DEFAULT NULL,
  `DIFERENCIA` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_caja_establecimiento` (`ESTABLECIMIENTOID`),
  KEY `fk_caja_usuario` (`USUARIOID`),
  CONSTRAINT `fk_caja_establecimiento` FOREIGN KEY (`ESTABLECIMIENTOID`) REFERENCES `establecimiento` (`ID`),
  CONSTRAINT `fk_caja_usuario` FOREIGN KEY (`USUARIOID`) REFERENCES `usuario` (`ID`),
  CONSTRAINT `chk_caja_montoinicial` CHECK ((`MONTOINICIAL` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `caja`
--

LOCK TABLES `caja` WRITE;
/*!40000 ALTER TABLE `caja` DISABLE KEYS */;
/*!40000 ALTER TABLE `caja` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `ESTABLECIMIENTOID` int NOT NULL,
  `Nombre` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `Descripcion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Estado` int DEFAULT '1',
  PRIMARY KEY (`ID`),
  KEY `fk_categoria_establecimiento` (`ESTABLECIMIENTOID`),
  CONSTRAINT `fk_categoria_establecimiento` FOREIGN KEY (`ESTABLECIMIENTOID`) REFERENCES `establecimiento` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categoria`
--

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comanda`
--

DROP TABLE IF EXISTS `comanda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comanda` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `MESEROID` int NOT NULL,
  `MESA` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ESTADO` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'ABIERTA',
  PRIMARY KEY (`ID`),
  KEY `fk_comanda_mesero` (`MESEROID`),
  CONSTRAINT `fk_comanda_mesero` FOREIGN KEY (`MESEROID`) REFERENCES `usuario` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comanda`
--

LOCK TABLES `comanda` WRITE;
/*!40000 ALTER TABLE `comanda` DISABLE KEYS */;
/*!40000 ALTER TABLE `comanda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalleapartado`
--

DROP TABLE IF EXISTS `detalleapartado`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalleapartado` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `APARTADOID` int NOT NULL,
  `PLATILLOID` int NOT NULL,
  `CANTIDAD` int NOT NULL,
  `PRECIOUNITARIO` decimal(10,2) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_detalleapartado_apartado` (`APARTADOID`),
  KEY `fk_detalleapartado_platillo` (`PLATILLOID`),
  CONSTRAINT `fk_detalleapartado_apartado` FOREIGN KEY (`APARTADOID`) REFERENCES `apartado` (`ID`),
  CONSTRAINT `fk_detalleapartado_platillo` FOREIGN KEY (`PLATILLOID`) REFERENCES `platillo` (`ID`),
  CONSTRAINT `chk_detalleapartado_cantidad` CHECK ((`CANTIDAD` > 0)),
  CONSTRAINT `chk_detalleapartado_precio` CHECK ((`PRECIOUNITARIO` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalleapartado`
--

LOCK TABLES `detalleapartado` WRITE;
/*!40000 ALTER TABLE `detalleapartado` DISABLE KEYS */;
/*!40000 ALTER TABLE `detalleapartado` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detallecomanda`
--

DROP TABLE IF EXISTS `detallecomanda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detallecomanda` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `COMANDAID` int NOT NULL,
  `PLATILLOID` int NOT NULL,
  `CANTIDAD` int NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_detallecomanda_comanda` (`COMANDAID`),
  KEY `fk_detallecomanda_platillo` (`PLATILLOID`),
  CONSTRAINT `fk_detallecomanda_comanda` FOREIGN KEY (`COMANDAID`) REFERENCES `comanda` (`ID`),
  CONSTRAINT `fk_detallecomanda_platillo` FOREIGN KEY (`PLATILLOID`) REFERENCES `platillo` (`ID`),
  CONSTRAINT `chk_detallecomanda_cantidad` CHECK ((`CANTIDAD` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detallecomanda`
--

LOCK TABLES `detallecomanda` WRITE;
/*!40000 ALTER TABLE `detallecomanda` DISABLE KEYS */;
/*!40000 ALTER TABLE `detallecomanda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `establecimiento`
--

DROP TABLE IF EXISTS `establecimiento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `establecimiento` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `Nombre` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `TenantId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `SystemId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CodigoQR` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DIRECCION` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DUENOID` int DEFAULT NULL,
  `GERENTEID` int DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_establecimiento_dueno` (`DUENOID`),
  KEY `fk_establecimiento_gerente` (`GERENTEID`),
  CONSTRAINT `fk_establecimiento_dueno` FOREIGN KEY (`DUENOID`) REFERENCES `usuario` (`ID`),
  CONSTRAINT `fk_establecimiento_gerente` FOREIGN KEY (`GERENTEID`) REFERENCES `usuario` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `establecimiento`
--

LOCK TABLES `establecimiento` WRITE;
/*!40000 ALTER TABLE `establecimiento` DISABLE KEYS */;
/*!40000 ALTER TABLE `establecimiento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimientocaja`
--

DROP TABLE IF EXISTS `movimientocaja`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientocaja` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `CAJAID` int NOT NULL,
  `COMANDAID` int DEFAULT NULL,
  `TIPO` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `MONTO` decimal(10,2) NOT NULL,
  `CONCEPTO` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `FECHAHORA` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `fk_movimientocaja_caja` (`CAJAID`),
  KEY `fk_movimientocaja_comanda` (`COMANDAID`),
  CONSTRAINT `fk_movimientocaja_caja` FOREIGN KEY (`CAJAID`) REFERENCES `caja` (`ID`),
  CONSTRAINT `fk_movimientocaja_comanda` FOREIGN KEY (`COMANDAID`) REFERENCES `comanda` (`ID`),
  CONSTRAINT `chk_movimientocaja_monto` CHECK ((`MONTO` > 0)),
  CONSTRAINT `chk_movimientocaja_tipo` CHECK ((`TIPO` in (_utf8mb4'INGRESO',_utf8mb4'EGRESO')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimientocaja`
--

LOCK TABLES `movimientocaja` WRITE;
/*!40000 ALTER TABLE `movimientocaja` DISABLE KEYS */;
/*!40000 ALTER TABLE `movimientocaja` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notificacion`
--

DROP TABLE IF EXISTS `notificacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificacion` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `APARTADOID` int NOT NULL,
  `MENSAJE` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `TIPO` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `ESTADO` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'PENDIENTE',
  `FECHA` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `fk_notificacion_apartado` (`APARTADOID`),
  CONSTRAINT `fk_notificacion_apartado` FOREIGN KEY (`APARTADOID`) REFERENCES `apartado` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notificacion`
--

LOCK TABLES `notificacion` WRITE;
/*!40000 ALTER TABLE `notificacion` DISABLE KEYS */;
/*!40000 ALTER TABLE `notificacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pago`
--

DROP TABLE IF EXISTS `pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pago` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `APARTADOID` int NOT NULL,
  `MONTO` decimal(10,2) NOT NULL,
  `METODO` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `REFERENCIA` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ESTADO` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'PENDIENTE',
  `FECHA` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `fk_pago_apartado` (`APARTADOID`),
  CONSTRAINT `fk_pago_apartado` FOREIGN KEY (`APARTADOID`) REFERENCES `apartado` (`ID`),
  CONSTRAINT `chk_pago_monto` CHECK ((`MONTO` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago`
--

LOCK TABLES `pago` WRITE;
/*!40000 ALTER TABLE `pago` DISABLE KEYS */;
/*!40000 ALTER TABLE `pago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `platillo`
--

DROP TABLE IF EXISTS `platillo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `platillo` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `CATEGORIAID` int NOT NULL,
  `ESTABLECIMIENTOID` int NOT NULL,
  `Nombre` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `Descripcion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Precio` decimal(10,2) NOT NULL,
  `CantidadEstimada` int DEFAULT '0',
  `CantidadRestante` int DEFAULT '0',
  `Estado` int DEFAULT '1',
  `COSTO` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_platillo_categoria` (`CATEGORIAID`),
  KEY `fk_platillo_establecimiento` (`ESTABLECIMIENTOID`),
  CONSTRAINT `fk_platillo_categoria` FOREIGN KEY (`CATEGORIAID`) REFERENCES `categoria` (`ID`),
  CONSTRAINT `fk_platillo_establecimiento` FOREIGN KEY (`ESTABLECIMIENTOID`) REFERENCES `establecimiento` (`ID`),
  CONSTRAINT `chk_platillo_cantidades` CHECK (((`CantidadEstimada` >= 0) and (`CantidadRestante` >= 0))),
  CONSTRAINT `chk_platillo_precio` CHECK ((`Precio` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `platillo`
--

LOCK TABLES `platillo` WRITE;
/*!40000 ALTER TABLE `platillo` DISABLE KEYS */;
/*!40000 ALTER TABLE `platillo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `ESTABLECIMIENTOID` int DEFAULT NULL,
  `Nombre` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Correo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Contrasenahash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Rol` int DEFAULT NULL,
  `Estado` int DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_usuario_establecimiento` (`ESTABLECIMIENTOID`),
  CONSTRAINT `fk_usuario_establecimiento` FOREIGN KEY (`ESTABLECIMIENTOID`) REFERENCES `establecimiento` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-14 22:52:34
