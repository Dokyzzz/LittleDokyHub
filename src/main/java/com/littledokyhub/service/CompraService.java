package com.littledokyhub.service;

import com.littledokyhub.model.*;
import com.littledokyhub.repository.*;
import com.littledokyhub.form.CheckoutForm;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;

@Service
@Transactional
public class CompraService {
    private final EntityManager em;
    private final CarritoRepository carritos;
    private final CarritoItemRepository items;
    private final ProductoRepository productos;
    private final OpcionRepository opciones;
    private final UsuarioRepository usuarios;
    private final PedidoRepository pedidos;
    private final DetallePedidoRepository detalles;
    private final CompraSnapshotRepository compras;
    private final DetalleSnapshotRepository snapshots;
    private final Validator validator;
    private final PersonalizacionService personalizacion;

    public CompraService(EntityManager em, CarritoRepository carritos, CarritoItemRepository items,
            ProductoRepository productos, OpcionRepository opciones, UsuarioRepository usuarios,
            PedidoRepository pedidos, DetallePedidoRepository detalles, CompraSnapshotRepository compras,
            DetalleSnapshotRepository snapshots, Validator validator, PersonalizacionService personalizacion) {
        this.em = em; this.carritos = carritos; this.items = items; this.productos = productos;
        this.opciones = opciones; this.usuarios = usuarios; this.pedidos = pedidos;
        this.detalles = detalles; this.compras = compras; this.snapshots = snapshots; this.validator = validator; this.personalizacion = personalizacion;
    }

    private <T> T bloquear(Class<T> tipo, Object id) {
        T entidad = em.find(tipo, id, LockModeType.PESSIMISTIC_WRITE);
        if (entidad == null) throw new IllegalArgumentException("Registro inexistente");
        // También actualiza entidades ya presentes en el contexto de persistencia.
        em.refresh(entidad, LockModeType.PESSIMISTIC_WRITE);
        return entidad;
    }

    private Carrito nuevoCarrito() {
        Carrito c = new Carrito();
        c.setId(UUID.randomUUID().toString());
        em.persist(c);
        return c;
    }

    public Producto producto(int id) {
        return productos.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto inexistente"));
    }

    public List<Opcion> opciones(int id) {
        return opciones.findByProductoIdAndActivoTrueOrderByGrupoAscIdAsc(id).stream().filter(personalizacion::coherente).toList();
    }

    public String carrito(String token, String email) {
        Usuario usuario = null;
        if (email != null) {
            var u = usuarios.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Usuario no disponible"));
            usuario = bloquear(Usuario.class, u.getId());
        }
        Carrito candidato = token != null && token.matches("[0-9a-f-]{36}") ? carritos.findById(token).orElse(null) : null;
        Carrito propio = usuario == null ? null : carritos.findByUsuarioId(usuario.getId()).orElse(null);
        // Orden fijo al bloquear dos carritos: evita bloqueos cruzados durante una fusión.
        var ids = new TreeSet<String>();
        if (candidato != null) ids.add(candidato.getId());
        if (propio != null) ids.add(propio.getId());
        for (String id : ids) bloquear(Carrito.class, id);
        if (candidato != null && candidato.getUsuario() != null
                && (usuario == null || !candidato.getUsuario().getId().equals(usuario.getId()))) candidato = null;
        if (propio != null) {
            if (candidato != null && !candidato.getId().equals(propio.getId()) && candidato.getUsuario() == null) {
                for (var origen : items.findByCarritoIdOrderById(candidato.getId())) {
                    CarritoItem destino = items.findByCarritoIdAndProductoIdAndConfiguracion(propio.getId(),
                            origen.getProducto().getId(), origen.getConfiguracion()).orElse(null);
                    if (destino == null) {
                        destino = new CarritoItem(); destino.setCarrito(propio); destino.setProducto(origen.getProducto());
                        destino.setConfiguracion(origen.getConfiguracion()); destino.setCantidad(0);
                    }
                    destino.setCantidad(Math.min(99, destino.getCantidad() + origen.getCantidad()));
                    items.save(destino);
                    items.delete(origen);
                }
            }
            return propio.getId();
        }
        if (candidato == null) candidato = nuevoCarrito();
        if (usuario != null) candidato.setUsuario(usuario);
        em.flush();
        return candidato.getId();
    }

