# react-rpc
## 定义rpc请求接口
```java
@RpcClient("demo")
public interface ApiTest extends BaseApiTest {

    @PostMapping("/api/login")
    Mono<Map<String, Object>> testMono(@RequestParam("username") String username,
                                       @RequestParam("password") String password);

    @PostMapping("/api/login")
    AsyncMono<Map<String, Object>> testAsyncMono(@RequestParam("username") String username,
                                                 @RequestParam("password") String password);

    @PostMapping("/api/login")
    CompletableFuture<Map<String, Object>> testCompletableFuture(@RequestParam("username") String username,
                                                                 @RequestParam("password") String password);
}
```
## 测试
```java
@RestController
@EnableReactRpc
@EnableDiscoveryClient
@SpringBootApplication
public class TestApp {

    public static void main(String[] args) {
        SpringApplication.run(TestApp.class, args);
    }

    @Autowired
    private ApiTest apiTest;

    @GetMapping("/test")
    public DeferredResult<Map<String, Object>> test() {
        AsyncMono<Map<String, Object>> asyncMono = apiTest.testAsyncMono("test", "test");
        return AsyncResult.create(asyncMono, 30 * 1000);
    }
}
```
