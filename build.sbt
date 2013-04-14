name := "october"

version := "0.1.0"

scalaVersion := "2.9.2"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

scalacOptions ++= Seq("-deprecation", "-unchecked")

// MongoDB
libraryDependencies ++= Seq("org.mongodb" %% "casbah" % "2.5.0",
                            "com.novus" %% "salat" % "1.9.2-SNAPSHOT")

// Testing, Thrift, and Config
libraryDependencies ++= Seq("org.apache.thrift" % "libthrift" % "0.9.0",
                            "com.typesafe" % "config" % "1.0.0",
                            "org.scalatest" %% "scalatest" % "1.6.1" % "test",
                            "org.clapper" %% "grizzled-slf4j" % "0.6.10",
                            "org.slf4j" % "slf4j-api" % "1.7.1",
                            "org.clapper" %% "avsl" % "0.4",
                            "org.scalaj" %% "scalaj-time" % "0.6",
                            "com.twitter" %% "finagle-thrift" % "6.0.5",
                            "com.twitter" %% "util-collection" % "5.3.10",
                            "com.twitter" %% "util-core" % "6.0.5")

testOptions in Test := Seq(Tests.Filter(s => s.endsWith("Suite")))

testOptions in Test += Tests.Setup( () => System.setProperty("org.clapper.avsl.config", "src/main/resources/avsl_test.conf"))

logBuffered in Test := false
