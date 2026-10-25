# Revisión técnica del avance 3 — LittleDokyHub

Fecha: 25 de octubre de 2026.

Base revisada: `IDynamixI985/LittleDokyHub`, rama `apf3-fabricio`, commit
`6a2e788acfd4983785be68bdef8edeae57693cfe`.

Se copió esa rama a `Dokyzzz/LittleDokyHub:apf3-fabricio`. Las correcciones se
mantienen en `revision-apf3`; las ramas `main` y `a` del fork se conservan.
La revisión incluye el código Java, repositorios, servicios, controladores,
configuración, plantillas, JavaScript, SQL, recursos locales y pruebas.
No incluye el contenido de los enlaces externos de informe y presentación.

## Errores encontrados y corregidos

| Prioridad | Problema original | Cambio |
| --- | --- | --- |
| Alta | El SQL insertaba datos sin crear ninguna tabla. Fallaba sobre una base nueva. | Script con las 12 tablas, claves foráneas, restricciones e índices, antes de los datos iniciales. |
| Alta | Personalización, resumen, modal y entrega usaban `localStorage`, desconectados de MySQL. | Formularios Thymeleaf y modal conectados al carrito del servidor. Las opciones se leen de la base de datos. |
| Alta | Los precios y opciones del prototipo diferían del SQL y podían alterarse en el navegador. | IDs de opciones validados por producto y estado. Precio y total se calculan otra vez en el servidor. |
| Alta | Checkout ignoraba la entrega y el pago seleccionados y no había pantalla de confirmación. | Validación de entrega/dirección/pago, envío calculado en servidor y persistencia de la selección; nueva vista del pedido con control de propietario. |
| Alta | `/producto/{id}` y `/pedidos/{id}` referían plantillas inexistentes. | Redirección a personalización existente y plantilla de confirmación. |
| Alta | “Desactivar” eliminaba físicamente el producto y podía fallar por referencias. | Retiro mediante stock cero, conservando producto, opciones e historial. Reponer stock lo habilita otra vez. |
| Alta | La contraseña de MySQL estaba escrita en el repositorio. | Variables `DB_USER`, `DB_PASSWORD`, `DB_URL`; ya no hay clave del servidor en la configuración actual. La clave anterior permanece en el historial: cambiarla si aún se utiliza. |
| Media | La recuperación simulaba códigos y un cambio de contraseña sin modificar el usuario. | Las rutas informan que el envío por correo no está configurado. No se simula éxito. |
| Media | Editar categorías reemplazaba descripción e icono por valores nulos. | El servicio actualiza código/nombre y conserva esos metadatos. |
| Media | Los mensajes se leían con JDBC como `Timestamp` pero se formateaban con `#temporals`. | Lectura JPA de `Mensaje` con `LocalDateTime`, compatible con la plantilla. |
| Media | La categoría “entradas” y las nuevas categorías no aparecían en el catálogo. | Se renderizan todas las categorías y sus productos. |
| Media | Los importes usaban `Double`. | `BigDecimal` y columnas `DECIMAL(10,2)`, con límite de decimales en el formulario de productos. |
| Media | Actualizar cantidades no comprobaba disponibilidad; confirmar no verificaba explícitamente al propietario del carrito. | Comprobación de stock y propiedad en el servicio, además de los controles del controlador. |
| Media | Un objeto categoría sin ID podía provocar `NullPointerException`. | Validación explícita de categoría e identificador antes de guardar. |
| Media | El listado de opciones mostraba “Producto #ID” y no una consulta DTO entre tablas. | Proyección `OpcionListado` mediante consulta JPQL que une producto y opción y muestra su nombre. |
| Media | Filtrar categorías y contar clientes cargaba tablas enteras en memoria. | Consultas derivadas de Spring Data para filtrar y contar en base de datos. |
| Media | Las pruebas dependían del MySQL personal y del mecanismo de instrumentación del equipo. | Base H2 aislada para pruebas y configuración de Mockito compatible con este entorno. |

