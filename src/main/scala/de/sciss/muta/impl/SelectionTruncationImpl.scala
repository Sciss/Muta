package de.sciss.muta
package impl

/** A truncation type selection algorithm. Cf. http://en.wikipedia.org/wiki/Truncation_selection */
trait SelectionTruncationImpl[Chromosome] extends Selection[Chromosome] {
  def size: SelectionSize

  def apply(pop: Vec[(Chromosome, Double)], r: util.Random): Vec[Chromosome] = {
    val n       = size(pop.size)
    val sorted  = pop.sortBy(_._2)
    sorted.takeRight(n).map(_._1)
  }
}
