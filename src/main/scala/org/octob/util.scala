package org.octob


object Util {

    /**
     * Dot products two vectors of terms
     * 
     * @param user, the idf'd vector representing the user
     * @param post, the idf'd vector representing the post
     */
    def dotProduct(user: Map[String,Double], post: Map[String,Double]): Double = (0.0 /: user) {
        case (a: Double, (k: String, v: Double)) => a + (v * post.getOrElse(k, 0.0))
    }

    /**
     * Takes a vector and a map of terms to calculate the IDF for each term in the vector
     *
     * @param vec, the vector to operate on
     * @param dfs, the map of terms to their global frequency
     */
    def idfVec(vec: Map[String,Long], dfs: Map[String,Long]): Map[String,Double] = {
        Map("a" -> 0.0)
    }
}
