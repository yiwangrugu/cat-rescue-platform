<template>
  <div class="home user-home">
    <!-- 用户端欢迎页面 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="title-main">流浪猫救助社区 </span>
          <span class="title-sub">为流浪猫寻找温暖的家</span>
        </h1>
        <p class="hero-description">
          这是一个充满爱心的社区，致力于帮助流浪猫找到温暖的家园。
          在这里，您可以浏览待领养的猫咪、申请领养、参与社区讨论。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/cats')" class="hero-btn">
            <el-icon><Star /></el-icon>
            浏览可爱猫咪
          </el-button>
          <el-button size="large" @click="$router.push('/community')" class="hero-btn">
            <el-icon><ChatDotRound /></el-icon>
            加入社区讨论
          </el-button>
        </div>
      </div>
      <div class="hero-image">
        <div class="cat-illustration">
          <div class="cat-silhouette"></div>
        </div>
      </div>
    </div>

    <section class="section1">
      <div class="content">
        <p>当我们穿行于钢铁与霓虹的丛林</p>
        <p>总会与它们不期而遇</p>
        <p>那些在车底、墙角、垃圾桶边悄然谋生的柔软生命</p>
      </div>
      <div class="show"></div>
    </section>
    <section class="section2">
      <div class="content">
        <p>它们可能曾是家中温暖的宝贝</p>
        <p>如今却成了城市缝隙里的漂泊者</p>
        <p>每一声怯生生的叫唤</p>
        <p>都是对一点温存的小心试探</p>
      </div>
      <div class="show"></div>    
    </section>
    <section class="section3">
      <div class="content">
        <p>它们不是城市的瑕疵</p>
        <p>而是与我们共享这片天地的生灵</p>
        <p>生命不该有贵贱之分</p>
      </div>
      <div class="show"></div>    
    </section>
    <section class="section4">
      <div class="content">
        <p>当我们在匆忙中停下脚步</p>
        <p>给予一点注视、一份关怀</p>
        <p>我们不仅是在救助它们</p>
        <p>也是在唤醒自己内心深处最柔软的善良</p>
      </div>
      <div class="show"></div>
    </section>
    
  </div>
</template>

<script setup>

</script>

<style scoped>
html, body {
  width: 100%;
  height: 100%;
}
 section[class^="section"] {
      width: 100%;
      height: 100%;
    }
  .section1 {
    background: url('../public/img/home1.jpg') no-repeat fixed center center/cover;
  }
  .section2 {
    background: url('../public/img/home2.jpg') no-repeat fixed center center/cover;
  }
  .section3 {
    background: url('../public/img/home3.jpg') no-repeat fixed center center/cover;
  }
  .section4 {
    background: url('../public/img/home4.jpg') no-repeat fixed center center/cover;
  }
  .content {
      width: 100%;
      height: 25%;
      background-color: rgba(0, 0, 0, 0.6);
      color: #fff;
      line-height: 15px;
      font-size: 16px;
      padding: 25px;
    }
    .show {
      width: 100%;
      height: 600px;
    }

.home-container {
  min-height: 100vh;
}

.header {
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 30px;
}

.nav-item {
  text-decoration: none;
  color: #666;
  font-weight: 500;
  transition: color 0.3s;
}

.nav-item:hover {
  color: #409eff;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  cursor: pointer;
  color: #333;
  font-weight: 500;
}

.auth-buttons {
  display: flex;
  gap: 15px;
}

.login-btn, .register-btn {
  padding: 8px 16px;
  text-decoration: none;
  border-radius: 4px;
  font-weight: 500;
}

.login-btn {
  color: #409eff;
  border: 1px solid #409eff;
}

.register-btn {
  background: #409eff;
  color: white;
}

.main-content {
  margin-top: 80px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding: 40px 20px;
}

.hero-section {
  text-align: center;
  padding: 20px 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 12px;
}

.hero-section h2 {
  font-size: 36px;
  margin-bottom: 20px;
  color: #333;
}

.hero-section p {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
}

.hero-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>