c---
layout: home

hero:
  name: KernelSU
  text: is
  tagline: Not Ready

features:
  - title: Kernel-based
    details: As the name suggests, KernelSU runs inside the Linux kernel, giving it more control over userspace apps.
  - title: Root access control
    details: Only permitted apps can access or see su; all other apps remain unaware of it.
  - title: Customizable root privileges
    details: KernelSU allows customization of su's uid, gid, groups, capabilities, and SELinux rules.
  - title: Metamodule system
    details: Pluggable module infrastructure allows systemless /system modifications.
---

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700;800&display=swap');

:root {
  --vp-home-hero-name-color: transparent;
  --vp-home-hero-name-background: linear-gradient(120deg, #000 0%, #000 100%);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.VPHome {
  padding: 0 !important;
}

.VPHero {
  padding: 0 !important;
  position: relative;
  overflow: hidden;
}

.VPHero .container {
  max-width: 100% !important;
  padding: 0 !important;
}

.VPHero .text,
.VPHero .image,
.VPHero .actions {
  display: none !important;
}

.VPHero .name {
  font-family: 'Inter', sans-serif !important;
  font-size: 48px !important;
  font-weight: 800 !important;
  text-align: left !important;
  padding-left: 24px !important;
  padding-top: 48px !important;
  line-height: 44px !important;
}

.VPHero .tagline {
  display: none !important;
}

/* Main container */
.ksu-container {
  position: relative;
  min-height: 100vh;
  background: #fff;
}

/* Header - pure black background */
.ksu-header {
  position: relative;
  height: 280px;
  background: #000;
}

/* White square - left edge */
.ksu-square {
  position: absolute;
  left: 0;
  bottom: 0;
  width: 96px;
  height: 96px;
  background: #fff;
}

.ksu-square::after {
  content: '';
  position: absolute;
  width: 48px;
  height: 48px;
  background: #000;
  bottom: 0;
  right: 0;
}

/* Title - left aligned, tight line-height */
.ksu-title {
  position: absolute;
  top: 48px;
  left: 130px;
  font-family: 'Inter', sans-serif;
  color: #fff;
}

.ksu-title .main {
  font-size: 48px;
  font-weight: 800;
  line-height: 44px;
}

.ksu-title .sub {
  font-size: 24px;
  font-weight: 400;
  line-height: 20px;
  margin-top: -6px;
}

.ksu-title .status {
  font-size: 32px;
  font-weight: 800;
  line-height: 36px;
  margin-left: 42px;
  margin-top: 4px;
}

/* Gradient strip - hard-stepped gradient (scanline effect) */
.ksu-gradient {
  height: 48px;
  display: flex;
  flex-direction: column;
}

.ksu-gradient .line {
  flex: 1;
}

.ksu-gradient .line:nth-child(1) { background: #000000; }
.ksu-gradient .line:nth-child(2) { background: #111111; }
.ksu-gradient .line:nth-child(3) { background: #222222; }
.ksu-gradient .line:nth-child(4) { background: #444444; }
.ksu-gradient .line:nth-child(5) { background: #666666; }
.ksu-gradient .line:nth-child(6) { background: #888888; }
.ksu-gradient .line:nth-child(7) { background: #aaaaaa; }
.ksu-gradient .line:nth-child(8) { background: #ffffff; }

/* Content - right aligned */
.ksu-content {
  background: #fff;
  padding: 32px 24px 80px 0;
  text-align: right;
}

.ksu-item {
  margin-bottom: 24px;
}

.ksu-label {
  font-family: 'Inter', sans-serif;
  font-size: 18px;
  font-weight: 700;
  color: #000;
  margin-bottom: 4px;
}

.ksu-value {
  font-family: 'Inter', sans-serif;
  font-size: 12px;
  font-weight: 400;
  color: #000;
  word-break: break-all;
}

/* Footer - clickable block button area */
.ksu-footer {
  position: relative;
  height: 112px;
  background: #fff;
}

/* Asymmetric blocks - corner to corner alignment */
.ksu-blocks {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 160px;
  height: 160px;
}

.ksu-blocks .big {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 112px;
  height: 112px;
  background: #000;
  cursor: pointer;
}

.ksu-blocks .small {
  position: absolute;
  width: 48px;
  height: 48px;
  background: #000;
  right: 112px;
  bottom: 112px;
  cursor: pointer;
}

/* Floating menu - Material Design 2 Card background */
.ksu-menu {
  position: fixed;
  bottom: 160px;
  right: 24px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.ksu-menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
}

.ksu-menu-icon {
  width: 24px;
  height: 24px;
  background: #000;
}

.ksu-menu-text {
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: #000;
  margin-left: 16px;
}

/* Features */
.VPFeatures {
  padding: 0 !important;
  background: #fff !important;
}

.VPFeatureRoot {
  background: #fff !important;
}

.VPFeature .title {
  font-family: 'Inter', sans-serif !important;
  color: #000 !important;
}

.VPFeature .details {
  font-family: 'Inter', sans-serif !important;
  color: #666 !important;
}

/* Footer */
.VPFooter {
  background: #000 !important;
}

.VPFooter .message,
.VPFooter .copyright {
  font-family: 'Inter', sans-serif !important;
  color: #fff !important;
}

@media (max-width: 768px) {
  .ksu-header { height: 240px; }
  .ksu-title { top: 32px; left: 110px; }
  .ksu-title .main { font-size: 36px; }
  .ksu-title .sub { font-size: 18px; }
  .ksu-title .status { font-size: 24px; margin-left: 30px; }
  .ksu-square { width: 72px; height: 72px; }
  .ksu-content { padding: 24px 16px 60px 0; }
  .ksu-blocks { width: 120px; height: 120px; }
  .ksu-blocks .big { width: 84px; height: 84px; }
  .ksu-blocks .small { width: 36px; height: 36px; right: 84px; bottom: 84px; }
}
</style>

<div class="ksu-container">
  <header class="ksu-header">
    <div class="ksu-title">
      <div class="main">KernelSU</div>
      <div class="sub">is</div>
      <div class="status">Not Ready</div>
    </div>
    <div class="ksu-square"></div>
  </header>

  <div class="ksu-gradient">
    <div class="line"></div>
    <div class="line"></div>
    <div class="line"></div>
    <div class="line"></div>
    <div class="line"></div>
    <div class="line"></div>
    <div class="line"></div>
    <div class="line"></div>
  </div>

  <section class="ksu-content">
    <div class="ksu-item">
      <div class="ksu-label">内核版本</div>
      <div class="ksu-value">6.1.134-android14-11-o-ExSmartTeam4KSU</div>
    </div>
    <div class="ksu-item">
      <div class="ksu-label">管理器版本</div>
      <div class="ksu-value">v1.0.0 (10001)</div>
    </div>
    <div class="ksu-item">
      <div class="ksu-label">系统指纹</div>
      <div class="ksu-value">realme/RMX5060/RE6022L1:16/BP2A.250605.015/V.5c99146-3b83f6f-3bce505:user/release-keys</div>
    </div>
    <div class="ksu-item">
      <div class="ksu-label">SELinux状态</div>
      <div class="ksu-value">Unknown</div>
    </div>
  </section>

  <!-- Floating Menu - Flat design (static preview) -->
  <div class="ksu-menu">
    <div class="ksu-menu-item">
      <div class="ksu-menu-icon">M</div>
      <span class="ksu-menu-text">odules</span>
    </div>
    <div class="ksu-menu-item">
      <div class="ksu-menu-icon">A</div>
      <span class="ksu-menu-text">pplication</span>
    </div>
    <div class="ksu-menu-item">
      <div class="ksu-menu-icon">S</div>
      <span class="ksu-menu-text">etting</span>
    </div>
  </div>

  <footer class="ksu-footer">
    <div class="ksu-blocks">
      <div class="small"></div>
      <div class="big"></div>
    </div>
  </footer>
</div>