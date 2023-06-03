import sbt._

// Simplifies sbt project syntax so we don't have any settings in build.sbt, just dependencies.
object syntax extends AutoPlugin {
  object autoImport {
    implicit def projectSyntax(p: Project) = new {
      def dependsOnExternal(modules: ModuleID*): Project =
        p.settings(Keys.libraryDependencies ++= modules)
      def name(name: String): Project =
        p.settings(Keys.name := name)
    }
  }
}