<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const email = ref('')
const sent = ref(false)
const auth = useAuthStore()

async function submit() {
  await auth.forgotPassword(email.value)
  sent.value = true
}
</script>

<template>
  <div class="auth-shell">
    <div class="form-card surface">
      <h1>Quên mật khẩu</h1>
      <p class="sub">Nhập email để nhận liên kết đặt lại mật khẩu.</p>
      <form @submit.prevent="submit">
        <label>Email</label>
        <input v-model="email" type="email" required placeholder="you@example.com" />
        <button type="submit">Gửi liên kết</button>
        <p v-if="sent" class="success">Chúng tôi đã gửi hướng dẫn khôi phục.</p>
      </form>
      <p class="foot">
        <RouterLink :to="{ name: 'login' }">Quay lại đăng nhập</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.auth-shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 40px 16px;
  background: var(--bg);
}

.form-card {
  width: min(420px, 100%);
  padding: 28px;
  border-radius: 20px;
  box-shadow: var(--shadow);
}

h1 {
  margin: 0;
  font-size: 26px;
}

.sub {
  margin: 6px 0 16px;
  color: var(--muted);
}

form {
  display: grid;
  gap: 10px;
}

label {
  font-weight: 600;
  color: #1f2937;
}

input {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: #f7f9fc;
  font-size: 15px;
}

button {
  margin-top: 10px;
  padding: 12px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  color: white;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 12px 30px rgba(37, 99, 235, 0.35);
}

.foot {
  margin-top: 12px;
  color: var(--muted);
}

.success {
  color: #16a34a;
  font-weight: 600;
}
</style>
