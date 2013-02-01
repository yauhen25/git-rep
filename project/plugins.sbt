resolvers ++= Seq(
	"sbt-idea-repo" at "http://mpeltonen.github.com/maven/",
	"Web plugin repo" at "http://siasia.github.com/maven2"
	)

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.10"))

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.7.2")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.0.0-M2")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.0.0")

