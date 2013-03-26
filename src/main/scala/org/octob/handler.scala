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
        // TODO: Throw errors if user doesn't exist
        val termMap = mongo("users").findOne(MongoDBObject("_id" -> userId), MongoDBObject("terms" -> 1))
            .get.getAs[BasicDBObject]("terms").get
        val docs = mongo("tokens").find("_id" $in termMap.keySet().toArray())
            .map(_.getAs[Seq[Long]]("posts"))
            .flatten.flatten.toSet
        //logger.info(docs.map(docid => docid).toString)
        // TODO: Dot Product each of these to get weights and ordering
        Future.value(PostList(Option(0.5), docs.map( docid => Post(docid, Option(0.5))).toSeq ))
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
        logger.info("new post committed!")
        Future.value(true)
    }
}

