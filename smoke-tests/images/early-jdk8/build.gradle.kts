import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage

plugins {
  id("com.bmuschko.docker-remote-api")
}

val imageTag = "local"

tasks {
  val dockerWorkingDir = layout.buildDirectory.dir("docker")

  val imagePrepare by registering(Copy::class) {
    into(dockerWorkingDir)
    from("Dockerfile")
  }

  val imageBuild by registering(DockerBuildImage::class) {
    dependsOn(imagePrepare)
    inputDir.set(dockerWorkingDir)

    images.add("smoke-test-zulu-openjdk-8u31:$imageTag")
    dockerFile.set(dockerWorkingDir.get().file("Dockerfile"))
  }
}
