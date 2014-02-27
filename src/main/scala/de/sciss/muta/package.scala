/*
 *  package.scala
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

package de.sciss

package object muta {
  val  Vec      = collection.immutable.IndexedSeq
  type Vec[+A]  = collection.immutable.IndexedSeq[A]
}
