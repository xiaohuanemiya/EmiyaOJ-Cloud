# Vue 3 前端开发规范

> **版本**: V1.0 | **日期**: 2026-06-03 | **技术栈**: Vue 3 + TypeScript + Vite + Pinia + Element Plus

---

## 目录

1. [项目结构规范](#1-项目结构规范)
2. [命名规范](#2-命名规范)
3. [组件开发规范](#3-组件开发规范)
4. [TypeScript 使用规范](#4-typescript-使用规范)
5. [状态管理规范（Pinia）](#5-状态管理规范pinia)
6. [路由规范（Vue Router）](#6-路由规范vue-router)
7. [API 请求规范（Axios）](#7-api-请求规范axios)
8. [样式规范](#8-样式规范)
9. [代码质量规范](#9-代码质量规范)
10. [Git 提交规范](#10-git-提交规范)
11. [性能优化规范](#11-性能优化规范)
12. [安全规范](#12-安全规范)

---

## 1. 项目结构规范

### 1.1 标准目录结构

```
project-root/
├── public/                          # 静态资源（不经过构建处理）
│   └── favicon.ico
│
├── src/
│   ├── api/                         # API 接口层
│   │   ├── modules/                 # 按业务模块拆分
│   │   │   ├── auth.ts              # 认证相关接口
│   │   │   ├── problem.ts           # 题目相关接口
│   │   │   ├── judge.ts             # 判题相关接口
│   │   │   ├── blog.ts              # 博客相关接口
│   │   │   └── contest.ts           # 竞赛相关接口
│   │   └── request.ts               # Axios 实例与拦截器
│   │
│   ├── assets/                      # 构建处理的静态资源
│   │   ├── images/
│   │   ├── icons/
│   │   └── styles/
│   │       ├── variables.scss       # SCSS 变量
│   │       ├── mixins.scss          # SCSS Mixin
│   │       └── global.scss          # 全局样式
│   │
│   ├── components/                  # 全局通用组件
│   │   ├── common/                  # 基础组件（Button、Modal 封装等）
│   │   │   ├── PageContainer.vue    # 页面容器
│   │   │   ├── DataTable.vue        # 通用表格
│   │   │   └── SearchForm.vue       # 搜索表单
│   │   └── business/                # 业务组件
│   │       ├── CodeEditor.vue       # 代码编辑器
│   │       ├── MarkdownViewer.vue   # Markdown 渲染器
│   │       └── UserAvatar.vue       # 用户头像
│   │
│   ├── composables/                 # 组合式函数（Hooks）
│   │   ├── useAuth.ts               # 认证相关逻辑
│   │   ├── usePagination.ts         # 分页逻辑
│   │   ├── usePermission.ts         # 权限判断
│   │   └── useWebSocket.ts          # WebSocket 连接
│   │
│   ├── layouts/                     # 布局组件
│   │   ├── DefaultLayout.vue        # 默认布局（顶栏+侧栏+内容）
│   │   ├── BlankLayout.vue          # 空白布局（登录页等）
│   │   └── AdminLayout.vue          # 管理后台布局
│   │
│   ├── router/                      # 路由配置
│   │   ├── index.ts                 # 路由实例创建
│   │   ├── routes.ts                # 路由表定义
│   │   └── guards.ts                # 路由守卫
│   │
│   ├── stores/                      # Pinia 状态管理
│   │   ├── modules/
│   │   │   ├── user.ts              # 用户状态
│   │   │   ├── app.ts               # 应用全局状态
│   │   │   └── permission.ts        # 权限状态
│   │   └── index.ts                 # Store 入口
│   │
│   ├── types/                       # TypeScript 类型定义
│   │   ├── api.ts                   # API 响应类型
│   │   ├── user.ts                  # 用户相关类型
│   │   ├── problem.ts               # 题目相关类型
│   │   └── global.d.ts              # 全局类型声明
│   │
│   ├── utils/                       # 工具函数
│   │   ├── format.ts                # 格式化工具（日期、数字）
│   │   ├── storage.ts               # 本地存储封装
│   │   ├── validate.ts              # 校验工具
│   │   └── constants.ts             # 常量定义
│   │
│   ├── views/                       # 页面视图
│   │   ├── auth/                    # 认证模块
│   │   │   ├── LoginView.vue
│   │   │   └── RegisterView.vue
│   │   ├── problem/                 # 题目模块
│   │   │   ├── ProblemList.vue
│   │   │   ├── ProblemDetail.vue
│   │   │   └── components/          # 页面私有组件
│   │   │       └── ProblemCard.vue
│   │   ├── judge/                   # 判题模块
│   │   │   ├── SubmitCode.vue
│   │   │   └── SubmissionList.vue
│   │   ├── blog/                    # 博客模块
│   │   │   ├── BlogList.vue
│   │   │   ├── BlogDetail.vue
│   │   │   └── BlogEditor.vue
│   │   ├── contest/                 # 竞赛模块
│   │   ├── admin/                   # 管理后台
│   │   └── error/                   # 错误页面
│   │       ├── 404.vue
│   │       └── 500.vue
│   │
│   ├── App.vue                      # 根组件
│   └── main.ts                      # 入口文件
│
├── .env                             # 环境变量（默认）
├── .env.development                 # 开发环境
├── .env.production                  # 生产环境
├── .eslintrc.cjs                    # ESLint 配置
├── .prettierrc.json                 # Prettier 配置
├── index.html                       # HTML 模板
├── package.json
├── tsconfig.json                    # TypeScript 配置
└── vite.config.ts                   # Vite 配置
```

### 1.2 文件命名规则

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| 页面组件 | PascalCase，以 `View` 结尾 | `LoginView.vue`, `ProblemDetailView.vue` |
| 通用组件 | PascalCase，描述性名称 | `CodeEditor.vue`, `DataTable.vue` |
| 页面私有组件 | PascalCase，放在 `components/` 子目录 | `ProblemCard.vue` |
| 组合式函数 | camelCase，以 `use` 开头 | `useAuth.ts`, `usePagination.ts` |
| 工具函数 | camelCase | `formatDate.ts`, `storage.ts` |
| 类型定义 | camelCase | `user.ts`, `api.ts` |
| Store 模块 | camelCase | `user.ts`, `app.ts` |
| API 模块 | camelCase | `auth.ts`, `problem.ts` |

### 1.3 组件目录规范

每个页面模块按以下结构组织：

```
views/problem/
├── ProblemListView.vue          # 列表页
├── ProblemDetailView.vue        # 详情页
├── ProblemEditorView.vue        # 编辑页
└── components/                  # 该模块私有组件
    ├── ProblemCard.vue
    ├── ProblemFilter.vue
    └── TestCaseTable.vue
```

---

## 2. 命名规范

### 2.1 变量命名

| 类型 | 规则 | 示例 |
|------|------|------|
| 普通变量 | camelCase | `userName`, `problemList` |
| 常量 | UPPER_SNAKE_CASE | `MAX_FILE_SIZE`, `API_BASE_URL` |
| 布尔变量 | `is` / `has` / `can` 前缀 | `isLoading`, `hasPermission`, `canEdit` |
| Ref 响应式 | camelCase | `const count = ref(0)` |
| Reactive 响应式 | camelCase | `const form = reactive({})` |
| 函数/方法 | camelCase，动词开头 | `fetchUserList()`, `handleSubmit()` |
| 事件处理函数 | `handle` / `on` 前缀 | `handleClick()`, `onSubmit()` |
| 私有变量/函数 | `_` 前缀（不推荐，优先用作用域控制） | `_internalState` |

### 2.2 组件命名

```vue
<!-- ✅ 正确：多词 PascalCase -->
<UserAvatar />
<CodeEditor />
<ProblemCard />

<!-- ❌ 错误：单词组件名（易与 HTML 元素冲突） -->
<Avatar />
<Editor />
```

### 2.3 Props 命名

```typescript
// ✅ 正确：camelCase（JS/TS 中），kebab-case（模板中）
defineProps<{
  problemId: number;
  isOwner: boolean;
  maxLength?: number;
}>();

// 模板使用
<ProblemCard :problem-id="id" :is-owner="true" />
```

### 2.4 Emits 命名

```typescript
// ✅ 正确：camelCase
const emit = defineEmits<{
  'update:modelValue': [value: string];
  'submit': [data: FormData];
  'delete': [id: number];
}>();
```

---

## 3. 组件开发规范

### 3.1 组件编写顺序

```vue
<script setup lang="ts">
// 1. 类型导入
import type { ProblemVO, PageDTO } from '@/types/problem';

// 2. 第三方库导入
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

// 3. 项目内部导入
import { getProblemList } from '@/api/modules/problem';
import ProblemCard from './components/ProblemCard.vue';

// 4. Props 定义
const props = defineProps<{
  difficulty?: number;
}>();

// 5. Emits 定义
const emit = defineEmits<{
  select: [id: number];
}>();

// 6. 组合式函数
const router = useRouter();

// 7. 响应式状态
const loading = ref(false);
const list = ref<ProblemVO[]>([]);

// 8. 计算属性
const filteredList = computed(() =>
  props.difficulty
    ? list.value.filter(item => item.difficulty === props.difficulty)
    : list.value
);

// 9. 方法
async function fetchData() {
  loading.value = true;
  try {
    const res = await getProblemList({ pageNum: 1, pageSize: 20 });
    list.value = res.data.list;
  } finally {
    loading.value = false;
  }
}

// 10. 生命周期
onMounted(() => {
  fetchData();
});
</script>

<template>
  <!-- 11. 模板 -->
  <div class="problem-list">
    <ProblemCard
      v-for="item in filteredList"
      :key="item.id"
      :data="item"
      @click="emit('select', item.id)"
    />
  </div>
</template>

<style scoped lang="scss">
/* 12. 样式 */
.problem-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}
</style>
```

### 3.2 组件设计原则

| 原则 | 说明 |
|------|------|
| **单一职责** | 每个组件只负责一个功能领域 |
| **Props 向下，Events 向上** | 父→子用 Props 传数据，子→父用 Emits 发事件 |
| **避免直接修改 Props** | Props 是只读的，使用 `v-model` 或 emit 更新 |
| **优先使用 `defineProps<T>()`** | TypeScript 泛型定义 Props，享受类型推断 |
| **合理拆分** | 超过 300 行的组件应考虑拆分 |
| **组件无副作用** | 组件不应直接操作全局状态或 DOM（除自身外） |

### 3.3 `v-model` 规范

```vue
<!-- 父组件 -->
<UserForm v-model:visible="dialogVisible" v-model:data="formData" />

<!-- 子组件 -->
<script setup lang="ts">
const props = defineProps<{
  visible: boolean;
  data: UserFormData;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  'update:data': [value: UserFormData];
}>();
</script>
```

### 3.4 插槽使用规范

```vue
<template>
  <div class="card">
    <!-- 默认插槽 -->
    <slot />

    <!-- 具名插槽：有默认内容 -->
    <div class="card-header">
      <slot name="header">
        <h3>{{ title }}</h3>
      </slot>
    </div>

    <!-- 作用域插槽 -->
    <slot name="item" :item="currentItem" :index="currentIndex" />
  </div>
</template>
```

---

## 4. TypeScript 使用规范

### 4.1 类型定义

```typescript
// types/api.ts — 统一 API 响应类型
export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
}

export interface PageVO<T> {
  total: number;
  list: T[];
  pageNum: number;
  pageSize: number;
}

// types/problem.ts — 业务类型
export interface ProblemVO {
  id: number;
  title: string;
  difficulty: 1 | 2 | 3;        // 1-简单, 2-中等, 3-困难
  status: 0 | 1;                // 0-隐藏, 1-公开
  tags: TagVO[];
  createTime: string;            // ISO 日期字符串
}

export type ProblemDifficulty = 1 | 2 | 3;

export interface ProblemQueryDTO {
  title?: string;
  difficulty?: ProblemDifficulty;
  pageNum: number;
  pageSize: number;
}
```

### 4.2 禁止 `any`

```typescript
// ❌ 错误
function process(data: any): any {
  return data.value;
}

// ✅ 正确：使用具体类型
function process(data: ProblemVO): string {
  return data.title;
}

// ✅ 正确：确实无法确定类型时使用 unknown
function parseJson(text: string): unknown {
  return JSON.parse(text);
}

// ✅ 正确：泛型约束
function getValue<T extends Record<string, unknown>>(obj: T, key: keyof T): T[keyof T] {
  return obj[key];
}
```

### 4.3 Props 类型定义

```typescript
// ✅ 推荐：使用 interface + 泛型
defineProps<{
  problem: ProblemVO;
  loading?: boolean;
}>();

// ✅ 复杂默认值时使用 withDefaults
const props = withDefaults(defineProps<{
  pageSize?: number;
  placeholder?: string;
}>(), {
  pageSize: 10,
  placeholder: '请输入搜索内容',
});
```

### 4.4 函数类型标注

```typescript
// ✅ 标注参数和返回值
async function fetchProblem(id: number): Promise<ProblemVO> {
  const res = await getProblemById(id);
  return res.data;
}

// ✅ 事件处理函数
function handleSubmit(data: FormData): void {
  // ...
}

// ✅ 泛型函数
function createPageVO<T>(list: T[], total: number, pageNum: number, pageSize: number): PageVO<T> {
  return { list, total, pageNum, pageSize };
}
```

---

## 5. 状态管理规范（Pinia）

### 5.1 Store 定义规范

```typescript
// stores/modules/user.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { UserVO } from '@/types/user';
import { getCurrentUser, login, logout } from '@/api/modules/auth';
import { storage } from '@/utils/storage';

export const useUserStore = defineStore('user', () => {
  // --- 状态 ---
  const currentUser = ref<UserVO | null>(null);
  const token = ref<string>(storage.get('token') || '');

  // --- 计算属性 ---
  const isLoggedIn = computed(() => !!token.value);
  const userId = computed(() => currentUser.value?.id);
  const permissions = computed(() => currentUser.value?.permissions ?? []);

  // --- 方法 ---
  async function loginAction(username: string, password: string): Promise<void> {
    const res = await login({ username, password });
    token.value = res.data.token;
    storage.set('token', token.value);
    await fetchCurrentUser();
  }

  async function fetchCurrentUser(): Promise<void> {
    const res = await getCurrentUser();
    currentUser.value = res.data;
  }

  function logoutAction(): void {
    token.value = '';
    currentUser.value = null;
    storage.remove('token');
  }

  function hasPermission(code: string): boolean {
    return permissions.value.includes(code);
  }

  return {
    currentUser,
    token,
    isLoggedIn,
    userId,
    permissions,
    loginAction,
    fetchCurrentUser,
    logoutAction,
    hasPermission,
  };
});
```

### 5.2 Store 使用规范

```vue
<script setup lang="ts">
import { useUserStore } from '@/stores/modules/user';
import { storeToRefs } from 'pinia';

const userStore = useUserStore();

// ✅ 响应式解构状态和计算属性
const { currentUser, isLoggedIn } = storeToRefs(userStore);

// ✅ 方法直接解构（不需要 storeToRefs）
const { loginAction, logoutAction } = userStore;
</script>
```

### 5.3 Store 命名与组织

| 规范 | 说明 |
|------|------|
| Store ID | 小写，描述性名称（`'user'`, `'app'`, `'problem'`） |
| 文件名 | 与 Store ID 一致（`user.ts`, `app.ts`） |
| 组合式风格 | 统一使用 `setup store` 风格（`() => { ... }`） |
| 单一职责 | 一个 Store 只管理一个领域的状态 |
| 避免循环依赖 | Store 之间互相引用时使用 `storeToRefs` 或延迟获取 |

---

## 6. 路由规范（Vue Router）

### 6.1 路由定义

```typescript
// router/routes.ts
import type { RouteRecordRaw } from 'vue-router';

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: '首页', icon: 'home' },
      },
      {
        path: 'problem',
        name: 'Problem',
        redirect: '/problem/list',
        meta: { title: '题库', icon: 'code' },
        children: [
          {
            path: 'list',
            name: 'ProblemList',
            component: () => import('@/views/problem/ProblemListView.vue'),
            meta: { title: '题目列表' },
          },
          {
            path: ':id',
            name: 'ProblemDetail',
            component: () => import('@/views/problem/ProblemDetailView.vue'),
            meta: { title: '题目详情' },
          },
        ],
      },
    ],
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { title: '管理后台', requiresAuth: true, roles: ['admin'] },
    children: [/* ... */],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' },
  },
];
```

### 6.2 路由 Meta 规范

```typescript
// types/router.d.ts
import 'vue-router';

declare module 'vue-router' {
  interface RouteMeta {
    title: string;              // 页面标题（必填）
    icon?: string;              // 菜单图标
    requiresAuth?: boolean;     // 是否需要登录
    roles?: string[];           // 允许访问的角色列表
    permissions?: string[];     // 允许访问的权限码列表
    hidden?: boolean;           // 是否在菜单中隐藏
    keepAlive?: boolean;        // 是否缓存页面
  }
}
```

### 6.3 路由守卫示例

```typescript
// router/guards.ts
import type { Router } from 'vue-router';
import { useUserStore } from '@/stores/modules/user';

export function setupGuards(router: Router): void {
  router.beforeEach(async (to, _from, next) => {
    // 设置页面标题
    document.title = `${to.meta.title} - EmiyaOJ`;

    const userStore = useUserStore();

    // 需要认证的路由
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
      next({ name: 'Login', query: { redirect: to.fullPath } });
      return;
    }

    // 角色权限检查
    if (to.meta.roles && to.meta.roles.length > 0) {
      const hasRole = userStore.currentUser?.roles?.some(
        role => to.meta.roles!.includes(role)
      );
      if (!hasRole) {
        next({ name: 'Forbidden' });
        return;
      }
    }

    next();
  });
}
```

### 6.4 路由命名规范

| 规范 | 示例 |
|------|------|
| `name` 使用 PascalCase | `ProblemList`, `BlogDetail` |
| `path` 使用 kebab-case | `/problem-list`, `/blog-detail/:id` |
| 嵌套路由的 path 不包含父路径前缀 | ✅ `list`, ❌ `problem/list` |
| Tab 页签使用 `query` 参数 | `/problem/list?tab=unsolved` |

---

## 7. API 请求规范（Axios）

### 7.1 Axios 实例封装

```typescript
// api/request.ts
import axios, { type AxiosInstance, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios';
import type { ApiResponse } from '@/types/api';
import { useUserStore } from '@/stores/modules/user';
import { ElMessage } from 'element-plus';
import router from '@/router';

const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

// 请求拦截器
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore();
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, message, data } = response.data;

    if (code === 200) {
      return data as any;
    }

    // 业务错误
    ElMessage.error(message || '请求失败');
    return Promise.reject(new Error(message));
  },
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore();
      userStore.logoutAction();
      router.push({ name: 'Login' });
      ElMessage.error('登录已过期，请重新登录');
    } else if (error.response?.status === 403) {
      ElMessage.error('无权限访问');
    } else {
      ElMessage.error(error.message || '网络错误');
    }
    return Promise.reject(error);
  }
);

export default instance;
```

### 7.2 API 模块组织

```typescript
// api/modules/problem.ts
import request from '@/api/request';
import type { ApiResponse, PageVO } from '@/types/api';
import type { ProblemVO, ProblemQueryDTO, ProblemSaveDTO } from '@/types/problem';

/** 分页查询题目列表 */
export function getProblemList(params: ProblemQueryDTO): Promise<PageVO<ProblemVO>> {
  return request.get('/problem/list', { params });
}

/** 查询题目详情 */
export function getProblemById(id: number): Promise<ProblemVO> {
  return request.get(`/problem/${id}`);
}

/** 新增题目 */
export function createProblem(data: ProblemSaveDTO): Promise<void> {
  return request.post('/problem', data);
}

/** 更新题目 */
export function updateProblem(data: ProblemSaveDTO): Promise<void> {
  return request.put('/problem', data);
}

/** 删除题目 */
export function deleteProblem(id: number): Promise<void> {
  return request.delete(`/problem/${id}`);
}
```

### 7.3 API 调用规范

```vue
<script setup lang="ts">
import { ref } from 'vue';
import { getProblemList } from '@/api/modules/problem';
import type { ProblemVO } from '@/types/problem';

const loading = ref(false);
const list = ref<ProblemVO[]>([]);

async function fetchData() {
  loading.value = true;
  try {
    const data = await getProblemList({ pageNum: 1, pageSize: 20 });
    list.value = data.list;
  } catch (error) {
    // 错误已在拦截器统一处理，此处仅记录或做降级
    console.error('获取题目列表失败:', error);
  } finally {
    loading.value = false;
  }
}
</script>
```

### 7.4 请求方法语义

| HTTP 方法 | 用途 |
|-----------|------|
| `GET` | 查询数据 |
| `POST` | 新增数据 / 复杂查询 |
| `PUT` | 全量更新数据 |
| `DELETE` | 删除数据 |
| `PATCH` | 部分更新数据（备选） |

---

## 8. 样式规范

### 8.1 样式编写规范

```vue
<style scoped lang="scss">
/* ✅ 使用 scoped 避免样式污染 */
/* ✅ 使用 SCSS 预处理器 */

/* 根元素类名使用 kebab-case */
.problem-detail {
  padding: 24px;

  /* 嵌套选择器不超过 3 层 */
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  &__title {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-primary);
  }

  &__content {
    line-height: 1.8;
  }

  /* 状态修饰符 */
  &--loading {
    opacity: 0.6;
    pointer-events: none;
  }
}
</style>
```

### 8.2 BEM 命名约定

```scss
.block { }                    // 块
.block__element { }           // 元素
.block--modifier { }          // 修饰符
.block__element--modifier { } // 元素的修饰符
```

### 8.3 CSS 变量（设计令牌）

```scss
// assets/styles/variables.scss
:root {
  // 主题色
  --color-primary: #409eff;
  --color-success: #67c23a;
  --color-warning: #e6a23c;
  --color-danger: #f56c6c;
  --color-info: #909399;

  // 文字色
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --text-placeholder: #c0c4cc;

  // 边框色
  --border-base: #dcdfe6;
  --border-light: #e4e7ed;

  // 背景色
  --bg-page: #f5f7fa;
  --bg-card: #ffffff;

  // 间距
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;

  // 圆角
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
}
```

### 8.4 响应式设计

```scss
// 断点定义
$breakpoints: (
  'sm': 640px,
  'md': 768px,
  'lg': 1024px,
  'xl': 1280px,
);

@mixin respond-to($breakpoint) {
  @media (min-width: map-get($breakpoints, $breakpoint)) {
    @content;
  }
}

// 使用
.problem-grid {
  grid-template-columns: 1fr;

  @include respond-to('md') {
    grid-template-columns: repeat(2, 1fr);
  }

  @include respond-to('lg') {
    grid-template-columns: repeat(3, 1fr);
  }
}
```

---

## 9. 代码质量规范

### 9.1 ESLint 配置要点

```javascript
// .eslintrc.cjs
module.exports = {
  root: true,
  extends: [
    'plugin:vue/vue3-recommended',
    '@vue/eslint-config-typescript/recommended',
    '@vue/eslint-config-prettier',
    'plugin:import/recommended',
    'plugin:import/typescript',
  ],
  rules: {
    // TypeScript
    '@typescript-eslint/no-explicit-any': 'error',         // 禁止 any
    '@typescript-eslint/no-unused-vars': ['error', {        // 未使用变量
      argsIgnorePattern: '^_',
      varsIgnorePattern: '^_',
    }],

    // Vue
    'vue/multi-word-component-names': 'error',              // 多词组件名
    'vue/component-name-in-template-casing': ['error', 'PascalCase'], // 模板中 PascalCase
    'vue/require-default-prop': 'off',                      // TS 类型已足够

    // 通用
    'no-console': ['warn', { allow: ['warn', 'error'] }],   // 禁止 console.log
    'no-debugger': 'error',                                  // 禁止 debugger
    'import/order': ['error', {                              // import 排序
      groups: ['type', 'builtin', 'external', 'internal', 'parent', 'sibling'],
    }],
  },
};
```

### 9.2 Prettier 配置

```json
{
  "semi": true,
  "singleQuote": true,
  "trailingComma": "es5",
  "printWidth": 100,
  "tabWidth": 2,
  "arrowParens": "avoid",
  "endOfLine": "lf"
}
```

### 9.3 代码审查检查清单

| 检查项 | 说明 |
|--------|------|
| 类型安全 | 无 `any` 类型使用 |
| Props 校验 | 所有 Props 有 TypeScript 类型定义 |
| 响应式正确 | Ref/Reactive 使用正确，无直接修改 Props |
| 组件拆分 | 超过 300 行的组件已拆分 |
| 无副作用 | `onMounted` / `watch` 中有对应的清理逻辑 |
| 错误处理 | try/catch 覆盖了所有异步操作 |
| 样式隔离 | 使用 `scoped` 或 CSS Modules |
| 性能考虑 | 大列表使用虚拟滚动，懒加载路由 |

---

## 10. Git 提交规范

### 10.1 Conventional Commits

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 10.2 Type 类型

| Type | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | 修复 Bug |
| `docs` | 文档变更 |
| `style` | 代码格式（不影响功能） |
| `refactor` | 重构（既非新功能也非修复） |
| `perf` | 性能优化 |
| `test` | 测试相关 |
| `chore` | 构建/工具/依赖变更 |
| `ci` | CI/CD 配置变更 |

### 10.3 提交示例

```
feat(judge): 添加代码提交页面和判题结果展示

- 新增 SubmitCode 组件，支持多语言代码编辑
- 新增 SubmissionResult 组件，展示判题状态和详情
- 集成 Monaco Editor 代码编辑器

Closes #42
```

---

## 11. 性能优化规范

### 11.1 组件懒加载

```typescript
// router/routes.ts
const ProblemDetail = () => import('@/views/problem/ProblemDetailView.vue');
const BlogEditor = () => import('@/views/blog/BlogEditorView.vue');
```

### 11.2 合理使用计算属性

```typescript
// ✅ 计算属性缓存
const filteredList = computed(() =>
  list.value.filter(item => item.status === 1)
);

// ❌ 方法（每次渲染都重新计算）
function getFilteredList() {
  return list.value.filter(item => item.status === 1);
}
```

### 11.3 列表渲染优化

```vue
<!-- ✅ 使用唯一 key -->
<div v-for="item in list" :key="item.id">

<!-- ❌ 使用 index 作为 key -->
<div v-for="(item, index) in list" :key="index">
```

### 11.4 避免不必要的响应式

```typescript
// ✅ 不需要响应式的大对象，使用 shallowRef
const largeConfig = shallowRef<Config>(defaultConfig);

// ✅ 不需要响应式的常量，放在组件外部
const DIFFICULTY_MAP = { 1: '简单', 2: '中等', 3: '困难' };

// ✅ 使用 markRaw 标记不需要代理的对象
const editorInstance = markRaw(new MonacoEditor());
```

### 11.5 事件监听清理

```typescript
onMounted(() => {
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);  // ✅ 必须清理
});
```

---

## 12. 安全规范

| 规范 | 说明 |
|------|------|
| **XSS 防护** | 用户输入的 HTML 内容必须经过消毒处理（DOMPurify）再渲染 |
| **Token 存储** | JWT Token 存储在 `localStorage`（需防范 XSS），敏感操作二次验证 |
| **路由权限** | 前端路由守卫 + 后端接口鉴权双重保障 |
| **敏感信息** | 禁止在前端代码中硬编码 API Key、密钥等 |
| **HTTPS** | 生产环境必须使用 HTTPS |
| **CSP** | 配置 Content-Security-Policy 响应头 |
| **依赖安全** | 定期 `npm audit`，及时更新有漏洞的依赖 |

### 12.1 XSS 防护示例

```typescript
import DOMPurify from 'dompurify';

// ✅ 用户内容渲染前消毒
function renderMarkdown(raw: string): string {
  const html = marked(raw);
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'code', 'pre', 'a', 'ul', 'ol', 'li'],
    ALLOWED_ATTR: ['href', 'target'],
  });
}
```

### 12.2 环境变量管理

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080

# .env.production
VITE_API_BASE_URL=https://api.emiyaoj.com

# ⚠️ 禁止在 .env 中存储密钥
# ❌ VITE_SECRET_KEY=xxx
```

---

> **文档维护**: 本规范随项目迭代持续更新，所有团队成员必须遵守。如有疑议，通过 Code Review 讨论后统一修订。
