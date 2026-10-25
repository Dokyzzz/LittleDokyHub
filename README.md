# LittleDokyHub — avance 3

Aplicación de pedidos de pizzería con Spring Boot 3.3.4, Java 17, Thymeleaf,
Spring Security, Spring Data JPA/Hibernate y MySQL 8+.

La persistencia combina Spring Data JPA/Hibernate con `JdbcTemplate`: las
entidades, los CRUD de productos/categorías y las consultas en cascada usan JPA;
el flujo de compras usa JDBC dentro de transacciones, y parte de la gestión de
opciones también utiliza JDBC.

## Requisitos

- Git para descargar el repositorio.
- **JDK 17**, con `java` y `JAVA_HOME` configurados para esa instalación.
- MySQL 8+ instalado y su servicio iniciado para ejecutar la aplicación.
- Cliente `mysql` disponible en el PATH, o MySQL Workbench para importar el SQL.
- Conexión a internet para la primera descarga de Maven y las dependencias.
  Bootstrap e iconos también se cargan desde CDN al abrir las páginas.

Maven no necesita instalarse por separado: se utiliza el wrapper incluido.
Node.js 18+ y npm son opcionales y solo se requieren para las pruebas de interfaz.

## Descargar la versión del avance 3

Las correcciones y las consultas en cascada de este documento están en la rama
**`revision-apf3` del fork `Dokyzzz/LittleDokyHub`**. La rama `apf3-fabricio`
conserva el avance recibido del grupo; `main` puede contener otra versión.

Desde Bash, CMD o PowerShell:

```bash
git clone --branch revision-apf3 https://github.com/Dokyzzz/LittleDokyHub.git
cd LittleDokyHub
```

Ejecuta todos los comandos siguientes desde esta carpeta, donde están `pom.xml`,
`mvnw`, `mvnw.cmd` y `bd-littledokyhub.sql`.

## Preparar la base de datos

Para una instalación nueva, importa `bd-littledokyhub.sql` **antes** de iniciar
Spring Boot. El archivo crea `bd_littledokyhub`, sus 12 tablas, relaciones y datos
de demostración. El usuario utilizado para importarlo debe tener permisos para
crear la base y las tablas. Introduce la contraseña real de tu MySQL cuando se
solicite; corresponde al servidor. Las cuentas demo se utilizan en el login de
la aplicación.

En Bash o en CMD:

```bash
mysql -u root -p < bd-littledokyhub.sql
```

En PowerShell, abre primero el cliente MySQL desde la carpeta del proyecto:

```powershell
mysql -u root -p
```

Después, dentro del cliente MySQL, ejecuta:

```sql
SOURCE bd-littledokyhub.sql;
EXIT;
```

El operador `<` del primer ejemplo no funciona directamente en PowerShell.
Como alternativa, abre MySQL Workbench, conéctate a tu servidor, abre
`bd-littledokyhub.sql` mediante **File → Open SQL Script** y ejecuta el archivo
completo. No necesitas crear previamente las tablas.

Si ya tienes una base de una versión anterior, detén la aplicación y respáldala.
Consulta [la migración de importes](scripts/migrar-importes-apf3.sql) para convertir
las columnas de dinero y agregar la unicidad del carrito. Esa migración asume
que las tablas ya existen y no reconstruye un esquema incompleto. El script
inicial usa `CREATE TABLE IF NOT EXISTS`, que tampoco actualiza columnas
preexistentes. Revisa qué estructura tienes antes de elegir la migración.

## Configurar y ejecutar

En Bash:

```bash
export DB_USER=root
export DB_PASSWORD='tu-clave-local'
./mvnw spring-boot:run
```

En PowerShell:

```powershell
$env:DB_USER = 'root'
$env:DB_PASSWORD = 'tu-clave-local'
.\mvnw.cmd spring-boot:run
```

Sustituye `tu-clave-local` por la contraseña del usuario MySQL indicado en
`DB_USER`. Define las variables y ejecuta Maven en la misma terminal; estos
ejemplos configuran únicamente esa sesión.

| Variable | Valor predeterminado / uso |
| --- | --- |
| `DB_USER` | `root`; usuario con acceso a la base de la aplicación. |
| `DB_PASSWORD` | Vacía si no se define; configura la contraseña real de ese usuario. |
| `DB_URL` | `jdbc:mysql://localhost:3306/bd_littledokyhub?serverTimezone=America/Lima`. Cambiar si el servidor o puerto es diferente, apuntando a la base preparada. |
| `PORT` | `8081`; puerto HTTP de Spring Boot. |
| `DDL_AUTO` | `validate`; Hibernate comprueba la estructura existente al arrancar. |

