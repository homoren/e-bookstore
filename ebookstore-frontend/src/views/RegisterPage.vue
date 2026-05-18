<template>
  <div class="register-page">
    <Header />

    <div class="register-container">
      <div class="register-box">
        <h2 class="title">会员注册</h2>
        <p class="subtitle">加入 My-eBookStore，享受优质购书体验</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
          @submit.prevent="handleRegister"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名（3-20位）"
              @blur="checkUsername"
            />
            <span v-if="usernameChecked && !usernameExists" class="check-success">
              <el-icon><CircleCheck /></el-icon> 用户名可用
            </span>
            <span v-if="usernameExists" class="check-error">
              <el-icon><CircleClose /></el-icon> 用户名已存在
            </span>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（6-20位）"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
            />
          </el-form-item>

          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱（选填）" />
          </el-form-item>

          <el-form-item label="收货地址" prop="address">
            <el-input
              v-model="form.address"
              type="textarea"
              :rows="2"
              placeholder="请输入详细收货地址"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              style="width: 100%"
              @click="handleRegister"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, checkUsername as checkUsernameApi } from '@/api/user'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const usernameChecked = ref(false)
const usernameExists = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  address: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const checkUsername = async () => {
  if (!form.username || form.username.length < 3) {
    usernameChecked.value = false
    return
  }

  try {
    const res = await checkUsernameApi(form.username)
    usernameExists.value = res.exists
    usernameChecked.value = true
  } catch {
    usernameChecked.value = false
  }
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (usernameExists.value) {
    ElMessage.error('用户名已存在，请更换')
    return
  }

  loading.value = true
  try {
    // 直接解构剔除 confirmPassword，不赋值给变量
    await register({ ...form, confirmPassword: undefined })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    console.error('注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.register-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
}

.register-box {
  width: 600px;
  padding: 40px 50px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.title {
  font-size: 28px;
  color: #333;
  text-align: center;
  margin-bottom: 8px;
}

.subtitle {
  text-align: center;
  color: #999;
  margin-bottom: 30px;
}

.check-success {
  color: #67c23a;
  font-size: 13px;
  margin-left: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.check-error {
  color: #f56c6c;
  font-size: 13px;
  margin-left: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.register-footer a {
  color: #409eff;
  text-decoration: none;
  margin-left: 8px;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>
