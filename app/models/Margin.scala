package models

import play.api.db.DB
import anorm._
import org.joda.time.DateTime
import play.api.Play.current
import play.api.db._
import anorm.SqlParser._

case class Margin(id: Long,
                  okcoin_cny: String,
                  okcoin_usd: String,
                  coinbase_cny: String,
                  coinbase_usd: String,
                  okcoin2Coinbase: String,
                  coinbase2Okcoin: String,
                  exchange_rate: String,
                  created_at: String)

object Margin {
  def margin = {
    get[Long]("id") ~
    get[String]("okcoin_cny") ~
    get[String]("okcoin_usd") ~
    get[String]("coinbase_cny") ~
    get[String]("coinbase_usd") ~
    get[String]("okcoin2Coinbase") ~
    get[String]("coinbase2Okcoin") ~
    get[String]("exchange_rate") ~
    get[String]("created_at") map {
      case id~okcoin_cny~okcoin_usd~coinbase_cny~coinbase_usd~okcoin2Coinbase~coinbase2Okcoin~exchange_rate~created_at =>
        Margin(id,okcoin_cny,okcoin_usd,coinbase_cny,coinbase_usd,okcoin2Coinbase,coinbase2Okcoin,exchange_rate,created_at)
    }
  }


  def all(): List[Margin] = DB.withConnection { implicit c =>
    SQL("select * from margin").as(margin *)
  }

  def create(okcoin_cny: String,
             okcoin_usd: String,
             coinbase_cny: String,
             coinbase_usd: String,
             okcoin2Coinbase: String,
             coinbase2Okcoin: String,
             exchange_rate: String) {
    DB.withConnection { implicit c =>
      val sql = "insert into margin(okcoin_cny, okcoin_usd, coinbase_cny, coinbase_usd, okcoin2Coinbase, coinbase2Okcoin, exchange_rate, created_at) " +
        "values ({okcoin_cny}, {okcoin_usd}, {coinbase_cny}, {coinbase_usd}, {okcoin2Coinbase}, {coinbase2Okcoin}, {exchange_rate}, {created_at})"
      SQL(sql).on(
        'okcoin_cny      -> okcoin_cny     ,
        'okcoin_usd      -> okcoin_usd     ,
        'coinbase_cny    -> coinbase_cny   ,
        'coinbase_usd    -> coinbase_usd   ,
        'okcoin2Coinbase -> okcoin2Coinbase,
        'coinbase2Okcoin -> coinbase2Okcoin,
        'exchange_rate   -> exchange_rate  ,
        'created_at      -> DateTime.now.toString()
      ).executeUpdate()
    }
  }
}