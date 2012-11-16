import scala.slick.driver.H2Driver.simple._

object Demo extends App {

  case class Supplier(id: Int, name: String, city: String)
  case class Coffee(name: String, supID: Int, price: Double)

  object Suppliers extends Table[Supplier]("SUPPLIERS") {
    def id     = column[Int   ]("SUP_ID", O.PrimaryKey, O.AutoInc)
    def name   = column[String]("SUP_NAME")
    def city   = column[String]("CITY")
    def * = id ~ name ~ city <> (Supplier, Supplier.unapply _)
    def ins = name ~ city returning id
  }

  object Coffees extends Table[Coffee]("COFFEES") {
    def name  = column[String]("COF_NAME", O.PrimaryKey)
    def supID = column[Int]("SUP_ID")
    def price = column[Double]("PRICE")
    def * = name ~ supID ~ price <> (Coffee, Coffee.unapply _)
    //def supplier = foreignKey("SUP_FK", supID, Suppliers)(_.id)
  }

  val db = Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver")
  db.withSession { implicit session: Session =>
    (Suppliers.ddl ++ Coffees.ddl).create

    val Seq(sup1, sup2, sup3) = Suppliers.ins.insertAll(
      ("Acme, Inc.",      "Groundsville"),
      ("Superior Coffee", "Mendocino"),
      ("The High Ground", "Meadows")
    )

    Coffees.insertAll(
      Coffee("Colombian",          sup1, 7.99),
      Coffee("French_Roast",       sup2, 8.99),
      Coffee("Espresso",           sup3, 9.99),
      Coffee("Colombian_Decaf",    sup1, 8.99),
      Coffee("French_Roast_Decaf", sup2, 9.99)
    )

    println("==================================================================")
    println
    println("All coffees: " + Query(Coffees).list)
    println("Prices: " + Coffees.map(_.price).to[Vector])
    println("Prices: " + Coffees.map(_.price).to[Array])

    val q1 = Coffees.map(_.price)
    println("Prices: " + q1.to[Array])
    println("Statement: "+q1.selectStatement)
    println
  }
}
