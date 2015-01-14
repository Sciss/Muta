/*
 *  Breeding.scala
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

/** The breeding algorithm is given the current genome (selected and unselected chromosomes),
  * along with their fitness values and selection flag (`true` meaning a chromosome
  * was selected by the selection algorithm). It must return a new genome the
  * population size of which should match the input genome.
  */
trait Breeding[Chromosome, Global]
  extends ((Vec[(Chromosome, Double, Boolean)], Global, util.Random) => Vec[Chromosome])

/** The function is passed the genome selection, the target number of chromosomes to produce,
  * the global settings and a random-number-generator.
  */
trait BreedingFunction[Chromosome, Global] extends ((Vec[Chromosome], Int, Global, util.Random) => Vec[Chromosome])