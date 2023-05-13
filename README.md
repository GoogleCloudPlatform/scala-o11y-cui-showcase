## Scala observability sample



## Testing




Starting otel collector
```bash
docker-compose up
```


Pinging the service
```bash
curl http://localhost:8080/error -H "traceparent:  00-ff000000000000000000000000000041-ff00000000000041-01" -H "baggage: cui=test-use-case2"
```



### Pinging the auction service

Posting an item
```bash
curl -X POST http://localhost:8080/auctions \
     -H "Content-Type: application/json" \
     -H "baggage: cui=user-fun" \
     -d "{\"description\":\"The thing I'm going to sell\"}"
```

Bidding:
```bash
AUCTION_ID=5000
curl -X POST http://localhost:8080/auctions/${AUCTION_ID}/bid \
     -H "Content-Type: application/json" \
     -d "{\"bid\":2}"
```

Listing Auctions
```bash
curl http://localhost:8080/auctions
```

Listing a specific Auction
```bash
AUCTION_ID=5000
curl http://localhost:8080/auctions/${AUCTION_ID}
```