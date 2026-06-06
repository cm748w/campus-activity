<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-if="$route.meta.title && $route.path !== '/dashboard'">
      {{ $route.meta.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
</script>
