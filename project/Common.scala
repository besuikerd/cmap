import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Keys._
import sbt._

object Common {
  type Settings = Seq[Setting[_]]

  lazy val commonSettings: Settings = Seq(
    version := "0.0.1",
    scalaVersion := "2.12.4",
    commonDependencies,
    testDependencies
  )

  lazy val commonDependencies = libraryDependencies ++= Seq(
    "org.scalaz"    %%% "scalaz-core" % "7.2.17",
    "com.chuusai"   %%% "shapeless"   % "2.3.2",
    "org.typelevel" %% "cats-core"    % "1.0.0-RC1"
  )

  lazy val testDependencies = libraryDependencies ++= Seq(
    "org.scalatest" %%% "scalatest" % "3.0.4" % Test,
  )
}
