import sbt._
import Keys._

object MyBuild extends Build {
  def benchProject(name: String, extraSettings: Seq[Setting[_]] = Seq.empty)(dir: String = name) =
    Project(name, file(".")) settings(defaultSettings:_*) settings(extraSettings ++ scalaAt(name, dir): _*)

  lazy val benchProjects = Seq(
    benchProject("latest")(),
    benchProject("latestOpt", optimise)("latest"),
    benchProject("dcs5286")(),
    benchProject("dcs5286Opt", optimise)("dcs5286")
  )

  override def projects = benchProjects

  val distPath = "/home/dcs/github/scala/dists"
  def scalaAt(projectName: String, dir: String): Seq[Setting[_]] = Seq(
    scalaHome := Some(file(distPath) / dir),
    target <<= (baseDirectory, name) apply (_ / projectName / _)
  )

  val optimise = scalacOptions += "-optimise"

  val defaultSettings: Seq[Setting[_]] = Seq(
    organization := "com.example",
    name := "scala-benchmarking-template",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.9.1",
    libraryDependencies ++= Seq(
        "com.google.code.java-allocation-instrumenter" % "java-allocation-instrumenter" % "2.0",
        "com.google.code.caliper" % "caliper" % "1.0-SNAPSHOT",
        "com.google.code.gson" % "gson" % "1.7.1"
    ),
    resolvers += "sonatypeSnapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
    // enable forking in run
    fork in run := true,

    // custom kludge to get caliper to see the right classpath

    // define the onLoad hook
    onLoad in Global <<= (onLoad in Global) ?? identity[State],
    {
      // attribute key to prevent circular onLoad hook
      val key = AttributeKey[Boolean]("loaded")
      val f = (s: State) => {
        val loaded: Boolean = s get key getOrElse false
        if (!loaded) {
          var cpString: String = ""
          // get the runtime classpath
          Project.evaluateTask(fullClasspath.in(Runtime), s) match {
	    // make a colon-delimited string of the classpath
            case Some(Value(cp)) => cpString = cp.files.mkString(":")
	    // probably should handle an error here, but not sure you can
	    //  ever get here with a working sbt
            case _ => Nil
            }
          val extracted: Extracted = Project.extract(s)
          // return a state with loaded = true and javaOptions set correctly
          extracted.append(Seq(javaOptions in run ++= Seq("-cp", cpString)),
			   s.put(key, true))
        } else {
          // return the state, unmodified
          s
        }
      } // f
      onLoad in Global ~= (f compose _)
    } // onLoad
  ) // defaultSettings
}
