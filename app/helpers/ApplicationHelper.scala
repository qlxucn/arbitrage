package helpers

import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.{BasicResponseHandler, DefaultHttpClient}
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import play.Logger
import play.api.libs.json.{Json, JsValue}
import play.api.db.DB
import org.joda.time.DateTime
import models.Margin
import org.apache.commons.lang3.StringUtils
import org.apache.http.params.{HttpConnectionParams, BasicHttpParams}
import helpers.ArbConstants._


/**
 * User: xiaolongxu
 * Date: 1/1/14
 * Time: 4:57 PM
 */
object ApplicationHelper {
  val logger = Logger.of(this.getClass)

  var usd_okcoin   = ""
  var usd_coinbase = ""
  var cny_okcoin   = ""
  var cny_coinbase = ""
  var usd_to_cny   = ""

  def genData = {
    try {
      usd_to_cny   = fetchExRate_Usd2Cny
      cny_okcoin   = fetchCNY_OkCoin.toDouble.formatted("%1.3f")
      usd_okcoin   = (cny_okcoin.toDouble / usd_to_cny.toDouble).formatted("%1.3f")
      usd_coinbase = fetchUSD_CoinBase.toDouble.formatted("%1.3f")
      cny_coinbase = (usd_coinbase.toDouble * usd_to_cny.toDouble).formatted("%1.3f")
    } catch {
      case ex:Exception => logger.error(ex.getMessage, ex)
    }
  }

  def getProfit_Okcoin2Coinbase = {
    val oc = usd_okcoin.toDouble
    val cb = usd_coinbase.toDouble
    ((cb - oc) / oc * 100).formatted("%1.3f")
  }

  def getProfit_Coinbase2Okcoin = {
    val oc = usd_okcoin.toDouble
    val cb = usd_coinbase.toDouble
    ((oc - cb) / cb * 100).formatted("%1.3f")
  }

  def getExUsd2Cny   = usd_to_cny
  def getCnyOkcoin   = cny_okcoin
  def getUsdOkcoin   = usd_okcoin
  def getCnyCoinbase = cny_coinbase
  def getUsdCoinbase = usd_coinbase

  def genDataForHighStock(duration:String) = {
    var margins:List[Margin] = null

    duration match {
      case DURATION_ONE_DAY => margins = Margin.fromOneDay()
      case DURATION_ONE_WEEK => margins = Margin.fromOneWeek()
      case DURATION_ONE_MONTH => margins = Margin.fromOneMonth()
      case _ => margins = Margin.fromOneDay()
    }

    var okcoin_cny_data      = "["
    var coinbase_cny_data    = "["
    var okcoin2Coinbase_data = "["

    margins.foreach { m =>
      val millis = DateTime.parse(m.created_at).getMillis.toString
      okcoin_cny_data      += s"[${millis}, ${m.okcoin_cny}], "
      coinbase_cny_data    += s"[${millis}, ${m.coinbase_cny}], "
      okcoin2Coinbase_data += s"[${millis}, ${m.okcoin2Coinbase}], "
    }

    okcoin_cny_data       += "]"
    coinbase_cny_data     += "]"
    okcoin2Coinbase_data  += "]"

    (okcoin_cny_data, coinbase_cny_data, okcoin2Coinbase_data)
  }


  def fetchCNY_OkCoin:String = {
    def fetchRate(json:JsValue):Option[String] = {
      (json \ "ticker" \ "last").asOpt[String]
    }

    val uri = "https://www.okcoin.com/api/ticker.do"

    val rate = getRate(uri, fetchRate)
    if (StringUtils.isBlank(rate)) {
      logger.error(s"fetchCNY_OkCoin failed, rate=${rate}")
    }

    rate
  }

  def fetchUSD_CoinBase:String  = {
    def fetchRate(json:JsValue):Option[String] = {
      (json \ "btc_to_usd").asOpt[String]
    }
    val uri = "http://coinbase.com/api/v1/currencies/exchange_rates"

    val rate = getRate(uri, fetchRate)
    if (StringUtils.isBlank(rate)) {
      logger.error(s"fetchUSD_CoinBase failed, rate=${rate}")
    }

    rate
  }

  def fetchExRate_Usd2Cny:String = {
    val resp = httpGet("http://www.likeforex.com/currency-converter/us-dollar-usd_cny-chinese-yuan-renminbi.htm/1")
    val rateRegex = "1 USD =(.*?)CNY".r

    val g = rateRegex.findFirstMatchIn(resp)

    if (g.nonEmpty) {
     return g.get.group(1).trim
    }

    logger.error(s"fetchExRate_Usd2Cny failed")
    return null
  }

  private def getRate(uri:String, fetchRateFunc:(JsValue)=>Option[String] ):String = {
    (1 to 5).foreach{ i=>
      try {
        val res = httpGet(uri)
        val json = Json.parse(res)
        val rate = fetchRateFunc(json)

        if (rate.isEmpty) {
          throw new Exception("rate is empty")
        } else {
          return rate.get
        }
      } catch {
        case e:Exception => {
          if (i==5) {
            Logger.error("Json parse error", e)
          }
        }
      }
    }

    return null
  }

  private def httpGet(uri:String): String = {
    val params = new BasicHttpParams
    HttpConnectionParams.setConnectionTimeout(params, 20*1000)
    HttpConnectionParams.setSoTimeout(params, 20*1000)

    val client = new DefaultHttpClient(params)
    val method = new HttpGet(uri)
    val responseHandler = new BasicResponseHandler

    var response = ""
    try {
      response = client.execute(method, responseHandler)
    } catch {
      case e: Exception => {
        val err = s"Get rate from [${uri}] Error."
        Logger.error(err, e)
        return null
      }
    }

    response
  }
}
