<template>
  <div id="app">
    <main class="main-content">
      <component :is="currentPage" />
    </main>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      currentRoute: window.location.pathname || '/'
    };
  },
  computed: {
    currentPage() {
      return 'div';
    }
  }
};
</script>

<style>
#app {
  min-height: 100vh;
}

.main-content {
  background-color: #0b1326;
}
</style>