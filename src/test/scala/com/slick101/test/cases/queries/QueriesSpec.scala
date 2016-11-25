import com.slick101.test.cases.queries.CourseModel._
import com.slick101.test.{BaseTest, ServerDb}
import slick.jdbc.H2Profile.api._
import slick.lifted
import slick.lifted.Functions._
import com.slick101.test.cases.conversation.TypesafeId._

import scala.concurrent.ExecutionContext.Implicits.global

// show general scheme of executing query (db.run, result)
// show grouping by

class QueriesSpec extends BaseTest with ServerDb {


  // tests
  "Students search" must {
    "return at least 5 students" in {
      db.run(StudentTable.result).map { results =>
        results.length should be >= 5
      }.futureValue

      db.run(
        StudentTable.map(student =>
          (student.name, student.surname)
        ).result
      ).map { results =>
        results.length should be >= 5
      }.futureValue
    }

    "general query test" in {
      db.run(
        StudentTable
          .result
      ).map { results =>
        log.info(s"\n${results.mkString("\n")}")
        results.length should be > 0
      }.futureValue
    }
  }

  "various tests" must {
    "generate queries" in {
      simpleSelect
      projections
      filtering
      otherQueries
    }
  }

  def simpleSelect: Unit = {
    log.info("=== Simple select")
    querySync(
      StudentTable
    )

    log.info("=== Simple select / for-comprehension")
    querySync(
      for {student <- StudentTable }
        yield student
    )
  }

  def projections: Unit = {
    log.info("=== Simple select with projection")
    querySync(
      StudentTable
        .map(_.name)
    )

    log.info("=== Multiple mapping")
    querySync(
      StudentTable
        .map(nat => nat.nationality ++ "  ")
        .map(_.toUpperCase)
        .map(_.trim)
        .map((_, currentTime, pi))
        .map(row => row._1 ++ " " ++ row._2.asColumnOf[String] ++ " " ++ row._3.asColumnOf[String])
    )

    log.info("=== Simple select with more complicated projection")
    querySync(
      StudentTable
        .sortBy(_.name)
        .map(s => (s.name, s.middleName.ifNull("*no-middlename*")))
    )

    log.info("=== Simple select with more complicated projection (reversed order)")
    querySync(
      StudentTable
        .map(s => (s.name, s.middleName.ifNull("*no-middlename*")))
        .sortBy(_._1)
    )
  }

  def filtering {
    log.info("=== Select with filter")
    querySync(
      StudentTable.filter(_.name === "Tom")
    )

    log.info("=== Select with filter / for-comprehension")
    querySync(
      (for {
        student <- StudentTable if student.name === "Tom"
      } yield student)
    )

    log.info("=== Select with more filters")
    querySync(
      StudentTable
        .filterNot(student => student.name === "Tom" && student.surname.startsWith("Smi"))
    )

    log.info("=== Select with sorting")
    querySync(
      StudentTable
        .filter(student => student.middleName.nonEmpty)
        .sortBy(_.name)
    )

    log.info("=== Select more complicated sorting")
    querySync(
      StudentTable
        .filter(student => student.middleName.nonEmpty)
        .sortBy(s => (s.name.desc, s.middleName.asc))
        .distinct
    )

    log.info("=== Select more distinct")
    querySync(
      StudentTable
        .map(_.name)
        .distinct
    )
  }

  def otherQueries: Unit = {
    log.info("=== Select limit / offset")
    querySync(
      StudentTable
        .map(s => (s.name, s.surname))
        .drop(2)
        .take(3)
    )

    log.info("=== Select limit / offset (reversed order)")
    querySync(
      StudentTable
        .map(s => (s.name, s.surname))
        .take(3)
        .drop(2)
    )

    log.info("=== Interesting distinct")
    querySync(
      StudentTable
        .map(s => (s.name, s.surname))
        .distinctOn(_._1)
    )

    log.info("=== Group by")
    querySync(
      StudentTable
        .filter(_.surname =!= "Test")
        .groupBy(_.surname)
        .map { case (surname, group) =>
          (surname, group.map(_.name).countDistinct)
        }
        .filter(row => row._2 > 5)
    )
  }
}
