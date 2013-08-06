package de.sciss.muta

// trait Breeding[S <: Sys[S]] extends ((S#GenomeSel, S#Global, util.Random) => S#Genome)

trait Breeding[Chromosome, Global]
  extends ((Vec[(Chromosome, Double, Boolean)], Global, util.Random) => Vec[Chromosome])

trait BreedingFunction[Chromosome, Global] extends ((Vec[Chromosome], Int, Global, util.Random) => Vec[Chromosome])