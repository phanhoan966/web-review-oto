<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const email = ref('')
const password = ref('')
const auth = useAuthStore()
const router = useRouter()

async function submit() {
  try {
    await auth.adminLogin(email.value, password.value)
    router.push({ name: 'admin-dashboard' })
  } catch (error) {}
}
</script>

<template>
  <div class="auth-shell">
    <div class="form-card surface">
      <h1>Đăng nhập Admin</h1>
      <p class="sub">Chỉ dành cho admin/manager/system admin.</p>
      <form @submit.prevent="submit">
        <label>Email</label>
        <input v-model="email" type="email" required placeholder="admin@example.com" />

        <label>Mật khẩu</label>
        <input v-model="password" type="password" required placeholder="••••••••" />

        <button type="submit" :disabled="auth.loading">{{ auth.loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}</button>
        <p v-if="auth.error" class="error">{{ auth.error }}</p>
      </form>

      <p class="foot">
        <RouterLink :to="{ name: 'login' }">Quay lại đăng nhập thường</RouterLink>
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

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.foot {
  margin-top: 12px;
  color: var(--muted);
}

.error {
  color: #b91c1c;
  font-weight: 600;
}
</style>
