package com.stratesys.streaming.estacionesBicis.Models

object ModelClass {
  case class Parent(
                     stations: List[Station],
                     updateTime: BigInt)
  case class Station(
                      id: String,
                      latitude: String,
                      longitude: String,
                      streetName: String,
                      streetNumber: String,
                      altitude: String,
                      slots: String,
                      bikes: String,
                      status: String)


  case class StationFormatted(
                      id: Int,
                      location: String,
                      streetName: String,
                      streetNumber: String,
                      slots: Int,
                      bikes: Int,
                      status: String)

}
