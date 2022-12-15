package app.vercel.shiftup

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.Architectures
import io.kotest.core.spec.style.FreeSpec

class ArchitectureTest : FreeSpec({
    "依存関係" - {
        "内部" - {
            "レイヤ間の依存関係" {
                Architectures.layeredArchitecture().consideringAllDependencies()
                    // レイヤの定義
                    .layer(LayerName.Domain.MODEL).definedBy(PackageId.Domain.MODEL)
                    .layer(LayerName.Domain.SERVICE).definedBy(PackageId.Domain.SERVICE)
                    .layer(LayerName.INFRASTRUCTURE).definedBy(PackageId.INFRASTRUCTURE)
                    .layer(LayerName.APPLICATION).definedBy(PackageId.APPLICATION)
                    .layer(LayerName.PRESENTATION).definedBy(PackageId.PRESENTATION)
                    // 制約の定義
                    .whereLayer(LayerName.Domain.MODEL).mayOnlyBeAccessedByLayers(
                        LayerName.Domain.SERVICE, LayerName.APPLICATION,
                        LayerName.INFRASTRUCTURE, LayerName.PRESENTATION,
                    )
                    .whereLayer(LayerName.Domain.SERVICE).mayOnlyBeAccessedByLayers(
                        LayerName.APPLICATION, LayerName.INFRASTRUCTURE,
                    )
                    .whereLayer(LayerName.INFRASTRUCTURE).mayOnlyBeAccessedByLayers(
                        LayerName.APPLICATION,
                    )
                    .whereLayer(LayerName.APPLICATION).mayOnlyBeAccessedByLayers(
                        LayerName.PRESENTATION,
                    )
                    .whereLayer(LayerName.PRESENTATION).mayNotBeAccessedByAnyLayer()
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
            "プレゼンテーション層以外は、Ktorに依存しない" {
                val ktor = PackageId.Dependencies.Ktor
                ArchRuleDefinition.noClasses()
                    .that().resideOutsideOfPackage(PackageId.PRESENTATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                        ktor.APPLICATION,
                        ktor.ROUTING,
                        ktor.RESOURCES,
                    )
                    .allowEmptyShould(true)
                    .check(CLASSES)
            }
            "インフラ層以外は、KMongoに依存しない" {
                ArchRuleDefinition.noClasses()
                    .that().resideOutsideOfPackage(PackageId.INFRASTRUCTURE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(PackageId.Dependencies.KMONGO)
                    .allowEmptyShould(true)
                    .check(CLASSES)
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
                .that().haveSimpleNameEndingWith(Suffix.REPOSITORY_INTERFACE)
                .and().areInterfaces()
                .should().resideInAPackage(PackageId.Domain.SERVICE)
                .allowEmptyShould(true)
                .check(CLASSES)
        }
    }
})

private val CLASSES = ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .withImportOption { it.contains("/ApplicationKt.class").not() }
    .withImportOption { it.contains("/KoinModulesKt").not() }
    .importPackages("app.vercel.shiftup")

private object LayerName {
    object Domain {
        const val MODEL = "ドメインモデル層"
        const val SERVICE = "ドメインサービス層"
    }

    const val APPLICATION = "アプリケーション層"
    const val INFRASTRUCTURE = "インフラ層"
    const val PRESENTATION = "プレゼンテーション層"
}

private object PackageId {
    private const val SHIFTUP_PACKAGE = "app.vercel.shiftup"
    private const val FEATURES_PACKAGE = "$SHIFTUP_PACKAGE.features.(**)"

    object Domain {
        private const val DOMAIN_PACKAGE = "$FEATURES_PACKAGE.domain"
        const val MODEL = "$DOMAIN_PACKAGE.model.."
        const val SERVICE = "$DOMAIN_PACKAGE.service.."
    }

    const val APPLICATION = "$FEATURES_PACKAGE.application.."
    const val INFRASTRUCTURE = "$FEATURES_PACKAGE.infra.."
    const val PRESENTATION = "$SHIFTUP_PACKAGE.presentation.."

    object Dependencies {
        const val KMONGO = "org.litote.kmongo.."

        object Ktor {
            private const val KTOR_SERVER_PACKAGE = "io.ktor.server"
            const val APPLICATION = "$KTOR_SERVER_PACKAGE.application.."
            const val ROUTING = "$KTOR_SERVER_PACKAGE.routing.."
            const val RESOURCES = "$KTOR_SERVER_PACKAGE.resources.."
        }
    }
}

private object Suffix {
    const val USE_CASE = "UseCase"
    const val REPOSITORY = "Repository"
    const val REPOSITORY_INTERFACE = "RepositoryInterface"
}