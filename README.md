# Scala observability sample

A highlight of the Li Haoyi Scala 3 ecosystem as "enhanced" by opentelemetry-java.

This project attempts to:

- Demonstrate the value of CUI annotations (tbd link)
- Demonstrate how to "instrument" Li Haoyi's ecosystem of Scala 3 libraries
  - In some cases, there is an elegant solution
  - In many cases, there is no affordance for our use case, so we duplicate the API.
- Demonstrate (via docker-compose) how to run all these microservices together with ONE observability funnel 

## How to run locallly

First push local docker images
```
sbt "Docker/publishLocal"
```


Start up the microservices
```bash
docker-compose up
```


Pinging the service with your own trace/baggage
```bash
curl http://localhost:8081/ \
  -H "traceparent:  00-ff000000000000000000000000000041-ff00000000000041-01" \
  -H "baggage: cui=test-use-case2"
```

### Pinging the auction service directly

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