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
     * Takes a vector and a map of terms to calculate the TF-IDF for each term in the vector
     *
     * @param vec, the vector to operate on
     * @param docCount, the number of documents submitted
     * @param countMap, a map of term to number of documents containing that term
     */
    def tfIdfVec(vec: Map[String,Long], docCount: Long, countMap: Map[String,Long]): Map[String,Double] =
        vec.map {case (s: String, v: Long) =>
            s -> (v.toDouble / (0l /: vec) { case (a: Long, (k: String, v: Long)) =>
                (if (a > v) a else v)}) * (docCount.toDouble / countMap.getOrElse(s, docCount))}
}
