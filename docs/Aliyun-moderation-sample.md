# 阿里云文本审核（ScanText）接口文档

> **服务地址**: `imageaudit.cn-shanghai.aliyuncs.com`  
> **接口 Action**: `ScanText`  
> **SDK 依赖**: `com.aliyun:imageaudit20191230`

---

## 一、请求参数

### 1.1 公共请求头（Request Headers）

| Header | 值 |
|---|---|
| `user-agent` | `AlibabaCloud API Workbench` |
| `content-type` | `application/x-www-form-urlencoded` |
| `host` | `imageaudit.cn-shanghai.aliyuncs.com` |

### 1.2 业务请求参数

| 名称 | 类型 | 是否必选 | 示例值 | 描述 |
|---|---|---|---|---|
| `Action` | String | **是** | `ScanText` | 系统规定参数，取值：`ScanText` |
| `Tasks.N.Content` | String | **是** | `维修管道，联系weixin` | 指定检测的对象，JSON 数组中的每个元素是一个文字检测任务结构体。<br/><br/>**说明**：N 个 Task 会折算为 N 次调用进行计费。算法识别效果问题请通过钉钉群（23109592）加入阿里云视觉智能开放平台咨询群联系我们。 |
| `Labels.N.Label` | String | **是** | `ad` | 指定文本检测的应用场景，可选值包括： |

### 1.3 检测标签（Labels）说明

| 标签值 | 说明 |
|---|---|
| `spam` | 文字垃圾内容识别 |
| `politics` | 文字敏感内容识别 |
| `abuse` | 文字辱骂内容识别 |
| `terrorism` | 文字暴恐内容识别 |
| `porn` | 文字鉴黄内容识别 |
| `flood` | 文字灌水内容识别 |
| `contraband` | 文字违禁内容识别 |
| `ad` | 文字广告内容识别 |

---

## 二、返回数据

### 2.1 公共响应头（Response Headers）

| Header | 示例值 |
|---|---|
| `date` | `Wed, 06 May 2026 02:49:04 GMT` |
| `content-type` | `application/json;charset=utf-8` |
| `content-length` | `248` |
| `connection` | `keep-alive` |
| `keep-alive` | `timeout=25` |
| `access-control-allow-origin` | `*` |
| `access-control-expose-headers` | `*` |
| `x-acs-request-id` | `B41D0F89-8BE5-54F6-BC7B-387F53C62FC7` |
| `x-acs-trace-id` | `e7d0b5857092cdc23617e232d2c5194d` |
| `etag` | `2b0Z0iIEquV8k5cTa/VeUPg4` |

### 2.2 响应体字段说明

| 名称 | 类型 | 示例值 | 描述 |
|---|---|---|---|
| `RequestId` | String | `C7CD87E3-57A5-4E2F-8A44-809F3554692C` | 请求 ID |
| `Data` | Object | — | 返回的结果数据内容 |
| `Data.Elements` | Array of Element | — | 检测结果的各个子元素 |
| `Data.Elements[].TaskId` | String | `txt6Vh5Fv0DAFy5hgdVRt3pqf-1s82jj` | 任务 ID |
| `Data.Elements[].Results` | Array of Result | — | 检测结果。如果返回为空，表示系统识别命中了其他 Label，扩大 Label 重新发起请求 |
| `Data.Elements[].Results[].Suggestion` | String | `block` | 建议执行的操作：<br/>• `pass` — 文本正常<br/>• `review` — 需要人工审核<br/>• `block` — 文本违规，可直接删除或做限制处理 |
| `Data.Elements[].Results[].Label` | String | `ad` | 检测结果的分类 |
| `Data.Elements[].Results[].Rate` | Float | `99.91` | 结果为该分类的概率，取值范围 `[0.00, 100.00]`。值越高，越可能属于该分类 |
| `Data.Elements[].Results[].Details` | Array of Detail | — | 文本的检测结果详情 |
| `Data.Elements[].Results[].Details[].Label` | String | `ad` | 命中风险文本的分类 |
| `Data.Elements[].Results[].Details[].Contexts` | Array of Context | — | 命中该风险的信息 |
| `Data.Elements[].Results[].Details[].Contexts[].Context` | String | `联系weixin` | 检测文本命中的风险内容 |