    public record Linea(long id, int productoId, String nombre, String configuracion, int cantidad,
            BigDecimal precio, BigDecimal subtotal, boolean disponible) {}

    private List<Integer> ids(String config) {
        return config.isEmpty() ? List.of() : Arrays.stream(config.split(",")).map(Integer::valueOf).toList();
    }

    private List<Opcion> validar(int pid, List<Integer> selected) {
        if (selected.size() != new HashSet<>(selected).size()) throw new IllegalArgumentException("Opciones duplicadas");
        var all = opciones(pid);
        var chosen = all.stream().filter(o -> selected.contains(o.getId())).toList();
        if (chosen.size() != selected.size()) throw new IllegalArgumentException("Una opción ya no está disponible");
        for (String group : List.of("TAMANO", "MASA", "SALSA", "TEMPERATURA"))
            if (all.stream().anyMatch(o -> group.equals(personalizacion.seccion(o)))
                    && chosen.stream().filter(o -> group.equals(personalizacion.seccion(o))).count() != 1)
                throw new IllegalArgumentException("Selecciona una opción de " + group);
        return chosen;
    }

    public void agregar(String cart, int pid, List<Integer> options, int qty) {
        Carrito carrito = bloquear(Carrito.class, cart);
        if (qty < 1 || qty > 99) throw new IllegalArgumentException("Cantidad de 1 a 99");
        Producto p = producto(pid);
        validar(pid, options);
        String config = options.stream().sorted().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        CarritoItem item = items.findByCarritoIdAndProductoIdAndConfiguracion(cart, pid, config).orElse(null);
        int total = qty + (item == null ? 0 : item.getCantidad());
        if (total > 99 || total > p.getStock()) throw new IllegalArgumentException("Cantidad superior al stock disponible");
        if (item == null) {
            item = new CarritoItem(); item.setCarrito(carrito); item.setProducto(p); item.setConfiguracion(config);
        }
        item.setCantidad(total);
        items.saveAndFlush(item);
    }

    public List<Linea> lineas(String cart) {
        List<Linea> result = new ArrayList<>();
        for (var item : items.findByCarritoIdOrderById(cart)) {
            Producto p = item.getProducto();
            BigDecimal unit = p.getPrecio();
            String desc = "";
            boolean disponible = p.getStock() >= item.getCantidad();
            try {
                for (var o : validar(p.getId(), ids(item.getConfiguracion()))) {
                    unit = unit.add(o.getPrecio());
                    desc += o.getGrupo() + ": " + o.getNombre() + "; ";
                }
            } catch (IllegalArgumentException e) {
                disponible = false; desc = "Opciones no disponibles; elimina y vuelve a configurar";
            }
            result.add(new Linea(item.getId(), p.getId(), p.getNombre(), desc, item.getCantidad(),
                    unit, unit.multiply(BigDecimal.valueOf(item.getCantidad())), disponible));
        }
        return result;
    }

    public void cantidad(String cart, long id, int qty) {
        bloquear(Carrito.class, cart);
        if (qty < 0 || qty > 99) throw new IllegalArgumentException("Cantidad inválida");
        var item = items.findByIdAndCarritoId(id, cart).orElse(null);
        if (qty == 0) {
            if (item != null) items.delete(item);
        } else {
            if (item == null) throw new IllegalArgumentException("El producto no pertenece a tu carrito");
            if (item.getProducto().getStock() < qty) throw new IllegalArgumentException("Cantidad superior al stock disponible");
            item.setCantidad(qty);
        }
        em.flush();
    }

