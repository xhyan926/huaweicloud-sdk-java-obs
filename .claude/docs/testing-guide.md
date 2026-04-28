# OBS Java SDK 测试指导

本文档为 OBS Java SDK 项目提供全面的测试实践指南，涵盖测试哲学、组织策略、工具使用和最佳实践。

## 目录

- [测试分层策略](#测试分层策略)
- [测试工具栈](#测试工具栈)
- [测试组织结构](#测试组织结构)
- [命名约定](#命名约定)
- [AAA 模式](#aaa-模式)
- [断言最佳实践](#断言最佳实践)
- [参数化测试](#参数化测试)
- [异常测试](#异常测试)
- [Mock 与隔离](#mock-与隔离)
- [测试生命周期管理](#测试生命周期管理)
- [测试覆盖率](#测试覆盖率)
- [测试反模式](#测试反模式)
- [常见场景模板](#常见场景模板)

---

## 测试分层策略

项目采用经典的测试金字塔策略：

```
        ╱ 集成测试 (IT) ╲           ← 少量，验证端到端交互
       ╱──────────────────╲
      ╱   功能测试 (FT)     ╲        ← 适中，验证业务流程
     ╱────────────────────────╲
    ╱      单元测试 (Unit Test) ╲     ← 大量，快速验证逻辑正确性
   ╱──────────────────────────────╲
```

| 层级 | 文件位置 | 命名后缀 | 运行命令 | 职责 |
|------|----------|----------|----------|------|
| 单元测试 | `src/test/java/` | `*Test.java` | `mvn test` | 验证单个类/方法的逻辑正确性 |
| 集成测试 | `src/it/java/` | `*IT.java` | `mvn failsafe:integration-test` | 验证与 OBS 服务端的真实交互 |

### 核心原则

1. **单元测试优先**：所有新增业务逻辑必须有对应的单元测试
2. **单元测试不依赖外部服务**：需要外部依赖时使用 Mock 隔离
3. **集成测试验证真实行为**：断言必须基于服务端实际响应，不能基于假设
4. **每层独立可运行**：单元测试和集成测试可以独立执行

---

## 测试工具栈

项目当前使用的测试框架：

| 工具 | 版本 | 用途 |
|------|------|------|
| JUnit 4 | 4.13.2 | 测试框架（@Test, @Before, @After, @Rule, @RunWith） |
| Mockito | 3.12.4 | Mock 框架（mock(), when(), verify()） |
| PowerMock | 2.0.9 | 静态方法/私有方法 Mock（谨慎使用） |
| JUnit Rules | — | 测试生命周期管理（TestName, ExpectedException, TemporaryFolder） |

### Maven 依赖（已配置）

```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>3.12.4</version>
    <scope>test</scope>
</dependency>
```

---

## 测试组织结构

### 包结构

单元测试和集成测试应镜像源码的包结构：

```
src/test/java/com/obs/
├── services/
│   ├── internal/                          # 内部服务层测试
│   │   ├── ObsPropertiesTest.java        # 配置解析测试
│   │   ├── ObsConvertorTest.java         # 转换器测试
│   │   ├── XmlResponsesSaxParserTest.java
│   │   └── util/                          # 工具类测试
│   │       ├── ServiceUtilsTest.java
│   │       ├── RestUtilsTest.java
│   │       └── ObjectUtilsTest.java
│   ├── model/                             # 模型/DTO 测试
│   │   ├── Qos/                           # QoS 相关模型
│   │   ├── lifecycle/                     # 生命周期配置
│   │   ├── cors/                          # CORS 配置
│   │   ├── compress/                      # 压缩策略
│   │   └── objectlock/                    # 对象锁
│   └── internal/task/                     # 任务类测试
├── test/                                  # 测试基础设施
│   ├── TestTools.java                    # 通用测试工具
│   └── tools/
│       ├── PrepareTestBucket.java        # @Rule 桶管理
│       └── PropertiesTools.java          # 配置加载

src/it/java/com/obs/
├── integrated_test/                       # 端到端集成测试
│   ├── buckets/                           # 桶级操作 IT
│   ├── objectlock/                        # 对象锁 IT
│   └── symlink/                           # 软链接 IT
└── services/internal/                     # 内部服务 IT
```

### 一个测试类的基本结构

```java
package com.obs.services.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * {@link SomeModel} 的单元测试。
 */
public class SomeModelTest {

    @Test
    public void should_returnCorrectValue_when_validInput() {
        // Arrange
        SomeModel model = new SomeModel("input");

        // Act
        String result = model.getValue();

        // Assert
        assertEquals("expected", result);
    }
}
```

---

## 命名约定

### 测试类命名

| 被测类 | 测试类 |
|--------|--------|
| `ObsProperties` | `ObsPropertiesTest` |
| `QosRule` | `QosRuleTest` |
| `BucketCors` | `BucketCorsIT`（集成测试） |
| `AbstractBucketClient` | `AbstractBucketClientQosUnitTest` |

### 测试方法命名

格式：`should_[ExpectedBehavior]_when_[Condition]`

```java
// 好的命名 —— 一目了然测试的意图和预期
should_return_true_and_cancel_handler_when_cancel_with_handler()
should_throw_exception_when_concurrentLimit_is_negative()
should_parse_json_fields_when_content_type_is_application_json()

// 不好的命名 —— 无法从名称理解测试意图
testCancel()
testConfig1()
testLogin()
```

### 集成测试方法命名

集成测试采用 `test_SDK_[功能]_[序号]` 的格式，便于与测试用例管理系统对应：

```java
test_SDK_cors_001()     // CORS 配置 CRUD 测试
test_SDK_cors_002()     // CORS 完整生命周期测试
test_SDK_cors_003()     // 多规则配置测试
test_SDK_cors_004()     // 错误场景测试
```

---

## AAA 模式

每个测试方法应遵循 **Arrange-Act-Assert** 三段式结构：

```java
@Test
public void should_update_properties_when_setters_called() {
    // Arrange（准备）—— 创建测试数据、设置前置条件
    QosRule qosRule = new QosRule(null, 0, null, null);
    NetworkType newNetworkType = NetworkType.EXTRANET;
    long newConcurrentLimit = 10000;

    // Act（执行）—— 调用被测方法
    qosRule.setNetworkType(newNetworkType);
    qosRule.setConcurrentRequestLimit(newConcurrentLimit);

    // Assert（断言）—— 验证结果
    assertEquals("网络类型设置不正确", newNetworkType, qosRule.getNetworkType());
    assertEquals("并发请求限制设置不正确", newConcurrentLimit, qosRule.getConcurrentRequestLimit());
}
```

### AAA 原则

1. **Arrange 要简洁**：只准备当前测试所需的最少数据
2. **Act 要明确**：通常只有一行代码调用被测方法
3. **Assert 要充分**：验证所有相关的副作用和返回值

---

## 断言最佳实践

### 基本断言选择

```java
// 使用最具体的断言方法
assertEquals(expected, actual);       // 相等性比较
assertTrue(condition);                // 布尔条件
assertNull(object);                   // null 检查
assertSame(expected, actual);         // 引用相等（同一对象）

// 带消息的断言 —— 当失败原因不明显时使用
assertEquals("网络类型不匹配", networkType, qosRule.getNetworkType());
assertTrue("Expected 200 or 201, got: " + response.getStatusCode(),
    response.getStatusCode() == 200 || response.getStatusCode() == 201);
```

### 断言原则

1. **每个测试一个核心断言概念**：但可以对同一概念的多个属性进行断言
2. **断言消息要有意义**：说明"期望什么"而非"什么错了"
3. **不要断言中间状态**：只断言与测试目的直接相关的结果
4. **使用正确的断言方法**：`assertEquals(expected, actual)` 而非 `assertTrue(expected.equals(actual))`

### 集成测试断言特殊规则

```java
// SET 操作状态码：兼容 200 和 201
assertTrue("Expected 200 or 201, got: " + response.getStatusCode(),
    response.getStatusCode() == 200 || response.getStatusCode() == 201);

// 错误场景断言：优先验证 responseCode 和 xmlMessage
catch (ObsException e) {
    assertEquals(404, e.getResponseCode());
    assertNotNull("Error body should not be null", e.getXmlMessage());
    assertNotNull("ErrorRequestId should not be null", e.getErrorRequestId());
}

// 不要假设 null —— 服务端可能返回默认值
// 错误：assertNull(result.getDays());    // 服务端可能返回 0
// 正确：assertEquals(Integer.valueOf(0), result.getDays());
```

---

## 参数化测试

### 适用场景

参数化测试**仅用于同一测试逻辑搭配不同输入参数的正交验证**。

```java
// 正确用法：同一解析方法，不同输入，相同断言逻辑
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
        ServiceException ex = new ServiceException("msg", body, contentType);
        assertEquals(expectedParseMode, ex.getParsedFromJson() ? "JSON" : "XML");
    }
}
```

### 禁止的用法

```java
// 错误：用 switch/case 分发完全不同的测试逻辑
@Parameterized.Parameter(0) public String testCategory;
@Test
public void should_verify_behavior() {
    switch (testCategory) {
        case "JSON_PARSING": runJsonTest(); break;
        case "XML_PARSING": runXmlTest(); break;
        case "TO_STRING": runToStringTest(); break;
    }
}
```

### 参数化测试命名

使用 `@Parameters(name = "{0}")` 让每组参数有可识别的名称：

```java
@Parameterized.Parameters(name = "{0}-{2}")
public static Collection<Object[]> testData() {
    return Arrays.asList(new Object[][] {
        {"OBS", AuthTypeEnum.OBS, "OBJECT", BucketTypeEnum.OBJECT},
        {"V2", AuthTypeEnum.V2, "OBJECT", BucketTypeEnum.OBJECT},
    });
}
// 输出: test[OBS-OBJECT], test[V2-OBJECT]
```

### 判断标准

| 场景 | 使用参数化 | 使用独立 @Test |
|------|-----------|---------------|
| 同一方法，不同输入值 | 是 | 否 |
| 同一方法，不同边界值 | 是 | 否 |
| 不同方法 | 否 | 是 |
| 不同代码路径 | 否 | 是 |
| 不同断言结构 | 否 | 是 |

**核心判断**：如果每组参数需要不同的 setup/exercise/verify 步骤，则不应使用参数化测试。

---

## 异常测试

### 方式一：@Test(expected = ...)（简单场景）

```java
@Test(expected = IllegalArgumentException.class)
public void should_throw_exception_when_invalidProperty() {
    ObsProperties obsProperties = new ObsProperties();
    obsProperties.setProperty("testKey", "ff");
    obsProperties.getBoolProperty("testKey", true);
}
```

**限制**：无法验证异常消息或异常后的状态。

### 方式二：@Rule ExpectedException（需要验证异常消息）

```java
@Rule
public ExpectedException expectedException = ExpectedException.none();

@Test
public void should_throw_exception_when_concurrentLimit_is_negative() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("concurrentRequestLimit value cannot be negative: -100");

    new QosRule(NetworkType.INTRANET, -100, qpsConfig, bpsConfig);
}
```

### 方式三：try-catch（需要验证异常后的状态）

```java
@Test
public void should_preserve_value_when_setting_negative_concurrentLimit() {
    QosRule qosRule = new QosRule(NetworkType.EXTRANET, 1000, null, null);
    long originalLimit = qosRule.getConcurrentRequestLimit();

    try {
        qosRule.setConcurrentRequestLimit(-500);
        fail("Should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException e) {
        // 验证原始值未被修改
        assertEquals("设置负数后原始值不应改变", originalLimit, qosRule.getConcurrentRequestLimit());
    }
}
```

### 异常测试选择指南

| 需求 | 推荐方式 |
|------|----------|
| 仅验证异常类型 | `@Test(expected = ...)` |
| 验证异常类型 + 消息 | `@Rule ExpectedException` |
| 验证异常后的对象状态 | try-catch + fail |
| 验证异常中的多个属性 | try-catch + fail |

---

## Mock 与隔离

### 何时使用 Mock

| 场景 | 是否 Mock |
|------|----------|
| 被测类的协作者（依赖） | 是 |
| 被测类自身的方法 | 否 |
| 外部服务（网络、数据库） | 是 |
| 简单的值对象（DTO/Model） | 否 |
| 静态工具方法 | 通常不需要 |

### 项目中的 Mock 使用模式

```java
// 使用 Mockito.mock() 创建 mock 对象
AbstractClient mockClient = mock(AbstractClient.class);

// 将 mock 对象注入被测类
DownloadFileTask task = new DownloadFileTask(mockClient, "test-bucket", request, null);

// 验证被测类行为（不依赖 mock 的返回值时）
boolean result = task.cancel();
assertTrue("cancel() should return true", result);
```

### Mock 原则

1. **最小化 Mock 范围**：只 Mock 直接依赖，不 Mock 传递依赖
2. **优先使用真实对象**：简单值对象和纯函数不需要 Mock
3. **Mock 是最后的手段**：如果能通过构造真实对象来测试，就不要 Mock
4. **避免 Mock 被测类自身**：不要 Mock 被测类的部分方法来测试其他方法

### PowerMock 使用注意

PowerMock 用于 Mock 静态方法和私有方法，仅在必要时使用：

```java
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceUtils.class})  // 明确声明需要 Mock 的静态类
public class SomeTest {
    // ...
}
```

**注意**：PowerMock 与 Mockito 的版本兼容性需要确认。优先考虑重构代码使依赖可注入，而非使用 PowerMock。

---

## 测试生命周期管理

### @Before 和 @After

```java
public class SomeTest {
    private ObsClient obsClient;
    private String bucketName;

    @Before
    public void setUp() throws IOException {
        // 每个测试方法前执行：初始化测试环境
        obsClient = TestTools.getPipelineEnvironment();
        bucketName = TestTools.generateBucketName(testName.getMethodName());
    }

    @After
    public void tearDown() {
        // 每个测试方法后执行：清理资源
        if (obsClient != null && bucketName != null) {
            TestTools.delete_bucket(obsClient, bucketName);
        }
    }
}
```

### @Rule 使用

#### TestName —— 获取当前测试方法名

```java
@Rule
public TestName testName = new TestName();

@Test
public void test_SDK_cors_001() {
    String methodName = testName.getMethodName();
    // → "test_SDK_cors_001[OBS-OBJECT]"
}
```

#### ExpectedException —— 灵活的异常断言

```java
@Rule
public ExpectedException expectedException = ExpectedException.none();
```

#### PrepareTestBucket —— 集成测试桶管理

```java
@Rule
public PrepareTestBucket prepareTestBucket = new PrepareTestBucket();
// 自动创建桶（测试方法名转换）和清理桶
```

### 生命周期原则

1. **@Before 做初始化**：创建测试所需的全部前置条件
2. **@After 做清理**：即使测试失败也能执行清理
3. **@Rule 管理共享资源**：桶等需要跨测试方法的资源由 @Rule 统一管理
4. **不要在 @Before/@After 和 @Rule 中重复管理同一资源**

---

## 测试覆盖率

### 目标

- **新增代码可达分支覆盖率必须达到 100%**（不含经分析确认的不可达防御性分支）
- 每个公共方法的所有可达路径都应有对应的测试用例

### 覆盖率维度

| 维度 | 含义 | 优先级 |
|------|------|--------|
| 行覆盖率 | 代码行是否被执行 | 基线 |
| 分支覆盖率 | if/else 分支是否全部走到 | **必须** |
| 条件覆盖率 | 组合条件的子条件是否全覆盖 | 推荐 |

### 确保覆盖率的方法

```java
// 覆盖构造函数
@Test
public void should_create_instance_when_valid_params() {
    SomeClass obj = new SomeClass("param");
    assertNotNull(obj);
}

// 覆盖 getter/setter
@Test
public void should_return_value_when_getter_called() {
    SomeClass obj = new SomeClass();
    obj.setValue("test");
    assertEquals("test", obj.getValue());
}

// 覆盖 null/边界输入
@Test
public void should_handle_null_gracefully_when_input_is_null() {
    String result = ServiceUtils.toHex(null);
    assertNull(result);
}

// 覆盖异常路径
@Test(expected = IllegalArgumentException.class)
public void should_throw_when_invalid_input() {
    new SomeClass(-1);
}

// 覆盖 equals/hashCode/toString
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
    assertFalse(rule.equals("string"));
}
```

---

## 测试反模式

### 1. 测试私有方法

```java
// 反模式：通过反射测试私有方法
@Test
public void testPrivateMethod() throws Exception {
    Method method = MyClass.class.getDeclaredMethod("privateMethod");
    method.setAccessible(true);
    Object result = method.invoke(myClass);
}

// 正确做法：通过公共 API 间接测试私有方法的逻辑
@Test
public void should_calculate_correctly_when_valid_input() {
    // 公共方法内部调用了私有方法
    assertEquals(expected, myClass.publicMethod(input));
}
```

### 2. 测试间的隐式依赖

```java
// 反模式：测试 B 依赖测试 A 的执行结果
@Test
public void testA() {
    sharedState.value = "modified";
}

@Test
public void testB() {
    // 依赖 testA 先执行
    assertEquals("modified", sharedState.value);
}

// 正确做法：每个测试独立准备自己的数据
@Test
public void should_read_modified_state() {
    sharedState.value = "modified";  // 自己设置前置条件
    assertEquals("modified", sharedState.value);
}
```

### 3. 过度 Mock

```java
// 反模式：Mock 了几乎所有东西
@Test
public void test() {
    SomeModel model = mock(SomeModel.class);
    when(model.getName()).thenReturn("test");
    when(model.getValue()).thenReturn(100);
    // 实际上什么都没测到
}

// 正确做法：使用真实对象
@Test
public void test() {
    SomeModel model = new SomeModel("test", 100);
    // 测试真实的行为
}
```

### 4. 捕获异常后不做断言

```java
// 反模式：吞掉异常
@Test
public void test() {
    try {
        myClass.doSomething();
    } catch (Exception e) {
        // 什么都不做
    }
}

// 正确做法：至少 fail 或 assert
@Test
public void test() {
    try {
        myClass.doSomething();
        fail("Should have thrown exception");
    } catch (IllegalArgumentException e) {
        assertEquals("expected message", e.getMessage());
    }
}
```

### 5. 一个测试验证过多行为

```java
// 反模式：巨型测试方法
@Test
public void testEverything() {
    // 验证构造函数
    // 验证 getter
    // 验证 setter
    // 验证 equals
    // 验证 toString
    // 验证业务方法
    // 50行断言...
}

// 正确做法：拆分为多个独立测试
@Test public void should_initialize_when_valid_params() { /* 构造 */ }
@Test public void should_return_value_when_getter_called() { /* getter */ }
@Test public void should_update_value_when_setter_called() { /* setter */ }
```

### 6. 集成测试断言基于假设

```java
// 反模式：假设服务端返回 null
assertNull(result.getDays());  // 服务端实际返回 0

// 正确做法：基于实际响应验证
assertEquals(Integer.valueOf(0), result.getDays());
```

---

## 常见场景模板

### 场景一：测试 Model/DTO 类

```java
public class SomeModelTest {
    @Test
    public void should_initialize_with_all_fields_when_constructor_called() {
        SomeModel model = new SomeModel("name", 100, Type.ACTIVE);
        assertEquals("name", model.getName());
        assertEquals(100, model.getValue());
        assertEquals(Type.ACTIVE, model.getType());
    }

    @Test
    public void should_update_fields_when_setters_called() {
        SomeModel model = new SomeModel();
        model.setName("test");
        model.setValue(50);
        assertEquals("test", model.getName());
        assertEquals(50, model.getValue());
    }

    @Test
    public void should_be_equal_when_same_fields() {
        SomeModel a = new SomeModel("name", 100, Type.ACTIVE);
        SomeModel b = new SomeModel("name", 100, Type.ACTIVE);
        assertTrue(a.equals(b));
    }

    @Test
    public void should_not_be_equal_when_different_type() {
        SomeModel a = new SomeModel("name", 100, Type.ACTIVE);
        assertFalse(a.equals("string"));
    }

    @Test
    public void should_not_be_equal_when_null() {
        SomeModel a = new SomeModel("name", 100, Type.ACTIVE);
        assertFalse(a.equals(null));
    }

    @Test
    public void should_include_fields_in_toString() {
        SomeModel model = new SomeModel("test", 100, Type.ACTIVE);
        String str = model.toString();
        assertTrue(str.contains("test"));
        assertTrue(str.contains("100"));
    }
}
```

### 场景二：测试工具/服务类

```java
public class ServiceUtilsTest {
    @Test
    public void should_return_valid_result_when_input_is_valid() {
        // 正常路径
        String result = ServiceUtils.toHex(new byte[]{0x0A});
        assertEquals("0a", result);
    }

    @Test
    public void return_empty_when_input_is_empty() {
        // 边界：空输入
        String result = ServiceUtils.toHex(new byte[]{});
        assertEquals("", result);
    }

    @Test
    public void should_return_null_when_input_is_null() {
        // 边界：null 输入
        String result = ServiceUtils.toHex(null);
        assertNull(result);
    }
}
```

### 场景三：集成测试（参数化 + 认证类型）

```java
@RunWith(Parameterized.class)
public class SomeFeatureIT {
    @Rule
    public TestName testName = new TestName();

    @Parameterized.Parameter()
    public String authTypeName;
    @Parameterized.Parameter(1)
    public AuthTypeEnum authType;
    @Parameterized.Parameter(2)
    public String bucketTypeName;
    @Parameterized.Parameter(3)
    public BucketTypeEnum bucketType;

    @Parameterized.Parameters(name = "{0}-{2}")
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][] {
            {"OBS", AuthTypeEnum.OBS, "OBJECT", BucketTypeEnum.OBJECT},
            {"OBS", AuthTypeEnum.OBS, "POSIX", BucketTypeEnum.PFS},
            {"V2", AuthTypeEnum.V2, "OBJECT", BucketTypeEnum.OBJECT},
            {"V4", AuthTypeEnum.V4, "OBJECT", BucketTypeEnum.OBJECT},
        });
    }

    private ObsClient obsClient;
    private String bucketName;

    @Before
    public void setUp() throws IOException {
        obsClient = TestTools.getPipelineEnvironmentByAuthType(authType);
        assertNotNull("ObsClient should not be null", obsClient);
        bucketName = TestTools.generateBucketName(testName.getMethodName());
        // 创建桶...
    }

    @After
    public void tearDown() {
        if (obsClient != null && bucketName != null) {
            TestTools.delete_bucket(obsClient, bucketName);
        }
    }

    @Test
    @AIGenerated(author = "git_user_name", date = "2026-04-28",
        description = "集成测试：功能描述")
    public void test_SDK_feature_001() {
        // Arrange + Act + Assert
    }
}
```

### 场景四：测试需要 Mock 的类

```java
public class DownloadFileTaskTest {
    @Test
    public void should_return_true_when_cancel_with_handler() {
        // Arrange
        AbstractClient mockClient = mock(AbstractClient.class);
        DownloadFileRequest request = new DownloadFileRequest("bucket", "key");
        CallCancelHandler handler = new CallCancelHandler();
        request.setCancelHandler(handler);
        DownloadFileTask task = new DownloadFileTask(mockClient, "bucket", request, null);

        // Act
        boolean result = task.cancel();

        // Assert
        assertTrue("cancel() should return true when handler is set", result);
        assertTrue("handler should be cancelled", handler.isCancelled());
    }
}
```

---

## 快速参考卡片

| 规则 | 要点 |
|------|------|
| 命名 | `should_X_when_Y`（单元测试）、`test_SDK_feature_NNN`（集成测试） |
| 结构 | Arrange-Act-Assert 三段式 |
| 独立性 | 每个测试独立准备数据，不依赖其他测试的执行顺序 |
| 覆盖率 | 新增代码可达分支 100% |
| 参数化 | 仅用于同一逻辑不同参数，禁止 switch/case 分发 |
| Mock | 最小范围，优先真实对象 |
| 异常 | 选择合适的异常测试方式（expected/Rule/try-catch） |
| @AIGenerated | AI 生成的测试方法必须添加注解 |
| 编译验证 | 所有测试生成后必须通过 `mvn compile test-compile` |
| 运行验证 | 单元测试 `mvn test`，集成测试 `mvn failsafe:integration-test` |
