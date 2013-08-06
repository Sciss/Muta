package de.sciss.muta

//case class Generation[S <: Sys[S]](size: Int = 100, global: S#Global, seed: Int = 0) {
//  // def wholeDur = Rational(duration, 4)
//}

trait Generation[Chromosome, Global] extends (util.Random => Chromosome) {
  def size: Int
  def global: Global
  def seed: Int
}