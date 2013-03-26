package org.octob

import october._

import com.mongodb.casbah.Imports.{ MongoDB, WriteConcern }
import com.mongodb.casbah.query.Imports._

import com.twitter.util._
import com.twitter.logging.Logger

// TODO: Add other persistence hook into constructor
class RecHandler(mongo: MongoDB) extends october.Recommender.FutureIface {
    private val logger = Logger.get(getClass)

    override def ping(): Future[String] = {
        logger.info("Ping received.")
        Future.value("Pong")
    }

    override def recPosts(userId: Long): Future[PostList] = {
        logger.info("posts requested!")
        Future.value(PostList(Option(0.5),
            for (i <- 1 until 11) yield Post(i, Option(1.0/i))))
    }

    override def userVPost(userId: Long, verb: october.Action, postId: Long) : Future[Unit] = {
        logger.info("user did something to post")
        Future.value(false)
    }

    override def userVComment(userId: Long, verb: october.Action, commentId: Long) : Future[Unit] = {
        logger.info("user did something to comment")
        Future.value(false)
    }

    override def addUser(userId: Long) : Future[Boolean] = {
        logger.info("new user!")
        Future.value(true)
    }

    override def addPost(userId: Long, postId: Long, rawTokens: Seq[Token]) : Future[Boolean] = {
        logger.info("new post submitted!")
        val tokens = rawTokens map ((token:Token) => token.t.filterNot((p:Char) => p == '.' || p == '$') -> token.f)
        mongo("posts") += MongoDBObject("_id" -> postId, "tf" -> tokens)

        val tokColl = mongo("tokens")
        val userColl = mongo("users")
        val uQuery = MongoDBObject("_id" -> userId)
        for (token <- tokens) {
            val query = MongoDBObject("_id" -> token._1)
            // TODO: Maybe make this one operation?
            tokColl.update(query, $inc("df" -> token._2), true, false)
            tokColl.update(query, $push("posts" -> postId), true, false)
            userColl.findAndModify(uQuery, MongoDBObject("terms" -> 1), MongoDBObject("_id" -> 1),
                false, $inc("terms.".concat(token._1) -> token._2), false, true)
        }
        //val max = if (rawTokens.length > 0) rawTokens.map(_.f).reduceLeft (_ max _) else 0 // Valuable code for later, but not needed here
        logger.info("new post committed!")
        Future.value(true)
    }
}

