package com.geosinergia.gestiontareas.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geosinergia.gestiontareas.dto.UsuarioSesionDto;
import com.geosinergia.gestiontareas.dto.ColaboradorEditForm;
import com.geosinergia.gestiontareas.entity.Rol;
import com.geosinergia.gestiontareas.entity.Usuario;
import com.geosinergia.gestiontareas.repository.RolRepository;
import com.geosinergia.gestiontareas.repository.UsuarioRepository;

import java.util.List;

@Service
public class UsuarioService {

        private final UsuarioRepository usuarioRepository;
        private final RolRepository rolRepository;
        private final PasswordEncoder passwordEncoder;

        public UsuarioService(
                        UsuarioRepository usuarioRepository,
                        RolRepository rolRepository,
                        PasswordEncoder passwordEncoder) {

                this.usuarioRepository = usuarioRepository;
                this.rolRepository = rolRepository;
                this.passwordEncoder = passwordEncoder;
        }

        public Usuario registrarUsuario(
                        String nombres,
                        String apellidos,
                        String correo,
                        String password,
                        String cargo,
                        String nombreRol) {

                if (usuarioRepository.existsByCorreo(correo)) {
                        throw new IllegalArgumentException(
                                        "El correo ya se encuentra registrado.");
                }

                Rol rol = rolRepository.findByNombre(nombreRol)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "El rol indicado no existe."));

                Usuario usuario = new Usuario();

                usuario.setNombres(nombres);
                usuario.setApellidos(apellidos);
                usuario.setCorreo(correo);
                usuario.setPasswordHash(passwordEncoder.encode(password));
                usuario.setCargo(cargo);
                usuario.setActivo(true);
                usuario.setRol(rol);

                return usuarioRepository.save(usuario);
        }

        public Usuario registrarColaborador(
                        String nombres,
                        String apellidos,
                        String correo,
                        String password,
                        String cargo) {

                return registrarUsuario(
                                nombres,
                                apellidos,
                                correo,
                                password,
                                cargo,
                                "COLABORADOR");
        }

        public List<Usuario> listarColaboradores() {

                return usuarioRepository
                                .findByRol_NombreOrderByApellidosAsc(
                                                "COLABORADOR");
        }

        public Usuario buscarColaborador(Long idUsuario) {

                Usuario usuario = usuarioRepository
                                .findById(idUsuario)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "El colaborador no existe."));

                if (!"COLABORADOR".equals(
                                usuario.getRol().getNombre())) {

                        throw new IllegalArgumentException(
                                        "El usuario indicado no es colaborador.");
                }

                return usuario;
        }

        public Usuario actualizarColaborador(
                        Long idUsuario,
                        ColaboradorEditForm formulario) {

                Usuario usuario = buscarColaborador(idUsuario);

                if (usuarioRepository
                                .existsByCorreoAndIdUsuarioNot(
                                                formulario.getCorreo(),
                                                idUsuario)) {

                        throw new IllegalArgumentException(
                                        "El correo ya se encuentra registrado.");
                }

                usuario.setNombres(formulario.getNombres());
                usuario.setApellidos(formulario.getApellidos());
                usuario.setCorreo(formulario.getCorreo());
                usuario.setCargo(formulario.getCargo());
                usuario.setActivo(formulario.getActivo());

                return usuarioRepository.save(usuario);
        }

        @Transactional(readOnly = true)
        public UsuarioSesionDto obtenerUsuarioSesion(String correo) {

                Usuario usuario = usuarioRepository.findByCorreo(correo)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "El usuario autenticado no existe."));

                String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();

                String iniciales = obtenerIniciales(
                                usuario.getNombres(),
                                usuario.getApellidos());

                return new UsuarioSesionDto(
                                nombreCompleto,
                                usuario.getCorreo(),
                                usuario.getRol().getNombre(),
                                iniciales);
        }

        private String obtenerIniciales(
                        String nombres,
                        String apellidos) {

                String inicialNombre = "";
                String inicialApellido = "";

                if (nombres != null && !nombres.isBlank()) {
                        inicialNombre = nombres.trim()
                                        .substring(0, 1);
                }

                if (apellidos != null && !apellidos.isBlank()) {
                        inicialApellido = apellidos.trim()
                                        .substring(0, 1);
                }

                return (inicialNombre + inicialApellido)
                                .toUpperCase();
        }
}