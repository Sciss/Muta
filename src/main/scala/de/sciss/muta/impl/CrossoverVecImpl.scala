package de.sciss.muta
package impl

/** Implements a single-point cross over between two chromosomes. */
trait CrossoverVecImpl[A, Global] extends BreedingFunction[Vec[A], Global] {
  type Chromosome = Vec[A]
  type Genome     = Vec[Chromosome]

  def apply(gen: Genome, num: Int, glob: Global, r: util.Random): Genome = Vec.fill(num) {
    val i   = r.nextInt(gen.size)
    val j   = r.nextInt(gen.size)
    val gi  = gen(i)
    val gj  = gen(j)
    val szi = gi.size
    val szj = gj.size
    val len = (szi + szj) / 2
    val li  = r.nextInt(math.min(len, szi))
    val lj  = math.min(szj, len - li)
    gi.take(li) ++ gj.drop(szj - lj)
  }
}
