package actor

import akka.actor.{ActorLogging, Actor}
import play.Logger
import helpers.ApplicationHelper._
import org.joda.time.DateTime
import models.Margin

/**
 * User: xiaolongxu
 * Date: 1/7/14
 * Time: 4:28 PM
 */
class MarginMonitActor extends Actor with ActorLogging{
  def receive:Receive = {
    case MarginMonitMsg(msg:String) => {
      Logger.info("fetch price data ...")
      genData

      val cnyOkcoin              = getCnyOkcoin
      val usdOkcoin              = getUsdOkcoin
      val cnyCoinbase            = getCnyCoinbase
      val usdCoinbase            = getUsdCoinbase
      val profit_Okcoin2Coinbase = getProfit_Okcoin2Coinbase
      val profit_Coinbase2Okcoin = getProfit_Coinbase2Okcoin
      val exUsd2Cny              = getExUsd2Cny

      Margin.create(
        cnyOkcoin              ,
        usdOkcoin              ,
        cnyCoinbase            ,
        usdCoinbase            ,
        profit_Okcoin2Coinbase ,
        profit_Coinbase2Okcoin ,
        exUsd2Cny
      )

      Logger.info(
        s"okcoin_cny=${cnyOkcoin}, " +
        s"okcoin_usd=${usdOkcoin}, " +
        s"coinbase_cny=${cnyCoinbase}, " +
        s"coinbase_usd=${usdCoinbase}, " +
        s"Okcoin2Coinbase=${profit_Okcoin2Coinbase}, " +
        s"Coinbase2Okcoin=${profit_Coinbase2Okcoin}, " +
        s"exchange_rate=${exUsd2Cny}")
    }
  }
}