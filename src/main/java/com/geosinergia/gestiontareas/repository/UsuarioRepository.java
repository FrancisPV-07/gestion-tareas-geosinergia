package com.geosinergia.gestiontareas.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.geosinergia.gestiontareas.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<Usuario> findByRol_NombreOrderByApellidosAsc(String nombreRol);

    boolean existsByCorreoAndIdUsuarioNot(
            String correo,
            Long idUsuario);
}