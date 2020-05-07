package com.github.scala2ts.configuration

import com.github.scala2ts.configuration.DateMapping.DateMapping
import com.github.scala2ts.configuration.LongDoubleMapping.LongDoubleMapping

case class Configuration(
  debug: Boolean = false,
  files: IncludeExclude = IncludeExclude(),
  types: IncludeExclude = IncludeExclude(),
  typeNamePrefix: String = "",
  typeNameSuffix: String = "",
  dateMapping: DateMapping = DateMapping.AsDate,
  longDoubleMapping: LongDoubleMapping = LongDoubleMapping.AsString,
  outDir: Option[String] = None,
  outFileName: String = "index.ts",
  packageJson: PackageJson = PackageJson()
) {
  import Configuration.Args._

  def fromCompilerOptions(options: List[String]): Configuration =
    options.foldLeft(this) {
      case (config, option) if option.startsWith(debugArg) =>
        config.copy(
          debug = argValue(option, debugArg) == "true"
        )
      case (config, option) if option.startsWith(fileIncludesArg) =>
        config.copy(
          files = config.files.copy(
            include = config.files.include :+ argValue(option, fileIncludesArg).r
          )
        )
      case (config, option) if option.startsWith(fileExcludesArg) =>
        config.copy(
          files = config.files.copy(
            exclude = config.files.exclude :+ argValue(option, fileExcludesArg).r
          )
        )
      case (config, option) if option.startsWith(typeIncludesArg) =>
        config.copy(
          types = config.types.copy(
            include = config.files.include :+ argValue(option, typeIncludesArg).r
          )
        )
      case (config, option) if option.startsWith(typeExcludesArg) =>
        config.copy(
          types = config.types.copy(
            exclude = config.files.exclude :+ argValue(option, typeExcludesArg).r
          )
        )
      case (config, option) if option.startsWith(typeNamePrefixArg) =>
        config.copy(
          typeNamePrefix = argValue(option, typeNamePrefixArg)
        )
      case (config, option) if option.startsWith(typeNameSuffixArg) =>
        config.copy(
          typeNameSuffix = argValue(option, typeNameSuffixArg)
        )
      case (config, option) if option.startsWith(dateMappingArg) =>
        config.copy(
          dateMapping = DateMapping.withName(argValue(option, dateMappingArg))
        )
      case (config, option) if option.startsWith(longDoubleMappingArg) =>
        config.copy(
          longDoubleMapping = LongDoubleMapping.withName(argValue(option, longDoubleMappingArg))
        )
      case (config, option) if option.startsWith(outDirArg) =>
        config.copy(
          outDir = Some(argValue(option, outDirArg))
        )
      case (config, option) if option.startsWith(outFileNameArg) =>
        config.copy(
          outFileName = argValue(option, outFileNameArg)
        )
      case (config, option) if option.startsWith(packageJsonNameArg) =>
        config.copy(
          packageJson = config.packageJson.copy(
            name = Some(argValue(option, packageJsonNameArg))
          )
        )
      case (config, option) if option.startsWith(packageJsonVersionArg) =>
        config.copy(
          packageJson = config.packageJson.copy(
            version = Some(argValue(option, packageJsonVersionArg))
          )
        )
      case (config, option) if option.startsWith(packageJsonTypesArg) =>
        config.copy(
          packageJson = config.packageJson.copy(
            types = Some(argValue(option, packageJsonTypesArg))
          )
        )
      case (config, option) if option.startsWith(packageJsonRegistryArg) =>
        config.copy(
          packageJson = config.packageJson.copy(
            publishConfig = Some(PublishConfig(
              registry = argValue(option, packageJsonRegistryArg)
            ))
          )
        )
    }
}

object Configuration {

  object Args {
    private[this] def argBuilder(part: String*): String =
      part.mkString(":") + ":"
    def argValue(option: String, arg: String): String =
      option.substring(arg.length)

    lazy val debugArg: String =
      argBuilder("debug")
    lazy val fileIncludesArg: String =
      argBuilder("file", "includes")
    lazy val fileExcludesArg: String =
      argBuilder("file", "excludes")
    lazy val typeIncludesArg: String =
      argBuilder("type", "includes")
    lazy val typeExcludesArg: String =
      argBuilder("type", "excludes")
    lazy val typeNamePrefixArg: String =
      argBuilder("type", "prefix")
    lazy val typeNameSuffixArg: String =
      argBuilder("type", "suffix")
    lazy val dateMappingArg: String =
      argBuilder("date")
    lazy val longDoubleMappingArg: String =
      argBuilder("longDouble")
    lazy val outDirArg: String =
      argBuilder("outDir")
    lazy val outFileNameArg: String =
      argBuilder("outFileName")
    lazy val packageJsonNameArg: String =
      argBuilder("pj", "name")
    lazy val packageJsonVersionArg: String =
      argBuilder("pj", "version")
    lazy val packageJsonTypesArg: String =
      argBuilder("pj", "types")
    lazy val packageJsonRegistryArg: String =
      argBuilder("pj", "registry")
  }

}