Por defecto, Hibernate valida la estructura y no crea ni modifica tablas.
Cuando la consola indique que la aplicación ha iniciado, abre
`http://localhost:8081`. Si cambias `PORT`, usa ese puerto en la dirección.
Para detenerla, pulsa `Ctrl+C` en la terminal donde se está ejecutando.

## Usuarios de demostración

Solo para una base local de desarrollo:

| Rol | Correo | Contraseña inicial |
| --- | --- | --- |
| Cliente | cliente.demo@littledokyhub.test | ClienteDemo2026! |
| Administrador | admin.demo@littledokyhub.test | AdminDemo2026! |

El script almacena hashes BCrypt. Cambiar estas claves o eliminar las cuentas
antes de usar datos reales. No se incluyen credenciales de tu servidor MySQL.

## Flujos

- `/categorias`: catálogo de todas las categorías registradas; sus tarjetas
  pueden mostrar productos agotados, cuya compra queda bloqueada.
- `/personalizar/{id}`: opciones y precios de MySQL.
- `/pedido/resumen`: carrito persistente y actualización de cantidades.
- `/pedido/entrega`: elección de entrega y pago al recibir.
- `/pedidos/{id}`: confirmación visible solo para el propietario.
- `/administracion`: CRUD de productos. Retirar deja el stock en cero y conserva
  el historial; reponer stock lo habilita de nuevo.
- `/administracion/gestion`: CRUD de categorías, edición de opciones y mensajes.
- `/cuenta`: cambio de contraseña con comprobación de la actual.

No hay pasarela de pago. La recuperación por correo aún no está implementada;
la interfaz informa esa limitación sin simular un cambio de contraseña.

## Consultas en cascada (dos páginas)

En `/categorias`, el panel **Encuentra tu producto** permite seleccionar categoría,
producto y opción. Los productos se consultan al elegir categoría; sus opciones
se consultan al elegir producto. Este panel filtra únicamente productos con
stock y opciones activas; ese filtro no oculta las tarjetas agotadas del catálogo
general. Presenta precio base, stock y adicional de la opción, y permite continuar
a la personalización completa.

En `/administracion/gestion`, **Consultar personalizaciones** ofrece la misma
secuencia, incluyendo agotados e inactivas. Filtra la tabla de opciones por la
categoría, producto y opción seleccionados. El botón de edición abre el modal de
esa opción. Estos datos administrativos requieren sesión con rol ADMIN.

Al cambiar un padre se limpian y deshabilitan los desplegables dependientes hasta
recibir los nuevos datos. Se muestran estados de carga, listas vacías y errores
con reintento. Se ignoran respuestas anteriores que llegan fuera de orden.
Las consultas usan Spring Data JPA y DTO; no dependen de listas fijas en JavaScript.
No se necesita modificar el esquema SQL para esta funcionalidad.

## Pruebas

Desde la carpeta del proyecto, en Bash:

```bash
./mvnw test
```

En PowerShell:

```powershell
.\mvnw.cmd test
```

Las pruebas de servidor configuran su propia base H2 en memoria; no necesitan
que tu servidor MySQL esté iniciado ni usan su contraseña.

Pruebas opcionales de interacción de los desplegables (Node.js 18+):

```bash
npm ci
npm run test:ui
```

En PowerShell puedes usar los ejecutables de Windows:

```powershell
npm.cmd ci
npm.cmd run test:ui
```

Node.js solo es necesario para estas pruebas de interfaz; la aplicación sigue
arrancando con Java/Maven y no necesita un proceso de compilación de frontend.
Las pruebas de JavaScript usan jsdom y respuestas simuladas para comprobar
selecciones, limpieza, consultas tardías, permisos de edición en la interfaz,
listas vacías, errores y reintento. Las pruebas de servidor comprueban por separado
los permisos reales y las consultas contra la base de datos.

Las pruebas de servidor usan H2 en modo MySQL, aislada de la base local; incluyen
renderizado Thymeleaf con MockMvc, reglas de carrito, confirmación, permisos y CRUD. H2 no
sustituye la prueba final sobre MySQL 8 ni la revisión visual en navegador.

Ver [revisión del avance 3](docs/revision-avance3.md) para hallazgos, correcciones,
limitaciones y pendientes de la rúbrica.

## Documentos del grupo

- [Informe compartido](https://utpedupe-my.sharepoint.com/:w:/g/personal/u24268913_utp_edu_pe/IQAj_ABtRRULRZs7FaCx9Uj9AQ8qbTL_AkfGYUdiAa0A1Uw)
- [Presentación](https://www.canva.com/design/DAHWceo15xg/nIt4bVkshpe6G_nwakZIHg/edit)

Estos enlaces se conservan como referencias del grupo; su contenido no forma
parte de las comprobaciones automatizadas de este repositorio.
