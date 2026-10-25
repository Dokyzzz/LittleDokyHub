package com.littledokyhub.controller;

import com.littledokyhub.model.Producto;
import com.littledokyhub.service.CategoriaService;
import com.littledokyhub.service.ProductImageStorage;
import com.littledokyhub.service.ProductoService;
import java.io.IOException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/administracion/productos")
public class AdminProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private com.littledokyhub.service.PersonalizacionService personalizacion;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ProductImageStorage productImageStorage;

    @PostMapping("/guardar")
    public String guardar(
            @Valid @ModelAttribute("producto") Producto producto,
            BindingResult bindingResult,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            RedirectAttributes flash) {

        // Si Spring Validator encuentra errores de anotaciones (@NotBlank, @DecimalMin, etc.)
        if (bindingResult.hasErrors()) {
            // Capturamos el primer error para mostrarlo en la vista administrativa
            String mensajeError = bindingResult.getAllErrors().get(0).getDefaultMessage();
            flash.addFlashAttribute("error", "Error de validación: " + mensajeError);
            return "redirect:/administracion#productos";
        }

        if (producto.getCategoria() == null || producto.getCategoria().getId() == null
                || categoriaService.listar().stream().noneMatch(c -> c.getId().equals(producto.getCategoria().getId()))) {
            flash.addFlashAttribute("error", "Categoría inválida");
            return "redirect:/administracion";
        }

        try {
            if (imagen != null && !imagen.isEmpty())
                producto.setImagenUrl(productImageStorage.store(imagen));
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/administracion#productos";
        } catch (IOException e) {
            flash.addFlashAttribute("error", "No se pudo guardar la imagen. Inténtalo nuevamente.");
            return "redirect:/administracion#productos";
        }

        if (producto.getImagenUrl() == null || producto.getImagenUrl().isBlank())
            producto.setImagenUrl("/images/logo.png");
        if (!isAllowedImagePath(producto.getImagenUrl())) {
            flash.addFlashAttribute("error", "Selecciona una imagen de /images/ o sube un archivo válido.");
            return "redirect:/administracion#productos";
        }

        boolean esNuevo = (producto.getId() == null);
        
        // El servicio procesa el guardado mediante JPA
        if (productoService.guardar(producto)) {
            personalizacion.completarProducto(producto.getId());
            flash.addFlashAttribute("exito",
                    esNuevo ? "Producto creado correctamente." : "Producto actualizado correctamente.");
        } else {
            flash.addFlashAttribute("error", "El producto que intentas editar no existe.");
        }
        
        return "redirect:/administracion#productos";
    }

    private boolean isAllowedImagePath(String imagePath) {
        return (imagePath.startsWith("/images/") || imagePath.startsWith("/uploads/products/"))
                && !imagePath.contains("..")
                && !imagePath.contains("\\");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public String cargaDemasiadoGrande(RedirectAttributes flash) {
        flash.addFlashAttribute("error", "La imagen no puede superar los 5 MB.");
        return "redirect:/administracion#productos";
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class) public String conflicto(RedirectAttributes flash) { flash.addFlashAttribute("error", "Nombre duplicado o categoría inválida."); return "redirect:/administracion"; }
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes flash) {
        if (productoService.eliminar(id)) {
            flash.addFlashAttribute("exito", "Producto desactivado correctamente.");
        } else {
            flash.addFlashAttribute("error", "No se encontró el producto a eliminar.");
        }
        return "redirect:/administracion#productos";
    }
}