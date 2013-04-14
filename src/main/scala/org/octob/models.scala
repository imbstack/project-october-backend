package org.octob

import com.mongodb.casbah.Imports.{ MongoDB, WriteConcern }
import com.mongodb.casbah.query.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.global._
import com.novus.salat.dao._

case class MUser(@Key("_id") id: Long, tokens: Map[String, Long] = Map(), friends: Seq[Long] = List())

case class MToken(@Key("_id") id: String, df: Long, posts: Seq[Long] = List())

case class MPost(@Key("_id") id: Long, tokens: Map[String, Long] = Map())

object UserDAO extends SalatDAO[MUser, Long](collection = RecServer.mongo("users"))
object PostDAO extends SalatDAO[MPost, Long](collection = RecServer.mongo("posts"))
object TokenDAO extends SalatDAO[MToken, String](collection = RecServer.mongo("tokens"))
