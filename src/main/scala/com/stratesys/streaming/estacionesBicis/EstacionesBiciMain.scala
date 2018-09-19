package com.stratesys.streaming.estacionesBicis

import Models.ModelClass._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.flume._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.elasticsearch.spark.streaming.EsSparkStreaming
import net.liftweb.json._

import scala.collection.JavaConverters._

object EstacionesBiciMain {

  def main(args: Array[String]) {

    if (args.length < 2) {
      System.err.println(
        s"""
           |Usage: EstacionesBiciMain <host> <port>
           |  <host> Host to listen on avro
           |  <port> Port of Host
           |
        """.stripMargin)
      System.exit(1)
    }

    val hostname = args(0)
    val port = args(1).toInt

    // Create the context with a 1 second batch size
    val sparkConf = new SparkConf().setAppName("StationsBikes")

    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val flumeStream = FlumeUtils.createStream(ssc, hostname, port)

    val stationsStream: DStream[List[Station]] = flumeStream.map { sparkFlumeEvent =>
      implicit val formats = DefaultFormats // Brings in default date formats etc.
    val event = sparkFlumeEvent.event
      val messageBody = new String(event.getBody.array())
      parse(messageBody).extract[ParentStation].stations
    }

    val idStationStream = stationsStream.flatMap(x => x.groupBy(s => s.id))
      .map(x => new StationFormatted(x._2(0).id.toInt, x._2(0).latitude + "," + x._2(0).longitude, x._2(0).streetName,
        x._2(0).streetNumber, x._2(0).slots.toInt, x._2(0).bikes.toInt, x._2(0).status))

    EsSparkStreaming.saveToEs(idStationStream, "spark/bikes", Map("es.mapping.id" -> "id"))
    ssc.start()
    ssc.awaitTermination()
  }
}