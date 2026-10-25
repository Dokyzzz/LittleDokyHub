package com.littledokyhub.service;

import com.littledokyhub.model.Categoria;
import com.littledokyhub.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorCodigo(String codigo) {
        return categoriaRepository.findByCodigoIgnoreCase(codigo);
    }

    public Categoria guardar(Categoria categoria) {
        if (categoria.getId() != null) {
            Categoria existente = categoriaRepository.findById(categoria.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría inexistente"));
            existente.setCodigo(categoria.getCodigo());
            existente.setNombre(categoria.getNombre());
            return categoriaRepository.save(existente);
        }
        return categoriaRepository.save(categoria);
    }

    public void eliminar(Integer id) {
        categoriaRepository.deleteById(id);
    }
}
