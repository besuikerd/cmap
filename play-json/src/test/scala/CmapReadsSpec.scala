import cats.implicits._
import com.besuikerd.cmap.integration.playjson.CmapReads
import org.scalatest.{EitherValues, FlatSpec, MustMatchers}
import play.api.libs.functional.syntax._
import play.api.libs.json._

class CmapReadsSpec extends FlatSpec with MustMatchers with EitherValues {
  implicit val addressWrites: Writes[Address] = Json.writes[Address]
  implicit val personWrite: Writes[Person]    = Json.writes[Person]
  import CmapReads._

  "CmapReads" should "wrap Reads to Cmap" in {

    import Implicits._

    val person     = Person("John", "Doe", 42, Address("Baker Street", 123))
    val personJson = personWrite.writes(person)

    implicit val addressFormat: Format[Address] = Json.format[Address]
    implicit val personFormat: Format[Person]   = Json.format[Person]

    val cmapPerson: CmapReads[Person] = implicitly

    val result = cmapPerson.runCmap(personJson)
    result mustBe Right(person)
  }

  it should "wrap JsPaths to Cmap" in {
    import Implicits._

    val person = Person("John", "Doe", 42, Address("Baker Street", 123))

    val personJson = personWrite.writes(person)

    implicit val cmapAddress: CmapReads[Address] =
      CmapReads(Address.curried)(
        fromPath(JsPath \ "street"),
        fromPath(JsPath \ "number")
      )

    val cmapPerson: CmapReads[Person] =
      CmapReads(Person.curried)(
        JsPath \ "name",
        JsPath \ "surname",
        JsPath \ "age",
        JsPath \ "address"
      )

    val result = cmapPerson.runCmap(personJson)
    result mustBe Right(person)
  }

  it should "collect multiple validation errors" in {
    import Implicits._
    val json = JsObject(
      Map(
        "key1" -> JsString("val1"),
        "key2" -> JsString("val2")
      ))

    val path1 = JsPath \ "wrongkey1"
    val path2 = JsPath \ "wrongkey2"

    val cmap = CmapReads.apply[String, String](
      path1,
      path2
    )

    val result = cmap.runCmap(json)

    result mustBe 'left
    val Left(errors) = result
    errors.size mustBe 2

    errors(0)._1 mustBe path1
    errors(0)._2.head.message mustBe "error.path.missing"

    errors(1)._1 mustBe path2
    errors(1)._2.head.message mustBe "error.path.missing"
  }

  it should "deserialize sequences of values" in {
    val addresses     = Seq(Address("Baker street", 123), Address("West street", 456))
    val addressesJson = Writes.seq[Address].writes(addresses)

    val cmap = CmapReads.sequence(fromReads(Json.reads[Address]))

    cmap.runCmap(addressesJson) mustBe Right(addresses)
  }

  it should "fail and collect errors on deserializing sequences" in {
    import Implicits._

    val elements     = Seq(1, 2, 3)
    val elementsJson = Writes.seq[Int].writes(elements)

    val cmap: CmapReads[List[String]] = implicitly

    val expectJsString = List(JsonValidationError("error.expected.jsstring"))

    cmap.runCmap(elementsJson) mustBe Left(
      List(
        (JsPath(0), expectJsString),
        (JsPath(1), expectJsString),
        (JsPath(2), expectJsString)
      ))
  }

  it should "deserialize recursive data structures with Option types" in {

    implicit val recWrites = Json.writes[Rec]
    val rec                = Rec(Some(Rec(None, None)), Some(Rec(None, None)))
    val recJson            = recWrites.writes(rec)

    implicit lazy val cmap: CmapReads[Rec] =
      CmapReads(Rec.curried)(
        fromPath(JsPath \ "left")(lazily(cmap)).optional,
        fromPath(JsPath \ "right")(lazily(cmap)).optional
      )

    cmap.runCmap(recJson) mustBe Right(rec)
  }

