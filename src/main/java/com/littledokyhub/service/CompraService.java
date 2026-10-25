package com.littledokyhub.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;

@Service
@Transactional
public class CompraService {
    private final JdbcTemplate db;

    public CompraService(JdbcTemplate db) {
        this.db = db;
    }

    public List<Map<String, Object>> query(String sql, Object... args) {
        return db.queryForList(sql, args);
    }

    public Map<String, Object> producto(int id) {
        var list = query("SELECT * FROM productos WHERE id=?", id);
        if (list.isEmpty())
            throw new IllegalArgumentException("Producto inexistente");
        return list.get(0);
    }

    public List<Map<String, Object>> opciones(int id) {
        return query("SELECT * FROM opciones WHERE producto_id=? AND activo=1 ORDER BY grupo,id", id);
    }

    public String carrito(String token, String email) {
        if (token == null || !token.matches("[0-9a-f-]{36}"))
            token = UUID.randomUUID().toString();
        if (email == null && !query("SELECT id FROM carritos WHERE id=? AND usuario_id IS NOT NULL", token).isEmpty())
            token = UUID.randomUUID().toString();
        db.update("INSERT IGNORE INTO carritos(id) VALUES(?)", token);
        if (email != null) {
            var users = query("SELECT id FROM usuarios WHERE email=?", email);
            if (users.isEmpty())
                throw new IllegalArgumentException("Usuario no disponible");
            int uid = ((Number) users.get(0).get("id")).intValue();
            db.queryForList("SELECT id FROM usuarios WHERE id=? FOR UPDATE", uid);
            var own = query("SELECT id FROM carritos WHERE usuario_id=?", uid);
            if (own.isEmpty()) {
                if (!query("SELECT id FROM carritos WHERE id=? AND usuario_id IS NOT NULL AND usuario_id<>?", token,
                        uid).isEmpty()) {
                    token = UUID.randomUUID().toString();
                    db.update("INSERT INTO carritos(id) VALUES(?)", token);
                }
                db.update("UPDATE carritos SET usuario_id=? WHERE id=? AND usuario_id IS NULL", uid, token);
            } else {
                String target = own.get(0).get("id").toString();
                if (!target.equals(token)) {
                    var guest = query("SELECT id FROM carritos WHERE id=? AND usuario_id IS NULL FOR UPDATE", token);
                    if (!guest.isEmpty()) {
                        for (var item : query("SELECT * FROM carrito_items WHERE carrito_id=?", token))
                            db.update(
                                    "INSERT INTO carrito_items(carrito_id,producto_id,configuracion,cantidad) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE cantidad=LEAST(99,cantidad+VALUES(cantidad))",
                                    target, item.get("producto_id"), item.get("configuracion"), item.get("cantidad"));
                        db.update("DELETE FROM carrito_items WHERE carrito_id=?", token);
                    }
                    token = target;
                }
            }
        }
        return token;
    }

    public record Linea(long id, int productoId, String nombre, String configuracion, int cantidad, BigDecimal precio,
            BigDecimal subtotal, boolean disponible) {
    }

