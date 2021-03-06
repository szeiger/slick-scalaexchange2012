import scala.slick.driver.H2Driver.simple._

object Demo extends App {
  println("==================================================================")
  println
  val db = Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver")
  db.withSession { implicit session: Session =>

    case class Supplier(id: Int, name: String, city: String)
    case class Coffee(name: String, supID: Int, price: Double)

    object Suppliers extends Table[Supplier]("SUPPLIER") {
      def id     = column[Int   ]("SUP_ID", O.PrimaryKey, O.AutoInc)
      def name   = column[String]("SUP_NAME")
      def city   = column[String]("CITY")
      def * = id ~ name ~ city <> (Supplier, Supplier.unapply _)
      def ins = name ~ city returning id
      def byId = createFinderBy(_.id)
    }

    object Coffees extends Table[Coffee]("COFFEE") {
      def name  = column[String]("COF_NAME", O.PrimaryKey)
      def supID = column[Int]("SUP_ID")
      def price = column[Double]("PRICE")
      def * = name ~ supID ~ price <> (Coffee, Coffee.unapply _)
      //def supplier = Suppliers.filter(_.id === supID)
      def supplier = foreignKey("SUP_FK", supID, Suppliers)(_.id)
    }

    (Suppliers.ddl ++ Coffees.ddl).create
    (Suppliers.ddl ++ Coffees.ddl).createStatements.foreach(s => println("DDL: "+s))
    (Suppliers.ddl ++ Coffees.ddl).dropStatements.foreach(s => println("Drop: "+s))

    //val (sup1, sup2, sup3) = (1, 2, 3)
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

    println("Suppliers: " + Query(Suppliers).list)
    println("Coffees: " + Query(Coffees).to[Vector])
    println("Coffee Prices: " + Coffees.map(_.price).to[Array])

    val q1 = Coffees.map(_.price)
    println("Prices: " + q1.to[Array])
    println("Statement: "+q1.selectStatement)

    val q2 = (for {
      c <- Coffees
      s <- c.supplier
    } yield (c, s)).sortBy(_._2.id)
    println(q2.list.mkString("q2: ", "\n    ", ""))
    println("Statement: " + q2.selectStatement)

    println("Supplier 2: "+Suppliers.byId(2).first)

    /*def coffeesByCity(city: String) = {
      (for {
        c <- Coffees
        s <- c.supplier if s.city === city.bind
      } yield (c.name, s.name)).sortBy(_._1)
    }*/

    val coffeesByCity = Parameters[String].flatMap { city =>
      (for {
        c <- Coffees
        s <- c.supplier if s.city === city
      } yield (c.name, s.name)).sortBy(_._1)
    }

    coffeesByCity("Mendocino").foreach { case (cname, sname) =>
      println(s"Coffee: $cname, Supplier: $sname")
    }
    println(coffeesByCity.selectStatement)

    /*val q3 = for {
      c <- Coffees
      s <- Suppliers.sortBy(_.id).take(2) if c.supID === s.id
    } yield (c.name, s.id)
    println(q3.list.mkString("q3: ", "\n    ", ""))*/

    val q3 = for {
      (c, s) <- Coffees leftJoin Suppliers.sortBy(_.id).take(2) on (_.supID === _.id)
    } yield (c.name, s.id.?)
    println(q3.list.mkString("q3: ", "\n    ", ""))

    val q4 = (for {
      c <- Coffees
      s <- c.supplier
    } yield (s.name, c)).groupBy(_._1).map { case (sname, ts) =>
      (sname, ts.length, ts.map(_._2.price).min.get)
    }
    println(q4.list.mkString("q4: ", "\n    ", ""))

    val upper = SimpleFunction.unary[String, String]("uCaSe", fn = true)

    val q1b = Coffees.map(c => upper(c.name))
    println(q1b.selectStatement)

  }
  println
}
