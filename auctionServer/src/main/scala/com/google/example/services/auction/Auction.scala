package com.google.example.services.auction

import upickle.default._

case class Auction(id: Long, description: String, owner: String, minBid: Float, bids: Seq[Bid]) derives ReadWriter
case class Bid(id: Long, onwer: String, amount: Float) derives ReadWriter
