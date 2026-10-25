package com.littledokyhub.integration;

import com.littledokyhub.form.CheckoutForm;
import com.littledokyhub.service.CompraService;
import com.littledokyhub.service.ProductoService;
import com.littledokyhub.service.CategoriaService;
import com.littledokyhub.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "cliente@test.pe", roles = "CLIENTE")
class CommerceIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate db;
    @Autowired CompraService compra;
    @Autowired ProductoService productos;
    @Autowired CategoriaService categorias;
    @Autowired CategoriaRepository categoriaRepository;

    @BeforeEach
    void seed() {
        db.update("INSERT INTO roles(id,nombre) VALUES(1,'CLIENTE'),(2,'ADMIN')");
        db.update("INSERT INTO usuarios(id,nombre,email,password,activo,rol_id) VALUES(1,'Cliente Demo','cliente@test.pe','password123',true,1),(2,'Otro Cliente','otro@test.pe','password123',true,1)");
        db.update("INSERT INTO categorias(id,codigo,nombre,descripcion,icono) VALUES(1,'clasicas','Clásicas','Descripción original','bi-star'),(2,'entradas','Entradas',null,null)");
        db.update("INSERT INTO productos(id,nombre,precio,stock,categoria_id,imagen_url) VALUES(1,'Pizza Test',32.00,10,1,'/images/logo.png'),(2,'Pan al ajo',10.00,5,2,'/images/logo.png')");
        db.update("INSERT INTO opciones(id,producto_id,grupo,nombre,precio,activo) VALUES(1,1,'TAMANO','Mediana',0,true),(2,1,'TAMANO','Familiar',12,true),(3,1,'MASA','Clásica',0,true),(4,1,'EXTRA','Queso',4,true),(5,1,'EXTRA','Inactiva',3,false)");
        db.update("INSERT INTO mensajes(nombre,email,asunto,mensaje,fecha) VALUES('Cliente','cliente@test.pe','Consulta','Hola',CURRENT_TIMESTAMP)");
    }

    @Test
    void paginasPublicasYCarritoSeRenderizan() throws Exception {
        for (String path : List.of("/", "/categorias", "/personalizar/1", "/pedido/resumen", "/pedido/entrega", "/contacto", "/registro", "/login", "/forgotpassword", "/verifycode", "/resetpassword", "/cuenta"))
            mvc.perform(get(path)).andExpect(status().isOk());
        mvc.perform(get("/categorias")).andExpect(content().string(containsString("Pan al ajo")));
        mvc.perform(get("/personalizar/1")).andExpect(content().string(containsString("Familiar")));
        mvc.perform(get("/producto/1")).andExpect(redirectedUrl("/personalizar/1"));
    }

    @Test
    @WithMockUser(username = "cliente@test.pe", roles = "ADMIN")
    void administracionConMensajesYOpcionesDtoSeRenderiza() throws Exception {
        mvc.perform(get("/administracion")).andExpect(status().isOk());
        mvc.perform(get("/administracion/gestion")).andExpect(status().isOk())
            .andExpect(content().string(containsString("Pizza Test")))
            .andExpect(content().string(containsString("Hola")));
    }

    @Test
    void clienteNoPuedeAdministrarYCheckoutExigeLoginYCsrf() throws Exception {
        mvc.perform(get("/administracion")).andExpect(status().isForbidden());
        mvc.perform(post("/checkout/confirmar").with(anonymous()).with(csrf()))
            .andExpect(status().is3xxRedirection());
        mvc.perform(post("/carrito/agregar").param("productoId", "1"))
            .andExpect(status().isForbidden());
    }

    @Test
    void checkoutPersisteImportesEntregaYDetalleYVaciaCarrito() throws Exception {
        String token = compra.carrito(null, "cliente@test.pe");
        compra.agregar(token, 1, List.of(2,3,4), 2);
        mvc.perform(get("/pedido/resumen").cookie(new Cookie("DOKY_CART", token)))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Pizza Test")));
        mvc.perform(post("/checkout/confirmar").with(csrf()).cookie(new Cookie("DOKY_CART", token))
            .param("entrega", "DELIVERY").param("direccion", "Av. Prueba 123, Lima").param("pago", "EFECTIVO")
            .param("total", "0.01")) // el cliente no decide los importes
            .andExpect(status().is3xxRedirection());
        Integer pedido = db.queryForObject("SELECT id FROM pedidos WHERE usuario_id=1", Integer.class);
        assertEquals(0, new BigDecimal("101.00").compareTo(db.queryForObject("SELECT total FROM pedidos WHERE id=?", BigDecimal.class, pedido)));
        assertEquals(8, db.queryForObject("SELECT stock FROM productos WHERE id=1", Integer.class));
        assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM carrito_items", Integer.class));
        assertEquals("DELIVERY", db.queryForObject("SELECT entrega FROM compra_snapshot WHERE pedido_id=?", String.class, pedido));
        mvc.perform(get("/pedidos/" + pedido)).andExpect(status().isOk())
            .andExpect(content().string(containsString("Av. Prueba 123, Lima")));
        mvc.perform(get("/pedidos/" + pedido).with(user("otro@test.pe")))
            .andExpect(status().isNotFound());
    }

    @Test
    void checkoutInvalidoNoCreaPedidoYConservaFormulario() throws Exception {
        String token = compra.carrito(null, "cliente@test.pe");
        compra.agregar(token, 1, List.of(1,3), 1);
        mvc.perform(post("/checkout/confirmar").with(csrf()).cookie(new Cookie("DOKY_CART", token))
            .param("entrega", "DELIVERY").param("direccion", "").param("pago", "EFECTIVO"))
            .andExpect(status().isOk()).andExpect(model().attributeHasErrors("checkout"));
        assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM pedidos", Integer.class));
        assertEquals(1, db.queryForObject("SELECT COUNT(*) FROM carrito_items", Integer.class));
    }

    @Test
    void opcionesYStockSeValidanEnServidor() {
        String token = compra.carrito(null, "cliente@test.pe");
        assertThrows(IllegalArgumentException.class, () -> compra.agregar(token, 1, List.of(1), 1));
        assertThrows(IllegalArgumentException.class, () -> compra.agregar(token, 1, List.of(1,3,5), 1));
        assertThrows(IllegalArgumentException.class, () -> compra.agregar(token, 1, List.of(1,1,3), 1));
        assertThrows(IllegalArgumentException.class, () -> compra.agregar(token, 1, List.of(1,3), 11));
        compra.agregar(token, 1, List.of(1,3), 2);
        compra.agregar(token, 1, List.of(1,3), 3);
        assertEquals(1, compra.lineas(token).size());
        assertEquals(5, compra.lineas(token).get(0).cantidad());
        long id = compra.lineas(token).get(0).id();
        assertThrows(IllegalArgumentException.class, () -> compra.cantidad(token, id, 11));
        String otro = compra.carrito(null, "otro@test.pe");
        compra.cantidad(otro, id, 0);
        assertEquals(5, compra.lineas(token).get(0).cantidad());
        assertThrows(IllegalArgumentException.class, () -> compra.confirmar(token, "otro@test.pe", new CheckoutForm()));
    }

    @Test
    void retiroProductoNoBorraOpcionesNiHistorialYBloqueaCompra() {
        String token = compra.carrito(null, "cliente@test.pe");
        compra.agregar(token, 1, List.of(1,3), 1);
        int pedido = compra.confirmar(token, "cliente@test.pe", new CheckoutForm());
        assertTrue(productos.eliminar(1));
        assertTrue(productos.buscarPorId(1).isPresent());
        assertEquals(0, productos.buscarPorId(1).orElseThrow().getStock());
        assertEquals(1, db.queryForObject("SELECT COUNT(*) FROM detalles_pedido WHERE pedido_id=?", Integer.class, pedido));
        assertThrows(IllegalArgumentException.class, () -> compra.agregar(token, 1, List.of(1,3), 1));
    }

    @Test
    void loginNoPermiteApropiarseDeCarritoAjeno() {
        String token = compra.carrito(null, "cliente@test.pe");
        compra.agregar(token, 1, List.of(1,3), 1);
        String otherToken = compra.carrito(token, "otro@test.pe");
        assertNotEquals(token, otherToken);
        assertTrue(compra.lineas(otherToken).isEmpty());
        assertFalse(compra.lineas(token).isEmpty());
    }

    @Test
    @WithMockUser(username = "cliente@test.pe", roles = "ADMIN")
    void crudCategoriaConservaMetadatosYProtegeCategoriasConProductos() throws Exception {
        mvc.perform(post("/administracion/gestion/categoria").with(csrf())
            .param("id", "1").param("codigo", "clasicas").param("nombre", "Clásicas nuevas"))
            .andExpect(status().is3xxRedirection());
        var categoria = categoriaRepository.findById(1).orElseThrow();
        assertEquals("Descripción original", categoria.getDescripcion());
        mvc.perform(post("/administracion/gestion/categoria/1/eliminar").with(csrf()))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("error"));
        assertTrue(categoriaRepository.existsById(1));
        mvc.perform(post("/administracion/gestion/categoria").with(csrf())
            .param("codigo", "postres").param("nombre", "Postres"))
            .andExpect(status().is3xxRedirection());
        int id = categoriaRepository.findByCodigoIgnoreCase("postres").orElseThrow().getId();
        mvc.perform(post("/administracion/gestion/categoria/" + id + "/eliminar").with(csrf()))
            .andExpect(status().is3xxRedirection());
        assertFalse(categoriaRepository.existsById(id));
    }

    @Test
    @WithMockUser(username = "cliente@test.pe", roles = "ADMIN")
    void productoConCategoriaSinIdNoProvocaNullPointer() throws Exception {
        mvc.perform(post("/administracion/productos/guardar").with(csrf())
            .param("nombre", "Producto válido").param("precio", "10.00").param("stock", "1").param("categoria.id", ""))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("error"));
    }
    @Test
    @WithMockUser(username = "cliente@test.pe", roles = "ADMIN")
    void productoSeCreaEditaYValidaImportes() throws Exception {
        mvc.perform(post("/administracion/productos/guardar").with(csrf())
            .param("nombre", "Pizza Nueva").param("precio", "20.50").param("stock", "6").param("categoria.id", "1"))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("exito"));
        int id = db.queryForObject("SELECT id FROM productos WHERE nombre='Pizza Nueva'", Integer.class);
        mvc.perform(post("/administracion/productos/guardar").with(csrf())
            .param("id", String.valueOf(id)).param("nombre", "Pizza Editada").param("precio", "25.75").param("stock", "7").param("categoria.id", "2"))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("exito"));
        var producto = productos.buscarPorId(id).orElseThrow();
        assertEquals("Pizza Editada", producto.getNombre());
        assertEquals(0, new BigDecimal("25.75").compareTo(producto.getPrecio()));
        assertEquals(2, producto.getCategoria().getId());
        mvc.perform(post("/administracion/productos/guardar").with(csrf())
            .param("nombre", "Precio inválido").param("precio", "1.001").param("stock", "1").param("categoria.id", "1"))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("error"));
    }

}
