/*
 * Copyright 2023 Google
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.example.services.auction

/** Abstraction we use to stub-out different datastores/backends. */
trait AuctionDataStore:
  /** Stores an auction. */
  def add(description: String, minBid: Float): Auction
  /** Deletes an auction, returning its value. */
  def delete(id: Long): Option[Auction]
  /** Returns all live auctions. */
  def get(id: Long): Option[Auction]
  /** Returns all live auctions. */
  def list(): Seq[Auction]
  /** Creates a new bid on a specific auction. */
  def bid(id: Long, bid: Float): Option[Auction]


/** Lame, synchronous in-memory implementation. */
class LocalAuctionDataStore extends AuctionDataStore:
  println("Starting in-memory auction datastore.")
  private var nextId = 1L
  private var auctions = List[Auction](Auction(5000, "seed item", "Josh", 0, Seq()))


  override def add(description: String, minBid: Float): Auction =
    synchronized {
      val auction = Auction(nextId, description, "{unknown}", minBid, List())
      nextId += 1
      auctions = auction :: auctions
      auction
    }
  override def delete(id: Long): Option[Auction] =
    auctions.find(_.id == id) match
      case None => None
      case Some(auction) =>
        synchronized {
          auctions = auctions.filter(_ != auction)
        }
        Some(auction)
  override def list(): Seq[Auction] = auctions
  override def get(id: Long): Option[Auction] = auctions.find(_.id == id)
  override def bid(id: Long, bid: Float): Option[Auction] =
    synchronized {
      auctions.find(_.id == id).map { auction =>
        val b = Bid(auction.bids.length + 1, "{unknown}", bid)
        val updated = auction.copy(bids = auction.bids :+ b)
        auctions = updated :: auctions.filter(_ != auction)
        updated
      }
    }