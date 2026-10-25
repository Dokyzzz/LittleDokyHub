package com.littledokyhub.service;

import com.littledokyhub.model.DetallePedido;
import com.littledokyhub.repository.DetallePedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;

    public DetallePedidoService(DetallePedidoRepository detallePedidoRepository) {
        this.detallePedidoRepository = detallePedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<DetallePedido> listarPorPedido(Integer pedidoId) {
        return detallePedidoRepository.findByPedidoId(pedidoId);
    }

    public DetallePedido guardar(DetallePedido detalle) {
        return detallePedidoRepository.save(detalle);
    }

    public void eliminar(Integer id) {
        detallePedidoRepository.deleteById(id);
    }
}