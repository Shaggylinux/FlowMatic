package com.back.integracion;

import com.back.BackApplication;

import org.springframework.stereotype.Service;
import org.springframework.stereotype.Controller;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.*;

@AnalyzeClasses(packages = "com.back", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    // ── Module isolation: auth (Mejora 2) ────────────────────────
    // auth es modulo base, no debe depender de modulos de negocio

    @ArchTest
    static final ArchRule auth_should_not_depend_on_candidatos =
            noClasses()
                    .that().resideInAnyPackage("com.back.auth..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("com.back.candidatos..");

    @ArchTest
    static final ArchRule auth_should_not_depend_on_drive =
            noClasses()
                    .that().resideInAnyPackage("com.back.auth..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("com.back.drive..");

    @ArchTest
    static final ArchRule auth_should_not_depend_on_calendario =
            noClasses()
                    .that().resideInAnyPackage("com.back.auth..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("com.back.calendario..");

    @ArchTest
    static final ArchRule auth_should_not_depend_on_admin_or_notificaciones =
            noClasses()
                    .that().resideInAnyPackage("com.back.auth..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("com.back.admin..", "com.back.notificaciones..");

    // ── Module isolation: services/repositories in calendario ────
    // EventoService no debe depender del modelo candidatos (Mejora 3)
    // Controllers pueden usar servicios de otros modulos

    @ArchTest
    static final ArchRule calendario_services_should_not_depend_on_candidatos =
            noClasses()
                    .that().resideInAnyPackage("com.back.calendario..")
                    .and().areAnnotatedWith(Service.class)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("com.back.candidatos..");

    // ── Module isolation: shared ─────────────────────────────────

    @ArchTest
    static final ArchRule shared_should_have_minimal_dependencies =
            classes()
                    .that().resideInAnyPackage("com.back.shared..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "com.back.shared..",
                            "java..",
                            "jakarta..",
                            "lombok..",
                            "org.slf4j..",
                            "org.springframework..",
                            "com.fasterxml.jackson..",
                            "org.slf4j.."
                    );

    // ── Module isolation: seguridad ──────────────────────────────
    // seguridad solo debe depender de auth y admin (auditoria, config)

    @ArchTest
    static final ArchRule seguridad_should_only_depend_on_allowed =
            classes()
                    .that().resideInAnyPackage("com.back.seguridad..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "com.back.seguridad..",
                            "com.back.auth..",
                            "com.back.admin..",
                            "java..",
                            "jakarta..",
                            "org.springframework..",
                            "lombok..",
                            "org.slf4j..",
                            "com.fasterxml.jackson..",
                            "com.back.shared.."
                    );

    // ── Naming conventions ───────────────────────────────────────

    @ArchTest
    static final ArchRule annotated_service_classes_should_be_named_service =
            classes()
                    .that().areAnnotatedWith(Service.class)
                    .should().haveSimpleNameEndingWith("Service")
                    .orShould().haveSimpleNameEndingWith("Services");

    @ArchTest
    static final ArchRule annotated_controller_classes_should_be_named_controller =
            classes()
                    .that().areAnnotatedWith(Controller.class)
                    .or().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .should().haveSimpleNameEndingWith("Controller");
}