---

## 三、调用示例

### 3.1 Java SDK 示例代码

```java
package com.aliyun.sample;

import com.aliyun.tea.*;

public class Sample {

    /**
     * <b>description</b> :
     * <p>使用凭据初始化账号Client</p>
     * @return Client
     * @throws Exception
     */
    public static com.aliyun.imageaudit20191230.Client createClient() throws Exception {
        // 工程代码建议使用更安全的无AK方式，凭据配置方式请参见：https://help.aliyun.com/document_detail/378657.html。
        com.aliyun.credentials.Client credential = new com.aliyun.credentials.Client();
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setCredential(credential);
        // Endpoint 请参考 https://api.aliyun.com/product/imageaudit
        config.endpoint = "imageaudit.cn-shanghai.aliyuncs.com";
        return new com.aliyun.imageaudit20191230.Client(config);
    }

    public static void main(String[] args_) throws Exception {

        com.aliyun.imageaudit20191230.Client client = Sample.createClient();
        com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels labels0 = new com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels()
                .setLabel("spam");
        com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels labels1 = new com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels()
                .setLabel("politics");
        com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels labels2 = new com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels()
                .setLabel("abuse");
        com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels labels3 = new com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels()
                .setLabel("terrorism");
        com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels labels4 = new com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels()
                .setLabel("porn");
        com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels labels5 = new com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels()
                .setLabel("flood");
        com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels labels6 = new com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels()
                .setLabel("contraband");
        com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels labels7 = new com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels()
                .setLabel("ad");
        com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestTasks tasks0 = new com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestTasks()
                .setContent("本校小额贷款，安全、快捷、方便、无抵押，随机随贷，当天放款，上门服务。联系weixin 123456");
        com.aliyun.imageaudit20191230.models.ScanTextRequest scanTextRequest = new com.aliyun.imageaudit20191230.models.ScanTextRequest()
                .setTasks(java.util.Arrays.asList(
                    tasks0
                ))
                .setLabels(java.util.Arrays.asList(
                    labels0,
                    labels1,
                    labels2,
                    labels3,
                    labels4,
                    labels5,
                    labels6,
                    labels7
                ));
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            com.aliyun.imageaudit20191230.models.ScanTextResponse resp = client.scanTextWithOptions(scanTextRequest, runtime);
            System.out.println(new com.google.gson.Gson().toJson(resp));
        } catch (TeaException error) {
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
        }
    }
}
```

### 3.2 响应 JSON 示例

```json
{
  "RequestId": "B41D0F89-8BE5-54F6-BC7B-387F53C62FC7",
  "Data": {
    "Elements": [
      {
        "TaskId": "txt3Hk8NrVF4yN5jNdDOtsNK9-1DvIMn",
        "Results": [
          {
            "Suggestion": "block",
            "Details": [
              {
                "Label": "ad",
                "Contexts": [
                  {
                    "Context": "联系weixin"
                  }
                ]
              }
            ],
            "Rate": 99.91,
            "Label": "ad"
          }
        ]
      }
    ]
  }
}
```

> **Suggestion 取值说明**：
> - `pass`：文本正常
> - `review`：需要人工审核
> - `block`：文本违规，可以直接删除或者做限制处理

---

## 四、注意事项

1. **鉴权方式**：生产环境建议使用无 AK（AccessKey）方式，通过凭据（Credentials）进行鉴权，详见 [阿里云凭据配置文档](https://help.aliyun.com/document_detail/378657.html)。
2. **计费说明**：N 个 Task 会折算为 N 次调用进行计费。
3. **空结果处理**：如果 `Results` 返回为空，表示系统识别命中了其他 Label，需扩大 Label 范围重新发起请求。
4. **异常处理**：工程代码中请勿直接忽略异常，需根据业务需要进行妥善处理。