    private BigDecimal precio(Map<String, Object> p) {
        return new BigDecimal(p.get("precio").toString()).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private List<Integer> ids(String config) {
        return config.isEmpty() ? List.of() : Arrays.stream(config.split(",")).map(Integer::valueOf).toList();
    }

    private List<Map<String, Object>> validar(int pid, List<Integer> selected) {
        if (selected.size() != new HashSet<>(selected).size())
            throw new IllegalArgumentException("Opciones duplicadas");
        var all = opciones(pid);
        var chosen = all.stream().filter(o -> selected.contains(((Number) o.get("id")).intValue())).toList();
        if (chosen.size() != selected.size())
            throw new IllegalArgumentException("Una opción ya no está disponible");
        for (String group : List.of("TAMANO", "MASA"))
            if (all.stream().anyMatch(o -> group.equals(o.get("grupo")))
                    && chosen.stream().filter(o -> group.equals(o.get("grupo"))).count() != 1)
                throw new IllegalArgumentException("Selecciona una opción de " + group);
        return chosen;
    }

    public void agregar(String cart, int pid, List<Integer> options, int qty) {
        db.queryForList("SELECT id FROM carritos WHERE id=? FOR UPDATE", cart);
        if (qty < 1 || qty > 99)
            throw new IllegalArgumentException("Cantidad de 1 a 99");
        var p = producto(pid);
        validar(pid, options);
        if (((Number) p.get("stock")).intValue() < qty)
            throw new IllegalArgumentException("Producto sin disponibilidad suficiente");
        String config = options.stream().sorted().map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
        var existing = query(
                "SELECT cantidad FROM carrito_items WHERE carrito_id=? AND producto_id=? AND configuracion=?", cart,
                pid, config);
        int total = qty + (existing.isEmpty() ? 0 : ((Number) existing.get(0).get("cantidad")).intValue());
        if (total > 99 || total > ((Number) p.get("stock")).intValue())
            throw new IllegalArgumentException("Cantidad superior al stock disponible");
        db.update(
                "INSERT INTO carrito_items(carrito_id,producto_id,configuracion,cantidad) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE cantidad=VALUES(cantidad)",
                cart, pid, config, total);
    }

    public List<Linea> lineas(String cart) {
        List<Linea> result = new ArrayList<>();
        for (var item : query("SELECT * FROM carrito_items WHERE carrito_id=? ORDER BY id", cart)) {
            int pid = ((Number) item.get("producto_id")).intValue(), qty = ((Number) item.get("cantidad")).intValue();
            var p = producto(pid);
            BigDecimal unit = precio(p);
            String desc = "";
            boolean available = ((Number) p.get("stock")).intValue() >= qty;
            try {
                var chosen = validar(pid, ids(item.get("configuracion").toString()));
                for (var o : chosen) {
                    unit = unit.add(new BigDecimal(o.get("precio").toString()));
                    desc += o.get("grupo") + ": " + o.get("nombre") + "; ";
                }
            } catch (IllegalArgumentException e) {
                available = false;
                desc = "Opciones no disponibles; elimina y vuelve a configurar";
            }
            result.add(new Linea(((Number) item.get("id")).longValue(), pid, p.get("nombre").toString(), desc, qty,
                    unit, unit.multiply(BigDecimal.valueOf(qty)), available));
        }
        return result;
    }

    public void cantidad(String cart, long id, int qty) {
        db.queryForList("SELECT id FROM carritos WHERE id=? FOR UPDATE", cart);
        if (qty < 0 || qty > 99)
            throw new IllegalArgumentException("Cantidad inválida");
        if (qty == 0)
            db.update("DELETE FROM carrito_items WHERE id=? AND carrito_id=?", id, cart);
        else
            db.update("UPDATE carrito_items SET cantidad=? WHERE id=? AND carrito_id=?", qty, id, cart);
    }

    public BigDecimal total(List<Linea> lines) {
        return lines.stream().map(Linea::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int confirmar(String cart, String email) {
        db.queryForList("SELECT id FROM carritos WHERE id=? FOR UPDATE", cart);
        var items = query("SELECT producto_id FROM carrito_items WHERE carrito_id=? ORDER BY producto_id", cart);
        for (var item : items)
            db.queryForList("SELECT id FROM productos WHERE id=? FOR UPDATE", item.get("producto_id"));
        var lines = lineas(cart);
        if (lines.isEmpty() || lines.stream().anyMatch(l -> !l.disponible()))
            throw new IllegalArgumentException("Revisa el carrito y la disponibilidad antes de continuar");
        int uid = db.queryForObject("SELECT id FROM usuarios WHERE email=?", Integer.class, email);
        db.update("INSERT INTO pedidos(fecha,total,estado,usuario_id) VALUES(NOW(),?,'PENDIENTE_PAGO',?)", total(lines),
                uid);
        int order = db.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        db.update("INSERT INTO compra_snapshot VALUES(?,'RECOJO','Recojo en tienda','EFECTIVO_EN_TIENDA')", order);
        for (var l : lines) {
            if (db.update("UPDATE productos SET stock=stock-? WHERE id=? AND stock>=?", l.cantidad(), l.productoId(),
                    l.cantidad()) != 1)
                throw new IllegalArgumentException("Stock insuficiente");
            db.update(
                    "INSERT INTO detalles_pedido(cantidad,precio_unitario,subtotal,pedido_id,producto_id) VALUES(?,?,?,?,?)",
                    l.cantidad(), l.precio(), l.subtotal(), order, l.productoId());
            int detail = db.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
            db.update("INSERT INTO detalle_snapshot VALUES(?,?,?)", detail, l.nombre(), l.configuracion());
        }
        db.update("DELETE FROM carrito_items WHERE carrito_id=?", cart);
        return order;
    }
}
