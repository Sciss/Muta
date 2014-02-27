/*
 *  Settings.scala
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

object Settings {
  def apply(sys: System)(info: HeaderInfo, generation: sys.Generation, evaluation: sys.Evaluation,
                      selection: sys.Selection, breeding: sys.Breeding): Settings { type S = sys.type } = {
    val _info       = info
    val _generation = generation
    val _evaluation = evaluation
    val _selection  = selection
    val _breeding   = breeding

    new Settings {
      type S          = sys.type
      val system: S   = sys
      val info        = _info
      val generation  = _generation
      val evaluation  = _evaluation
      val selection   = _selection
      val breeding    = _breeding
    }
  }

  // def unapply(s: Settings)
}
trait Settings {
  type S <: System
  val system: S

  def info      : HeaderInfo
  def generation: system.Generation
  def evaluation: system.Evaluation
  def selection : system.Selection
  def breeding  : system.Breeding
}
