/*
 *  Breeding.scala
 *  (Muta)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU General Public License v2+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.muta

// trait Breeding[S <: System[S]] extends ((S#GenomeSel, S#Global, util.Random) => S#Genome)

trait Breeding[Chromosome, Global]
  extends ((Vec[(Chromosome, Double, Boolean)], Global, util.Random) => Vec[Chromosome])

trait BreedingFunction[Chromosome, Global] extends ((Vec[Chromosome], Int, Global, util.Random) => Vec[Chromosome])