# LittleDokyHub — avance 3

Aplicación de pedidos de pizzería con Spring Boot 3.3.4, Java 17, Thymeleaf,
Spring Security, Spring Data JPA/Hibernate y MySQL 8+.

## Ejecutar

1. Instalar Java 17 y MySQL 8+. Maven se descarga mediante el wrapper incluido.
2. En una base nueva, importar `bd-littledokyhub.sql` **antes** de iniciar la app:

   ```bash
   mysql -u root -p < bd-littledokyhub.sql
   ```

3. Configurar la conexión con variables de entorno. Ejemplo Bash:

   ```bash
   export DB_USER=root
   export DB_PASSWORD='tu-clave-local'
   # Opcional: DB_URL, PORT (por defecto 8081)
   ./mvnw spring-boot:run
   ```

   En PowerShell:

   ```powershell
   $env:DB_USER = 'root'
   $env:DB_PASSWORD = 'tu-clave-local'
   .\mvnw.cmd spring-boot:run
   ```

4. Abrir `http://localhost:8081`.

Hibernate valida la estructura (`DDL_AUTO=validate` por defecto); no crea ni
modifica tablas al arrancar. Si ya tienes la base antigua, respáldala y consulta
`scripts/migrar-importes-apf3.sql` para convertir importes y agregar la unicidad
del carrito. `CREATE TABLE IF NOT EXISTS` no actualiza columnas preexistentes.

## Usuarios de demostración

Solo para una base local de desarrollo:

| Rol | Correo | Contraseña inicial |
| --- | --- | --- |
| Cliente | cliente.demo@littledokyhub.test | ClienteDemo2026! |
| Administrador | admin.demo@littledokyhub.test | AdminDemo2026! |

El script almacena hashes BCrypt. Cambiar estas claves o eliminar las cuentas
antes de usar datos reales. No se incluyen credenciales de tu servidor MySQL.

## Flujos

- `/categorias`: catálogo de todas las categorías registradas.
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
se consultan al elegir producto. Solo muestra productos con stock y opciones
activas. Presenta precio base, stock y adicional de la opción, y permite continuar
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

```bash
./mvnw test
```

Pruebas opcionales de interacción de los desplegables (Node.js 18+):

```bash
npm ci
npm run test:ui
```

Node.js solo es necesario para estas pruebas de interfaz; la aplicación sigue
arrancando con Java/Maven y no necesita un proceso de compilación de frontend.
Las pruebas de JavaScript usan jsdom y respuestas simuladas para comprobar
selecciones, limpieza, consultas tardías, permisos de edición en la interfaz,
listas vacías, errores y reintento. Las pruebas de servidor comprueban por separado
los permisos reales y las consultas contra la base de datos.

Las pruebas usan H2 en modo MySQL, aislada de la base local; incluyen renderizado
Thymeleaf con MockMvc, reglas de carrito, confirmación, permisos y CRUD. H2 no
sustituye la prueba final sobre MySQL 8 ni la revisión visual en navegador.

Ver [revisión del avance 3](docs/revision-avance3.md) para hallazgos, correcciones,
limitaciones y pendientes de la rúbrica.

## Documentos del grupo

- [Informe compartido](https://utpedupe-my.sharepoint.com/:w:/g/personal/u24268913_utp_edu_pe/IQAj_ABtRRULRZs7FaCx9Uj9AQ8qbTL_AkfGYUdiAa0A1Uw)
- [Presentación](https://www.canva.com/design/DAHWceo15xg/nIt4bVkshpe6G_nwakZIHg/edit)

Estos enlaces se conservan como referencias del grupo; su contenido no forma
parte de las comprobaciones automatizadas de este repositorio.
