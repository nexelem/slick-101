package com.slick101.test.cases.queries

import com.slick101.test.cases.conversation.Id
import com.slick101.test.cases.queries.CourseModel._
import com.slick101.test.{BaseTest, ServerDb}
import slick.jdbc.H2Profile.api._
import com.slick101.test.cases.conversation.TypesafeId._

import scala.concurrent.ExecutionContext.Implicits.global

class JoinQueriesSpec extends BaseTest with ServerDb {


  // tests
  "Simple join between 3 tables" must {
    "work with monadic implicit joins and explicit comparison" in {
      db.run((
        for {
          segment <- StudentCourseSegmentTable
          course <- CourseTable if course.id === segment.courseId
          student <- StudentTable if student.id === segment.studentId
        } yield course
        ).distinct
        .result.map { results =>
        log.info(s"\nFound ${results.mkString("\n")}")
        results should contain theSameElementsAs Seq(Course("Algebra", Id(1)), Course("Algorithms", Id(2)), Course("AI", Id(3)))
      }).futureValue
    }

    "work with monadic implicit joins and foreign key" in {
      db.run((
        for {
          segment <- StudentCourseSegmentTable
          course <- segment.course
          student <- segment.student
        } yield course
      ).distinct
        .result.map { results =>
        log.info(s"\nFound ${results.mkString("\n")}")
        results should contain theSameElementsAs Seq(Course("Algebra", Id(1)), Course("Algorithms", Id(2)), Course("AI", Id(3)))
      }).futureValue
    }

    "work with applicative explicit join" in {
      db.run((
        StudentCourseSegmentTable
          join CourseTable on (_.courseId === _.id)
          join StudentTable on (_._1.studentId === _.id)
        ).map {
        case ((segment, course), student) => course
      }.distinct
        .result.map { results =>
        log.info(s"\nFound ${results.mkString("\n")}")
        results should contain theSameElementsAs Seq(Course("Algebra", Id(1)), Course("Algorithms", Id(2)), Course("AI", Id(3)))
      }).futureValue
    }
  }
}