Se eliminaron cuatro scripts del prototipo que ya no eran referenciados por
ninguna plantilla (`admin`, `contact`, `personalizar`, `resumen-pedido`) para
que no reintroduzcan la persistencia antigua en el navegador.

## Estructura de datos comprobada

| Tabla | Clave primaria | Relaciones |
| --- | --- | --- |
| roles | id | Un rol tiene muchos usuarios. |
| usuarios | id | rol_id → roles; un usuario tiene pedidos y un carrito. |
| categorias | id | Una categoría tiene muchos productos. |
| productos | id | categoria_id → categorias. |
| opciones | id | producto_id → productos. |
| pedidos | id | usuario_id → usuarios. |
| detalles_pedido | id | pedido_id → pedidos; producto_id → productos. |
| carritos | id | usuario_id → usuarios, único y opcional para invitados. |
| carrito_items | id | carrito_id → carritos; producto_id → productos. Combinación carrito/producto/configuración única. |
| compra_snapshot | pedido_id | pedido_id → pedidos, relación uno a uno. |
| detalle_snapshot | detalle_id | detalle_id → detalles_pedido, relación uno a uno. |
| mensajes | id | Mensajes de contacto independientes. |

El ejemplo de reservas de las indicaciones no corresponde literalmente a este
repositorio: LittleDokyHub vende productos y gestiona pedidos de una pizzería.
Las tablas propuestas en el enunciado son sugeridas. Confirmar con el docente
que el informe use de forma consistente este alcance y sus entidades reales.

## Estado frente a indicaciones y rúbrica

| Criterio | Evidencia / pendiente |
| --- | --- |
| Spring Boot, Thymeleaf, MySQL, JPA/Hibernate | Dependencias y configuración existentes, entidades y repositorios. Ejecución automatizada verificada con H2, no con MySQL real. |
| Mínimo 5 tablas relacionadas | Se identifican 12 tablas y se incluye estructura SQL. `mensajes` es independiente; las otras 11 forman el modelo conectado. |
| Entidades, repositorios, servicios y controladores | Existen. Compras y parte de gestión aún usan JDBC; si el profesor exige JPA en todos los flujos, migrarlos a los repositorios existentes. |
| Dos CRUD completos con Bootstrap | Categorías y productos. Productos usa baja lógica mediante stock cero; documentarlo en la exposición. |
| Relaciones entre entidades | `ManyToOne`, `OneToOne`, `MapsId`, claves foráneas en SQL. |
| Validación y mensajes en formularios | Validaciones de producto, categoría, registro y checkout; errores visibles. Administración muestra un resumen por redirección; conviene conservar los valores y asociar cada mensaje a su campo. |
| Consultas entre tablas con DTO | `OpcionListado`: JPQL producto/opción. `CompraService.Linea`: datos calculados del carrito. Si se espera más de un reporte DTO con joins, agregar ventas por fecha/categoría o pedidos por cliente. |
| Consultas en cascada con desplegables, mínimo 2 páginas | **Implementado.** Categoría → producto → opción en `/categorias` y `/administracion/gestion`, mediante consultas JPA/DTO al servidor. Limpieza de hijos, carga, estados vacíos, reintento y protección contra respuestas tardías. |
| Menús y páginas organizados | Navegación y plantillas Bootstrap existentes; renderizado verificado. Falta revisión visual manual en móvil y escritorio. |
| Modelo entidad-relación en el informe | El modelo puede derivarse de la tabla anterior; no se encontró un diagrama entregable en esta rama. |
| Informe y capturas de CRUD | No se verificó su contenido: el README enlaza documentos externos. Prepararlos con evidencia real de ejecución. |

## Comprobaciones

Resultado actualizado: **24 pruebas de servidor** mediante `./mvnw test` y **7 pruebas de interacción** mediante `npm run test:ui`, sin fallos ni errores.

