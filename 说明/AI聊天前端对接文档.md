# AI 聊天对接文档

## 接口说明

### POST /chat

基于 SSE（Server-Sent Events）的流式 AI 聊天接口。客户端发送用户输入，服务端以事件流形式实时返回 AI 回复。

### 请求头

| 请求头 | 值 | 说明 |
|--------|-----|------|
| Authorization | `Bearer <jwt>` | 登录后获取的 JWT Token |
| Content-Type | `application/json` | 请求体格式 |

### 请求体

```json
{
  "userInput": "列出我的笔记",
  "userId": "1"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userInput | String | 是 | 用户输入的消息 |
| userId | String | 是 | 用户 ID |

### 响应格式

响应为 SSE 事件流（`text/event-stream`），包含两种事件类型：

**正常消息事件：**
```
event: message
data: 好的，我来查询您的笔记列表。

event: message
data: 您共有 5 篇笔记...
```

**错误事件：**
```
event: error
data: {"error":"内部错误，请稍后重试"}
```

| 事件类型 | data 格式 | 说明 |
|---------|-----------|------|
| `message` | 纯文本 | AI 回复的文本片段 |
| `error` | JSON `{"error":"..."}` | 错误信息 |

---

## 前端实现（Vue3 + Composition API）

### 方案一：使用 fetch-event-source 库（推荐）

[@microsoft/fetch-event-source](https://www.npmjs.com/package/@microsoft/fetch-event-source) 支持 POST 请求和自定义请求头，专为 SSE 设计。

#### 安装

```bash
npm install @microsoft/fetch-event-source
```

#### 完整聊天组件

```vue
<template>
  <div class="chat-container">
    <!-- 消息列表 -->
    <div class="messages" ref="messagesRef">
      <div v-for="(msg, i) in messages" :key="i" :class="msg.role">
        <div class="avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
        <div class="content">{{ msg.content }}</div>
      </div>
      <!-- 正在输入指示 -->
      <div v-if="isLoading" class="assistant">
        <div class="avatar">AI</div>
        <div class="content thinking">思考中<span class="dots">...</span></div>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="input-area">
      <input
        v-model="inputText"
        placeholder="输入消息..."
        :disabled="isLoading"
        @keydown.enter="sendMessage"
      />
      <button :disabled="!inputText.trim() || isLoading" @click="sendMessage">
        发送
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { fetchEventSource } from '@microsoft/fetch-event-source'

// 从登录态获取（示例值，请替换为实际的 token 和 userId）
const token = localStorage.getItem('token')
const userId = localStorage.getItem('userId')

const messages = ref([])         // 消息列表
const inputText = ref('')        // 输入框内容
const isLoading = ref(false)     // 是否正在等待回复
const abortController = ref(null) // 用于取消请求
const messagesRef = ref(null)    // 消息列表 DOM 引用

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 发送消息
async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isLoading.value) return

  // 添加用户消息
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  isLoading.value = true
  scrollToBottom()

  // 添加占位的 AI 消息（逐步追加内容）
  const aiMsg = { role: 'assistant', content: '' }
  messages.value.push(aiMsg)

  abortController.value = new AbortController()

  try {
    await fetchEventSource('/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify({
        userInput: text,
        userId,
      }),
      signal: abortController.value.signal,

      // 收到 message 事件
      onmessage(event) {
        if (event.event === 'message') {
          aiMsg.content += event.data
          scrollToBottom()
        }
        if (event.event === 'error') {
          const err = JSON.parse(event.data)
          aiMsg.content = `[错误] ${err.error}`
          scrollToBottom()
        }
      },

      // 流结束
      onclose() {
        // 如果 AI 没返回任何内容，给一个默认提示
        if (!aiMsg.content) {
          aiMsg.content = '（未获取到回复）'
        }
        isLoading.value = false
        abortController.value = null
        scrollToBottom()
      },

      // 发生错误
      onerror(err) {
        console.error('SSE 错误:', err)
        aiMsg.content = aiMsg.content || '连接失败，请稍后重试'
        isLoading.value = false
        abortController.value = null
        scrollToBottom()
        // 返回非 undefined 阻止自动重连
        return
      },
    })
  } catch (err) {
    if (err.name === 'AbortError') {
      // 用户主动取消，不处理
      return
    }
    console.error('请求失败:', err)
    aiMsg.content = aiMsg.content || '请求失败，请稍后重试'
  } finally {
    isLoading.value = false
    abortController.value = null
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  max-width: 800px;
  margin: 0 auto;
}
.messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}
.user, .assistant {
  display: flex;
  margin-bottom: 16px;
  gap: 8px;
}
.assistant {
  justify-content: flex-start;
}
.user {
  justify-content: flex-end;
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}
.user .avatar {
  background: #1677ff;
  color: #fff;
}
.content {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.user .content {
  background: #1677ff;
  color: #fff;
  border-radius: 12px 4px 12px 12px;
}
.assistant .content {
  background: #f5f5f5;
  border-radius: 4px 12px 12px 12px;
}
.thinking {
  color: #999;
}
.dots {
  animation: blink 1s steps(1) infinite;
}
@keyframes blink {
  50% { opacity: 0; }
}
.input-area {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #eee;
}
.input-area input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  outline: none;
  font-size: 14px;
}
.input-area input:focus {
  border-color: #1677ff;
}
.input-area button {
  padding: 10px 20px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}
.input-area button:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}
</style>
```

---

### 方案二：使用原生 fetch + ReadableStream（无额外依赖）

如果不希望引入第三方库，可以用 `fetch` 配合 `ReadableStream` 手动解析 SSE 流。

```vue
<script setup>
import { ref } from 'vue'

