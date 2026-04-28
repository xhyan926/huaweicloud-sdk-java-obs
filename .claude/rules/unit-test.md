# 单元测试规则

**应用场景**: 当编写、修改、审查单元测试（`*Test.java`）时

## 禁止事项

1. **禁止不遵循 AAA 结构**
   - 每个测试方法必须清晰地分为 Arrange（准备）、Act（执行）、Assert（断言）三段
   - 不得在 Act 之后穿插新的准备逻辑
   - 不得在 Assert 之后执行新的业务操作
   - 每段之间用空行分隔，提高可读性

2. **禁止违反方法命名规范**
   - 单元测试方法命名必须遵循 `should_[ExpectedBehavior]_when_[Condition]` 格式
   - 不得使用模糊命名如 `testMethod1`、`testConfig`、`testCase`
   - 方法名应能独立表达测试意图，无需阅读测试代码即可理解

3. **禁止测试间的隐式依赖**
   - 每个测试方法必须独立运行，不依赖其他测试的执行顺序或副作用
   - 不得通过共享可变状态在测试之间传递数据
   - `@Before` 中准备的数据必须足以支撑当前测试独立运行
   - `@After` 必须清理当前测试产生的所有副作用

4. **禁止一个测试方法验证多个不相关的行为**
   - 每个测试方法应验证一个明确的行为或一个逻辑概念
   - 构造函数测试、getter/setter 测试、业务方法测试、equals/hashCode 测试必须拆分为独立的 `@Test` 方法
   - 不得在一个巨型测试方法中验证被测类的所有功能

5. **禁止在参数化测试中使用 switch/case 分发不同测试分支**
   - 参数化测试仅用于**同一测试逻辑**搭配不同输入参数的正交验证
   - 不同测试场景（不同被测方法、不同代码路径、不同断言结构）必须拆分为独立的 `@Test` 方法
   - 不得在单个 `@Test` 方法内通过 `switch(testCategory)` 或 `if(testCategory)` 分发到完全不同的 private 辅助方法
   - 判断标准：如果每组参数需要不同的 setup / exercise / verify 步骤，则不应使用参数化测试

6. **禁止遗漏可达分支的覆盖**
   - 新增代码可达分支覆盖率必须达到 100%（不含经分析确认的不可达防御性分支）
   - 不得只测试 happy path 而忽略边界值、null 输入、异常路径
   - 以下分支必须覆盖：
     - null 输入处理
     - 空集合/空字符串处理
     - 数值边界（0、负数、最大值）
     - 异常抛出路径
     - equals/hashCode/toString 方法
     - 枚举类型的所有枚举值

7. **禁止缺少断言的测试**
   - 每个测试方法必须包含至少一个断言
   - 不得只调用方法而不验证结果（`obj.doSomething()` 后没有任何 assert）
   - 不得用 `System.out.println` 代替断言来"验证"结果
   - try-catch 捕获异常后必须调用 `fail()` 或进行异常断言，不得静默吞掉异常

8. **禁止测试私有方法**
   - 不得通过反射 (`setAccessible(true)`) 直接测试私有方法
   - 私有方法的逻辑应通过公共 API 间接验证
   - 如果私有方法过于复杂需要独立测试，说明被测类职责过多，应考虑重构

9. **禁止过度 Mock**
   - 不得 Mock 简单的值对象（DTO、Model、Configuration 等纯数据类）
   - 不得 Mock 被测类自身的其他方法（这会导致测试与实现细节耦合）
   - 不得 Mock 链式调用（`when(a.getB().getC()).thenReturn(...)`），应重构被测代码降低耦合
   - Mock 的对象必须是被测类的直接协作者（依赖），不得 Mock 传递依赖

10. **禁止AI生成的测试方法缺少 @AIGenerated 注解**
    - 所有AI生成的测试方法必须添加 `com.obs.aitool.AIGenerated` 注解
    - `author` 字段必须使用当前 git 用户名（通过 `git config user.name` 获取）
    - `date` 字段必须使用当前系统日期（YYYY-MM-DD 格式）
    - `description` 字段应简要描述测试意图

## 正向示例

