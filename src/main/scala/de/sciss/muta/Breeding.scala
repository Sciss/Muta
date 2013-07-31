package de.sciss.muta

trait Breeding[S <: Sys[S]] extends ((S#GenomeSel, S#Global, util.Random) => S#Genome)
