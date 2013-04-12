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

// TODO: Add other persistence hook into constructor
class RecHandler(mongo: MongoDB) extends october.Recommender.FutureIface {
    private val logger = Logger.get(getClass)
    object UserDAO extends SalatDAO[MUser, Long](collection = mongo("users"))
    object PostDAO extends SalatDAO[MPost, Long](collection = mongo("posts"))
    object TokenDAO extends SalatDAO[MToken, String](collection = mongo("tokens"))

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
            case october.Action.Follow => UserDAO.update(MongoDBObject("_id" -> actionerId),
                                                         $push("friends" -> actioneeId),
                                                         true,
                                                         false)
            case _ => 
        }

        // TODO: make all of these return trues to a more functional thing
        Future.value(true)
    }

    // TODO: Consider using a top-k algorithm for this
    override def userTopTerms(userId: Long, limit: Int): Future[Map[String, Long]] = Future.value(
        UserDAO.findOneByID(id = userId).get.tokens.toList.sortBy{-_._2}.slice(0, limit).toMap)

    // TODO: Make the limit in this and recPosts work on the database level!
    override def textSearch(tokens: Seq[String], limit: Int): Future[Map[Long, Double]] = Future.value(
        searchInternal(tokens.map(x => (x -> 5l)).toMap).toList.sortBy{-_._2}.slice(0, limit).toMap)

    override def recPosts(userId: Long, limit: Int): Future[PostList] = {
        logger.info("posts requested!")
        // TODO: Throw errors if user doesn't exist
        val user: MUser = UserDAO.findOneByID(id = userId).get
        // TODO: Get all of the friends in one query rather than a bunch of findOnes
        val results: Map[Long, Double] = (searchInternal(user.tokens) /: user.friends) {
            case (a: Map[Long, Double], b: Long) => a ++ searchInternal(UserDAO.findOneByID(id = b).get.tokens).map {
                case ((a: Long, b: Double)) => (a -> 0.45 * b) // TODO: Make that scale not a magic value
            }
        }

        // TODO: Merge these into a list returned from friends!
        Future.value(PostList(Option(0.5), results.map(post => Post(post._1, Some(post._2))).toSeq.slice(0, limit)))
    }

    def searchInternal(tokens: Map[String, Long]): Map[Long, Double] = {
        val posts = TokenDAO.find(MongoDBObject("_id" -> MongoDBObject("$in" -> tokens.toArray.filter{_._2 > 2}.map(_._1))))
        val candidates: Set[Long] = posts.map(_.posts).flatten.toSet
        val tokenFreq: Map[String,Long] = posts.map { case (x: MToken) => x.id -> x.posts.size.toLong }.toMap
        val docCount: Long = mongo("posts").count()
        val qVec = Util.tfIdfVec(tokens, docCount, tokenFreq)

        // TODO: Don't get the posts twice?
        candidates.par.map {  (postId: Long) => {
            val temptokens = PostDAO.findOneByID(id = postId).getOrElse(MPost(id=0, tokens=Map[String,Long]())).tokens
            (postId -> Util.dotProduct(qVec,
                Util.tfIdfVec(
                    temptokens,
                    docCount,
                    temptokens.map {
                        case (x: (String, Long)) => x._1 -> TokenDAO.findOneByID(id=x._1).get.posts.size.toLong }.toMap
            )))}}.seq.toMap
    }

    override def userToPost(userId: Long, verb: october.Action, postId: Long) : Future[Boolean] = {
        logger.info("user did something to post")
        // TODO: Error stuff when things don't exist... maybe
        val uQuery = MongoDBObject("_id" -> userId)
        val post = PostDAO.findOneByID(id = postId).get
        var multiplier = verb match {
            case october.Action.VoteUp => 1
            case october.Action.VoteDown => -1
            case october.Action.VoteUpNegate => -1
            case october.Action.VoteDownNegate => 1
            case _ => 0
        }
        post.tokens.foreach { token =>
            UserDAO.update(uQuery, $inc("tokens.".concat(token._1) -> multiplier * token._2), false, true)
        }
        Future.value(true)
    }

    override def userToComment(userId: Long, verb: october.Action, commentId: Long) : Future[Boolean] = {
        logger.info("user did something to comment")
        Future.value(true)
    }

    override def addUser(userId: Long) : Future[Boolean] = {
        logger.info("new user!")
        val u = MUser(id=userId, tokens=Map[String,Long](), friends=Seq[Long]())
        UserDAO.insert(u, new WriteConcern(1))
        Future.value(true)
    }

    override def addPost(userId: Long, postId: Long, rawTokens: Seq[Token]) : Future[Boolean] = {
        logger.info("new post submitted!")
        val tokens: Map[String, Long] = (rawTokens map ((token:Token) => 
                token.t.filterNot((p:Char) => p == '.' || p == '$') -> token.f.toLong)).filter{_._2 > 2}.toMap

        // TODO: Start logging the id of the post (and other stuff too!

        PostDAO.insert(MPost(id=postId, tokens=tokens))
        val uQuery = MongoDBObject("_id" -> userId)
        tokens.par.map { token =>
            val query = MongoDBObject("_id" -> token._1)
            TokenDAO.update(query, $push("posts" -> postId), true, false)
            if (userId > 0) UserDAO.update(uQuery, $inc("tokens.".concat(token._1) -> token._2), false, true)
        }
        logger.info("new post committed!")
        Future.value(true)
    }
}

