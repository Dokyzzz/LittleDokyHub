package com.littledokyhub.integration;

import com.littledokyhub.service.CompraService;
import com.littledokyhub.form.CheckoutForm;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Sin transacción envolvente de prueba: verifica commits y rollback reales del servicio. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:jpa-transactions;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000")
class JpaCompraTransactionTests {
    @Autowired CompraService compra;
    @Autowired JdbcTemplate db;

    @BeforeEach
    void preparar() {
        for (String tabla : List.of("detalle_snapshot", "compra_snapshot", "detalles_pedido", "pedidos", "carrito_items", "carritos", "opciones", "productos", "categorias", "usuarios", "roles"))
            db.update("DELETE FROM " + tabla);
        db.update("INSERT INTO roles(id,nombre) VALUES(1,'CLIENTE')");
        db.update("INSERT INTO usuarios(id,nombre,email,password,activo,rol_id) VALUES(1,'Cliente Uno','uno@test.pe','password123',true,1),(2,'Cliente Dos','dos@test.pe','password123',true,1)");
        db.update("INSERT INTO categorias(id,codigo,nombre) VALUES(1,'clasicas','Clásicas')");
        db.update("INSERT INTO productos(id,nombre,precio,stock,categoria_id) VALUES(1,'Pizza',32,10,1)");
        db.update("INSERT INTO opciones(id,producto_id,grupo,nombre,precio,activo) VALUES(1,1,'TAMANO','Mediana',0,true),(2,1,'TAMANO','Familiar',12,true),(3,1,'MASA','Clásica',0,true)");
    }

    @Test
    void fusionDeInvitadoEsPersistenteYNoDuplicaLineas() {
        String propio = compra.carrito(null, "uno@test.pe");
        compra.agregar(propio, 1, List.of(1,3), 2);
        String invitado = compra.carrito(null, null);
        compra.agregar(invitado, 1, List.of(1,3), 3);
        assertEquals(propio, compra.carrito(invitado, "uno@test.pe"));
        assertEquals(1, compra.lineas(propio).size());
        assertEquals(5, compra.lineas(propio).get(0).cantidad());
        assertTrue(compra.lineas(invitado).isEmpty());
        compra.carrito(invitado, "uno@test.pe");
        assertEquals(5, compra.lineas(propio).get(0).cantidad());
    }

    @Test
    void stockSeCompruebaSumandoConfiguracionesYSinPedidoParcial() {
        String cart = compra.carrito(null, "uno@test.pe");
        compra.agregar(cart, 1, List.of(1,3), 6);
        compra.agregar(cart, 1, List.of(2,3), 5);
        assertThrows(IllegalArgumentException.class, () -> compra.confirmar(cart, "uno@test.pe", new CheckoutForm()));
        assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM pedidos", Integer.class));
        assertEquals(10, db.queryForObject("SELECT stock FROM productos WHERE id=1", Integer.class));
        assertEquals(2, compra.lineas(cart).size());
    }

    @Test
    void confirmarDosVecesNoDuplicaElPedido() {
        String cart = compra.carrito(null, "uno@test.pe");
        compra.agregar(cart, 1, List.of(1,3), 1);
        int id = compra.confirmar(cart, "uno@test.pe", new CheckoutForm());
        assertThrows(IllegalArgumentException.class, () -> compra.confirmar(cart, "uno@test.pe", new CheckoutForm()));
        assertEquals(1, db.queryForObject("SELECT COUNT(*) FROM pedidos", Integer.class));
        db.update("UPDATE productos SET nombre='Nuevo nombre' WHERE id=1");
        assertEquals("Pizza", compra.consultarPedido(id, "uno@test.pe").detalles().get(0).nombre());
        assertEquals(9, db.queryForObject("SELECT stock FROM productos WHERE id=1", Integer.class));
    }

    @Test
    void dosComprasConcurrentesDelUltimoStockSoloConfirmanUna() throws Exception {
        db.update("UPDATE productos SET stock=1 WHERE id=1");
        String uno = compra.carrito(null, "uno@test.pe"), dos = compra.carrito(null, "dos@test.pe");
        compra.agregar(uno, 1, List.of(1,3), 1); compra.agregar(dos, 1, List.of(1,3), 1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            var first = pool.submit(() -> confirmarAlIniciar(inicio, uno, "uno@test.pe"));
            var second = pool.submit(() -> confirmarAlIniciar(inicio, dos, "dos@test.pe"));
            inicio.countDown();
            assertEquals(1, first.get(10, TimeUnit.SECONDS) + second.get(10, TimeUnit.SECONDS));
            assertEquals(0, db.queryForObject("SELECT stock FROM productos WHERE id=1", Integer.class));
            assertEquals(1, db.queryForObject("SELECT COUNT(*) FROM pedidos", Integer.class));
            assertEquals(1, db.queryForObject("SELECT COUNT(*) FROM carrito_items", Integer.class));
        } finally { pool.shutdownNow(); }
    }

    private int confirmarAlIniciar(CountDownLatch inicio, String cart, String email) throws InterruptedException {
        inicio.await();
        try { compra.confirmar(cart, email, new CheckoutForm()); return 1; }
        catch (IllegalArgumentException expected) { return 0; }
    }
}
