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

object UserDAO extends SalatDAO[MUser, Long](collection = RecServer.mongo("users")) {
    def create(userId: Long) {
        val u = MUser(id=userId)
        this.insert(u, new WriteConcern(1))
    }

    def follow(u1id: Long, u2id: Long) {
        this.update(MongoDBObject("_id" -> u1id), $push("friends" -> u2id), true, false)
    }

}

object PostDAO extends SalatDAO[MPost, Long](collection = RecServer.mongo("posts")) {
    def create(postId: Long, userId: Long, tokens: Map[String, Long]) {
        this.insert(MPost(id=postId, tokens=tokens))
        val uQuery = MongoDBObject("_id" -> userId)
        tokens.par.map { token =>
            val query = MongoDBObject("_id" -> token._1)
            TokenDAO.update(query, $push("posts" -> postId), true, false)
            TokenDAO.update(query, $inc("df" -> 1l), true, false)
            UserDAO.update(uQuery, $inc("tokens.".concat(token._1) -> token._2), false, true)
        }
    }

    def search(rawTokens: Map[String, Long]): Map[Long, Double] = {
        val fTokens = rawTokens.toArray.filter{_._2 > 1}.map(_._1)
        val uTokens = TokenDAO.find(MongoDBObject("_id" -> MongoDBObject("$in" -> fTokens)))
        val candidates = uTokens.map(_.posts).flatten.toSet
        val tokenMap = uTokens.map{x => x.id -> x.df}.toMap
        val postMap = this.find(MongoDBObject("_id" -> MongoDBObject("$in" -> candidates))).map{x => x.id -> x.tokens}.toMap
        val docCount = RecServer.mongo("posts").count()
        val uVec = Util.tfIdfVec(rawTokens, docCount, tokenMap)
        postMap.par.map {
            case (id: Long, pTokens: Map[String, Long]) => id -> Util.dotProduct(uVec, Util.tfIdfVec(pTokens, docCount, tokenMap))
        }.seq.toMap
    }
}

object TokenDAO extends SalatDAO[MToken, String](collection = RecServer.mongo("tokens"))
