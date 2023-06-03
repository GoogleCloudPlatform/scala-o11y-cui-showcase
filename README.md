# Scala observability sample

A highlight of the Li Haoyi Scala 3 ecosystem as "enhanced" by opentelemetry-java.

This project attempts to:

- Demonstrate the value of CUI annotations (See the [talk](https://www.youtube.com/watch?v=qFTVJtlj0xo&ab_channel=ContainerSolutions))
- Demonstrate how to "instrument" Li Haoyi's ecosystem of Scala 3 libraries
  - We try to tackle the raw exchange handler.
  - This still has issues w/ websockets.
- Demonstrate (via docker-compose) how to run all these microservices together with ONE observability funnel 

## What are CUI?

CUI stand for "Critical User Interaction". It allows tracking behavior of your system be important usages.

- Have you ever wondered how many times your message database was accessed from the web front-end vs. API usage?
- Have you ever tried to determine whether a server is used when serving your primary web application?

CUI tracking can help.  This project demonstrates the possibility of leveraging [Baggage](https://www.w3.org/TR/baggage/)
and [OpenTelemetry](https://opentelemetry.io) to propagate CUI between microservices and track usage by important
customer.

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

This sample relies on a working OpenTelemetry operator running on your GKE cluster.  Please see 
http://github.com/GoogleCloudPlatform/otel-operator-sample for instructions on how to run the OpenTelemetry operator
with GKE.

## Configuring Artifact Registry

This example requires Artifact Registry to be setup and configured with your local docker. You can find
[instructions here](https://cloud.google.com/artifact-registry/docs/docker/store-docker-container-images).

Once this is setup, you need to update the `common_variables.sh` script with your values, e.g.

```bash
export GCP_PROJECT=my-demo-project-1321414
export GCP_REPO_NAME=my-docker-registry
export GCP_REGION=us-east1
```

## Pushing Images

Push docker images to artifact registry
```bash
sbt Docker/publishLocal && ./push_docker_images.sh
```

## Pushing CRDs

Push the deployments, services and OpenTelemetry configuration for the example:
```bash
./push_k8s_crds.sh
```

## Trying the demo locally

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

You will find all traces coming from the `frontend-service` annotated with `cui=search`.

# Analytics

Coming soon.