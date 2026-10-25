package com.littledokyhub.integration;

import com.littledokyhub.LegacyAwarePasswordEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.*;

class SqlBootstrapTests {
    @Test
    void scriptCreaBaseVaciaYSePuedeRepetirSinBorrarDatos() throws Exception {
        // H2 no tiene CREATE DATABASE / USE de MySQL; se selecciona la base en la URL.
        String sql = Files.readString(Path.of("bd-littledokyhub.sql"))
            .replaceAll("(?m)^CREATE DATABASE[^;]+;", "")
            .replaceAll("(?m)^USE bd_littledokyhub;", "");
        var resource = new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8));
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:bootstrap;MODE=MySQL", "sa", "")) {
            ScriptUtils.executeSqlScript(connection, resource);
            ScriptUtils.executeSqlScript(connection, resource);
            try (var stmt = connection.createStatement()) {
                var rs = stmt.executeQuery("SELECT COUNT(*) FROM productos");
                rs.next();
                assertEquals(14, rs.getInt(1));
                rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='PUBLIC'");
                rs.next();
                assertEquals(12, rs.getInt(1));
                rs = stmt.executeQuery("SELECT password FROM usuarios WHERE email='admin.demo@littledokyhub.test'");
                assertTrue(rs.next());
                assertTrue(new LegacyAwarePasswordEncoder().matches("AdminDemo2026!", rs.getString(1)));
                assertThrows(java.sql.SQLException.class, () -> stmt.executeUpdate("INSERT INTO productos(nombre,precio,stock,categoria_id) VALUES('Referencia inválida',10,1,999)"));
                stmt.executeUpdate("INSERT INTO carritos(id) VALUES('test-cart')");
                stmt.executeUpdate("INSERT INTO carrito_items(carrito_id,producto_id,configuracion,cantidad) VALUES('test-cart',1,'1,3',1)");
                assertThrows(java.sql.SQLException.class, () -> stmt.executeUpdate("INSERT INTO carrito_items(carrito_id,producto_id,configuracion,cantidad) VALUES('test-cart',1,'1,3',1)"));
            }
        }
    }
}
