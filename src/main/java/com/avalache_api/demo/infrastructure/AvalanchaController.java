package com.avalache_api.demo.infrastructure;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avalache_api.demo.application.AvalanchaService;
import com.avalache_api.demo.domain.Usuario;

@RestController
@RequestMapping("/api/v1/avalancha")
public class AvalanchaController {
    private final AvalanchaService service; 
    private final UsuarioMapper usuarioMapper;

    public AvalanchaController(AvalanchaService service, UsuarioMapper usuarioMapper){
        this.service = service;
        this.usuarioMapper = usuarioMapper;
    }
    
    @PostMapping("simular")
    public Integer simular(@RequestBody UsuarioRequestDTO usuario){
        Usuario usuarioDominio = usuarioMapper.toUsuario(usuario);
        return service.ejecutarAvalancha(usuarioDominio);
    }
}
