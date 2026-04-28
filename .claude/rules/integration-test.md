# 集成测试规则

**应用场景**: 当编写、修改、调试集成测试（`*IT.java`）或新增 `SpecialParamEnum` 子资源参数时

## 禁止事项

1. **禁止遗漏签名白名单注册**
   - 新增 `SpecialParamEnum` 枚举值后，必须同步将其 `stringCode` 添加到 `Constants.ALLOWED_RESOURCE_PARAMTER_NAMES` 列表中
   - 注意：常量名中的 `PARAMTER` 是代码中的实际拼写（历史原因），非笔误，不得修正为 `PARAMETER`
   - 不得只添加枚举值而忽略签名白名单，否则 V1 签名计算不会包含该子资源参数，导致服务端返回 `403 SignatureDoesNotMatch`
   - 使用 `getStringCode()` 返回值作为白名单项，确保与签名计算逻辑一致

2. **禁止硬编码配置文件路径**
   - 所有加载测试配置的组件必须与 `TestTools` 使用一致的环境选择逻辑（读取 `test.env` 系统属性）
   - 不得在新组件中硬编码 `test_data.properties` 路径
   - 配置文件路径必须通过动态拼接支持多环境切换

3. **禁止重复的测试资源生命周期管理**
   - 使用 `@Rule`（如 `PrepareTestBucket`）管理资源的创建和清理时，`@Before`/`@After` 中不得再对同一资源执行创建/删除操作
   - 资源生命周期必须由单一职责的组件统一管理，避免 `409 BucketAlreadyExists` 或 `404 NoSuchBucket`
   - 不得在测试方法和 `@Rule` 中各自创建同名桶

4. **禁止不一致的资源名称转换**
   - 所有从测试方法名生成桶名的位置，转换逻辑必须完全一致（`replace("_", "-")`、`replace("[", "")`、`replace("]", "")`、`toLowerCase`）
   - 参数化测试（`@Parameterized`）的方法名包含 `[OBS]`、`[V2]` 等中括号，不得遗漏 `[]` 的移除
   - 转换步骤的顺序必须一致：先 `replace("_", "-")` → 再 `replace("[", "")` → 再 `replace("]", "")` → 最后 `toLowerCase`

5. **禁止测试断言脱离服务端实际行为**
   - 不得假设服务端在设置 A 字段后 B 字段一定返回 `null`，应先通过实际请求验证返回值
   - 对象锁配置中，当设置 `Years` 时服务端返回 `Days=0` 而非 `Days=null`，断言必须匹配实际行为
   - 新增集成测试的断言必须基于对服务端实际响应的验证，不得基于协议文档的假设

6. **禁止假设错误响应格式与 API 格式一致**
   - JSON 格式的 API（如在线解压策略）其错误响应可能仍为 XML 格式，不得假设错误响应也是 JSON
   - 服务端会将下游服务（OEF/IAM）的错误包装在 XML `<Error>` 信封中返回，`<Code>` 可能为空，实际错误详情在 `<Message>` 中
   - 错误场景测试断言应优先验证 `getXmlMessage()`（原始响应体）和 `getErrorRequestId()` 不为空，而非强依赖 `getErrorCode()`/`getErrorMessage()`
   - 如需验证具体错误码/错误信息，应先通过诊断输出确认服务端实际返回格式后再编写断言

7. **禁止遗漏测试配置项的双路径同步**
   - 新增测试所需的配置项（如 `projectId`、`agency`）必须同时添加到 `app/src/test/resource/test_data_prod.properties` 和 `src/it/resources/test_data_prod.properties`
   - 不得只修改其中一个配置文件，否则会出现单元测试通过但集成测试因缺少配置而报错
   - 配置值必须使用实际可用的值，不得使用占位符（如 `your-project-id`），否则运行时会触发服务端 400 错误

8. **禁止对 SET 操作的 HTTP 状态码做单一值断言**
   - PUT/POST 操作的状态码可能因是否为首次创建而不同：首次创建返回 `201`，更新已有配置返回 `200`
   - 必须使用 `assertTrue(statusCode == 200 || statusCode == 201)` 兼容两种情况
   - 不得使用 `assertEquals(200, statusCode)` 硬编码单一期望值

