import com.besuikerd.cmap.rowmapping.RowMapping

object PersonMapping {
  import RowMapping._

  lazy val personMapping: RowMapping[Person] =
    RowMapping(Person.curried)(
      byName("name"),
      byName("surname"),
      byName("age"),
      addressMapping
    )

  lazy val addressMapping: RowMapping[Address] =
    RowMapping(Address.curried)(
      byName("street_name"),
      byPredicate(_.equalsIgnoreCase("street_number"))
    )
}
