package com.twitter.scrooge
package scalagen

import AST._

object ServiceTemplate extends ScalaTemplate {
  val functionThrowsTemplate = template[Field]("@throws(classOf[{{ scalaType(`type`) }}])")

  val functionDeclarationTemplate = template[Function](
"""def {{name}}({{ fieldArgs(args) }}): {{scalaType(`type`)}}""")

  val futureFunctionDeclarationTemplate = template[Function](
"""def {{name}}({{ fieldArgs(args) }}): Future[{{scalaType(`type`)}}]""")

  val functionTemplate = template[Function](
"""{{ throws.map { t => functionThrowsTemplate(t, scope) + "\n" }.mkString }}{{ functionDeclarationTemplate(self, scope) }}""")

  val futureFunctionTemplate = template[Function](
"""{{ throws.map { t => functionThrowsTemplate(t, scope) + "\n" }.mkString }}{{ futureFunctionDeclarationTemplate(self, scope) }}""")

  def serviceFunctionResultStruct(f: Function) = {
    val resultField = AST.Field(0, "success", f.`type`.asInstanceOf[FieldType], None, AST.Requiredness.Optional)
    AST.Struct(f.name + "_result", (resultField :: f.throws.toList).toArray)
  }

  val serviceTemplate = template[Service](
"""object {{name}} {
  trait Iface {{ parent.map { "extends " + _ }.getOrElse("") }}{
{{ functions.map { f => functionTemplate(f, scope).indent(2) }.mkString("\n") }}
  }

  trait FutureIface {{ parent.map { "extends " + _ }.getOrElse("") }}{
{{ functions.map { f => futureFunctionTemplate(f, scope).indent(2) }.mkString("\n") }}
  }

{{
functions.map { f =>
  // generate a Struct for each function's args & retval
  structTemplate(AST.Struct(f.name + "_args", f.args), scope) + "\n" +
    structTemplate(serviceFunctionResultStruct(f), scope)
}.mkString("\n").indent
}}
}
""")
}