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

  "various other queries" should {
    "work nice too" in {
      log.info("Simple outer join")
      querySync(
        DocumentTable
          .joinLeft(StudentTable).on(_.studentId === _.id)
          .filter { case(doc, student) => student.map(_.name) === "Test" }
      )

      log.info("Multiple joins with case decomposition")
      querySync(
        StudentCourseSegmentTable
            .join(StudentTable)
                .on { case (segment, student) => student.id === segment.studentId }
            .join(CourseTable)
                .on { case ((segment, _), course) => course.id === segment.courseId }
            .join(SemesterTable)
                .on { case (((segment, _), _), semester) => semester.id === segment.semesterId }
            .filter { case (((_, student), _), _) =>
                student.name === "Tim"
            }
      )

      log.info("Multiple joins with")
      querySync(
        for {
          segment <- StudentCourseSegmentTable
          student <- segment.student if student.name === "Tim"
          course <- segment.course
          semester <- segment.semester
        } yield (segment, student, course, semester)
      )
    }

    "compiled query" in {
      log.info("Multiple joins with case decomposition")
      1.to(3).foreach { counter =>
        querySync(
          for {
            segment <- StudentCourseSegmentTable
            student <- segment.student if student.name === "Tim"
            course <- segment.course
            semester <- segment.semester
          } yield (segment, student, course, semester)
        )
      }

      val query = Compiled { name: Rep[String] =>
        for {
          segment <- StudentCourseSegmentTable
          student <- segment.student if student.name === name
          course <- segment.course
          semester <- segment.semester
        } yield (segment, student, course, semester)
      }

      log.info("First compiled execution ***")
      1.to(5).foreach { counter =>
        log.info(s"Execution: ${counter}")
        blockingWait(db.run(query("Tim").result))
      }
    }
  }
}
