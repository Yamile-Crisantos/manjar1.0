# Manjar 1.0

Sistema SaaS multi-tenant de administración para fondas y restaurantes pequeños. El alcance del MVP incluye comandas digitales, menú digital con código QR, control de caja, reportes de ventas y panel de administración web.

Proyecto de residencia del Instituto Tecnológico de Oaxaca (ITO), desarrollado para la empresa InToGlobe.

**Estado actual:** versión inicial con el modelo de datos y la capa de acceso a datos (entidades JPA, facades EJB y capa de negocio). La interfaz web está en desarrollo.

## Tecnologías

| Componente | Versión |
|---|---|
| Java | JDK 17 |
| Plataforma | Java EE 8 (EJB, JPA 2.2 con EclipseLink, JSF) |
| Interfaz | PrimeFaces 5.0 (el `.jar` ya viene incluido en `Manjar-war/lib`) |
| Servidor de aplicaciones | Payara 5 |
| Base de datos | MySQL 8.0 |
| IDE | NetBeans (proyecto Ant, creado con NetBeans 25) |

## Estructura del repositorio

La raíz del repositorio es el proyecto empresarial (EAR) de NetBeans.

```
manjar1.0/
├── Manjar-ejb/      Módulo EJB
│   └── src/java/
│       ├── modelo/      Entidades JPA (una por tabla)
│       ├── dao/         Facades (acceso a datos)
│       └── logicaneg/   Lógica de negocio
├── Manjar-war/      Módulo web (JSF + PrimeFaces)
│   └── lib/             primefaces-5.0.jar
├── database/        Script de la base de datos (manjar.sql)
├── nbproject/       Configuración del proyecto empresarial
├── src/conf/
├── build.xml
└── README.md
```

## Base de datos

Base `manjar` en MySQL con las tablas: `apartado`, `caja`, `categoria`, `comanda`, `detalleapartado`, `detallecomanda`, `establecimiento`, `movimientocaja`, `notificacion`, `pago`, `platillo` y `usuario`.

## Requisitos previos

- JDK 17
- NetBeans con soporte Java EE / Jakarta EE
- Payara 5 descomprimido en tu computadora (por ejemplo `C:\payara5`)
- MySQL 8.0 y MySQL Workbench
- MySQL Connector/J 8.x (el `.jar` del conector, desde <https://dev.mysql.com/downloads/connector/j/>)
- Git o GitHub Desktop

## Instalación paso a paso

### 1. Clonar el repositorio

```
git clone https://github.com/Yamile-Crisantos/manjar1.0.git
```

Clónalo en una carpeta fuera de OneDrive (por ejemplo `C:\Proyectos`), porque OneDrive interfiere con Git y con las compilaciones de NetBeans.

### 2. Crear la base de datos

1. Abre MySQL Workbench y conéctate a tu servidor local.
2. Menú **Server → Data Import**.
3. Elige **Import from Self-Contained File** y selecciona `database/manjar.sql`.
4. Pulsa **Start Import**. El script crea el esquema `manjar`.

### 3. Crear el usuario de MySQL

La aplicación se conecta con el usuario `manjar`. Créalo con tu propia contraseña:

```sql
CREATE USER 'manjar'@'localhost' IDENTIFIED BY 'tu_contraseña';
GRANT ALL PRIVILEGES ON manjar.* TO 'manjar'@'localhost';
FLUSH PRIVILEGES;
```

### 4. Copiar el conector de MySQL a Payara

Copia el `.jar` de Connector/J a:

```
<carpeta de Payara>/glassfish/domains/domain1/lib
```

Si Payara estaba encendido, reinícialo.

### 5. Escribir tu contraseña en la configuración

Abre `Manjar-ejb/src/conf/META-INF/glassfish-resources.xml` y reemplaza `CAMBIAR_CONTRASENA` en la propiedad `Password` por la contraseña del paso 3.

> No subas ese cambio al repositorio. Si usas Git, puedes evitarlo con `git update-index --assume-unchanged Manjar-ejb/src/conf/META-INF/glassfish-resources.xml`.

### 6. Configurar NetBeans

La carpeta `nbproject/private` no se sube a Git (contiene rutas de cada computadora), así que hay que configurar el servidor y la plataforma en tu equipo:

1. **Registrar Payara:** pestaña *Services → Servers → Add Server → Payara Server*, elige tu carpeta de Payara y el dominio `domain1`.
2. **Registrar JDK 17:** *Tools → Java Platforms → Add Platform*, si aún no aparece como `JDK 17`.
3. **Abrir el proyecto:** *File → Open Project*, elige la carpeta del repositorio (la del ícono de triángulo) y marca **Open Required Projects**. Se abren `Manjar`, `Manjar-ejb` y `Manjar-war`.
4. **Asignar el servidor a los tres proyectos:** clic derecho en cada uno → *Properties → Run* → Server: **Payara Server**, Java EE Version: **Java EE 8**. Si NetBeans ofrece *Resolve Server Problem*, úsalo.

### 7. Compilar y ejecutar

1. Clic derecho en `Manjar` → **Clean and Build**. Debe terminar en `BUILD SUCCESSFUL`.
2. Inicia MySQL.
3. Clic derecho en `Manjar` → **Run**.

## Conexión a la base de datos

- Unidad de persistencia: `Manjar-ejbPU` (`Manjar-ejb/src/conf/persistence.xml`).
- Pool de conexiones: `mysql_manjar_manjarPool`, apuntando a `localhost:3306/manjar` (definido en `Manjar-ejb/src/conf/META-INF/glassfish-resources.xml`).
- También puedes crear el pool y el recurso JDBC a mano desde la consola de administración de Payara (`http://localhost:4848`, *Resources → JDBC*), con los mismos valores de ese archivo.

## Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| `package javax.persistence does not exist` al compilar | Falta asignar Payara al proyecto. Revisa el paso 6.4. |
| `Communications link failure` o `Access denied for user` | MySQL no está encendido, o la contraseña de `glassfish-resources.xml` no coincide con la del usuario `manjar`. |
| Error de data source o JNDI al desplegar | Revisa en la consola de Payara (*Resources → JDBC → JDBC Resources*) que exista el recurso y que coincida con el nombre usado en `persistence.xml`. |
| NetBeans marca referencias rotas | Clic derecho en el proyecto → *Resolve Reference Problems* (servidor y plataforma Java). |

## Seguridad

No subas contraseñas reales de MySQL ni datos personales al repositorio.
