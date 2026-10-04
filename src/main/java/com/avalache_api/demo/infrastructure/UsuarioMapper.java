package com.avalache_api.demo.infrastructure;

import com.avalache_api.demo.domain.Usuario;
import com.avalache_api.demo.domain.DeudaUsuario;
import java.util.ArrayList;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {
    
    public Usuario toUsuario(UsuarioRequestDTO usuario){
        ArrayList<DeudaUsuario> deudasDominio = new ArrayList<>();
        
        for (DeudaRequestDTO deudas : usuario.getDeudas()){
            DeudaUsuario deudaActual = new DeudaUsuario(deudas.getNombreDeuda(), deudas.getSaldo(), deudas.getPagoMinimo(), deudas.getTasaInteres());
            deudasDominio.add(deudaActual);
        }

        Usuario usuario1 = new Usuario(usuario.getNombreUsuario(), deudasDominio, usuario.getMontoExtra());

        return usuario1;
    }
}
