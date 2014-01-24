package controllers

import play.api._
import play.api.mvc._
import helpers.ArbConstants._

object Application extends Controller {
  
  def index = duration(DURATION_ONE_DAY)

  def duration(range:String) = Action {
    Ok(views.html.index(range))
  }
}