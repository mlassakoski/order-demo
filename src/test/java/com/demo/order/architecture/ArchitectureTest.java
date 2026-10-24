package com.demo.order.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Architecture fitness rules (feedback sensor).
 *
 * <p>Owned by the team (see CODEOWNERS). AI agents must not edit this file: a hook blocks it.
 * If a rule is wrong, a human changes it in a reviewed PR.
 */
@AnalyzeClasses(packages = "com.demo.order", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String PACKAGE_INFO = "package-info";

    // ---- Layers (hexagonal / onion) ----

    @ArchTest
    static final ArchRule hexagonal_layers = onionArchitecture()
            .domainModels("..domain..")
            .applicationServices("..application..")
            .adapter("web", "..adapter.in.web..")
            .adapter("persistence", "..adapter.out.persistence..")
            .withOptionalLayers(true)
            .because("dependencies point inwards and adapters never talk to each other");

    @ArchTest
    static final ArchRule domain_is_pure_java = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta..", "lombok..")
            .because("the domain must be framework-free");

    @ArchTest
    static final ArchRule application_does_not_depend_on_web = noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework.web..", "org.springframework.http..", "jakarta.servlet..")
            .because("HTTP concerns belong to the web adapter");

    @ArchTest
    static final ArchRule web_adapter_uses_only_input_ports = noClasses()
            .that()
            .resideInAPackage("..adapter.in..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..application.service..", "..application.port.out..")
            .because("controllers depend only on input ports (*UseCase)");

    // ---- Spring stereotypes in the right place ----

    @ArchTest
    static final ArchRule web_components_live_in_web_adapter = classes()
            .that()
            .areAnnotatedWith(RestController.class)
            .or()
            .areAnnotatedWith(RestControllerAdvice.class)
            .should()
            .resideInAPackage("..adapter.in.web..");

    @ArchTest
    static final ArchRule services_live_in_application_service =
            classes().that().areAnnotatedWith(Service.class).should().resideInAPackage("..application.service..");

    @ArchTest
    static final ArchRule driven_adapters_live_in_adapter_out = classes()
            .that()
            .areAnnotatedWith(Repository.class)
            .or()
            .areAnnotatedWith(Component.class)
            .should()
            .resideInAPackage("..adapter.out..");

    // ---- Naming and shape ----

    @ArchTest
    static final ArchRule input_ports_are_use_case_interfaces = classes()
            .that()
            .resideInAPackage("..application.port.in..")
            .and()
            .areInterfaces()
            .and()
            .doNotHaveSimpleName(PACKAGE_INFO)
            .should()
            .haveSimpleNameEndingWith("UseCase");

    @ArchTest
    static final ArchRule output_ports_are_port_interfaces = classes()
            .that()
            .resideInAPackage("..application.port.out..")
            .and()
            .areTopLevelClasses()
            .and()
            .doNotHaveSimpleName(PACKAGE_INFO)
            .should()
            .beInterfaces()
            .andShould()
            .haveSimpleNameEndingWith("Port");

    @ArchTest
    static final ArchRule services_implement_a_use_case = classes()
            .that()
            .resideInAPackage("..application.service..")
            .and()
            .areTopLevelClasses()
            .and()
            .doNotHaveSimpleName(PACKAGE_INFO)
            .should()
            .haveSimpleNameEndingWith("Service")
            .andShould()
            .implement(JavaClass.Predicates.resideInAPackage("..application.port.in.."));

    @ArchTest
    static final ArchRule domain_has_no_public_setters = noMethods()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage("..domain..")
            .and()
            .arePublic()
            .should()
            .haveNameMatching("set[A-Z].*")
            .because("state changes are aggregate methods such as order.cancel(), never setters");

    // ---- General coding rules ----

    @ArchTest
    static final ArchRule no_generic_exceptions = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS.because(
            "use a specific domain or application exception so it maps to the right HTTP status");

    @ArchTest
    static final ArchRule no_field_injection =
            NO_CLASSES_SHOULD_USE_FIELD_INJECTION.because("use constructor injection");

    @ArchTest
    static final ArchRule no_java_util_logging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING.because("use SLF4J");
}
