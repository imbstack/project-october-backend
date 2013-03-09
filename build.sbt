name := "october"

version := "0.1.0"

scalaVersion := "2.9.2"

// Need to add this repository for BerkeleyDB dep of titan
resolvers += "Oracle Repository" at "http://download.oracle.com/maven"

// TitanDB deps
libraryDependencies ++= Seq("com.thinkaurelius.titan" % "titan" % "0.2.0")

// Testing, Thrift, and Config
libraryDependencies ++= Seq("org.apache.thrift" % "libthrift" % "0.9.0",
                            "com.typesafe" % "config" % "1.0.0",
                            "org.scalatest" %% "scalatest" % "1.6.1" % "test",
                            "com.twitter" %% "finagle-thrift" % "6.0.5",
                            "com.twitter" %% "util-collection" % "5.3.10",
                            "com.twitter" %% "util-core" % "6.0.5")
