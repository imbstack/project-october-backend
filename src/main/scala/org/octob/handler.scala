package org.octob

import october._

import com.mongodb.casbah.Imports._

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
        logger.info("new post!")
        // TODO: Do something with the userId here
        val docColl = mongo("documents")
        docColl += MongoDBObject("docid" -> postId, "df" -> rawTokens.map((token: Token) => token.t -> token.f))
        //val max = if (rawTokens.length > 0) rawTokens.map(_.f).reduceLeft (_ max _) else 0 // Valuable code for later, but not needed here
        for (token <- rawTokens) {
            // TODO: take each token and: 1. increment the document frequency of that token 2. calculate tf/idf and stick it in the docment
            // value with that token as key
        }
        Future.value(true)
    }
}

