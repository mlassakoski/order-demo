package com.demo.order.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Test conventions (feedback sensor). Owned by the team; agents must not edit it.
 */
@AnalyzeClasses(packages = "com.demo.order", importOptions = ImportOption.OnlyIncludeTests.class)
class TestConventionsTest {

    @ArchTest
    static final ArchRule test_names_follow_should_when = methods()
            .that().areAnnotatedWith(Test.class)
            .or().areAnnotatedWith(ParameterizedTest.class)
            .should().haveNameMatching("should_.+_when_.+")
            .because("test names describe behaviour: should_<behavior>_when_<condition>");

    @ArchTest
    static final ArchRule domain_tests_are_plain_junit = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..")
            .because("domain tests run without a Spring context");
}
