package de.sciss.muta

/** Selects an absolute number of individuals
  *
  * @param value  the number of individuals to select
  */
case class SelectionNumber(value: Int = 10) extends SelectionSize {
  require(value > 0)
  override def apply(pop: Int): Int = math.min(pop, value)
}
/** Selects the number of individuals corresponding to
  * a given percentage of the total population.
  *
  * @param value  the percentage value ranging from 0 to 100
  */
case class SelectionPercent(value: Int = 20) extends SelectionSize {
  require(value >= 0 && value <= 100)
  override def apply(pop: Int): Int = pop * value / 100
}

sealed trait SelectionSize extends (Int => Int)