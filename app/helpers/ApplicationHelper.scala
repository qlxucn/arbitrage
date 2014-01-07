package helpers

import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.{BasicResponseHandler, DefaultHttpClient}
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import play.Logger
import play.api.libs.json.{Json, JsValue}


/**
 * User: xiaolongxu
 * Date: 1/1/14
 * Time: 4:57 PM
 */
object ApplicationHelper {
  var usd_okcoin   = ""
  var usd_coinbase = ""
  var cny_okcoin   = ""
  var cny_coinbase = ""
  var usd_to_cny   = ""

  def genData = {
    usd_to_cny   = fetchExRate_Usd2Cny
    cny_okcoin   = fetchCNY_OkCoin.toDouble.formatted("%1.3f")
    usd_okcoin   = (cny_okcoin.toDouble / usd_to_cny.toDouble).formatted("%1.3f")
    usd_coinbase = fetchUSD_CoinBase.toDouble.formatted("%1.3f")
    cny_coinbase = (usd_coinbase.toDouble * usd_to_cny.toDouble).formatted("%1.3f")
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


  def fetchCNY_OkCoin:String = {
    def fetchRate(json:JsValue):Option[String] = {
      (json \ "ticker" \ "last").asOpt[String]
    }

    val uri = "https://www.okcoin.com/api/ticker.do"

    getRate(uri, fetchRate)
  }

  def fetchUSD_CoinBase:String  = {
    def fetchRate(json:JsValue):Option[String] = {
      (json \ "btc_to_usd").asOpt[String]
    }
    val uri = "http://coinbase.com/api/v1/currencies/exchange_rates"
    getRate(uri, fetchRate)
  }

  def fetchExRate_Usd2Cny:String = {
    val resp = httpGet("http://www.likeforex.com/currency-converter/us-dollar-usd_cny-chinese-yuan-renminbi.htm/1")
    val rateRegex = "1 USD =(.*?)CNY".r

    val g = rateRegex.findFirstMatchIn(resp)

    if (g.nonEmpty) {
     return g.get.group(1).trim
    }

    return null
  }

  private def getRate(uri:String, fetchRateFunc:(JsValue)=>Option[String] ):String = {
    val res = httpGet(uri)
    if (res == null) return null

    try {
      val json = Json.parse(res)
      val rate = fetchRateFunc(json)

      if (rate.isEmpty) {
        return null
      } else {
        return rate.get
      }

    } catch {
      case e:Exception => {
        Logger.error("Json parse error", e)
        return null
      }
    }
  }

  private def httpGet(uri:String): String = {
    val client = new DefaultHttpClient
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
