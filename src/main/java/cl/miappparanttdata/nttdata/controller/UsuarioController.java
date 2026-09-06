package cl.miappparanttdata.nttdata.controller;

import cl.miappparanttdata.nttdata.entity.Usuario;
import cl.miappparanttdata.nttdata.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * GET http://localhost:8080/api/usuarios
     * Retorna todos los usuarios (datos de personas) desde la tabla `usuario` de nttdata.
     * Ideal para probar en Postman.
     */
    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * GET http://localhost:8080/api/usuarios/{id}
     * Retorna una persona por su user_id.
     * Ejemplo: http://localhost:8080/api/usuarios/1  -> Ana Torres
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Integer id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET http://localhost:8080/api/usuarios/health
     * Endpoint simple para verificar que la API está viva sin tocar la BD.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("API NTTDATA OK - conexion a nttdata.usuario activa");
    }
}
