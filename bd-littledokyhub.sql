-- USE bd_littledokyhub
CREATE DATABASE bd_littledokyhub;
USE bd_littledokyhub;

-- Roles
INSERT INTO roles (id, nombre) VALUES
(1, 'CLIENTE'),
(2, 'ADMIN');

-- Usuarios
INSERT INTO usuarios (id, activo, email, nombre, password, rol_id) VALUES
(1, 1, 'cliente.demo@littledokyhub.test', 'Cliente Demo', 'ClienteDemo2026!', 1),
(2, 1, 'admin.demo@littledokyhub.test', 'Admin Demo', 'AdminDemo2026!', 2);

-- Categorías
INSERT INTO categorias (id, codigo, nombre) VALUES
(1, 'clasicas', 'Pizzas Clásicas'),
(2, 'especiales', 'Pizzas Especiales'),
(3, 'bebidas', 'Bebidas'),
(4, 'entradas', 'Entradas y Piques');

-- Productos 
INSERT INTO productos (id, nombre, descripcion, precio, stock, imagen_url, categoria_id) VALUES
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

INSERT INTO opciones (id, producto_id, grupo, nombre, precio, activo) VALUES
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
