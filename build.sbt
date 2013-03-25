name := "october"

version := "0.1.0"

scalaVersion := "2.9.2"

// MongoDB
libraryDependencies ++= Seq("org.mongodb" %% "casbah" % "2.5.0")

// Testing, Thrift, and Config
libraryDependencies ++= Seq("org.apache.thrift" % "libthrift" % "0.9.0",
                            "com.typesafe" % "config" % "1.0.0",
                            "org.scalatest" %% "scalatest" % "1.6.1" % "test",
                            "com.twitter" %% "finagle-thrift" % "6.0.5",
                            "com.twitter" %% "util-collection" % "5.3.10",
                            "com.twitter" %% "util-core" % "6.0.5")
