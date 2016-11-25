package com.slick101.test.cases.queries

import com.slick101.test.cases.conversation.Id
import slick.jdbc.H2Profile.api._
import com.slick101.test.cases.conversation.TypesafeId._

object CourseModel {

  // student
  case class Student(name: String,
                     middleName: Option[String],
                     surname: String,
                     nationality: String,
                     id: Id[Student] = Id.none)

  class StudentTable(tag: Tag) extends Table[Student](tag, "STUDENT") {
    def name = column[String]("NAME")
    def middleName = column[Option[String]]("MIDDLE_NAME")
    def surname = column[String]("SURNAME")
    def nationality = column[String]("NATIONALITY")
    def id = column[Id[Student]]("ID", O.PrimaryKey, O.AutoInc)

    def * = (name, middleName, surname, nationality, id) <> (Student.tupled, Student.unapply)
  }

  lazy val StudentTable = TableQuery[StudentTable]

  // document
  case class Document(studentId: Option[Id[Student]],
                      name: String,
                      uuid: String,
                      id: Id[Document] = Id.none)

  class DocumentTable(tag: Tag) extends Table[Document](tag, "DOCUMENT") {
    def studentId = column[Option[Id[Student]]]("STUDENT_ID")
    def name = column[String]("NAME")
    def uuid = column[String]("UUID")
    def id = column[Id[Document]]("ID", O.PrimaryKey, O.AutoInc)

    def * = (studentId, name, uuid, id) <> (Document.tupled, Document.unapply)

    def student = foreignKey("fk_document_student", studentId, StudentTable)(_.id.?)
  }

  lazy val DocumentTable = TableQuery[DocumentTable]

  // course
  case class Course(name: String,
                    id: Id[Course] = Id.none)

  class CourseTable(tag: Tag) extends Table[Course](tag, "COURSE") {
    def name = column[String]("NAME")
    def id = column[Id[Course]]("ID", O.PrimaryKey, O.AutoInc)

    def * = (name, id) <> (Course.tupled, Course.unapply)
  }

  lazy val CourseTable = TableQuery[CourseTable]

  // semester
  case class Semester(year: Int,
                      part: String,
                      id: Id[Semester] = Id.none)

  class SemesterTable(tag: Tag) extends Table[Semester](tag, "SEMESTER") {
    def year = column[Int]("YEAR")
    def part = column[String]("PART")
    def id = column[Id[Semester]]("ID", O.PrimaryKey, O.AutoInc)

    def * = (year, part, id) <> (Semester.tupled, Semester.unapply)
  }

  lazy val SemesterTable = TableQuery[SemesterTable]

  // student course segment
  case class StudentCourseSegment(studentId: Id[Student],
                                  courseId: Id[Course],
                                  semesterId: Id[Semester],
                                  id: Id[StudentCourseSegment] = Id.none)

  class StudentCourseSegmentTable(tag: Tag) extends Table[StudentCourseSegment](tag, "STUDENT_COURSE_SEGMENT") {
    def studentId = column[Id[Student]]("STUDENT_ID")
    def courseId = column[Id[Course]]("COURSE_ID")
    def semesterId = column[Id[Semester]]("SEMESTER_ID")
    def id = column[Id[StudentCourseSegment]]("ID", O.PrimaryKey, O.AutoInc)

    def * = (studentId, courseId, semesterId, id) <> (StudentCourseSegment.tupled, StudentCourseSegment.unapply)

    def student = foreignKey("fk_segment_student", studentId, StudentTable)(_.id)
    def course = foreignKey("fk_segment_course", courseId, CourseTable)(_.id)
    def semester = foreignKey("fk_segment_semester", semesterId, SemesterTable)(_.id)
  }

  lazy val StudentCourseSegmentTable = TableQuery[StudentCourseSegmentTable]

  // grade
  case class Grade(studentCourseSegmentId: Id[StudentCourseSegment],
                   grade: Int,
                   id: Id[Grade] = Id.none)

  class GradeTable(tag: Tag) extends Table[Grade](tag, "GRADE") {
    def studentCourseSegmentId = column[Id[StudentCourseSegment]]("STUDENT_COURSE_SEGMENT_ID")
    def grade = column[Int]("GRADE")
    def id = column[Id[Grade]]("ID", O.PrimaryKey, O.AutoInc)

    def * = (studentCourseSegmentId, grade, id) <> (Grade.tupled, Grade.unapply)

    def segment = foreignKey("fk_grade_segment", studentCourseSegmentId, StudentCourseSegmentTable)(_.id)
  }

  lazy val GradeTable = TableQuery[GradeTable]
}