```java
// 正确的 AAA 结构和命名
@Test
public void should_return_true_and_cancel_handler_when_cancel_with_handler() {
    // Arrange
    AbstractClient mockClient = mock(AbstractClient.class);
    DownloadFileRequest request = new DownloadFileRequest("test-bucket", "test-key");
    CallCancelHandler handler = new CallCancelHandler();
    request.setCancelHandler(handler);
    DownloadFileTask task = new DownloadFileTask(mockClient, "test-bucket", request, null);

    // Act
    boolean result = task.cancel();

    // Assert
    assertTrue("cancel() should return true when cancelHandler is not null", result);
    assertTrue("cancelHandler should be cancelled after calling cancel()", handler.isCancelled());
}

// 正确的独立测试方法（不使用参数化 switch/case）
@Test
public void should_initialize_rule_when_valid_params_provided() { /* 构造函数测试 */ }

@Test
public void should_update_properties_when_setters_called() { /* setter 测试 */ }

@Test
public void should_throw_exception_when_concurrentLimit_is_negative() { /* 异常路径测试 */ }

// 正确的参数化测试：同一逻辑，不同参数
@RunWith(Parameterized.class)
public class ContentTypeParsingTest {
    @Parameterized.Parameter(0) public String testName;
    @Parameterized.Parameter(1) public String contentType;
    @Parameterized.Parameter(2) public String expectedParseMode;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][] {
            {"JsonWithCharset", "application/json;charset=utf-8", "JSON"},
            {"JsonPlain", "application/json", "JSON"},
            {"XmlContentType", "application/xml", "XML"},
            {"NullContentType", null, "XML"}
        });
    }

    @Test
    public void should_select_correct_parser_when_content_type_varies() {
        // 同一断言逻辑，仅输入和期望输出不同
        ServiceException ex = new ServiceException("msg", body, contentType);
        assertEquals(expectedParseMode, ex.getParsedFromJson() ? "JSON" : "XML");
    }
}

// 正确的异常测试：使用 try-catch 验证异常后的状态
@Test
public void should_preserve_value_when_setting_negative_concurrentLimit() {
    QosRule qosRule = new QosRule(NetworkType.EXTRANET, 1000, null, null);
    long originalLimit = qosRule.getConcurrentRequestLimit();

    try {
        qosRule.setConcurrentRequestLimit(-500);
        fail("Should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException e) {
        assertEquals(originalLimit, qosRule.getConcurrentRequestLimit());
    }
}

// 正确的边界值覆盖
@Test
public void should_return_null_when_input_is_null() {
    assertNull(ServiceUtils.toHex(null));
}

@Test
public void should_return_empty_string_when_input_is_empty_array() {
    assertEquals("", ServiceUtils.toHex(new byte[]{}));
}

// 正确的 equals 测试覆盖
@Test
public void should_be_equal_when_same_id() {
    Rule rule1 = config.new Rule("a", "", true);
    Rule rule2 = config.new Rule("a", "", true);
    assertTrue(rule1.equals(rule2));
}

@Test
public void should_not_be_equal_when_null() {
    Rule rule = config.new Rule("a", "", true);
    assertFalse(rule.equals(null));
}

@Test
public void should_not_be_equal_when_different_type() {
    Rule rule = config.new Rule("a", "", true);
    assertFalse(rule.equals("not a rule"));
}

// 正确的 @AIGenerated 注解
@Test
@AIGenerated(author = "yanliwei", date = "2026-04-25",
    description = "Test cancel returns true and cancels handler when cancelHandler is set")
public void should_return_true_and_cancel_handler_when_cancel_with_handler() {
    // ...
}
```

## 反向示例

```java
// 错误：缺少 AAA 结构，准备和断言混在一起
@Test
public void testSomething() {
    obj.setValue(10);
    assertEquals(10, obj.getValue());
    obj.setName("test");       // Act 之后又做了新的准备
    assertEquals("test", obj.getName());
}

// 错误：模糊的命名，无法理解测试意图
@Test
public void testMethod1() { /* ??? */ }

// 错误：一个测试验证所有功能
@Test
public void testEverything() {
    // 构造函数测试
    QosRule rule = new QosRule(NetworkType.INTRANET, 100, null, null);
    // getter 测试
    assertEquals(NetworkType.INTRANET, rule.getNetworkType());
    // setter 测试
    rule.setNetworkType(NetworkType.EXTRANET);
    assertEquals(NetworkType.EXTRANET, rule.getNetworkType());
    // 异常测试
    try { rule.setConcurrentRequestLimit(-1); } catch (Exception e) {}
    // toString 测试
    assertNotNull(rule.toString());
    // ... 50 行断言
}

// 错误：参数化测试用 switch/case 分发（违反规则 #5）
@Parameterized.Parameter(0) public String testCategory;
@Test
public void should_verify_behavior() {
    switch (testCategory) {
        case "INITIAL_STATE": runInitialStateTest(); break;
        case "PAUSE": runPauseTest(); break;
        case "CANCEL": runCancelTest(); break;
    }
}

// 错误：缺少断言
@Test
public void testMethod() {
    ServiceUtils.toHex(new byte[]{1, 2, 3});
    // 没有任何断言！
}

// 错误：用 println 代替断言
@Test
public void testMethod() {
    String result = ServiceUtils.toHex(new byte[]{1});
    System.out.println(result);  // 不是断言
}

// 错误：静默吞掉异常
@Test
public void testException() {
    try {
        myClass.doSomething();
    } catch (Exception e) {
        // 什么都不做
    }
}

// 错误：通过反射测试私有方法
@Test
public void testPrivateMethod() throws Exception {
    Method method = MyClass.class.getDeclaredMethod("internalMethod");
    method.setAccessible(true);
    Object result = method.invoke(myClass);
}

// 错误：过度 Mock —— Mock 了简单的值对象
@Test
public void test() {
    SomeModel model = mock(SomeModel.class);  // 不应该 Mock
    when(model.getName()).thenReturn("test");
    // 实际上什么都没测到
}

// 错误：测试间通过共享状态依赖
static String sharedValue;

@Test
public void testA() {
    sharedValue = "modified";
}

@Test
public void testB() {
    assertEquals("modified", sharedValue);  // 依赖 testA 先执行
}
```

## 测试编写检查清单

- [ ] 方法命名遵循 `should_X_when_Y` 格式
- [ ] 测试结构清晰分为 Arrange-Act-Assert 三段
- [ ] 每个测试独立运行，不依赖其他测试
- [ ] 每个测试只验证一个明确的行为
- [ ] 参数化测试无 switch/case 分发（仅用于同一逻辑不同参数）
- [ ] 可达分支覆盖率达到 100%
- [ ] 包含 null、空值、边界值、异常路径测试
- [ ] 断言充分且有意义
- [ ] 异常被正确捕获并断言（无静默吞异常）
- [ ] 不测试私有方法
- [ ] Mock 范围最小化
- [ ] AI 生成代码有 @AIGenerated 注解
- [ ] 编译通过：`mvn compile test-compile -s /usr/local/Maven/conf/settings.xml -T 1C`
- [ ] 测试通过：`mvn test -Dtest=TestClassName -s /usr/local/Maven/conf/settings.xml -T 1C`
