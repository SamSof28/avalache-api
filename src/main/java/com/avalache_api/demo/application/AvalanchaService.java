package com.avalache_api.demo.application;

import java.math.BigDecimal;
import java.util.PriorityQueue;

import org.springframework.stereotype.Service;

import com.avalache_api.demo.domain.Usuario;
import com.avalache_api.demo.domain.DeudaUsuario;

@Service
public class AvalanchaService {
    public Integer ejecutarAvalancha(Usuario usuario){
        PriorityQueue<DeudaUsuario> filtrador = new PriorityQueue<>(usuario.getDeudas());
        Integer mesesTranscurridos = 0;
        
        while (filtrador.size() != 0){
            mesesTranscurridos += 1;
            BigDecimal dineroDisponibleEsteMes = usuario.getMonto_extra();

            while (dineroDisponibleEsteMes.compareTo(BigDecimal.ZERO) > 0 && filtrador.size() != 0){
                DeudaUsuario current = filtrador.poll();

                if (current.getSaldo().compareTo(dineroDisponibleEsteMes) < 1){
                    dineroDisponibleEsteMes =  dineroDisponibleEsteMes.subtract(current.getSaldo());
                    continue;
                }

                current.setSaldo(current.getSaldo().subtract(dineroDisponibleEsteMes));
                dineroDisponibleEsteMes = BigDecimal.ZERO;
                filtrador.add(current);
            }

            for (DeudaUsuario deudas : filtrador){
                deudas.setSaldo(deudas.getSaldo().add((deudas.getSaldo().multiply(deudas.getTasaInteres()))));
            }
            
        }

        return mesesTranscurridos;
    }

}

