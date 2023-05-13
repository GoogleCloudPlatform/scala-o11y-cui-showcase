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