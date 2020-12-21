## 프레임워크 버전
- Spring Boot 2.2.2.RELEASE
- Spring Framework 5.2.2.RELEASE
- Reactor Netty 0.9.2.RELEASE

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
`toBodilessEntity()` 를 사용할 때,
```java
webClient.get()
	.uri(uri)
	.retrieve()
	.toBodilessEntity();
```

스프링 프레임워크 내 DefaultWebClient 에서 releaseBody() 메소드가 실행되지 않습니다. ((no-op) call)

```java
@Override
public Mono<ResponseEntity<Void>> toBodilessEntity() {
	return this.responseMono.flatMap(response ->
	WebClientUtils.toEntity(response, handleBodyMono(response, Mono.<Void>empty()))
	.doOnNext(entity -> response.releaseBody()) // releaseBody() 는 Mono 인데 subscribe 하는 곳이 없음
	);
}

@Override
public Mono<Void> releaseBody() { 
	return body(BodyExtractors.toDataBuffers())
	.map(DataBufferUtils::release) // 이 메소드가 실제로는 호출되지 않음
	.then();
}
```

## 해결
`toBodiless()` 메소드를 쓰는 한 우회할 방법은 없습니다.

스프링 5.2.6.RELEASE 에서 [이 커밋](https://github.com/spring-projects/spring-framework/commit/1822f272c754953c2527b5344b14cecfc5b76a9f)으로 수정이 되었습니다.
스프링 5.2.6.RELEASE, Spring Boot 2.2.7 이상을 사용한다면 더 이상 같은 문제가 발생하지 않습니다.

## 교훈
- 이건 우리가 할 만한게... Bytebuf leak 을 잘 살피자(?)

## 참고 자료
- https://github.com/spring-projects/spring-framework/issues/24788
- https://github.com/spring-projects/spring-framework/commit/1822f272c754953c2527b5344b14cecfc5b76a9f