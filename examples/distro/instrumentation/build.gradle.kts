val instrProject = project

subprojects {
  afterEvaluate {
    if (plugins.hasPlugin("java")) {
      // Make it so all instrumentation subproject tests can be run with a single command.
      instrProject.tasks.named("test").configure {
        dependsOn(tasks.named("test"))
      }

      instrProject.dependencies {
        add("implementation", project(path))
      }
    }
  }
}
