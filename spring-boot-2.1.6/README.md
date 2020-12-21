## 프레임워크 버전
- Spring Boot 2.1.6.RELEASE
- Spring Framework 5.1.8.RELEASE
- Reactor Netty 0.8.9.RELEASE

## 자바 버전
```
openjdk 11.0.7 2020-04-14
OpenJDK Runtime Environment AdoptOpenJDK (build 11.0.7+10)
OpenJDK 64-Bit Server VM AdoptOpenJDK (build 11.0.7+10, mixed mode)
```

## 재현 경로
아래와 같이 테스트를 실행하면,
```sh
$ ./gradlew clean test --tests "com.example.leak.LeakApplicationTests"
```

Netty 에서 출력하는 Bytebuf leak 관련 로그를 확인할 수 있습니다.
```sh
LEAK: ByteBuf.release() was not called before it's garbage-collected. See http://netty.io/wiki/reference-counted-objects.html for more information.
```

## 원인
어플리케이션 코드를 아래와 같이 작성하면,
```java
webClient.get()
    .uri(uri)
    .retrieve()
    .onStatus(Predicate.not(HttpStatus::is2xxSuccessful), res -> {
        logger.error("Fail to get uri: {}", uri);
        throw new RuntimeException("Fail to get uri: " + uri); // 여기서 exception 을 던지면
    })
    .bodyToMono(String.class);
```

스프링 프레임워크 내 DefaultWebClient 에서 exception 을 catch 하지 않아서 이후 코드가 실행되지 않습니다.

```java
private <T extends Publisher<?>> T handleBody(ClientResponse response,
        T bodyPublisher, Function<Mono<? extends Throwable>, T> errorFunction) {

    if (HttpStatus.resolve(response.rawStatusCode()) != null) {
        for (StatusHandler handler : this.statusHandlers) {
            if (handler.test(response.statusCode())) {
                HttpRequest request = this.requestSupplier.get();
                Mono<? extends Throwable> exMono = handler.apply(response, request); // 여기서 exception 발생해서 drainBody 를 실행하지 못함
                exMono = exMono.flatMap(ex -> drainBody(response, ex));
                exMono = exMono.onErrorResume(ex -> drainBody(response, ex));
                return errorFunction.apply(exMono);
            }
        }
        return bodyPublisher;
    }
    else {
        return errorFunction.apply(createResponseException(response, this.requestSupplier.get()));
    }
}
```

## 해결
[이 이슈](https://github.com/spring-projects/spring-framework/issues/22005#issuecomment-453479197)를 보면, 
일반적으로 어플리케이션에서 body 를 consume 할 것으로 기대하기 때문에 버그는 아니지만 개선하겠다고 답변합니다.

스프링 5.1.9.RELEASE 에서 [이 커밋](https://github.com/spring-projects/spring-framework/commit/2aec175ccc5a5c3cbcd152697956ee5da90e5214)으로 수정이 되었습니다.  
스프링 5.1.9.RELEASE 이상을 사용한다면 더 이상 같은 문제가 발생하지 않습니다. (exception 던지는 게 좋은 코드는 아니지만)

## 교훈
- WebClient 사용할 때 에러 상황에서 body 가 정상적으로 consume 되는지 확인해야 함
- Mono/Flux 사용시 exception 을 바로 던지기보다는 Mono.error() 등을 사용해서 에러를 제어하는 편이 좋음

## 참고 자료
- https://github.com/reactor/reactor-netty/issues/119
- https://github.com/reactor/reactor-netty/issues/422
- https://github.com/spring-projects/spring-framework/issues/23230
- https://github.com/spring-projects/spring-framework/commit/2aec175ccc5a5c3cbcd152697956ee5da90e5214
