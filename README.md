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


# Deploy to GKE-autopilot

TODO - instructions for configuring OTEL operator http://github.com/GoogleCloudPlatform/otel-operator-sample

## Configuring Artifact Registry

TODO - link to documents on how to enable permissions and set up a docker registry
TODO - Descript how to update `common_veriables.sh`

Push docker images to artifact registry
```bash
sbt Docker/publishLocal && ./push_docker_images.sh
```


Then to try things out:

```bash
kubectl run mycurlpod --image=curlimages/curl -i --tty -- sh
```

```bash
kubectl attach mycurlpod -c mycurlpod -i -t
```

```bash
curl  http://frontend-service.default.svc.cluster.local
```