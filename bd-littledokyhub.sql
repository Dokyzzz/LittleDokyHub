-- MySQL 8+. Ejecutar antes de iniciar Spring Boot en una base vacía.
-- No elimina ni sustituye datos existentes. Las cuentas demo son solo para desarrollo.
CREATE DATABASE IF NOT EXISTS bd_littledokyhub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bd_littledokyhub;
CREATE TABLE IF NOT EXISTS roles (
 id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(30) NOT NULL UNIQUE
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS usuarios (
 id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL UNIQUE,
 password VARCHAR(255) NOT NULL, telefono VARCHAR(15), activo BOOLEAN NOT NULL DEFAULT TRUE,
 rol_id INT NOT NULL, FOREIGN KEY (rol_id) REFERENCES roles(id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS categorias (
 id INT AUTO_INCREMENT PRIMARY KEY, codigo VARCHAR(20) NOT NULL UNIQUE, nombre VARCHAR(50) NOT NULL,
 descripcion TEXT, icono VARCHAR(50)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS productos (
 id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) NOT NULL UNIQUE, descripcion TEXT,
 precio DECIMAL(10,2) NOT NULL, stock INT NOT NULL DEFAULT 0, imagen_url VARCHAR(255), categoria_id INT NOT NULL,
 FOREIGN KEY (categoria_id) REFERENCES categorias(id), CHECK (precio > 0), CHECK (stock >= 0)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS opciones (
 id INT AUTO_INCREMENT PRIMARY KEY, producto_id INT NOT NULL, grupo VARCHAR(20) NOT NULL,
 nombre VARCHAR(100) NOT NULL, precio DECIMAL(10,2) NOT NULL DEFAULT 0, activo BOOLEAN NOT NULL DEFAULT TRUE,
 FOREIGN KEY (producto_id) REFERENCES productos(id), CHECK (precio >= 0), CHECK (grupo IN ('TAMANO','MASA','EXTRA'))
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS pedidos (
 id INT AUTO_INCREMENT PRIMARY KEY, fecha DATETIME(6) NOT NULL, total DECIMAL(10,2) NOT NULL,
 estado VARCHAR(30) NOT NULL, usuario_id INT NOT NULL,
 FOREIGN KEY (usuario_id) REFERENCES usuarios(id), CHECK (total >= 0)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS detalles_pedido (
 id INT AUTO_INCREMENT PRIMARY KEY, cantidad INT NOT NULL, precio_unitario DECIMAL(10,2) NOT NULL,
 subtotal DECIMAL(10,2) NOT NULL, pedido_id INT NOT NULL, producto_id INT NOT NULL,
 FOREIGN KEY (pedido_id) REFERENCES pedidos(id), FOREIGN KEY (producto_id) REFERENCES productos(id),
 CHECK (cantidad > 0), CHECK (precio_unitario > 0), CHECK (subtotal > 0)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS carritos (
 id VARCHAR(36) PRIMARY KEY, usuario_id INT UNIQUE, FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS carrito_items (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, carrito_id VARCHAR(36) NOT NULL, producto_id INT NOT NULL,
 configuracion VARCHAR(500) NOT NULL, cantidad INT NOT NULL,
 FOREIGN KEY (carrito_id) REFERENCES carritos(id), FOREIGN KEY (producto_id) REFERENCES productos(id),
 UNIQUE KEY uq_carrito_config (carrito_id,producto_id,configuracion), CHECK (cantidad BETWEEN 1 AND 99)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS compra_snapshot (
 pedido_id INT PRIMARY KEY, entrega VARCHAR(30) NOT NULL, direccion VARCHAR(255) NOT NULL, pago VARCHAR(30) NOT NULL,
 FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS detalle_snapshot (
 detalle_id INT PRIMARY KEY, nombre VARCHAR(100) NOT NULL, personalizacion TEXT NOT NULL,
 FOREIGN KEY (detalle_id) REFERENCES detalles_pedido(id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS mensajes (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL, telefono VARCHAR(15),
 asunto VARCHAR(100) NOT NULL, mensaje TEXT NOT NULL, fecha DATETIME(6) NOT NULL
) ENGINE=InnoDB;

-- Roles
INSERT IGNORE INTO roles (id, nombre) VALUES
(1, 'CLIENTE'),
(2, 'ADMIN');

-- Usuarios
INSERT IGNORE INTO usuarios (id, activo, email, nombre, password, rol_id) VALUES
(1, 1, 'cliente.demo@littledokyhub.test', 'Cliente Demo', '$2b$12$oF9vHxPL3vRMI6X9YPmgqutsdket93Vjam3JeNKlbyMkeWhQy4CAS', 1),
(2, 1, 'admin.demo@littledokyhub.test', 'Admin Demo', '$2b$12$zVXT39WHoozenzs4P.JR9uaRIiAYFAA.z5jmESewvjAP0SaX2xHF6', 2);

-- Categorías
INSERT IGNORE INTO categorias (id, codigo, nombre) VALUES
(1, 'clasicas', 'Pizzas Clásicas'),
(2, 'especiales', 'Pizzas Especiales'),
(3, 'bebidas', 'Bebidas'),
(4, 'entradas', 'Entradas y Piques');

-- Productos 
INSERT IGNORE INTO productos (id, nombre, descripcion, precio, stock, imagen_url, categoria_id) VALUES
-- Clásicas
(1, 'Pizza Americana', 'Tradicional pizza con salsa de tomate casera, queso mozzarella fundido y jamón seleccionado.', 32.00, 50, '/images/americana.jpg', 1),
(2, 'Pizza Pepperoni', 'La clásica favorita con abundante queso mozzarella y rodajas de pepperoni crocante.', 36.00, 45, '/images/pepperoni.jpg', 1),
(3, 'Pizza Hawaiana', 'Combinación equilibrada de jamón selecto, mozzarella y trozos de piña glaseada.', 35.00, 40, '/images/hawaiana.jpg', 1),

-- Especiales
(4, 'Pizza Little Doky', 'La estrella de la casa con carnes, champiñones, pimientos, salsa especial y toque artesanal.', 42.00, 30, '/images/doky_house.jpg', 2),
(5, 'Pizza Cuatro Quesos', 'Mezcla cremosa de mozzarella, parmesano, queso azul y provolone.', 40.00, 25, '/images/cuatro_quesos.jpg', 2),
(6, 'Pizza Full Meat', 'Para amantes de la carne: jamón, pepperoni, salchicha italiana y carne sazonada.', 44.00, 35, '/images/full_meat.jpg', 2),
(7, 'Pizza Huachana', 'Exclusiva receta peruana con la inconfundible salchicha huachana y suave queso derretido.', 39.00, 20, '/images/huachana.jpg', 2),
(8, 'Pizza Lomo Saltado', 'Fusión criolla con trozos de lomo saltado al wok, cebolla, tomate y base de queso.', 45.00, 20, '/images/lomo_saltado.jpg', 2),
(9, 'Pizza Ají de Gallina', 'Tradición peruana llevada a la masa con crema de ají de gallina y queso gratinado.', 43.00, 20, '/images/aji_gallina.jpg', 2),

-- Bebidas
(10, 'Gaseosa Personal', 'Botella individual de 500ml refrescante (Inca Kola o Coca-Cola).', 5.00, 100, '/images/gaseosas.jpg', 3),
(11, 'Chicha Morada Casera', 'Bebida natural de maíz morado, piña, manzana, canela y gotas de limón (1 Litro).', 8.00, 50, '/images/chicha.jpg', 3),
(12, 'Cerveza Personal', 'Cerveza artesanal/industrial de 330ml para acompañar tus pizzas.', 9.00, 60, '/images/cerveza.jpg', 3),

-- Entradas
(13, 'Pan al Ajo Clásico', 'Porción de 6 panes artesanales horneados con mantequilla aromatizada al ajo y hierbas.', 10.00, 40, '/images/pan_ajo.jpg', 4),
(14, 'Pan al Ajo Especial', 'Nuestros panes crujientes al ajo cubiertos con una capa de queso mozzarella gratinado.', 14.00, 40, '/images/pan_ajo_especial.jpg', 4);

INSERT IGNORE INTO opciones (id, producto_id, grupo, nombre, precio, activo) VALUES
(1, 1, 'TAMANO', 'Mediana', 0.00, 1),
(2, 1, 'TAMANO', 'Familiar', 12.00, 1),
(3, 1, 'MASA', 'Clásica', 0.00, 1),
(4, 1, 'MASA', 'Delgada', 0.00, 1),
(5, 1, 'EXTRA', 'Queso adicional', 4.00, 1),

(6, 2, 'TAMANO', 'Mediana', 0.00, 1),
(7, 2, 'TAMANO', 'Familiar', 12.00, 1),
(8, 2, 'MASA', 'Clásica', 0.00, 1),
(9, 2, 'MASA', 'Delgada', 0.00, 1),
(10, 2, 'EXTRA', 'Queso adicional', 4.00, 1),

(11, 3, 'TAMANO', 'Mediana', 0.00, 1),
(12, 3, 'TAMANO', 'Familiar', 12.00, 1),
(13, 3, 'MASA', 'Clásica', 0.00, 1),
(14, 3, 'MASA', 'Delgada', 0.00, 1),
(15, 3, 'EXTRA', 'Queso adicional', 4.00, 1),

(16, 4, 'TAMANO', 'Mediana', 0.00, 1),
(17, 4, 'TAMANO', 'Familiar', 12.00, 1),
(18, 4, 'MASA', 'Artesanal Gruesa', 0.00, 1),
(19, 4, 'MASA', 'Delgada', 0.00, 1),
(20, 4, 'EXTRA', 'Extra tocino', 5.00, 1),

(21, 5, 'TAMANO', 'Mediana', 0.00, 1),
(22, 5, 'TAMANO', 'Familiar', 12.00, 1),
(23, 5, 'MASA', 'Clásica', 0.00, 1),
(24, 5, 'EXTRA', 'Doble queso azul', 5.00, 1),

(25, 6, 'TAMANO', 'Mediana', 0.00, 1),
(26, 6, 'TAMANO', 'Familiar', 14.00, 1),
(27, 6, 'MASA', 'Clásica', 0.00, 1),
(28, 6, 'EXTRA', 'Queso adicional', 4.00, 1);
