package de.sciss.muta

trait Selection[S <: Sys[S]] extends ((S#GenomeVal, util.Random) => S#Genome)