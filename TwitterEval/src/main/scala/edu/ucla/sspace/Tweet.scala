package edu.ucla.sspace

import edu.ucla.sspace.similarity.SimilarityFunction
import edu.ucla.sspace.vector.CompactSparseVector
import edu.ucla.sspace.vector.SparseDoubleVector
import edu.ucla.sspace.vector.VectorMath

import scala.math.abs
import scala.math.pow


class Tweet(val timestamp: Long,
            val tokenVector: SparseDoubleVector,
            val neVector: SparseDoubleVector,
            val text: String) {

    def +(t: Tweet) = {
        VectorMath.add(tokenVector, t.tokenVector)
        VectorMath.add(neVector, t.neVector)
        Tweet(timestamp + t.timestamp, tokenVector, neVector, text)
    }

    def +?(t: Tweet) = {
        VectorMath.add(tokenVector, t.tokenVector)
        VectorMath.add(neVector, t.neVector)
        Tweet(timestamp, tokenVector, neVector, text)
    }

    def avgTime(n: Int) =
        Tweet(timestamp / n, tokenVector, neVector, text)
}

object Tweet {

    def apply() = new Tweet(0, new CompactSparseVector(), new CompactSparseVector(), "")

    def apply(ts: Long, tVec: SparseDoubleVector, neVec: SparseDoubleVector, text: String) =
        new Tweet(ts, tVec, neVec, text)

    def apply(t: Tweet) = {
        val tv = new CompactSparseVector(t.tokenVector.length)
        VectorMath.add(tv, t.tokenVector)
        val nv = new CompactSparseVector(t.neVector.length)
        VectorMath.add(nv, t.neVector)
        new Tweet(t.timestamp, tv, nv, t.text)
    }

    def sumsim(t1: Tweet, t2: Tweet,
               lambda: Double, beta: Double, weights: (Double, Double, Double),
               simFunc: SimilarityFunction) =
        weights._1*pow(lambda, abs(t1.timestamp - t2.timestamp)/beta) + // Time
        (if (weights._2 != 0d) weights._2*simFunc.sim(t1.tokenVector, t2.tokenVector) else 0d) + // Topic
        (if (weights._3 != 0d) weights._3*simFunc.sim(t1.neVector, t2.neVector) else 0d) // Named Entities

    def prodSim(t1: Tweet, t2: Tweet,
                lambda: Double, beta: Double, weights: (Double, Double, Double),
                simFunc: SimilarityFunction) =
        pow(lambda, abs(t1.timestamp - t2.timestamp)/beta) *
        simFunc.sim(t1.tokenVector, t2.tokenVector)

    def sim(t1: Tweet, t2: Tweet,
            lambda: Double, beta: Double, weights: (Double, Double, Double),
            simFunc: SimilarityFunction) =
        //prodSim(t1, t2, lambda, beta, weights, simFunc)
        sumsim(t1, t2, lambda, beta, weights, simFunc)

}
