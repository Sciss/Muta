package de.sciss.muta

case class Settings[S <: Sys[S]](info: HeaderInfo /* [S] */, generation: Generation[S],
                    evaluation: Evaluation[S], selection: Selection[S], breeding: Breeding[S])