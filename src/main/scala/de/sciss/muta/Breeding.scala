package de.sciss.muta

// trait Breeding[S <: Sys[S]] extends ((S#GenomeSel, S#Global, util.Random) => S#Genome)

case class Breeding[Chromosome, Global](elitism        : SelectionSize     = SelectionNumber(5),
                              crossoverWeight: SelectionPercent  = SelectionPercent(80),
                              crossover      : BreedingFunction[Chromosome, Global],
                              mutation       : BreedingFunction[Chromosome, Global])
  extends ((Vec[(Chromosome, Double, Boolean)], Global, util.Random) => Vec[Chromosome]) {

  override def apply(g: Vec[(Chromosome, Double, Boolean)], global: Global, r: util.Random): Vec[Chromosome] = {
    val szOut = g.size
    val szEl  = elitism(szOut)
    val out1  = if (szEl == 0) Vec.empty else {
      // ensure that elite choices are distinct (don't want to accumulate five identical chromosomes over time)!
      val eliteCandidates = g.map { case (c, f, _) => (c, f) } .distinct.sortBy(-_._2).map(_._1)
      eliteCandidates.take(szEl)
    }
    val szBr  = szOut - out1.size
    val szX   = crossoverWeight(szBr)
    val szMut = szBr - szX
    val sel   = g.collect {
      case (c, _, true) => c
    }
    val out2  = if (szX   == 0) out1 else out1 ++ crossover(sel, szX  , global, r)
    val out3  = if (szMut == 0) out2 else out2 ++ mutation (sel, szMut, global, r)
    out3
  }
}

trait BreedingFunction[Chromosome, Global] extends ((Vec[Chromosome], Int, Global, util.Random) => Vec[Chromosome])