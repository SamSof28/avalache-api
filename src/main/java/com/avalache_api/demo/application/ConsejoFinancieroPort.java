package com.avalache_api.demo.application;

import com.avalache_api.demo.application.dto.ResultadoAvalancha;
import com.avalache_api.demo.domain.Usuario;

public interface ConsejoFinancieroPort {
    String generarConsejo(Usuario usuario, ResultadoAvalancha resultado);
}
