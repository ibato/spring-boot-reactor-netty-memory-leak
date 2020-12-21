spring & reactor-netty 조합으로 개발하다가 만난 Bytebuf leak 사례를 적어둡니다.
`LEAK: ByteBuf.release() was not called before it's garbage-collected.`

- [Spring Boot 2.1.6 > onStatus() 에서 exception 을 던질 때](./spring-boot-2.1.6/README.md)
- [Spring Boot 2.2.2 > toBodilessEntity() 를 사용할 때](./spring-boot-2.2.2/README.md)