/*
 *  MutationVecImpl.scala
 *  (Muta)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.muta
package impl

/** Implements a mutation function based on a given number of mutations per chromosome and a gene mutation function. */
trait MutationVecImpl[A, Global] extends BreedingFunction[Vec[A], Global] {
  type Chromosome = Vec[A]
  type Genome     = Vec[Chromosome]

  def apply(gen: Genome, num: Int, glob: Global, r: util.Random): Genome = Vec.fill(num) {
    val i   = r.nextInt(gen.size)
    val gi  = gen(i)
    val mut = numGenes(gi)(r)
    (gi /: (1 to mut)) { (gj, _) =>
      val j = r.nextInt(gj.size)
      val m = mutate(gj(j))(r)
      gj.updated(j, m)
    }
  }

  /** This size is chosen if `numGenes` is not overriden. Defaults to 10 percent. */
  protected val numGenesSize: SelectionSize = SelectionPercent(10)

  /** The number of genes to mutate, given a particular chromosome. Defaults to
    * applying `numGenesSize` with the chromosome size.
    */
  protected def numGenes(chromosome: Chromosome)(implicit random: util.Random): Int =
    random.nextInt(numGenesSize(chromosome.size))

  protected def mutate(gene: A)(implicit random: util.Random): A
}