package com.github.halfmatthalfcat.scala2ts

import com.github.halfmatthalfcat.scala2ts.settings._
import sbt.Keys._
import sbt._

object Scala2TSPlugin extends AutoPlugin {
  object autoImport {
    val scala2ts = inputKey[Unit]("Scala2TS Options")

    val addTypeNamePrefix = settingKey[String]("Prefix which will be added to names of classes, interfaces, enums.")
    val addTypeNameSuffix = settingKey[String]("Suffix which will be added to names of classes, interfaces, enums.")

    val classPatterns = settingKey[Seq[String]]("Classes to process specified using glob patterns so it is possible to specify package or class name suffix.")
    val classes = settingKey[Seq[String]]("Classes to process.")
    val classesExtendingClasses = settingKey[Seq[String]]("Classes to process specified by extended superclasses.")
    val classesFromAutomaticJaxrsApplication = settingKey[Boolean]("Scans JAX-RS resources for JSON classes to process.")
    val classesFromJaxrsApplication = settingKey[String]("Scans specified JAX-RS Application for classes to process.")
    val classesImplementingInterfaces = settingKey[Seq[String]]("Classes to process specified by implemented interface.")
    val classesWithAnnotations = settingKey[Seq[String]]("Classes to process specified by annotations.")

    val customTypeAliases = settingKey[Seq[String]]("List of custom type aliases.")
    val customTypeMappings = settingKey[Seq[String]]("List of custom type mappings.")
    val customTypeNaming = settingKey[Seq[String]]("Specifies custom TypeScript names for Java classes.")
    val customTypeNamingFunction = settingKey[String]("Specifies JavaScript function for getting custom TypeScript names for Java classes.")
    val customTypeProcessor = settingKey[String]("Specifies custom class implementing TypeProcessor.")

    val declarePropertiesAsReadOnly = settingKey[Boolean]("If true declared properties will be readonly.")

    val disableTaggedUnions = settingKey[Boolean]("If true tagged unions will not be generated for Jackson 2 polymorphic types.")

    val excludeClassPatterns = settingKey[Seq[String]]("Excluded classes specified using glob patterns.")
    val excludeClasses = settingKey[Seq[String]]("List of classes excluded from processing.")
    val excludePropertyAnnotations = settingKey[Seq[String]]("Properties with any of these annotations will be excluded.")

    val extensions = settingKey[Seq[String]]("List of extensions specified as fully qualified class name.")
    // val extensionsWithConfiguration = settingKey[Seq[String]]("List of extensions with their configurations.")

    val generateConstructors = settingKey[Boolean]("If true generated classes will also have constructors.")
    val generateInfoJson = settingKey[Boolean]("If true JSON file describing generated module will be generated.")
    val generateJaxrsApplicationClient = settingKey[Boolean]("If true client for JAX-RS REST application will be generated.")
    val generateJaxrsApplicationInterface = settingKey[Boolean]("If true interface for JAX-RS REST application will be generated.")
    val generatePackageJson = settingKey[Boolean]("Generate a package.json for the definitions")
    val generateSpringApplicationClient = settingKey[Boolean]("If true client for Spring REST application will be generated.")
    val generateSpringApplicationInterface = settingKey[Boolean]("If true interface for Spring REST application will be generated.")

    val ignoreSwaggerAnnotations = settingKey[Boolean]("If true Swagger annotations will not be used.")

    val importDeclarations = settingKey[Seq[String]]("List of import declarations which will be added to generated output.")

    val includePropertyAnnotations = settingKey[Seq[String]]("If this list is not empty then only properties with any of these annotations will be included.")

    val indentString = settingKey[String]("Specifies indentation string.")

    //val jackson2Configuration = settingKey[String]("Specifies Jackson 2 global configuration.")
    val jackson2ModuleDiscovery = settingKey[Boolean]("Turns on Jackson2 automatic module discovery.")
    val jackson2Modules = settingKey[Seq[String]]("Specifies Jackson2 modules to use.")

    val javadocXmlFiles = settingKey[Seq[String]]("List of Javadoc XML files to search for documentation comments.")

    val jsonLibrary = settingKey[String]("The json serialization library to use")

    val jsonbConfiguration = settingKey[String]("Specifies JSON-B global configuration")

