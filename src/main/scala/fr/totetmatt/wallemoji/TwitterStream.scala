package fr.totetmatt.wallemoji
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import twitter4j._
import twitter4j.conf.ConfigurationBuilder
case class TwittAuth(consumerKey:String,consumerSecret:String,accessToken:String,accessTokenSecret:String)
object TwitterStream {
  implicit val system: ActorSystem = ActorSystem("wall-emoji")
  val config =
    TwittAuth( system.settings.config.getString("twitter.consumerKey"),
      system.settings.config.getString("twitter.consumerSecret"),
      system.settings.config.getString("twitter.accessToken"),
      system.settings.config.getString("twitter.accessTokenSecret")
    )


  val cb = new ConfigurationBuilder
  cb.setOAuthConsumerKey(config.consumerKey)
  cb.setOAuthConsumerSecret(config.consumerSecret)
  cb.setJSONStoreEnabled(true)
  cb.setDebugEnabled(false)
  cb.setPrettyDebugEnabled(false)
  cb.setOAuthAccessToken(config.accessToken)
  cb.setOAuthAccessTokenSecret(config.accessTokenSecret)

  val twitterStream = new TwitterStreamFactory(cb.build()).getInstance

  def listenAndStream()(implicit materializer:ActorMaterializer) = {

    val (actorRef, publisher) = Source.actorRef[Status](1000, OverflowStrategy.dropHead)
      .toMat(Sink.asPublisher(true))(Keep.both)
      .run()

    val statusListener = new StatusListener() {

      override def onStatus(status: Status) = {
        actorRef ! status
      }


      override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {
        // System.err.println(statusDeletionNotice)
      }

      override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {
        System.err.println(numberOfLimitedStatuses)
      }

      override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {
        System.err.println(userId)
      }

      override def onStallWarning(warning: StallWarning): Unit = {
        System.err.println(warning)
      }

      override def onException(ex: Exception): Unit = {
        System.err.println(ex)
      }
    }
    twitterStream.addListener(statusListener)
    twitterStream.sample()
    publisher
  }

}
