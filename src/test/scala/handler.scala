package org.octob.test 

import org.octob._

import com.foursquare.fongo.Fongo
import com.mongodb.casbah.Imports._

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import com.twitter.util._
import october._

class RecHandlerSuite extends FunSuite with BeforeAndAfter {

    var handler: RecHandler = _

    before {
        val fongo = new Fongo("mongoserver")
        val mongoClient =  MongoClient(fongo.getServerAddress())("october")
        handler = new RecHandler(mongoClient)
    }

    test("ping pongs when pinged") {
        assert(handler.ping.get === "Pong")
    }

    //test("placeholder posts are returned with proper weights") {
        //var postlist: Future[october.PostList] = handler.recPosts(0)
        //assert(postlist.posts.length === 10) // This is hardcoded in for now
        //assert(postlist.posts.head.weight.get === 1.0) // This is hardcoded in for now
        //assert(postlist.posts.last.weight.get === 0.1) // This is hardcoded in for now
    //}

    test("posts can be submitted") {
        assert(handler.addPost(0,0,Seq()).get())
    }

    test("users can be created") {
        assert(handler.addUser(0).get())
    }
}
