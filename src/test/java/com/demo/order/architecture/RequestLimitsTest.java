package com.demo.order.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import java.util.Collection;

/** Every public input has a business limit (ORD-16): fields of web request records must be bounded. */
@AnalyzeClasses(packages = "com.demo.order", importOptions = ImportOption.DoNotIncludeTests.class)
class RequestLimitsTest {

    private static final DescribedPredicate<JavaClass> NUMBER = assignableTo(Number.class)
            .or(equivalentTo(int.class))
            .or(equivalentTo(long.class))
            .or(equivalentTo(short.class))
            .as("a number");

    @ArchTest
    static final ArchRule request_strings_have_a_max_size = fields().that()
            .areDeclaredInClassesThat()
            .haveSimpleNameEndingWith("Request")
            .and()
            .haveRawType(String.class)
            .should()
            .beAnnotatedWith(Size.class)
            .because("every public input has a business limit; unbounded strings are a memory DoS");

    @ArchTest
    static final ArchRule request_collections_have_a_max_size = fields().that()
            .areDeclaredInClassesThat()
            .haveSimpleNameEndingWith("Request")
            .and()
            .haveRawType(assignableTo(Collection.class))
            .should()
            .beAnnotatedWith(Size.class)
            .because("every public input has a business limit; unbounded collections are a memory DoS");

    @ArchTest
    static final ArchRule request_numbers_have_a_max = fields().that()
            .areDeclaredInClassesThat()
            .haveSimpleNameEndingWith("Request")
            .and()
            .haveRawType(NUMBER)
            .should()
            .beAnnotatedWith(Max.class)
            .because("every public input has a business limit");
}
