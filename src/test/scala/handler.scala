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
        handler.addPost(10l, 1l, Seq(Token("a", 1), Token("b", 23)))
    }

    after {
        mongo.dropDatabase()
    }

    test("ping pongs when pinged") {
        assert(handler.ping.get === "Pong")
    }

    test("posts can be recommended") {
        handler.addPost(10l, 2l, Seq(Token("b", 1), Token("c", 21), Token("e", 2)))
        handler.addPost(10l, 3l, Seq(Token("a", 3), Token("d", 3)))
        var postlist: Future[october.PostList] = handler.recPosts(10l)
        assert(postlist.get.posts.size > 0)
    }

    test("posts can be submitted") {
        assert(handler.addPost(11l,4l,Seq(Token("a", 3))).get())
    }

    test("users can be created") {
        assert(handler.addUser(11l).get())
    }
}
