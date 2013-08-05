package de.sciss.muta

case class Settings[Chromosome, Global](info: HeaderInfo /* [S] */, generation: Generation[Global],
                    evaluation: Evaluation[Chromosome], selection: Selection[Chromosome],
                    breeding: Breeding[Chromosome, Global])