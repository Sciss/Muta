package de.sciss.muta
package impl

trait MutationVecImpl[A, Global] extends BreedingFunction[Vec[A], Global] {
  type Chromosome = Vec[A]
  type Genome     = Vec[Chromosome]

  def apply(gen: Genome, num: Int, glob: Global, r: util.Random): Genome = Vec.fill(num) {
    val i   = r.nextInt(gen.size)
    val gi  = gen(i)
    val mut = r.nextInt(gi.size / 10 + 1 )
    (gi /: (1 to mut)) { (gj, _) =>
      val j = r.nextInt(gj.size)
      val m = mutate(gj(j))
      gj.updated(j, m)
    }
  }

  protected def mutate(gene: A): A
}