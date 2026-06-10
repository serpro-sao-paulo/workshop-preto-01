package br.gov.sifap;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Enforça as fronteiras de bounded context em CI.
 * Nenhum contexto importa domain/ ou infrastructure/ de outro contexto.
 * ADR-004, CONSTITUTION.
 */
class BoundedContextArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void importClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.gov.sifap");
    }

    @Test
    void payment_domain_should_not_depend_on_beneficiary_infrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..payment.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..beneficiary.infrastructure..");
        rule.check(importedClasses);
    }

    @Test
    void payment_domain_should_not_depend_on_audit_infrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..payment.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..audit.infrastructure..");
        rule.check(importedClasses);
    }

    @Test
    void beneficiary_should_not_depend_on_payment_domain() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..beneficiary..")
                .should().dependOnClassesThat()
                .resideInAPackage("..payment.domain..");
        rule.check(importedClasses);
    }

    @Test
    void audit_should_not_depend_on_payment_domain() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..audit..")
                .should().dependOnClassesThat()
                .resideInAPackage("..payment.domain..");
        rule.check(importedClasses);
    }

    @Test
    void domain_classes_should_not_depend_on_infrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure..")
                .because("Domain layer must not depend on Infrastructure (clean architecture)");
        rule.check(importedClasses);
    }

    @Test
    void controllers_should_reside_in_infrastructure() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Controller")
                .should().resideInAPackage("..infrastructure..")
                .because("Controllers are infrastructure adapters (ADR-004)");
        rule.check(importedClasses);
    }

    @Test
    void repositories_should_reside_in_infrastructure() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Repository")
                .and().areInterfaces()
                .should().resideInAPackage("..infrastructure..")
                .because("Repositories are infrastructure (ADR-004)");
        rule.check(importedClasses);
    }

    @Test
    void services_should_reside_in_application() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Service")
                .and().areAnnotatedWith(org.springframework.stereotype.Service.class)
                .should().resideInAPackage("..application..")
                .because("@Service classes belong in the application layer (ADR-004)");
        rule.check(importedClasses);
    }
}
