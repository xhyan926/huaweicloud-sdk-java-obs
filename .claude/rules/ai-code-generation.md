# AI代码生成规则

**应用场景**: 当使用AI助手生成代码时

## 禁止事项

1. **禁止缺少@AIGenerated注解**
   - 所有AI生成的测试方法必须添加 `com.obs.aitool.AIGenerated` 注解
   - `author` 字段必须使用当前 git 用户名（通过 `git config user.name` 获取），不得使用 "AI" 等占位符
   - `date` 字段必须使用当前系统日期（YYYY-MM-DD 格式），不得使用过去的固定日期

2. **禁止跳过验证步骤**
   - 代码生成后必须执行编译验证（验证步骤详见 `development-workflow.md` 验证清单）
   - 不得仅生成代码而不验证功能
   - 识别到的问题必须主动解决，不能仅报告问题

3. **测试规范**（通用测试规范详见 `code-quality.md`）
   - 参数化测试仅用于同一测试逻辑搭配不同输入参数的正交验证，不同测试场景必须拆分为独立 `@Test` 方法
   - 不得在 `@Test` 方法内使用 `switch(testCategory)` 分发到不同的测试分支
   - 测试方法命名和参数化测试的通用规范遵循 `code-quality.md` 规则 #5 和 #9

## 正向示例

```java
// 正确的@AIGenerated注解使用
// author 应替换为 git config user.name 的实际输出
// date 应替换为当前系统日期（如 2026-04-20）
@Test
@AIGenerated(author = "实际的git用户名", date = "实际的当前日期", description = "测试用户登录成功场景")
void should_login_successfully_when_valid_credentials_provided() {
    // 测试逻辑
}

// 正确的参数化测试结构：同一测试逻辑，不同参数正交验证
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
        // 同一断言逻辑，仅 contentType 输入和期望解析模式不同
        ServiceException ex = new ServiceException("msg", body, contentType);
        assertEquals(expectedParseMode, ex.getParsedFromJson() ? "JSON" : "XML");
    }
}
```

## 反向示例

```java
// 错误的@AIGenerated注解使用：author 用 "AI" 占位符，date 使用过去的固定日期
@Test
@AIGenerated(author = "AI", date = "2024-01-01", description = "测试")
void test() {  // 命名不规范，应使用 should_X_when_Y 格式
    // 测试逻辑
}

// 错误：参数化测试用 switch/case 分发不同测试分支
@Parameterized.Parameter(0) public String testCategory;
@Test
public void should_verify_behavior() {
    switch (testCategory) {
        case "JSON_CTOR": runJsonTest(); break;
        case "XML_CTOR": runXmlTest(); break;
        case "TO_STRING": runToStringTest(); break;
    }
}
// ↑ 每个分支测试完全不同的场景，应拆分为独立的 @Test 方法
// 如：should_parse_json_fields_when_content_type_is_application_json()
//     should_parse_xml_fields_when_content_type_is_application_xml()
//     should_include_all_fields_in_toString_when_all_set()
```

## 强制验证清单

代码生成完成后必须执行：
- [ ] 代码文件已创建且语法正确
- [ ] 相关依赖已添加到pom.xml
- [ ] @AIGenerated注解正确导入和使用（`import com.obs.aitool.AIGenerated`）
- [ ] 编译无错误：`mvn compile test-compile -s /usr/local/Maven/conf/settings.xml -T 1C`
- [ ] 测试执行通过：`mvn test -Dtest=GeneratedTestClassName -s /usr/local/Maven/conf/settings.xml -T 1C`
- [ ] 测试覆盖率满足要求（100%，可达分支）
- [ ] 功能验证通过