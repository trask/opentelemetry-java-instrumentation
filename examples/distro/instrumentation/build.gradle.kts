val instrProject = project

subprojects {
  afterEvaluate {
    if (plugins.hasPlugin("java")) {
      val subprojectTest = tasks.named("test")
      // Make it so all instrumentation subproject tests can be run with a single command.
      instrProject.tasks.named("test").configure {
        dependsOn(subprojectTest)
      }

      instrProject.dependencies {
        add("implementation", project(path))
      }
    }
  }
}
