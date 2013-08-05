package de.sciss.muta

trait Selection[Chromosome] extends ((Vec[(Chromosome, Double)], util.Random) => Vec[Chromosome])