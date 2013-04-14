package org.octob

import october._

import com.mongodb.casbah.Imports.{ MongoDB, WriteConcern }
import com.mongodb.casbah.query.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.global._
import com.novus.salat.dao._

import com.twitter.util._
import com.twitter.logging.Logger

object RecHandler extends october.Recommender.FutureIface {
    private val logger = Logger.get(getClass)

    override def ping(): Future[String] = {
        logger.info("Ping received.")
        Future.value("Pong")
    }

    override def addUserTerms(userId: Long, terms: Seq[String]): Future[Boolean] = {
        val uQuery = MongoDBObject("_id" -> userId)
        for (term <- terms) {
            UserDAO.update(uQuery, $inc("tokens.".concat(term.filterNot((p:Char) => p == '.' || p == '$')) -> 100l), false, true)
        }
        Future.value(true)
    }

    override def userToUser(actionerId: Long, action: october.Action, actioneeId: Long): Future[Boolean] = {
        action match {
            case october.Action.Follow => UserDAO.follow(actionerId, actioneeId)
            case _ => 
        }
        Future.value(true)
    }

    // TODO: Consider using a top-k algorithm for this
    override def userTopTerms(userId: Long, limit: Int): Future[Map[String, Long]] = Future.value(
        UserDAO.findOneByID(id = userId).get.tokens.toList.sortBy{-_._2}.slice(0, limit).toMap)

    // TODO: Make the limit in this and recPosts work on the database level!
    override def textSearch(tokens: Seq[String], limit: Int): Future[Map[Long, Double]] = Future.value(
        PostDAO.search(tokens.map(x => (x -> 5l)).toMap).toList.sortBy{-_._2}.slice(0, limit).toMap)

    override def recPosts(userId: Long, limit: Int): Future[PostList] = {
        logger.info("posts requested")
        val t0 = System.nanoTime()
        val user: MUser = UserDAO.findOneByID(id = userId).get
        // TODO: Get all of the friends in one query rather than a bunch of findOnes
        val results: Map[Long, Double] = (PostDAO.search(user.tokens) /: user.friends) {
            case (a: Map[Long, Double], b: Long) => a ++ PostDAO.search(UserDAO.findOneByID(id = b).get.tokens).map {
                case ((a: Long, b: Double)) => (a -> 0.45 * b) // TODO: Make that scale not a magic value
            }
        }

        logger.info("posts returned in "+(System.nanoTime() - t0)+"ns ("+(System.nanoTime()-t0)/1000000000.0+" seconds)")
        Future.value(PostList(Option(0.5), results.map(post => Post(post._1, Some(post._2))).toSeq.slice(0, limit)))
    }

    override def userToPost(userId: Long, verb: october.Action, postId: Long) : Future[Boolean] = {
        logger.info("user did something to post")
        val uQuery = MongoDBObject("_id" -> userId)
        val post = PostDAO.findOneByID(id = postId).get
        var multiplier = verb match {
            case october.Action.VoteUp => 1.0
            case october.Action.VoteDown => -1.0
            case october.Action.VoteUpNegate => -1.0
            case october.Action.VoteDownNegate => 1.0
            case _ => 0
        }
        post.tokens.foreach { token =>
            UserDAO.update(uQuery, $inc("tokens.".concat(token._1) -> (multiplier * token._2).toLong), false, true)
        }
        Future.value(true)
    }

    override def userToComment(userId: Long, verb: october.Action, commentId: Long) : Future[Boolean] = {
        logger.info("user did something to comment")
        Future.value(true)
    }

    override def addUser(userId: Long) : Future[Boolean] = {
        logger.info("new user!")
        UserDAO.create(userId)
        Future.value(true)
    }

    override def addPost(userId: Long, postId: Long, rawTokens: Seq[Token]) : Future[Boolean] = {
        logger.info("new post submitted!")
        val tokens: Map[String, Long] = (rawTokens map ((token:Token) => 
                token.t.filterNot((p:Char) => p == '.' || p == '$') -> token.f.toLong)).filter{_._2 > 2}.toMap

        // TODO: Start logging the id of the post (and other stuff too!
        PostDAO.create(postId, userId, tokens)

        logger.info("new post committed!")
        Future.value(true)
    }
}

