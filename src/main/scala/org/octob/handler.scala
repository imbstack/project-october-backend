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

    // TODO: Consider using a top-k algorithm for this
    override def userTopTerms(userId: Long, limit: Int): Future[Map[String, Long]] = Future.value(
        UserDAO.findOneByID(id = userId).get.tokens.toList.sortBy{-_._2}.slice(0, limit).toMap)

    override def textSearch(tokens: Seq[String]): Future[Map[Long, Double]] = Future.value(searchInternal(tokens.map(x => (x -> 1l)).toMap))

    override def recPosts(userId: Long): Future[PostList] = {
        logger.info("posts requested!")
        // TODO: Throw errors if user doesn't exist
        val user: MUser = UserDAO.findOneByID(id = userId).get
        val results: Map[Long, Double] = searchInternal(user.tokens)

        // TODO: Merge these into a list returned from friends!
        Future.value(PostList(Option(0.5), results.map(post => Post(post._1, Some(post._2))).toSeq))
    }

    def searchInternal(tokens: Map[String, Long]): Map[Long, Double] = {
        val posts = TokenDAO.find(MongoDBObject("_id" -> MongoDBObject("$in" -> tokens.keySet.toArray)))
        val candidates: Set[Long] = posts.map(_.posts).flatten.toSet
        val tokenFreq: Map[String,Long] = posts.map { case (x: MToken) => x.id -> x.posts.size.toLong }.toMap
        val docCount: Long = mongo("posts").count()
        val qVec = Util.tfIdfVec(tokens, docCount, tokenFreq)

        // TODO: Don't get the posts twice?
        candidates.map {  (postId: Long) => postId ->
            Util.dotProduct(qVec,
                Util.tfIdfVec(
                    PostDAO.findOneByID(id = postId).getOrElse(MPost(id=0, tokens=Map[String,Long]())).tokens,
                    docCount,
                    PostDAO.findOneByID(id = postId).getOrElse(MPost(id=0, tokens=Map[String,Long]())).tokens.map {
                        case (x: (String, Long)) => x._1 -> TokenDAO.findOneByID(id=x._1).get.posts.size.toLong }.toMap
            ))}.toMap
    }

    override def userVpost(userId: Long, verb: october.Action, postId: Long) : Future[Boolean] = {
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

    override def userVcomment(userId: Long, verb: october.Action, commentId: Long) : Future[Boolean] = {
        logger.info("user did something to comment")
        Future.value(true)
    }

    override def addUser(userId: Long) : Future[Boolean] = {
        logger.info("new user!")
        val u = MUser(id=userId, tokens=Map[String,Long]())
        UserDAO.insert(u, new WriteConcern(1))
        Future.value(true)
    }

    override def addPost(userId: Long, postId: Long, rawTokens: Seq[Token]) : Future[Boolean] = {
        logger.info("new post submitted!")
        val tokens: Map[String, Long] = (rawTokens map ((token:Token) => 
                token.t.filterNot((p:Char) => p == '.' || p == '$') ->token.f.toLong)).toMap

        // TODO: Start logging the id of the post (and other stuff too!

        PostDAO.insert(MPost(id=postId, tokens=tokens))
        val uQuery = MongoDBObject("_id" -> userId)
        for (token <- tokens) {
            val query = MongoDBObject("_id" -> token._1)
            TokenDAO.update(query, $push("posts" -> postId), true, false)
            if (userId > 0) UserDAO.update(uQuery, $inc("tokens.".concat(token._1) -> token._2), false, true)
        }
        logger.info("new post committed!")
        Future.value(true)
    }
}

