import cats.Functor
import com.besuikerd.cmap.typeclass.{FAlgebra, Fix, IdCatamorphism}
import org.scalatest.{FlatSpec, MustMatchers}

class CatamorphismSpec extends FlatSpec with MustMatchers {
  "Natural numbers" should "Be a catamorphism" in {
    sealed trait Nat[N]
    object Nat {
      def suc(n: Fix[Nat]): Fix[Nat] = Fix(Suc(n))
      val zero: Fix[Nat]             = Fix.apply[Nat](Zero())

      implicit object NatFunctor extends Functor[Nat] {
        override def map[A, B](fa: Nat[A])(f: A => B): Nat[B] = fa match {
          case Suc(suc) => Suc(f(suc))
          case Zero()   => Zero()
        }
      }
    }

    case class Suc[N](suc: N) extends Nat[N]
    case class Zero[N]()      extends Nat[N]

    object NatCatamorphism extends IdCatamorphism[Nat]

    import Nat._

    val num: FAlgebra[Nat, Int] = {
      case Suc(n) => n + 1
      case Zero() => 0
    }

    val one   = suc(zero)
    val two   = suc(one)
    val three = suc(two)

    NatCatamorphism.cata(num)(three) mustBe 3
  }

  "Binary Tree" should "Be a catamorphism" in {

    sealed trait BinaryTreeF[+T]
    case class NodeF[T](left: T, right: T) extends BinaryTreeF[T]
    case class TailF[T]()                  extends BinaryTreeF[T]

    type BinaryTree = Fix[BinaryTreeF]
    object BinaryTree {
      def node(left: BinaryTree, right: BinaryTree): BinaryTree = Fix(NodeF(left, right))
      def leaf: BinaryTree                                      = Fix.apply[BinaryTreeF](TailF())
    }

    val treeDepth: FAlgebra[BinaryTreeF, Int] = {
      case NodeF(left, right) => Math.max(left, right) + 1
      case TailF()            => 0
    }

    val nodeCount: FAlgebra[BinaryTreeF, Int] = {
      case NodeF(left, right) => left + right + 1
      case TailF()            => 1
    }

    implicit object BinaryTreeFunctor extends Functor[BinaryTreeF] {
      override def map[A, B](fa: BinaryTreeF[A])(f: A => B) = fa match {
        case NodeF(left, right) => NodeF(f(left), f(right))
        case TailF()            => TailF()
      }
    }
    object BinaryTreeCatamorphism extends IdCatamorphism[BinaryTreeF]

    import BinaryTree._

    val tree1 = node(
      node(leaf, leaf),
      node(leaf, leaf)
    )

    BinaryTreeCatamorphism(treeDepth)(tree1) mustBe 2
    BinaryTreeCatamorphism(nodeCount)(tree1) mustBe 7

    val tree2 = node(tree1, tree1)
    BinaryTreeCatamorphism(treeDepth)(tree2) mustBe 3
    BinaryTreeCatamorphism(nodeCount)(tree2) mustBe 15

  }
}