9. **禁止AI生成的集成测试方法缺少@AIGenerated注解**
   - 所有AI生成的集成测试方法（`*IT.java` 中的 `@Test` 方法）必须添加 `com.obs.aitool.AIGenerated` 注解
   - `author` 字段必须使用当前 git 用户名，`date` 字段必须使用当前系统日期（YYYY-MM-DD 格式）
   - 通用注解规范详见 `ai-code-generation.md` 规则

## 正向示例

```java
// 正确的签名白名单注册（Constants.java）
// 1. 在 SpecialParamEnum 中添加枚举值
OBJECT_LOCK("object-lock");
// 2. 同步添加到 ALLOWED_RESOURCE_PARAMTER_NAMES
"x-obs-snapshot", "x-obs-snapshotroot", "object-lock"));

// 正确的多环境配置加载
File configFile;
{
    String env = System.getProperty("test.env", "");
    String fileName = env.isEmpty()
        ? "test_data.properties"
        : "test_data_" + env + ".properties";
    configFile = new File("./app/src/test/resource/" + fileName);
}

// 正确的资源名称转换（与 TestTools / PrepareTestBucket 一致）
String bucketName = description.getMethodName()
    .replace("_", "-")
    .replace("[", "").replace("]", "")
    .toLowerCase(Locale.ROOT);

// 正确的生命周期管理：@Rule 统一管理，@After 置空
@Rule
public PrepareTestBucket prepareTestBucket = new PrepareTestBucket();

@After
public void tearDown() {
    // 桶的创建和删除由 PrepareTestBucket @Rule 统一管理
}

// 正确的断言：基于服务端实际返回值
assertEquals(Integer.valueOf(0),
    result.getObjectLockConfiguration().getRule().getDefaultRetention().getDays());

// 正确的错误场景断言：验证原始响应体和 RequestId，不强依赖 errorCode
catch (ObsException e) {
    assertEquals(400, e.getResponseCode());
    assertNotNull("Error body should not be null", e.getXmlMessage());
    assertFalse("Error body should not be empty", e.getXmlMessage().trim().isEmpty());
    assertNotNull("ErrorRequestId should not be null", e.getErrorRequestId());
}

// 正确的 SET 操作状态码断言
assertTrue("Expected 200 or 201, got: " + response.getStatusCode(),
    response.getStatusCode() == 200 || response.getStatusCode() == 201);

// 正确的配置文件同步（两个文件必须同时添加相同配置项）
// app/src/test/resource/test_data_prod.properties
// src/it/resources/test_data_prod.properties
// 两个文件中都要有: projectId=xxx, agency=xxx
```

## 反向示例

```java
// 错误：只添加枚举值未更新签名白名单 → 403 SignatureDoesNotMatch
// SpecialParamEnum.java
OBJECT_LOCK("object-lock");
// Constants.java — 忘记添加 "object-lock" 到列表

// 错误：硬编码配置路径
File configFile = new File("./app/src/test/resource/test_data.properties");
// 无法通过 -Dtest.env=prod 切换环境

// 错误：@Rule 和测试方法重复创建桶
@Rule
public PrepareTestBucket prepareTestBucket = new PrepareTestBucket(); // 创建桶

@Test
public void test_xxx() {
    obsClient.createBucket(new CreateBucketRequest(bucketName)); // 再次创建 → 409
}

// 错误：遗漏 [] 移除，参数化测试方法名含中括号
// 方法名: test_SDK_objectlock_001[OBS]
String bucketName = description.getMethodName()
    .replace("_", "-").toLowerCase(Locale.ROOT);
// 结果: test-sdk-objectlock-001[obs] → bucketName is illegal

// 错误：断言基于假设而非实际返回值
assertNull(result.getDays()); // 服务端实际返回 Days=0，断言失败

// 错误：假设 JSON API 的错误响应也是 JSON 格式
catch (ObsException e) {
    assertNotNull(e.getErrorCode()); // 实际是 XML 错误，<Code> 为空 → null 或空串，断言失败
}

// 错误：SET 操作硬编码 200
assertEquals(200, setResponse.getStatusCode()); // 首次创建返回 201，断言失败

// 错误：只修改了一个配置文件
// app/src/test/resource/test_data_prod.properties 中添加了 agency=mirror_obs
// src/it/resources/test_data_prod.properties 中没有添加 → 集成测试读不到配置
```

## 通用最佳实践

### 测试结构规范

