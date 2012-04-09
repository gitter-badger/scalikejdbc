package scalikejdbc

import java.sql.Connection
import util.control.Exception._

object TestUtils {

  def initializeEmpRecords(session: DBSession, tableName: String) {
    session.update("delete from " + tableName)
    session.update("insert into " + tableName + " (id, name) values (?, ?)", 1, "name1")
    session.update("insert into " + tableName + " (id, name) values (?, ?)", 2, "name2")
  }

  def initialize(conn: Connection, tableName: String) {
    new DB(conn) autoCommit {
      session =>
        handling(classOf[Throwable]) by {
          t =>
            try {
              session.execute("create table " + tableName + " (id integer primary key, name varchar(30))")
            } catch {
              case e =>
                session.execute("create table " + tableName + " (id integer primary key, name varchar(30))")
            }
            initializeEmpRecords(session, tableName)
        } apply {
          session.single("select count(1) from " + tableName)(rs => rs.int(1))
          initializeEmpRecords(session, tableName)
        }
    }
  }

  def deleteTable(conn: Connection, tableName: String): Unit = {
    using(conn) {
      conn =>
        new DB(conn) autoCommit {
          session =>
            ignoring(classOf[Throwable]) {
              session.execute("drop table " + tableName)
            }
        }
    }
  }

}
