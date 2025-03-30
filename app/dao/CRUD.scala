package dao

import java.sql.SQLException
import javax.inject.Inject

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import org.slf4j
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.Logger
import play.api.Logging
import slick.jdbc.GetResult
import slick.jdbc.JdbcProfile

class CRUD @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends Logging
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def insert(tableName: String, values: String): Future[Int] = {
    val insertCommand = s"INSERT INTO $tableName VALUES($values);"
    insertToDB(insertCommand)
  }

  def insertAll(tableName: String, values: Seq[String]): Future[Int] = {
    val insertCommand = values.map(value => s"INSERT INTO $tableName VALUES($value);").mkString(" ")
    insertToDB(insertCommand)
  }

  private def insertToDB(insertCommand: String): Future[Int] = {
    logger.info(s"INSERT - $insertCommand")
    val query    = sqlu"#$insertCommand"
    val runQuery = Try(db.run(query))
    runQuery match {
      case Success(value) =>
        value
      case Failure(exception: SQLException) =>
        logger.error(s"SQL Exception occurred: ${exception.printStackTrace()}")
        throw exception
      case Failure(e) =>
        logger.error(s"An unexpected error occurred: ${e.printStackTrace()}")
        throw e
    }
  }

  def update(tableName: String, values: String, condition: String): Future[Int] = {
    val updateCommand = s"UPDATE $tableName SET $values WHERE $condition;"
    logger.info(s"UPDATE - $updateCommand")
    val query    = sqlu"#$updateCommand"
    val runQuery = Try(db.run(query))
    runQuery match {
      case Success(value) =>
        logger.info(s"Updated successfully")
        value
      case Failure(exception: SQLException) =>
        logger.error(s"SQL Exception occurred: ${exception.printStackTrace()}")
        throw exception
      case Failure(e) =>
        logger.error(s"An unexpected error occurred: ${e.printStackTrace()}")
        throw e
    }
  }

  def select[T](tableName: String, columnsToRetrive: Option[String] = Some("*"), condition: Option[String] = Some(""))(
      implicit getResult: GetResult[T]
  ): Future[List[T]] = {
    val finalColumnsToRetrive = if (columnsToRetrive.isEmpty) "*" else columnsToRetrive.get
    val finalCondition        = if (condition.isEmpty) "" else condition.get
    val selectCommand         = s"select $finalColumnsToRetrive FROM $tableName $finalCondition;"
    logger.info(s"SELECT - $selectCommand")
    val query    = sql"#$selectCommand".as[T]
    val runQuery = Try(db.run(query))
    runQuery match {
      case Success(value) =>
        logger.info(s"Retrived successfully")
        value.flatMap(rows => Future.successful(rows.toList))
      case Failure(exception: SQLException) =>
        logger.error(s"SQL Exception occurred: ${exception.printStackTrace()}")
        throw exception
      case Failure(e) =>
        logger.error(s"An unexpected error occurred: ${e.printStackTrace()}")
        throw e
    }
  }

}
