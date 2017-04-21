val scalaV = "2.12.2"

// ---- kind projector for nicer type lambdas ----
val kindProjectorPlugin = Seq(
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")
)

// ---- library for nicer typeclasses
val simulacrumPlugin = Seq(
  addCompilerPlugin(
    ("org.scalamacros" % "paradise" % "2.1.0").cross(CrossVersion.full)),
  libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.10.0"
)

// ---- enable wartremover ----

// todo - check if we have all warts
import wartremover.Wart._
val wartRemover = Seq(
  wartremoverWarnings in (Compile, compile) := Seq(
    Any,
    AsInstanceOf,
    DefaultArguments,
    EitherProjectionPartial,
    Enumeration,
    Equals,
    ExplicitImplicitTypes,
    FinalCaseClass,
    ImplicitConversion,
    ImplicitParameter,
    IsInstanceOf,
    JavaConversions,
    LeakingSealed,
    MutableDataStructures,
    NonUnitStatements,
    Nothing,
    Null,
    Option2Iterable,
    OptionPartial,
    Overloading,
    Product,
    PublicInference,
    Return,
    Serializable,
    StringPlusAny,
    Throw,
    TraversableOps,
    ToString,
    TryPartial,
    Var,
    While
  )
)

// ---- formatting ----
scalaVersion in ThisBuild := scalaV

// ---- acyclic ----
val acyclic = Seq(
  libraryDependencies += "com.lihaoyi" %% "acyclic" % "0.1.7" % "provided",
  autoCompilerPlugins := true,
  addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.7"),
  scalacOptions += "-P:acyclic:force"
)

val commonSettings = Seq(
  organization := "http4s-react-example",
  scalaVersion := scalaV,
  scalacOptions := Seq(
    // following two lines must be "together"
    "-encoding",
    "UTF-8",
    "-Xlint",
    "-Xlint:missing-interpolator",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Ywarn-dead-code",
    "-Yno-adapted-args",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-Ywarn-value-discard",
    "-Ywarn-unused-import",
    "-Ywarn-unused",
    "-Ywarn-numeric-widen",
    "-Ypartial-unification"
  ),
  shellPrompt in ThisBuild := { state ⇒
    scala.Console.GREEN + Project
      .extract(state)
      .currentRef
      .project + "> " + scala.Console.RESET
  }
) ++ kindProjectorPlugin ++ simulacrumPlugin ++ wartRemover ++ acyclic

// ---- publising ----

val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

// ---- dependencies ----

val reactVersion = "1.0.0-RC3"
val catsVersion = "0.9.0"

lazy val shared = (crossProject in file("modules/shared"))
  .settings(commonSettings, noPublishSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.chuusai" %%% "shapeless" % "2.3.2",
      "org.typelevel" %%% "cats-core" % catsVersion,
      "org.typelevel" %%% "cats-free" % catsVersion,
      "org.scalacheck" %%% "scalacheck" % "1.13.5" % Test
    )
  )

lazy val sharedJS = shared.js
lazy val sharedJVM = shared.jvm

lazy val backend = (project in file("modules/backend"))
  .settings(commonSettings, noPublishSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-core" % "0.17.0-M1",
      "co.fs2" %% "fs2-core" % "0.9.5"
    )
  )
  .dependsOn(sharedJVM)
  .aggregate(sharedJVM)

lazy val frontend = (project in file("modules/frontend"))
  .settings(commonSettings, noPublishSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % reactVersion,
      "com.github.japgolly.scalajs-react" %%% "extra" % reactVersion,
      "com.github.japgolly.scalajs-react" %%% "ext-cats" % reactVersion,
      "io.suzaku" %%% "diode" % "1.1.1",
      "com.github.japgolly.scalajs-react" %%% "test" % reactVersion % Test
    ),

    scalaJSUseMainModuleInitializer := true,
    scalaJSModuleKind := ModuleKind.CommonJSModule

  )
  .dependsOn(sharedJS)
  .aggregate(sharedJS)
  .enablePlugins(ScalaJSPlugin)

lazy val root = (project in file("."))
  .dependsOn(backend, frontend)
  .aggregate(backend, frontend)
