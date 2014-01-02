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
  def getCNYProfit = {
    val oc = getOKCoinRateInCNY.toDouble
    val cb = getCoinbaseRateInCNY.toDouble
    ((cb - oc) / oc * 100).formatted("%1.3f")
  }

  def getOKCoinRateInCNY = {
    val uri = "https://www.okcoin.com/api/ticker.do"

    getRate(uri, fetchJsonRateOkCoin)
  }

  def getCoinbaseRateInCNY:String  = {
    val uri = "http://coinbase.com/api/v1/currencies/exchange_rates"
    getRate(uri, fetchJsonRateCoinbase)
  }

  def getRate(uri:String, fetchRateFunc:(JsValue)=>Option[String] ):String = {
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

  def fetchJsonRateCoinbase(json:JsValue):Option[String] = {
    (json \ "btc_to_cny").asOpt[String]
  }

  def fetchJsonRateOkCoin(json:JsValue):Option[String] = {
    (json \ "ticker" \ "last").asOpt[String]
  }

  def httpGet(uri:String): String = {
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
