package org.octob.test 

import org.octob._

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.query.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.global._
import com.novus.salat.dao._

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import com.twitter.util._

class ModelSuite extends FunSuite with BeforeAndAfter {

    var mongo: MongoDB = _

    before {
        mongo =  MongoClient()("october-test")
    }

    after {
        mongo.dropDatabase()
    }

    test("user can be created") {
        val u = MUser(id = 0, tokens=Map[String,Long]())
    }

    test("users can be inserted") {
        object UserDAO extends SalatDAO[MUser, Long](collection = mongo("users"))
        val u = MUser(id=1, tokens=Map[String,Long]())
        UserDAO.insert(u)
        val user: MUser = UserDAO.findOneByID(id = 1).get
    }
}
