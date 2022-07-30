package app.vercel.shiftup

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.Architectures
import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.row

class ArchitectureTest : FreeSpec({
    "依存関係" - {
        "内部" - {
            "レイヤ間の依存関係" {
                Architectures.layeredArchitecture().consideringAllDependencies()
                    // レイヤの定義
                    .layer(PackageId.Domain.MODEL).definedBy(PackageId.Domain.MODEL)
                    .layer(PackageId.Domain.SERVICE).definedBy(PackageId.Domain.SERVICE)
                    .layer(PackageId.INFRASTRUCTURE).definedBy(PackageId.INFRASTRUCTURE)
                    .layer(PackageId.APPLICATION).definedBy(PackageId.APPLICATION)
                    .layer(PackageId.PRESENTATION).definedBy(PackageId.PRESENTATION)
                    // 制約の定義
                    .whereLayer(PackageId.Domain.MODEL).mayOnlyBeAccessedByLayers(
                        PackageId.Domain.SERVICE, PackageId.APPLICATION,
                        PackageId.INFRASTRUCTURE, PackageId.PRESENTATION,
                    )
                    .whereLayer(PackageId.Domain.SERVICE).mayOnlyBeAccessedByLayers(
                        PackageId.APPLICATION, PackageId.INFRASTRUCTURE,
                    )
                    .whereLayer(PackageId.INFRASTRUCTURE).mayOnlyBeAccessedByLayers(
                        PackageId.APPLICATION,
                    )
                    .whereLayer(PackageId.APPLICATION).mayOnlyBeAccessedByLayers(
                        PackageId.PRESENTATION,
                    )
                    .whereLayer(PackageId.PRESENTATION).mayNotBeAccessedByAnyLayer()
                    .withOptionalLayers(true)
                    .ensureAllClassesAreContainedInArchitecture()
                    .check(CLASSES)
            }
            "ユースケース間は依存しない" {
                ArchRuleDefinition.noClasses()
                    .that().resideInAPackage(PackageId.APPLICATION)
                    .and().haveSimpleNameEndingWith(Suffix.USE_CASE)
                    .should().onlyHaveDependentClassesThat().haveSimpleNameEndingWith(Suffix.USE_CASE)
                    .allowEmptyShould(true)
                    .check(CLASSES)
            }
            "リポジトリ間は依存しない" {
                ArchRuleDefinition.noClasses()
                    .that().resideInAPackage(PackageId.INFRASTRUCTURE)
                    .and().areNotInterfaces()
                    .and().haveSimpleNameEndingWith(Suffix.REPOSITORY)
                    .should().onlyHaveDependentClassesThat().haveSimpleNameEndingWith(Suffix.REPOSITORY)
                    .allowEmptyShould(true)
                    .check(CLASSES)
            }
        }
        "外部" - {
            arrayOf(
                row(
                    "プレゼンテーション層以外は、Ktorに依存しない",
                    PackageId.PRESENTATION, PackageId.Dependencies.KTOR,
                ),
                row(
                    "インフラ層以外は、KMongoに依存しない",
                    PackageId.INFRASTRUCTURE, PackageId.Dependencies.KMONGO,
                ),
            ).forEach { (testName, layer, dependenciesPackage) ->
                testName {
                    ArchRuleDefinition.noClasses()
                        .that().resideOutsideOfPackage(layer)
                        .should().dependOnClassesThat().resideInAPackage(dependenciesPackage)
                        .allowEmptyShould(true)
                        .check(CLASSES)
                }
            }
        }
    }

    "レイヤの責務" - {
        "ユースケースは、アプリケーション層にのみ存在する" {
            ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith(Suffix.USE_CASE)
                .and().areNotInterfaces()
                .should().resideInAPackage(PackageId.APPLICATION)
                .allowEmptyShould(true)
                .check(CLASSES)
        }
        "リポジトリは、インフラ層にのみ存在する" {
            ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith(Suffix.REPOSITORY)
                .and().areNotInterfaces()
                .should().resideInAPackage(PackageId.INFRASTRUCTURE)
                .allowEmptyShould(true)
                .check(CLASSES)
        }
        "リポジトリのインターフェースは、ドメインサービス層にのみ存在する" {
            ArchRuleDefinition.classes()
                .that().haveSimpleNameStartingWith(Prefix.REPOSITORY_INTERFACE)
                .and().haveSimpleNameEndingWith(Suffix.REPOSITORY)
                .and().areInterfaces()
                .should().resideInAPackage(PackageId.Domain.SERVICE)
                .allowEmptyShould(true)
                .check(CLASSES)
        }
    }

    "命名" - {
        "リポジトリのインターフェースは、接頭辞「I」を持つ" {
            ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith(Suffix.REPOSITORY)
                .and().areInterfaces()
                .should().haveSimpleNameStartingWith(Prefix.REPOSITORY_INTERFACE)
                .allowEmptyShould(true)
                .check(CLASSES)
        }
    }
})

private val CLASSES = ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .withImportOption { it.contains("/ApplicationKt.class").not() }
    .importPackages("app.vercel.shiftup")

private object PackageId {
    private const val SHIFTUP_PACKAGE = "app.vercel.shiftup"
    private const val FEATURES_PACKAGE = "$SHIFTUP_PACKAGE.features.*"

    object Domain {
        private const val DOMAIN_PACKAGE = "$FEATURES_PACKAGE.domain"
        const val MODEL = "$DOMAIN_PACKAGE.model.."
        const val SERVICE = "$DOMAIN_PACKAGE.service.."
    }

    const val APPLICATION = "$FEATURES_PACKAGE.usecase.."
    const val INFRASTRUCTURE = "$FEATURES_PACKAGE.infra.."
    const val PRESENTATION = "$SHIFTUP_PACKAGE.presentation.."

    object Dependencies {
        const val KTOR = "io.ktor.."
        const val KMONGO = "org.litote.kmongo.."
    }
}

private object Prefix {
    const val REPOSITORY_INTERFACE = "I"
}

private object Suffix {
    const val USE_CASE = "UseCase"
    const val REPOSITORY = "Repository"
}
