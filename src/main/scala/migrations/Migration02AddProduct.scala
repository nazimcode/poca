/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package poca

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.PostgresProfile.api._

class Migration02AddProduct(db: Database) extends Migration with LazyLogging {

  class CurrentProductsTable(tag: Tag)
      extends Table[(String, String, String, String)](tag, "products") {
    def productId          = column[String]("productid", O.PrimaryKey)
    def productName        = column[String]("productname")
    def productDescription = column[String]("productdescription")
    def productImages      = column[String]("productimages")
    def *                  = (productId, productName, productDescription, productImages)
  }

  override def apply() {
    val products = TableQuery[CurrentProductsTable]
    val setup: Future[Unit] = db.run(
      DBIO.seq(
        // Insert some products
        products ++= Seq(
          (
            "1",
            "test",
            "test",
            "https://cdn.pixabay.com/photo/2019/08/06/22/48/artificial-intelligence-4389372_960_720.jpg"
          )
        )
      )
    )

    Await.result(setup, Duration.Inf)
    logger.info("Done populating table: Products")
  }
}
