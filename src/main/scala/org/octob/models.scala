package org.octob

import com.mongodb.casbah.Imports.{ MongoDB, WriteConcern }
import com.mongodb.casbah.query.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.global._
import com.novus.salat.dao._

case class MUser(@Key("_id") id: Long, tokens: Map[String, Long], friends: Seq[Long])

case class MToken(@Key("_id") id: String, df: Long, posts: Seq[Long])

case class MPost(@Key("_id") id: Long, tokens: Map[String, Long])