    public BigDecimal total(List<Linea> lines) {
        return lines.stream().map(Linea::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int confirmar(String cart, String email, CheckoutForm checkout) {
        var violations = validator.validate(checkout);
        if (!violations.isEmpty()) throw new IllegalArgumentException(violations.iterator().next().getMessage());
        Carrito carrito = bloquear(Carrito.class, cart);
        if (carrito.getUsuario() == null || !carrito.getUsuario().getEmail().equals(email))
            throw new IllegalArgumentException("El carrito no pertenece al usuario");
        var seleccion = items.findByCarritoIdOrderById(cart);
        var cantidades = new TreeMap<Integer, Integer>();
        for (var item : seleccion) cantidades.merge(item.getProducto().getId(), item.getCantidad(), Integer::sum);
        for (var entry : cantidades.entrySet()) {
            Producto p = bloquear(Producto.class, entry.getKey());
            if (p.getStock() < entry.getValue()) throw new IllegalArgumentException("Stock insuficiente");
        }
        var lines = lineas(cart);
        if (lines.isEmpty() || lines.stream().anyMatch(l -> !l.disponible()))
            throw new IllegalArgumentException("Revisa el carrito y la disponibilidad antes de continuar");
        Pedido pedido = new Pedido(); pedido.setUsuario(carrito.getUsuario());
        pedido.setEstado("PENDIENTE_PAGO"); pedido.setTotal(total(lines).add(checkout.costoEnvio()));
        if (pedido.getTotal().compareTo(new BigDecimal("99999999.99")) > 0)
            throw new IllegalArgumentException("El total supera el importe permitido");
        pedidos.saveAndFlush(pedido);
        CompraSnapshot compra = new CompraSnapshot(); compra.setPedido(pedido);
        compra.setEntrega(checkout.getEntrega()); compra.setPago(checkout.getPago());
        compra.setDireccion("RECOJO".equals(checkout.getEntrega()) ? "Recojo en tienda" : checkout.getDireccion().trim());
        em.persist(compra);
        for (var l : lines) {
            Producto p = producto(l.productoId());
            p.setStock(p.getStock() - l.cantidad());
            DetallePedido detalle = detalles.saveAndFlush(new DetallePedido(l.cantidad(), l.precio(), pedido, p));
            DetalleSnapshot snapshot = new DetalleSnapshot(); snapshot.setDetallePedido(detalle);
            snapshot.setNombre(l.nombre()); snapshot.setPersonalizacion(l.configuracion()); em.persist(snapshot);
        }
        items.deleteAll(seleccion);
        em.flush();
        return pedido.getId();
    }

    public record CabeceraPedido(Integer id, String estado, BigDecimal total) {}
    public record DetalleVista(Integer cantidad, BigDecimal subtotal, String nombre, String personalizacion) {}
    public record EntregaVista(String entrega, String direccion, String pago) {}
    public record PedidoVista(CabeceraPedido pedido, List<DetalleVista> detalles, List<EntregaVista> entrega) {}

    @Transactional(readOnly = true)
    public PedidoVista consultarPedido(int id, String email) {
        Pedido pedido = pedidos.findByIdAndUsuarioEmail(id, email).orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        var lineas = detalles.findByPedidoId(id).stream().map(d -> {
            var s = snapshots.findByDetallePedidoId(d.getId()).orElseThrow();
            return new DetalleVista(d.getCantidad(), d.getSubtotal(), s.getNombre(), s.getPersonalizacion());
        }).toList();
        var entrega = compras.findById(id).map(s -> List.of(new EntregaVista(s.getEntrega(), s.getDireccion(), s.getPago())))
                .orElse(List.of());
        return new PedidoVista(new CabeceraPedido(id, pedido.getEstado(), pedido.getTotal()), lineas, entrega);
    }
}
