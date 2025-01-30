package dao

import org.slf4j
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{GetResult, JdbcProfile}

import java.sql.SQLException
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class CRUD @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  val logger: slf4j.Logger = Logger(getClass).logger

  def insert(tableName: String, values: String): Future[Either[Exception, Int]] = {
    val insertCommand = s"INSERT INTO $tableName VALUES($values);"
    logger.info(s"sql - $insertCommand")
    val query = sqlu"#$insertCommand"
    val runQuery = Try(db.run(query))
    runQuery match {
      case Success(value) =>
        logger.info(s"Inserted successfully")
        value.map(rowCount => Right(rowCount))
      case Failure(exception: SQLException) =>
        logger.error(s"SQL Exception occurred: ${exception.printStackTrace()}")
        Future(Left(exception))
      case Failure(e) =>
        logger.error(s"An unexpected error occurred: ${e.printStackTrace()}")
        throw e
    }
  }

  def select[T](tableName: String, columnsToRetrive: Option[String] = Some("*"), condition: Option[String] = Some(""))
               (implicit getResult: GetResult[T]): Future[Either[SQLException, List[T]]] = {
    val finalColumnsToRetrive = if (columnsToRetrive.isEmpty) "*" else columnsToRetrive.get
    val finalCondition = if (condition.isEmpty) "" else condition.get
    val selectCommand = s"select $finalColumnsToRetrive FROM $tableName $finalCondition;"
    println(s"sql - $selectCommand")
    val query = sql"#$selectCommand".as[T]
    val runQuery = Try(db.run(query))
    runQuery match {
      case Success(value) =>
        logger.info(s"Retrived successfully")
        val output = value.flatMap(rows => Future(Right(rows.toList)))
        output
      case Failure(exception: SQLException) =>
        logger.error(s"SQL Exception occurred: ${exception.printStackTrace()}")
        Future(Left(exception))
      case Failure(e) =>
        logger.error(s"An unexpected error occurred: ${e.printStackTrace()}")
        throw e
    }
  }

}
