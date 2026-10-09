package com.avalache_api.demo.infrastructure;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;

import com.avalache_api.demo.application.ReporteFinancieroService;
import com.avalache_api.demo.application.AvalanchaService;
import com.avalache_api.demo.domain.Usuario;

@RestController
@RequestMapping("/api/v1/avalancha")
public class AvalanchaController {
    private final AvalanchaService service; 
    private final UsuarioMapper usuarioMapper;
    private final ReporteFinancieroService reporteFinancieroService;

    public AvalanchaController(
        AvalanchaService service,
        UsuarioMapper usuarioMapper,
        ReporteFinancieroService reporteFinancieroService
    ){
        this.service = service;
        this.usuarioMapper = usuarioMapper;
        this.reporteFinancieroService = reporteFinancieroService;
    }
    
    @PostMapping("simular")
    public Integer simular(@Valid @RequestBody UsuarioRequestDTO usuario){
        Usuario usuarioDominio = usuarioMapper.toUsuario(usuario);
        return service.ejecutarAvalancha(usuarioDominio);
    }

    @PostMapping(value = "reporte", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generarReporte(@Valid @RequestBody UsuarioRequestDTO usuario){
        Usuario usuarioDominio = usuarioMapper.toUsuario(usuario);
        byte[] pdf = reporteFinancieroService.generar(usuarioDominio);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename("reporte-financiero.pdf")
            .build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
