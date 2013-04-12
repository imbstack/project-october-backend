package org.octob

import org.joda.time.DateTime

import com.mongodb.casbah.Imports.{ MongoDB, WriteConcern }
import com.mongodb.casbah.query.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.global._
import com.novus.salat.dao._

object UserDAO extends SalatDAO[MUser, Long](collection = RecServer.mongo("users")) {

    class UserTokenCollection(coll: MongoCollection, parentId: String)
        extends ChildCollection[MUserToken, ObjectId](coll, parentId)
    class UserUserCollection(coll: MongoCollection, parentId: String)
        extends ChildCollection[MUserUser, ObjectId](coll, parentId)

    val usertoken = UserTokenCollection(coll = RecServer.mongo("usertoken"), parentId = "userId")
    val useruser = UserUserCollection(coll = RecServer.mongo("friends"), parentId = "userId")
}

object PostDAO extends SalatDAO[MPost, Long](collection = RecServer.mongo("posts")) {

    class PostTokenCollection(coll: MongoCollection, parentId: String)
        extends ChildCollection[MPostToken, ObjectId](coll, parentId)

    val posttoken = PostTokenCollection(coll = RecServer.mongo("posttoken"), parentId = "postId")
}

object TokenDAO extends SalatDAO[MToken, String](collection = RecServer.mongo("tokens"))

case class MUser(@Key("_id") id: Long)

case class MUserToken(@Key("_id") id: ObjectId = new ObjectId,
    userId: ObjectId,
    tokenId: ObjectId)

case class MPostToken(@Key("_id") id: ObjectId = new ObjectId,
    postId: ObjectId,
    tokenId: ObjectId)

case class MUserUser(@Key("id") id: ObjectId = new ObjectId,
    followerId: ObjectId,
    followeeId: ObjectId)

case class MToken(@Key("_id") id: String)

case class MPost(@Key("_id") id: Long,
    submitted: DateTime = DateTime.now())
