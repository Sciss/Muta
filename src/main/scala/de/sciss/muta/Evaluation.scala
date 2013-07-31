package de.sciss.muta

trait Evaluation[S <: Sys[S]] extends (S#Chromosome => Double)