  it should "deserialize recursive data structures with List types" in {
    implicit val rec2Writes = Json.writes[Rec2]

    val rec2     = Rec2(List(Rec2(List.empty), Rec2(List.empty)))
    val rec2Json = rec2Writes.writes(rec2)

    implicit lazy val cmap: CmapReads[Rec2] =
      CmapReads(Rec2)(fromPath(JsPath \ "list")(lazily(CmapReads.sequence(cmap))))

    val result = cmap.runCmap(rec2Json)
    result mustBe Right(rec2)
  }

  it should "deserialize mutual recursive data structures" in {
    import Implicits._

    implicit lazy val mutualRec1Writes: Writes[MutualRec1] =
      (JsPath \ "mut2").lazyWriteNullable(mutualRec2Writes).contramap(unlift(MutualRec1.unapply))

    implicit lazy val mutualRec2Writes: Writes[MutualRec2] =
      (JsPath \ "mut1").lazyWriteNullable(mutualRec1Writes).contramap(unlift(MutualRec2.unapply))

    val mutualRec1     = MutualRec1(Some(MutualRec2(Some(MutualRec1(None)))))
    val mutualRec1Json = mutualRec1Writes.writes(mutualRec1)

    val mutualRec2     = MutualRec2(Some(MutualRec1(Some(MutualRec2(None)))))
    val mutualRec2Json = mutualRec2Writes.writes(mutualRec2)

    lazy implicit val mutualRec1Cmap: CmapReads[MutualRec1] =
      lazily[MutualRec2](JsPath \ "mut2").optional
        .map(MutualRec1)

    lazy implicit val mutualRec2Cmap: CmapReads[MutualRec2] =
      lazily[MutualRec1](JsPath \ "mut1").optional
        .map(MutualRec2)

    mutualRec1Cmap.runCmap(mutualRec1Json) mustBe Right(mutualRec1)
    mutualRec2Cmap.runCmap(mutualRec2Json) mustBe Right(mutualRec2)
  }

  it should "deserialize values that are both self recursive and mutual recursive" in {
    implicit lazy val mutualSelfWrites1: Writes[MutualSelf1] =
      (
        (JsPath \ "self").lazyWrite { Writes.seq(mutualSelfWrites1) } and
          (JsPath \ "mut2").lazyWrite { Writes.seq(mutualSelfWrites2) }
      )(unlift(MutualSelf1.unapply))

    implicit lazy val mutualSelfWrites2: Writes[MutualSelf2] =
      (
        (JsPath \ "self").lazyWrite { Writes.seq(mutualSelfWrites2) } and
          (JsPath \ "mut1").lazyWrite { Writes.seq(mutualSelfWrites1) }
      )(unlift(MutualSelf2.unapply))

    val mutualSelf1 = MutualSelf1(List(MutualSelf1(List.empty, List.empty)),
                                  List(MutualSelf2(List(MutualSelf2(List.empty, List.empty)), List.empty)))
    val mutualSelf1Json = mutualSelfWrites1.writes(mutualSelf1)

    val mutualSelf2 = MutualSelf2(List(MutualSelf2(List.empty, List.empty)),
                                  List(MutualSelf1(List(MutualSelf1(List.empty, List.empty)), List.empty)))
    val mutualSelf2Json = mutualSelfWrites2.writes(mutualSelf2)

    implicit lazy val mutualSelfCmap1: CmapReads[MutualSelf1] =
      CmapReads(MutualSelf1.curried)(
        fromPath(JsPath \ "self")(lazily { CmapReads.sequence(mutualSelfCmap1) }),
        fromPath(JsPath \ "mut2")(lazily { CmapReads.sequence(mutualSelfCmap2) })
      )

    implicit lazy val mutualSelfCmap2: CmapReads[MutualSelf2] =
      CmapReads(MutualSelf2.curried)(
        fromPath(JsPath \ "self")(lazily { CmapReads.sequence(mutualSelfCmap2) }),
        fromPath(JsPath \ "mut1")(lazily { CmapReads.sequence(mutualSelfCmap1) })
      )

    mutualSelfCmap1.runCmap(mutualSelf1Json) mustBe Right(mutualSelf1)
    mutualSelfCmap2.runCmap(mutualSelf2Json) mustBe Right(mutualSelf2)
  }
}
