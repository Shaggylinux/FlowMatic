package com.back.integracion;

import com.back.BackApplication;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithTest {

    ApplicationModules modules = ApplicationModules.of(BackApplication.class);

    @Test
    void verifyArchitecture() {
        // Verifica que la arquitectura modular esté limpia y no tenga dependencias cíclicas
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        // Genera los diagramas de C4 y PlantUML en target/spring-modulith-docs
        new Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml();
    }
}
