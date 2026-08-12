-- 初始化测试数据(幂等:INSERT IGNORE,已存在的键不会重复插入)

-- 一级目录
INSERT IGNORE INTO category (id, name, parent_id, sort_order)
VALUES
    (1, '英语', 0, 1),
    (2, '计算机', 0, 2);

-- 二级目录(英语下的子分类)
INSERT IGNORE INTO category (id, name, parent_id, sort_order)
VALUES
    (11, '英语词汇', 1, 1),
    (12, '英语语法', 1, 2),
    (13, '英语阅读', 1, 3);

-- 二级目录(计算机下的子分类)
INSERT IGNORE INTO category (id, name, parent_id, sort_order)
VALUES
    (21, '编程语言', 2, 1),
    (22, '数据库', 2, 2),
    (23, '操作系统', 2, 3);

-- 测试图书
INSERT IGNORE INTO book (
    id, title, author, isbn, publisher, category_id,
    price, cost_price, stock, cover_image, description, detail_html, difficulty_level
)
VALUES
    (
        1, 'Java编程思想', 'Bruce Eckel', '9787111213826', '机械工业出版社', 21,
        108.00, 70.00, 50, '/images/java-think.jpg',
        'Java经典著作', '<h3>内容简介</h3><p>本书赢得了全球程序员的广泛赞誉...</p>', 3
    ),
    (
        2, 'MySQL必知必会', 'Ben Forta', '9787115316882', '人民邮电出版社', 22,
        39.00, 25.00, 30, '/images/mysql.jpg',
        'MySQL入门经典', '<h3>内容简介</h3><p>书中从介绍简单的数据检索开始...</p>', 1
    ),
    (
        3, '新概念英语2', '亚历山大', '9787560013473', '外语教学与研究出版社', 13,
        49.90, 32.00, 100, '/images/nce2.jpg',
        '实践与进步', '<h3>内容简介</h3><p>构建完美的英语学习体系...</p>', 1
    );
