package com.littledokyhub.service;

import com.littledokyhub.model.*;
import com.littledokyhub.repository.*;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonalizacionService {
    private final ProductoRepository productos;
    private final OpcionRepository opciones;
    public PersonalizacionService(ProductoRepository productos, OpcionRepository opciones) {
        this.productos = productos; this.opciones = opciones;
    }
    private String normal(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
    public String tipo(Producto p) {
        String codigo = normal(p.getCategoria().getCodigo());
        if (codigo.equals("bebidas")) return "bebida";
        if (List.of("clasicas", "especiales").contains(codigo)) return "pizza";
        if (codigo.equals("entradas")) return "entrada";
        return "producto";
    }
    // Secciones de presentación sobre los grupos de BD existentes: no cambia el esquema.
    public String seccion(Opcion o) {
        String nombre = normal(o.getNombre());
        if (o.getGrupo().equals("EXTRA") && nombre.startsWith("salsa ")) return "SALSA";
        if (o.getGrupo().equals("EXTRA") && List.of("bien fria", "al tiempo", "con hielo").contains(nombre)) return "TEMPERATURA";
        return o.getGrupo();
    }
    public boolean coherente(Opcion o) {
        String tipo = tipo(o.getProducto()), nombre = normal(o.getNombre());
        if (tipo.equals("bebida")) return !o.getGrupo().equals("MASA") && !seccion(o).equals("SALSA")
                && !nombre.matches(".*(bbq|barbacoa|tomate|queso|pepperoni|jamon|tocino|masa|champinon|aceituna|pimiento).*");
        if (tipo.equals("entrada") && o.getGrupo().equals("MASA")) return false;
        return !seccion(o).equals("TEMPERATURA");
    }
    public record Seccion(String codigo, String titulo, boolean multiple, List<Opcion> opciones) {}
    public List<Seccion> secciones(Producto p, List<Opcion> disponibles) {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("TAMANO", tipo(p).equals("bebida") ? "Presentación" : "Elige el tamaño");
        labels.put("MASA", "Tipo de masa"); labels.put("SALSA", "Salsa");
        labels.put("TEMPERATURA", "¿Cómo la prefieres?"); labels.put("EXTRA", "Agrega extras");
        List<Seccion> result = new ArrayList<>();
        labels.forEach((codigo, label) -> {
            var rows = disponibles.stream().filter(this::coherente).filter(o -> seccion(o).equals(codigo)).toList();
            if (!rows.isEmpty()) result.add(new Seccion(codigo, label, codigo.equals("EXTRA"), rows));
        });
        return result;
    }
    private Opcion opcion(Producto p, String grupo, String nombre, String precio) {
        Opcion o = new Opcion(); o.setProducto(p); o.setGrupo(grupo); o.setNombre(nombre);
        o.setPrecio(new BigDecimal(precio)); o.setActivo(true); return o;
    }
    @Transactional
    public void completarFaltantes() {
        completar(productos.findAll());
    }
    @Transactional
    public void completarProducto(Integer id) {
        completar(productos.findById(id).stream().toList());
    }
    private void completar(List<Producto> lista) {
        for (Producto p : lista) {
            var existentes = opciones.findByProductoIdOrderById(p.getId());
            List<Opcion> defaults = new ArrayList<>();
            switch (tipo(p)) {
                case "pizza" -> {
                    defaults.add(opcion(p,"TAMANO","Personal","0.00"));
                    defaults.add(opcion(p,"TAMANO","Mediana","10.00"));
                    defaults.add(opcion(p,"TAMANO","Familiar","20.00"));
                    defaults.add(opcion(p,"MASA","Tradicional","0.00"));
                    defaults.add(opcion(p,"MASA","Borde relleno de queso","7.00"));
                    defaults.add(opcion(p,"EXTRA","Salsa de tomate","0.00"));
                    defaults.add(opcion(p,"EXTRA","Salsa BBQ","0.00"));
                    for (String extra : List.of("Pepperoni","Champiñones","Aceitunas","Jamón","Tocino","Pimiento","Queso extra"))
                        defaults.add(opcion(p,"EXTRA",extra,"3.50"));
                }
                case "bebida" -> {
                    defaults.add(opcion(p,"TAMANO","Presentación del producto","0.00"));
                    defaults.add(opcion(p,"EXTRA","Bien fría","0.00"));
                    defaults.add(opcion(p,"EXTRA","Al tiempo","0.00"));
                    defaults.add(opcion(p,"EXTRA","Vaso adicional","1.00"));
                }
                case "entrada" -> defaults.add(opcion(p,"EXTRA","Queso adicional","4.00"));
                default -> { }
            }
            // Completa secciones enteras ausentes; respeta precios y opciones desactivadas.
            for (Opcion o : defaults) {
                if (existentes.stream().noneMatch(e -> seccion(e).equals(seccion(o)))) opciones.save(o);
            }
        }
    }
}
