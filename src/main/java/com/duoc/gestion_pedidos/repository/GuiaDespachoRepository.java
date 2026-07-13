package com.duoc.gestion_pedidos.repository;

import com.duoc.gestion_pedidos.model.GuiaDespachoOracle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para operaciones sobre la tabla GUIAS_PROCESADAS en Oracle Cloud.
 *
 * @author Rafael Navarrete
 */

@Repository
public interface GuiaDespachoRepository extends JpaRepository<GuiaDespachoOracle, Long> {   
}