const messages = ref([])
const inputText = ref('')
const isLoading = ref(false)

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isLoading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  isLoading.value = true

  const aiMsg = { role: 'assistant', content: '' }
  messages.value.push(aiMsg)

  try {
    const response = await fetch('/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
      },
      body: JSON.stringify({
        userInput: text,
        userId: localStorage.getItem('userId'),
      }),
    })

    if (!response.ok) {
      aiMsg.content = `请求失败 (${response.status})`
      return
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })

      // 解析 SSE 数据块
      const lines = buffer.split('\n')
      buffer = lines.pop() // 保留不完整的行

      let currentEvent = ''
      for (const line of lines) {
        if (line.startsWith('event: ')) {
          currentEvent = line.slice(7)
        } else if (line.startsWith('data: ')) {
          const data = line.slice(6)
          if (currentEvent === 'message') {
            aiMsg.content += data
          } else if (currentEvent === 'error') {
            try {
              const err = JSON.parse(data)
              aiMsg.content = `[错误] ${err.error}`
            } catch {
              aiMsg.content = `[错误] ${data}`
            }
          }
        }
      }
    }
  } catch (err) {
    console.error('请求失败:', err)
    aiMsg.content = aiMsg.content || '请求失败，请检查网络'
  } finally {
    isLoading.value = false
  }
}
</script>
```

---

### 方案三：WebSocket（需要后端额外支持）

如果项目后续切换到 WebSocket 协议，可以使用 `vue-native-websocket` 或原生 `WebSocket`：

```javascript
// 连接
const ws = new WebSocket('ws://localhost:8080/chat/ws')

ws.onopen = () => {
  ws.send(JSON.stringify({ userInput: '你好', userId: '1' }))
}

ws.onmessage = (event) => {
  const data = JSON.parse(event.data)
  // data.type: 'message' | 'error'
  // data.content: 文本内容
}
```

> 目前后端仅支持 SSE（POST /chat），暂不支持 WebSocket。如有需要可后续扩展。

---

## 关键要点

### 1. 为什么不能用原生 EventSource？

浏览器原生 `EventSource` 只支持 `GET` 请求，无法携带请求体和自定义请求头（如 `Authorization`）。AI 聊天需要：
- `POST` 方法发送用户输入
- `Bearer Token` 鉴权

因此必须使用 `fetch` 或 `fetch-event-source` 库。

### 2. 取消请求

用户发送消息后、AI 回复完成前，可以取消请求：

```javascript
// 发起请求时保存 AbortController
abortController.value = new AbortController()

// 取消请求
function cancel() {
  if (abortController.value) {
    abortController.value.abort()
    isLoading.value = false
  }
}
```

### 3. 错误处理

前端需要处理几类错误：

| 场景 | 表现 | 处理方式 |
|------|------|---------|
| 网络断开 | fetch 抛出异常 | 显示"连接失败" |
| HTTP 4xx/5xx | response.ok 为 false | 检查状态码，提示用户 |
| SSE error 事件 | `event: error` | 解析 JSON 显示错误详情 |
| 空回复 | 流正常结束但无内容 | 显示"未获取到回复" |

### 4. 滚动行为

每次追加内容后自动滚动到底部，但用户主动上滑查看历史时不要强制滚动：

```javascript
// 监听用户是否在底部
let isNearBottom = true

function handleScroll() {
  const el = messagesRef.value
  if (!el) return
  isNearBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 100
}

// 追加内容时，仅在底部时才自动滚动
function appendContent(text) {
  aiMsg.content += text
  if (isNearBottom) scrollToBottom()
}
```

### 5. 消息历史

目前未提供获取历史记录的接口，前端可以在本地缓存：

```javascript
// 使用 localStorage 缓存当前会话
watch(messages, (val) => {
  localStorage.setItem('chatHistory', JSON.stringify(val))
}, { deep: true })

// 页面加载时恢复
const saved = localStorage.getItem('chatHistory')
if (saved) {
  messages.value = JSON.parse(saved)
}
```

---

## 完整流程时序

```
前端                                  后端
 │                                     │
 ├── POST /chat ──────────────────→    │
 │   { userInput, userId }             │
 │   Authorization: Bearer <jwt>       │
 │                                     │
 │    ←── event: message              │
 │         data: 您好，我来           │
 │         查询您的笔记...             │
 │                                     │
 │    ←── event: message              │
 │         data: 您共有 5 篇           │
 │         笔记，分别是...             │
 │                                     │
 │    ←── [连接关闭]                  │
 │                                     │
 ├── 追加 AI 回复到界面               │
 │                                     │
 ├── POST /chat ──────────────────→    │
 │   { userInput: "删除第3篇" }        │
 │                                     │
 │    ←── event: message              │
 │         data: 确定要删除吗？        │
 │                                     │
 │    ←── [连接关闭]                  │
 │                                     │
 └── 等待用户下一次输入                │
```

---

## 注意事项

1. **Token 有效期**：JWT 过期后请求会返回 401，需在请求拦截器中统一处理跳转到登录页
2. **并发控制**：同一时间只能有一条消息在发送，通过 `isLoading` 状态锁定
3. **连接断开**：SSE 流结束后不会自动重连，用户需重新发送消息
4. **敏感操作确认**：AI 执行删除等操作前会先向用户确认，前端正常展示确认消息即可
5. **内容安全**：AI 返回内容直接渲染到 DOM，建议对用户输入做 XSS 过滤
