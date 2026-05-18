<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-section">
          <h3>关于我们</h3>
          <p>
            My-eBookStore 是一家专注于英语和计算机书籍的网上书店，致力于为读者提供优质的购书体验。
          </p>
        </div>
        <div class="footer-section">
          <h3>联系方式</h3>
          <p>电话：138-0000-0000</p>
          <p>邮箱：service@myebookstore.com</p>
          <p>地址：北京市朝阳区xx路xx号</p>
        </div>
        <div class="footer-section">
          <h3>营业时间</h3>
          <p>周一至周日 9:00 - 21:00</p>
          <p>法定节假日正常营业</p>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2024 My-eBookStore. All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: #2c3e50;
  color: #ecf0f1;
  padding: 40px 0 20px;
  margin-top: 40px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
}

.footer-section h3 {
  margin-bottom: 16px;
  color: #fff;
}

.footer-section p {
  line-height: 1.8;
  color: #bdc3c7;
}

.footer-bottom {
  text-align: center;
  padding-top: 30px;
  margin-top: 30px;
  border-top: 1px solid #34495e;
  color: #7f8c8d;
}
</style>
