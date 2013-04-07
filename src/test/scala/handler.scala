package org.octob.test 

import org.octob._

import com.mongodb.casbah.Imports._

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import com.twitter.util._
import october._

class RecHandlerSuite extends FunSuite with BeforeAndAfter {

    var handler: RecHandler = _
    var mongo: MongoDB = _

    before {
        mongo =  MongoClient()("october-test")
        handler = new RecHandler(mongo)
        handler.addUser(10l)
        handler.addUser(20l)
    }

    after {
        mongo.dropDatabase()
    }

    test("ping pongs when pinged") {
        assert(handler.ping.get === "Pong")
    }

    test("posts can be recommended") {
        handler.addPost(10l, 1l, Seq(Token("a", 1), Token("b", 23)))
        handler.addPost(10l, 2l, Seq(Token("b", 1), Token("c", 21), Token("e", 2)))
        handler.addPost(10l, 3l, Seq(Token("a", 3), Token("d", 3)))
        var postlist: Future[october.PostList] = handler.recPosts(10l)
        assert(postlist.get.posts.size > 0)
    }

    test("text search does something sane") {
        handler.addPost(10l, 1l, Seq(Token("a", 1), Token("b", 23)))
        handler.addPost(10l, 2l, Seq(Token("b", 1), Token("c", 21), Token("e", 2)))
        expect(Map(2 -> 2.1904761904761907)) {
            handler.textSearch(Seq("c", "e")).get
        }
    }

    test("user top terms gives the proper terms") {
        handler.addPost(10l, 2l, Seq(Token("b", 1), Token("c", 21), Token("e", 2)))
        expect(Map("c" -> 21)) {
            handler.userTopTerms(10l,1).get
        }
    }

    test("tokens can be added to users without a new post") {
        handler.addUserTerms(10l, Seq[String]("a", "b", "c", "g"))
        expect(Map("a" -> 1, "b" -> 1, "c" -> 1, "g" -> 1)) {
            handler.userTopTerms(10l,4).get
        }
    }

    test("posts can be submitted") {
        assert(handler.addPost(11l,4l,Seq(Token("a", 3))).get())
    }

    test("users can be created") {
        assert(handler.addUser(11l).get())
    }

    test("upvotes effect properly") {
        handler.addPost(10l, 4l, Seq(Token("a", 1), Token("b", 1)))
        handler.addPost(20l, 5l, Seq(Token("b", 1), Token("c", 1)))
        handler.userVpost(10l, october.Action.VoteUp, 5l)
        expect("{ \"a\" : 1 , \"b\" : 2 , \"c\" : 1}") {
            mongo("users").findOne(MongoDBObject("_id" -> 10l)).get("tokens").toString
        }
    }

    test("downvotes effect properly") {
        handler.addPost(10l, 4l, Seq(Token("a", 1), Token("b", 1)))
        handler.addPost(20l, 5l, Seq(Token("b", 1), Token("c", 1)))
        handler.userVpost(10l, october.Action.VoteDown, 5l)
        expect("{ \"a\" : 1 , \"b\" : 0 , \"c\" : -1}") {
            mongo("users").findOne(MongoDBObject("_id" -> 10l)).get("tokens").toString
        }
    }

    test("undo upvotes effect properly") {
        handler.addPost(10l, 4l, Seq(Token("a", 1), Token("b", 1)))
        handler.addPost(20l, 5l, Seq(Token("b", 1), Token("c", 1)))
        handler.userVpost(10l, october.Action.VoteUpNegate, 5l)
        expect("{ \"a\" : 1 , \"b\" : 0 , \"c\" : -1}") {
            mongo("users").findOne(MongoDBObject("_id" -> 10l)).get("tokens").toString
        }
    }

    test("undo downvotes effect properly") {
        handler.addPost(10l, 4l, Seq(Token("a", 1), Token("b", 1)))
        handler.addPost(20l, 5l, Seq(Token("b", 1), Token("c", 1)))
        handler.userVpost(10l, october.Action.VoteDownNegate, 5l)
        expect("{ \"a\" : 1 , \"b\" : 2 , \"c\" : 1}") {
            mongo("users").findOne(MongoDBObject("_id" -> 10l)).get("tokens").toString
        }
    }
}
