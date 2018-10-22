package fr.totetmatt.wallemoji

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.vdurmont.emoji.EmojiParser
import fr.totetmatt.wallemoji.TwitterStream

import scala.collection.JavaConverters
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
case class ImageKey(url_key:String)
import scala.concurrent.duration._



object WebServer  {
  def main(args: Array[String]) {

    implicit val system: ActorSystem = ActorSystem("wall-emoji")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher


    val publisherTweet = TwitterStream.listenAndStream


    /* This prevent `publisherTweet` to stop when no more connection are active on `/twitter` */
    /*Source.fromPublisher(publisherTweet).runWith(Sink.ignore)*/
    import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
    val route =
      path("") {
        getFromFile("templates/index.html")
      } ~ path("twitter") {
        get {
          complete {
            Source.fromPublisher(publisherTweet)
              .map(s=>EmojiParser.extractEmojis(s.getText))
              .mapConcat(l =>JavaConverters.asScalaIteratorConverter(l.iterator()).asScala.toSet )
              .map(x=>ServerSentEvent(x,"emoji"))
              .keepAlive(5.second, () => ServerSentEvent.heartbeat)
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
