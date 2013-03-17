package org.octob.test 

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import com.twitter.util._
import com.october._

import com.tinkerpop.blueprints._
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion
import com.thinkaurelius.titan.core._

import com.twitter.cassie._
import com.twitter.finagle.stats.NullStatsReceiver

class RecHandlerSuite extends FunSuite with BeforeAndAfter {

    var handler: RecHandler = _

    before {
        val g: TitanGraph = TitanFactory.open("/tmp/octoborg/")
        val cass = new Cluster("nonhost")
        val p: Keyspace = cass.keyspace("posts").connect()
        handler = new RecHandler(g, p)
    }

    test("ping pongs when pinged") {
        assert(handler.ping.get === "Pong")
    }

    test("placeholder posts are returned with proper weights") {
        var postlist: october.PostList = handler.recPosts(0).get
        assert(postlist.posts.length === 10) // This is hardcoded in for now
        assert(postlist.posts.head.weight.get === 1.0) // This is hardcoded in for now
        assert(postlist.posts.last.weight.get === 0.1) // This is hardcoded in for now
    }

    test("posts can be submitted") {
        assert(handler.addPost(0,0,Seq()).get())
    }

    test("users can be created") {
        assert(handler.addUser(0).get())
    }
}
