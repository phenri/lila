package controllers

import play.navigator._

object nav extends PlayNavigator {

  val socket = GET on "socket" to Main.websocket

  namespace("games") {
    GET on root to Game.realtime
  }
//GET   /games              controllers.Game.realtime
//GET   /games/all          controllers.Game.all(page: Int ?= 1)
//GET   /games/checkmate    controllers.Game.checkmate(page: Int ?= 1)
//GET   /games/bookmark     controllers.Game.bookmark(page: Int ?= 1)
//GET   /games/popular      controllers.Game.popular(page: Int ?= 1)
//GET   /games/analysed     controllers.Game.analysed(page: Int ?= 1)
//GET   /games/featured/js  controllers.Game.featuredJs(id: String ?= "")
}
