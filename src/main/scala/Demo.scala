import scala.slick.driver.H2Driver.simple._

object Demo extends App {
  println("==================================================================")
  println
  val db = Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver")
  db.withSession { implicit session: Session =>

  }
  println
}
