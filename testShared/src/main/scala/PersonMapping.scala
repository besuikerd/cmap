import com.besuikerd.cmap.rowmapping.RowMapping

object PersonMapping {
  import RowMapping._

  def personMapping: RowMapping[Person] =
    RowMapping
      .success((Person.apply _).curried)
      .ap(byName("name"))
      .ap(byName("surname"))
      .ap(byName("age"))
      .ap(addressMapping)

  lazy val addressMapping: RowMapping[Address] =
    RowMapping.success((Address.apply _).curried) ap byName[String]("street_name") ap byName[Int]("street_number")
}
