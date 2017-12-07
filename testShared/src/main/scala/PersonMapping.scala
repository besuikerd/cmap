import com.besuikerd.cmap.Cmap
import com.besuikerd.cmap.rowmapping.RowMapping

object PersonMapping {
  import RowMapping._

  lazy val personMapping: RowMapping[Person] =
    RowMapping
      .success((Person.apply _).curried)
      .ap(byName("name"))
      .ap(byName("surname"))
      .ap(byName("age"))
      .ap(addressMapping)

  lazy val addressMapping: RowMapping[Address] =
    RowMapping
      .success((Address.apply _).curried)
      .ap(byName("street_name"))
      .ap(byPredicate(_.equalsIgnoreCase("street_number")))
}
