name := "october"

version := "0.0.1"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq("org.apache.thrift" % "libthrift" % "0.9.0",
                            "com.twitter" %% "finagle-thrift" % "6.0.5",
                            "com.twitter" %% "util-collection" % "5.3.10",
                            "com.twitter" %% "util-core" % "6.0.5")
