[![build_badge](https://circleci.com/gh/besuikerd/cmap.svg?&style=shield)](https://circleci.com/gh/besuikerd/cmap)

# Cmap

Static context parser combinators for scala

## Usage 

Parsing data structures with a specific context is a common use case.
Keeping this code understandable can be challenging.
Cmap allows you to create composable parsers.
The base trait wrap a parsing function under a certain context.

```scala
trait Cmap[Context, Error, T] {
  def runCmap(context: Context): Either[Error, T]
}
```

There are many operators to compose `Cmap` instances.

## Parsing tables to structured data

The `rowmapping` module adds a implementation for Cmap in the context of processing flat data records.

The context is a lookup of values in the current row you are parsing.
This lookup can be done by name or by predicate.

For example consider the following data set:

|name|surname|age|street_name   |STREET_NUMBER|
|----|-------|---|--------------|-------------|
|John|Doe    |42 |Baker Street  |123          |
|Jane|Doe    |44 |West  Street  |456          |

A mapping that can process this data can be specified with `RowMapping`:

```scala
case class Person(name: String, surname: String, age: Int, address: Address)

case class Address(street: String, number: Int)

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
```