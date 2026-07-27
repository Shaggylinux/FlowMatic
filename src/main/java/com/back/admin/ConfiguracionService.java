package com.back.admin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracionService {

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    public String getValor(String clave, String defaultValue) {
        return configuracionRepository.findByClave(clave)
            .map(Configuracion::getValor)
            .orElse(defaultValue);
    }

    public void setValor(String clave, String valor) {
        Configuracion c = configuracionRepository.findByClave(clave)
            .orElse(new Configuracion());
        c.setClave(clave);
        c.setValor(valor);
        configuracionRepository.save(c);
    }

    public List<Configuracion> obtenerTodas() {
        return configuracionRepository.findAll();
    }

    public void guardarTodas(Map<String, String> configs) {
        for (var entry : configs.entrySet()) {
            setValor(entry.getKey(), entry.getValue());
        }
    }
}
