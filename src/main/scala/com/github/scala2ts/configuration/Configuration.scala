package com.github.scala2ts.configuration

case class Configuration(
  files: IncludeExclude = IncludeExclude(),
  types: IncludeExclude = IncludeExclude(),
  indentString: String = "  ",
  typeNamePrefix: String = "",
  typeNameSuffix: String = "",
  emitInterfaces: Boolean = true,
  emitClasses: Boolean = false,
  optionToNullable: Boolean = true,
  optionToUndefined: Boolean = false
) {
  import Configuration.Args._

  def fromCompilerOptions(options: List[String]): Configuration =
    options.foldLeft(this) {
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
      case (config, option) if option.startsWith(indentStringArg) =>
        config.copy(
          indentString = argValue(option, indentStringArg)
        )
      case (config, option) if option.startsWith(typeNamePrefixArg) =>
        config.copy(
          typeNamePrefix = argValue(option, typeNamePrefixArg)
        )
      case (config, option) if option.startsWith(typeNameSuffixArg) =>
        config.copy(
          typeNameSuffix = argValue(option, typeNameSuffixArg)
        )
      case (config, option) if option.startsWith(emitInterfacesArg) =>
        config.copy(
          emitInterfaces = argValue(option, emitInterfacesArg) == "true"
        )
      case (config, option) if option.startsWith(emitClassesArg) =>
        config.copy(
          emitClasses = argValue(option, emitClassesArg) == "true"
        )
      case (config, option) if option.startsWith(optionToNullableArg) =>
        config.copy(
          optionToNullable = argValue(option, optionToNullableArg) == "true"
        )
      case (config, option) if option.startsWith(optionToUndefinedArg) =>
        config.copy(
          optionToUndefined = argValue(option, optionToUndefinedArg) == "true"
        )
    }
}

object Configuration {

  object Args {
    private[this] def argBuilder(part: String*): String =
      part.mkString(":") + ":"
    def argValue(option: String, arg: String): String =
      option.substring(arg.length)

    lazy val fileIncludesArg: String =
      argBuilder("file", "includes")
    lazy val fileExcludesArg: String =
      argBuilder("file", "excludes")
    lazy val typeIncludesArg: String =
      argBuilder("type", "includes")
    lazy val typeExcludesArg: String =
      argBuilder("type", "excludes")
    lazy val indentStringArg: String =
      argBuilder("indent")
    lazy val typeNamePrefixArg: String =
      argBuilder("type", "prefix")
    lazy val typeNameSuffixArg: String =
      argBuilder("type", "suffix")
    lazy val emitInterfacesArg: String =
      argBuilder("emitInterfaces")
    lazy val emitClassesArg: String =
      argBuilder("emitClasses")
    lazy val optionToNullableArg: String =
      argBuilder("optionToNullable")
    lazy val optionToUndefinedArg: String =
      argBuilder("optionToUndefined")
  }

}