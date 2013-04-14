package org.octob.test 

import org.octob._

import com.mongodb.casbah.query.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.global._
import com.novus.salat.dao._

class ModelSuite extends BaseTest {

    test("user can be created") {
        val u = MUser(id = 0, tokens=Map[String,Long](), friends=Seq[Long]())
        val u2 = MUser(id = 1)
    }

    test("users can be inserted") {
        object UserDAO extends SalatDAO[MUser, Long](collection = mongo("users"))
        val u = MUser(id=1, tokens=Map[String,Long](), friends=Seq[Long]())
        UserDAO.insert(u)
        val user: MUser = UserDAO.findOneByID(id = 1).get
    }
}