- Pruebas existentes de codificación de contraseñas, migración BCrypt y carga de imágenes.
- Inicio del contexto Spring Boot con esquema JPA aislado.
- MockMvc renderiza las páginas públicas, personalización, resumen, entrega, cuenta y administración con datos de prueba.
- CRUD de categorías, creación/edición de productos y validación de precio y categoría.
- Confirmación con opciones reales, cálculo de importes en servidor, envío, stock, snapshots y vaciado del carrito.
- Acceso a pedidos limitado a su propietario, protección CSRF y administración limitada al rol ADMIN.
- Rechazo de opciones duplicadas/inactivas/incompletas, exceso de stock y apropiación de otro carrito.
- Conservación del historial al retirar un producto y de metadatos al editar categoría.
- Ejecución del SQL sobre una base H2 vacía en modo MySQL, repetición sin duplicar productos y comprobación de claves foráneas/índice único y hash demo.
- Comprobación de rutas de imágenes del SQL y de errores de espacios en el diff.

H2 en modo MySQL no reproduce todos los detalles de MySQL 8, como colaciones,
comportamiento exacto de bloqueos y migraciones DDL. No se han ejecutado las
migraciones sobre tu base real ni pruebas visuales en un navegador. Antes de
entregar, probar importación, login demo, CRUD y pedido completo sobre MySQL 8.

## Recomendaciones priorizadas

1. Mostrar las dos consultas en cascada en la exposición y añadir capturas: categoría → producto → opción en catálogo y gestión, incluida una categoría vacía y el cambio de selección.
2. Probar la instalación desde cero sobre MySQL 8 y crear capturas reales de registrar/listar/editar/retirar producto y registrar/listar/editar/eliminar categoría.
3. Añadir el diagrama ER y actualizar el informe con las 12 tablas, relaciones, baja lógica, DTO y validaciones realmente implementadas.
4. Si se exige persistencia exclusivamente JPA, llevar compras y edición de opciones a repositorios/servicios JPA, manteniendo transacciones y bloqueos del stock.
5. Conservar datos de formularios administrativos cuando hay error, mostrar mensajes por campo y paginar listados. No cargar todo el catálogo y los mensajes en cada petición.
6. Definir una política única de contraseñas para registro y cambio. Implementar recuperación por correo con tokens de un solo uso y expiración si entra en el alcance.
7. Añadir estados de pedido administrables, historial del cliente y pruebas concurrentes en MySQL para compras del último stock y doble confirmación. Los bloqueos existentes necesitan comprobarse con ese motor.
8. Gestionar migraciones versionadas (por ejemplo Flyway), retirar archivos huérfanos de imágenes con una política definida y no ejecutar una migración masiva de contraseñas en cada arranque de producción.

Estas recomendaciones no significan que todos esos módulos sean obligatorios
para la entrega 3. Las dos páginas con consultas en cascada ya están implementadas;
falta incorporar su evidencia al informe y comprobarlas con MySQL real.


## Actualización: cascadas en catálogo y administración

Los endpoints públicos `/api/catalogo/categorias/{id}/productos` y
`/api/catalogo/categorias/{id}/productos/{productoId}/opciones` muestran únicamente
productos con stock y opciones activas. Las rutas equivalentes bajo
`/administracion/consultas` incluyen agotados e inactivas y requieren rol ADMIN.
El cliente no puede activar ese acceso enviando parámetros adicionales.

La relación categoría/producto se comprueba en el servidor y los joins filtran
los hijos por sus padres. Referencias inexistentes o combinaciones manipuladas
responden 404. Los resultados son DTO, sin exponer entidades de usuario.

En gestión, las selecciones filtran la tabla existente y el botón de edición
apunta al modal de la opción seleccionada. En catálogo, el enlace abre la
personalización del producto; la consulta de una opción no agrega automáticamente
esa configuración al carrito ni sustituye las validaciones de compra.

Las cinco nuevas pruebas de servidor comprueban filtros, estados, permisos,
referencias inválidas y renderizado de ambas páginas. Las siete pruebas de jsdom
comprueban limpieza de hijos, respuestas fuera de orden, listas vacías, errores,
reintento, filtrado/edición y representación segura de nombres. No equivalen a
una revisión visual en navegador. No se requieren cambios de SQL para las cascadas.
