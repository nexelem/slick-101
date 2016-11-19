import com.slick101.test.cases.queries.CourseModel
import com.slick101.test.{BaseTest, ServerDb}
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

class QueriesSpec extends BaseTest with ServerDb {


  // tests
  "Students search" must {
    "return at leat 5 students" in {
      db.run(CourseModel.StudentTable.result).map { results =>
        results.length should be >= 5
      }.futureValue

      db.run(
        CourseModel.StudentTable.map(student =>
          (student.name, student.surname)
        ).result
      ).map { results =>
        results.length should be >= 5
      }.futureValue
    }

    "general query test" in {
      db.run(
        CourseModel.StudentTable
          .result
      ).map { results =>
        log.info(s"\n${results.mkString("\n")}")
        results.length should be > 0
      }.futureValue
    }

//    "general update/delete" in {
//      exec(
//
//      ).map { results =>
//        log.info(s"\n${results}")
//        results should be >= 0
//      }.futureValue
//    }
  }
}
