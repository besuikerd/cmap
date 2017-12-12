name := "cmap"

lazy val root = project.in(file("."))
  .settings(Common.commonSettings)
  .aggregate(jvm, js, poi, playJson, shared, testShared)

lazy val shared = project.in(file("shared"))

lazy val testShared = project
  .in(file("testShared"))
  .dependsOn(shared)
  .configs(Test)

lazy val jvm = project.in(file("jvm"))
  .dependsOn(shared)

lazy val js = project.in(file("js"))
  .dependsOn(shared)

lazy val poi = project.in(file("poi"))
  .dependsOn(shared)
  .dependsOn(testShared)

lazy val playJson = project.in(file("play-json"))
  .dependsOn(shared)
  .dependsOn(testShared)