    val loggingLevel = settingKey[LoggingLevel]("Specifies level of logging output")

    val mapClasses = settingKey[ClassMapping]("Specifies whether Java classes will be mapped to TypeScript classes or interfaces.")
    val mapClassesAsClassesPattern = settingKey[String]("Specifies which Java classes should be mapped as TypeScript classes.")
    val mapDate = settingKey[DateMapping]("Specifies how Date will be mapped.")
    val mapEnum = settingKey[EnumMapping]("Specifies how enums will be mapped.")
    val mapPackagesToNamespace = settingKey[Boolean]("Generates TypeScript namespaces from Java packages.")

    val module = settingKey[String]("Name of generated ambient module.")

    val moduleDependencies = settingKey[Seq[String]]("List of modules (generated by typescript-generator!) on which currently generated module depends on.")

    val namespace = settingKey[String]("Generates specified namespace.")

    val noEslintDisable = settingKey[Boolean]("If true generated file will not be prevented from linting by ESLint.")
    val noFileComment = settingKey[Boolean]("If true generated file will not contain comment at the top.")
    val noTslintDisable = settingKey[Boolean]("If true generated file will not be prevented from linting by TSLint.")

    val nonConstEnumAnnotations = settingKey[Seq[String]]("If this list is not empty, then generated enums will not have const keyword, if the enum contains one of the annotations defined in this list.")
    val nonConstEnums = settingKey[Boolean]("If true generated enums will not have const keyword.")

    val npmBuildScript = settingKey[String]("Specifies NPM \"build\" script.")
    val npmName = settingKey[String]("Specifies NPM package name.")
    val npmVersion = settingKey[String]("Specifies NPM package version.")

    val nullabilityDefinition = settingKey[NullabilityDefinition]("Specifies how nullable types will be created in generated file.")

    val nullableAnnotations = settingKey[Seq[String]]("When any of specified annotations is used on a Java type typescript-generator treats this type as nullable.")

    val optionalAnnotations = settingKey[Seq[String]]("The presence of any annotation in this list on a JSON property will cause the typescript-generator to treat that property as optional when generating the corresponding TypeScript interface.")
    val optionalProperties = settingKey[OptionalProperties]("Specifies how properties are defined to be optional.")
    val optionalPropertiesDeclaration = settingKey[OptionalPropertiesDeclaration]("Specifies how optional properties will be declared in generated file.")

    val outputFile = settingKey[String]("Path and name of generated TypeScript file.")
    val outputFileType = settingKey[OutputFileType]("Output file format")

    val outputKind = settingKey[String]("Kind of generated TypeScript output.")

    val referencedFiles = settingKey[Seq[String]]("List of files which will be referenced using triple-slash directive")

    val removeTypeNamePrefix = settingKey[String]("Prefix which will be removed from names of classes, interfaces, enums.")
    val removeTypeNameSuffix = settingKey[String]("Suffix which will be removed from names of classes, interfaces, enums.")

    val requiredAnnotations = settingKey[Seq[String]]("Properties will be treated as optional except those annotated with any of specified annotations.")

    val restNamespacing = settingKey[RestNamespacing]("Specifies how JAX-RS REST operations will be grouped into objects.")
    val restNamespacingAnnotation = settingKey[String]("Specifies annotation used for grouping JAX-RS REST operations.")
    val restOptionsType = settingKey[String]("Specifies HTTP request options type in REST application.")
    val restResponseType = settingKey[String]("Specifies HTTP response type in REST application.")

    val scanSpringApplication = settingKey[Boolean]("If true Spring REST application will be loaded and scanned for classes to process.")

    val sortDeclarations = settingKey[Boolean]("If true TypeScript declarations (interfaces, properties) will be sorted alphabetically.")
    val sortTypeDeclarations = settingKey[Boolean]("If true TypeScript type declarations (interfaces) will be sorted alphabetically.")

    val stringQuotes = settingKey[StringQuotes]("Specifies how strings will be quoted.")

    val tsNoCheck = settingKey[Boolean]("If true generated file will have disabled TypeScript semantic checks using @ts-nocheck comment.")

    val umdNamespace = settingKey[String]("Turns proper module into UMD (Universal Module Definition) with specified namespace.")
  }
}
