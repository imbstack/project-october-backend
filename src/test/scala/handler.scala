package org.octob.test 

import org.octob._
import october._
import com.twitter.util._

import com.mongodb.casbah.Imports._

class RecHandlerSuite extends BaseTest {

    override def dobefore {
        RecHandler.addUser(10l)
        RecHandler.addUser(20l)
    }

    test("ping pongs when pinged") {
        assert(RecHandler.ping.get === "Pong")
    }

    test("posts can be recommended") {
        RecHandler.addPost(10l, 1l, Seq(Token("a", 1), Token("b", 23)))
        RecHandler.addPost(10l, 2l, Seq(Token("b", 1), Token("c", 21), Token("e", 2)))
        RecHandler.addPost(10l, 3l, Seq(Token("a", 3), Token("d", 3)))
        var postlist: Future[october.PostList] = RecHandler.recPosts(10l, 100, 0)
        assert(postlist.get.posts.size > 0)
    }

    test("text search does something sane") {
        RecHandler.addPost(10l, 1l, Seq(Token("a", 10), Token("b", 23)))
        RecHandler.addPost(10l, 2l, Seq(Token("b", 10), Token("c", 21), Token("e", 20)))
        expect(Map(2 -> 1.9523809523809523)) {
            RecHandler.textSearch(Seq("c", "e"), 100, 0).get
        }
    }

    test("search will skip results") {
        RecHandler.addPost(10l, 1l, Seq(Token("b", 10), Token("c", 21), Token("e", 20)))
        RecHandler.addPost(10l, 5l, Seq(Token("b", 5), Token("c", 25), Token("e", 7)))
        RecHandler.addPost(10l, 11l, Seq(Token("b", 7), Token("c", 51), Token("e", 8)))
        expect(Map(5 -> 1.28, 1 -> 1.9523809523809523)) {
            RecHandler.textSearch(Seq("c", "e"), 2, 1).get
        }
    }

    test("user top terms gives the proper terms") {
        RecHandler.addPost(10l, 2l, Seq(Token("b", 1), Token("c", 21), Token("e", 2)))
        expect(Map("c" -> 21)) {
            RecHandler.userTopTerms(10l,1).get
        }
    }

    test("tokens can be added to users without a new post") {
        RecHandler.addUserTerms(10l, Seq[String]("a", "b", "c", "g"))
        expect(Map("a" -> 100, "b" -> 100, "c" -> 100, "g" -> 100)) {
            RecHandler.userTopTerms(10l,4).get
        }
    }

    test("tokens can be removed from a user") {
        RecHandler.addUserTerms(10l, Seq[String]("a", "b", "c", "g"))
        RecHandler.removeUserTerms(10l, Seq[String]("b", "c"))
        expect(Map("a" -> 100, "g" -> 100)) {
            RecHandler.userTopTerms(10l,4).get
        }
    }

    test("users can follow each other") {
        RecHandler.userToUser(10l, october.Action.Follow, 20l)
        expect(Seq[Long](20)) {
            mongo("users").findOne(MongoDBObject("_id" -> 10l)).get.getAs[BasicDBObject]("friends").get
        }
    }

    test("posts can be submitted") {
        assert(RecHandler.addPost(11l,4l,Seq(Token("a", 3))).get())
    }

    test("users can be created") {
        assert(RecHandler.addUser(11l).get())
    }

    test("upvotes effect properly") {
        RecHandler.addPost(10l, 4l, Seq(Token("a", 10), Token("b", 10)))
        RecHandler.addPost(20l, 5l, Seq(Token("b", 10), Token("c", 10)))
        RecHandler.userToPost(10l, october.Action.VoteUp, 5l)
        expect("{ \"a\" : 10 , \"b\" : 20 , \"c\" : 10}") {
            mongo("users").findOne(MongoDBObject("_id" -> 10l)).get("tokens").toString
        }
    }

    test("downvotes effect properly") {
        RecHandler.addPost(10l, 4l, Seq(Token("a", 10), Token("b", 10)))
        RecHandler.addPost(20l, 5l, Seq(Token("b", 10), Token("c", 10)))
        RecHandler.userToPost(10l, october.Action.VoteDown, 5l)
        expect("{ \"a\" : 10 , \"b\" : 0 , \"c\" : -10}") {
            mongo("users").findOne(MongoDBObject("_id" -> 10l)).get("tokens").toString
        }
    }

    test("undo upvotes effect properly") {
        RecHandler.addPost(10l, 4l, Seq(Token("a", 10), Token("b", 10)))
        RecHandler.addPost(20l, 5l, Seq(Token("b", 10), Token("c", 10)))
        RecHandler.userToPost(10l, october.Action.VoteUpNegate, 5l)
        expect("{ \"a\" : 10 , \"b\" : 0 , \"c\" : -10}") {
            mongo("users").findOne(MongoDBObject("_id" -> 10l)).get("tokens").toString
        }
    }

    test("undo downvotes effect properly") {
        RecHandler.addPost(10l, 4l, Seq(Token("a", 10), Token("b", 10)))
        RecHandler.addPost(20l, 5l, Seq(Token("b", 10), Token("c", 10)))
        RecHandler.userToPost(10l, october.Action.VoteDownNegate, 5l)
        expect("{ \"a\" : 10 , \"b\" : 20 , \"c\" : 10}") {
            mongo("users").findOne(MongoDBObject("_id" -> 10l)).get("tokens").toString
        }
    }
}