10. **禁止违反集成测试的 AAA 结构**
    - 每个测试方法应分为 Arrange（准备）、Act（执行）、Assert（断言）三段
    - 复杂的 setup 可以抽取为语义清晰的 private 辅助方法（如 `setBucketCorsAndAssert()`）
    - 不得在辅助方法中隐藏核心断言逻辑，测试方法本身应能清晰表达验证意图

11. **禁止测试方法间存在执行顺序依赖**
    - 每个集成测试方法必须能独立运行，不依赖其他测试方法的前置状态
    - 不得假设测试方法按声明顺序执行（JUnit 不保证执行顺序）
    - 如需清理状态，在测试方法内部自行完成 SET → GET → DELETE → GET 验证

12. **禁止缺少错误诊断输出**
    - 所有 catch 块中的 `ObsException` 应打印完整诊断信息
    - 必须使用 `printErrorDiagnostics()` 或等效方法输出：`responseCode`、`errorCode`、`errorMessage`、`xmlMessage`、`errorRequestId`
    - 不得只断言异常类型而不输出诊断信息，否则失败时无法排查

### 测试命名规范

13. **禁止违反集成测试方法命名规范**
    - 集成测试方法命名格式：`test_SDK_[功能名]_[序号]`（如 `test_SDK_cors_001`）
    - 序号按功能模块内从 001 递增
    - 方法上方的 JavaDoc 应简要描述测试场景

### 测试组织规范

14. **禁止将不相关的功能测试放在同一个 IT 类**
    - 每个 IT 类对应一个功能模块（如 `BucketCorsIT` 对应桶 CORS 功能）
    - 典型的 IT 类应包含以下场景（按序号递增）：
      - `001`：基本 SET → GET 验证
      - `002`：完整 CRUD 生命周期（SET → GET → UPDATE → GET → DELETE → GET 404）
      - `003`：多规则/边界配置测试
      - `004`：错误场景验证（不存在的桶、无效参数等）
    - 当测试方法超过 10 个时，考虑是否应拆分为多个 IT 类

15. **禁止遗漏参数化维度的认证类型覆盖**
    - 集成测试应使用 `@Parameterized` 覆盖主要认证类型（OBS、V2、V4）和桶类型（OBJECT、POSIX）
    - 参数化数据使用 `@Parameters(name = "{0}-{2}")` 确保每组参数有可识别的名称
    - 如果某个认证类型不支持该功能，在测试方法中跳过而非省略参数化维度

### 资源管理规范

16. **禁止在 @After 中遗漏 null 检查**
    - `@After` 中清理资源前必须检查客户端和桶名是否为 null
    - 正确模式：`if (obsClient != null && bucketName != null) { ... }`
    - 避免因 setUp 失败导致 tearDown 中出现 NPE

17. **禁止在集成测试中使用硬编码的桶名（除固定桶模式外）**
    - 动态桶名必须通过 `TestTools.generateBucketName(testName.getMethodName())` 生成
    - 仅当测试场景要求桶持久存在（如 WORM 配置）时，才使用 `FIXED_BUCKET_NAME` 模式
    - 固定桶名使用 `ClassName.getSimpleName().toLowerCase(Locale.ROOT)` 生成

## 集成测试调试清单

集成测试出现 `403` 或签名错误时，按以下顺序排查：
- [ ] 确认新增的 `SpecialParamEnum` 已添加到 `Constants.ALLOWED_RESOURCE_PARAMTER_NAMES`
- [ ] 确认所有配置加载组件支持 `test.env` 多环境切换
- [ ] 确认参数化测试的桶名转换逻辑与 `PrepareTestBucket` 一致
- [ ] 确认资源生命周期由 `@Rule` 统一管理，无重复创建/删除
- [ ] 打印 `ObsException` 的 `getErrorCode()` + `getErrorMessage()` + `getXmlMessage()` 获取服务端详细错误

集成测试出现 `400` 或错误格式异常时，按以下顺序排查：
- [ ] 确认测试所需的配置项（projectId、agency 等）已同步到 `app/src/test/resource/` 和 `src/it/resources/` 两个目录
- [ ] 确认配置值是实际可用的（非占位符如 `your-project-id`）
- [ ] 打印 `ObsException` 完整信息：`getXmlMessage()`（原始响应体）、`getResponseHeaders()`、`getResponseCode()` 确认实际错误格式
- [ ] 如 `getErrorCode()` 返回空，说明错误体是 XML 格式且 `<Code>` 为空，实际错误在 `<Message>` 中
- [ ] 确认委托名（agency）具有正确的云服务委托权限
