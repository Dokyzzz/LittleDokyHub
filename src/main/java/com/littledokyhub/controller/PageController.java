package com.littledokyhub.controller;

import com.littledokyhub.form.RegistroForm;
import com.littledokyhub.model.Usuario;
import com.littledokyhub.service.ProductoService;
import com.littledokyhub.service.RolService;
import com.littledokyhub.service.CategoriaService;
import com.littledokyhub.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PageController {

    @Autowired
    private com.littledokyhub.service.CompraService compraService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private com.littledokyhub.service.PersonalizacionService personalizacion;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("favoritas", productoService.obtenerFavoritas());
        return "index";
    }

    @GetMapping("/categorias")
    public String categorias(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "") String buscar, Model model) {
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("productos", productoService.buscar(buscar));
        model.addAttribute("buscar", buscar);
        model.addAttribute("clasicas", productoService.obtenerClasicas());
        model.addAttribute("especiales", productoService.obtenerEspeciales());
        model.addAttribute("bebidas", productoService.obtenerBebidas());
        return "shop/categorias";
    }

    @GetMapping("/detalles")
    public String detalles() {
        return "shop/detalles";
    }

    @GetMapping({"/personalizar/{id}", "/personalizar-bebida/{id}"})
    public String personalizar(@org.springframework.web.bind.annotation.PathVariable Integer id, Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes flash) {
        var producto = productoService.buscarPorId(id);
        if (producto.isEmpty()) {
            flash.addFlashAttribute("error", "No encontramos ese producto.");
            return "redirect:/categorias";
        }
        model.addAttribute("producto", producto.get());
        var opciones = compraService.opciones(id);
        model.addAttribute("secciones", personalizacion.secciones(producto.get(), opciones));
        model.addAttribute("tipoProducto", personalizacion.tipo(producto.get()));
        return personalizacion.tipo(producto.get()).equals("bebida") ? "shop/personalizar-bebida" : "shop/personalizar";
    }

    @GetMapping("/contacto")
    public String contacto() {
        return "contact/contacto";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "account/login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("registro", new RegistroForm());
        return "account/create";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("registro") RegistroForm registro,
            BindingResult errors, RedirectAttributes flash) {
        if (errors.hasErrors())
            return "account/create";

        Usuario nuevo = new Usuario();
        nuevo.setNombre(registro.getNombre().trim());
        nuevo.setEmail(registro.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
        nuevo.setPassword(registro.getPassword());
        nuevo.setTelefono(registro.getTelefono() == null ? "" : registro.getTelefono().trim());
        nuevo.setActivo(true);

        var rolCliente = rolService.listar().stream()
            .filter(r -> "CLIENTE".equalsIgnoreCase(r.getNombre()))
            .findFirst()
            .orElse(null);

        if (rolCliente == null) {
            errors.reject("registro.rol", "No se pudo asignar el rol de cliente. Inténtalo más tarde.");
            return "account/create";
        }
        nuevo.setRol(rolCliente);

        boolean guardado;
        try {
            guardado = usuarioService.guardar(nuevo);
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            errors.rejectValue("email", "duplicate", "El correo electrónico ya se encuentra registrado.");
            return "account/create";
        }

        if (!guardado) {
            errors.rejectValue("email", "duplicate", "El correo electrónico ya se encuentra registrado.");
            return "account/create";
        }

        flash.addFlashAttribute("exito", "Cuenta creada exitosamente. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }

    @GetMapping("/forgotpassword")
    public String forgotPassword() {
        return "account/recovery-unavailable";
    }

    @GetMapping("/verifycode")
    public String verifyCode() {
        return "account/recovery-unavailable";
    }

    @GetMapping("/resetpassword")
    public String resetPassword() {
        return "account/recovery-unavailable";
    }

    @GetMapping("/administracion")
    public String administracion(Model model) {
        model.addAttribute("productos", productoService.listar());
        int nClasicas = productoService.obtenerClasicas().size();
        int nEspeciales = productoService.obtenerEspeciales().size();
        int nBebidas = productoService.obtenerBebidas().size();

        model.addAttribute("nClasicas", nClasicas);
        model.addAttribute("nEspeciales", nEspeciales);
        model.addAttribute("nBebidas", nBebidas);
        model.addAttribute("maxCategoria", Math.max(1, Math.max(nClasicas, Math.max(nEspeciales, nBebidas))));

        model.addAttribute("totalProductos", productoService.listar().size());
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("totalClientes", usuarioService.contarClientes());

        return "admin/admin";
    }
}
