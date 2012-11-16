import scala.slick.driver.H2Driver.simple._

object Demo { def main(args: Array[String]) {

  case class Supplier(id: Int, name: String, city: String)
  case class Coffee(name: String, supID: Int, price: Double)

  object Suppliers extends Table[Supplier]("SUPPLIERS") {
    def id     = column[Int   ]("SUP_ID", O.PrimaryKey, O.AutoInc)
    def name   = column[String]("SUP_NAME")
    def city   = column[String]("CITY")
    def * = id ~ name ~ city <> (Supplier, Supplier.unapply _)
    def ins = name ~ city returning id
    def isLocal = city === "Meadows"
    def byId = createFinderBy(_.id)
  }

  object Coffees extends Table[Coffee]("COFFEES") {
    def name  = column[String]("COF_NAME", O.PrimaryKey)
    def supID = column[Int]("SUP_ID")
    def price = column[Double]("PRICE")
    def * = name ~ supID ~ price <> (Coffee, Coffee.unapply _)
    def supplier = foreignKey("SUP_FK", supID, Suppliers)(_.id)
    //def supplier = Suppliers.filter(_.id === supID)
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

    val q2 = (for {
      c <- Coffees
      //s <- Suppliers if s.id === c.supID
      s <- c.supplier if s.isLocal
    } yield (c, s)).sortBy(_._2.id)
    println(q2.list.mkString("With prices: ", "\n             ", ""))

    println("Supplier 2: " + Suppliers.byId(2).list)

    /*
    def coffeesFromCity(city: String) = {
      (for {
        c <- Coffees
        s <- c.supplier if s.city === city
      } yield (c.name, s.id)).sortBy(_._1)
    }
    println("Coffees from Mendocino:")
    coffeesFromCity("Mendocino").foreach { case (name, supID) =>
      println(s"  Name: $name, supID: $supID")
    }
    println(coffeesFromCity("Mendocino").selectStatement)
    */

    val coffeesFromCity = Parameters[String].flatMap { city =>
      (for {
        c <- Coffees
        s <- c.supplier if s.city === city
      } yield (c.name, s.id)).sortBy(_._1)
    }
    println("Coffees from Mendocino:")
    coffeesFromCity("Mendocino").foreach { case (name, supID) =>
      println(s"  Name: $name, supID: $supID")
    }
    println(coffeesFromCity.selectStatement)

    println

    val q3 = for {
      (c, s) <- Coffees leftJoin Suppliers.sortBy(_.id).take(2) on (_.supID === _.id)
    } yield (c.name, s.city.?)
    println(q3.list.mkString("q3: ", "\n    ", ""))
    println("q3: " + q3.selectStatement)


    println
  }
}}
