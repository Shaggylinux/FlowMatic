package com.back.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.back.shared.api.ConfiguracionApi;

@Service
public class ConfiguracionService implements ConfiguracionApi {

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    @Cacheable(value = "configuracion", key = "#clave", unless = "#result == null")
    public String getValor(String clave, String defaultValue) {
        return configuracionRepository.findByClave(clave)
            .map(Configuracion::getValor)
            .orElse(defaultValue);
    }

    @CacheEvict(value = {"configuracion", "configuraciones_all"}, allEntries = true)
    public void setValor(String clave, String valor) {
        Configuracion c = configuracionRepository.findByClave(clave)
            .orElse(new Configuracion());
        c.setClave(clave);
        c.setValor(valor);
        configuracionRepository.save(c);
    }

    @Cacheable(value = "configuraciones_all")
    public List<Configuracion> obtenerTodas() {
        return configuracionRepository.findAll();
    }

    @CacheEvict(value = {"configuracion", "configuraciones_all"}, allEntries = true)
    public void guardarTodas(Map<String, String> configs) {
        for (var entry : configs.entrySet()) {
            Configuracion c = configuracionRepository.findByClave(entry.getKey())
                .orElse(new Configuracion());
            c.setClave(entry.getKey());
            c.setValor(entry.getValue());
            configuracionRepository.save(c);
        }
    }
}
