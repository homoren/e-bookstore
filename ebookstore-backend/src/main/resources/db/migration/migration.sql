-- 图书分类表（两级目录支持）
CREATE TABLE IF NOT EXISTS category (
                                        id INT PRIMARY KEY AUTO_INCREMENT,
                                        name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id INT DEFAULT 0 COMMENT '0表示一级目录，非0为二级目录的父ID',
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 图书表
CREATE TABLE IF NOT EXISTS book (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    title VARCHAR(200) NOT NULL COMMENT '书名',
    author VARCHAR(100) COMMENT '作者',
    isbn VARCHAR(20) UNIQUE COMMENT 'ISBN号',
    publisher VARCHAR(100) COMMENT '出版社',
    publish_date DATE COMMENT '出版日期',
    category_id INT COMMENT '所属二级分类ID',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    cost_price DECIMAL(10,2) COMMENT '成本价',
    stock INT DEFAULT 0 COMMENT '库存数量',
    cover_image VARCHAR(500) COMMENT '封面图片URL',
    description TEXT COMMENT '简介',
    detail_html TEXT COMMENT '详细描述（三层信息）',
    sample_code_url VARCHAR(500) COMMENT '随书源码下载地址',
    difficulty_level TINYINT COMMENT '难度等级：1入门 2进阶 3高级',
    sales_count INT DEFAULT 0 COMMENT '销量',
    status TINYINT DEFAULT 1 COMMENT '1上架 0下架',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category_id),
    INDEX idx_title (title)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化测试数据：一级目录
INSERT INTO category (id, name, parent_id, sort_order)
VALUES
    (1, '英语', 0, 1),
    (2, '计算机', 0, 2);

-- 初始化测试数据：二级目录（英语下的子分类）
INSERT INTO category (id, name, parent_id, sort_order)
VALUES
    (11, '英语词汇', 1, 1),
    (12, '英语语法', 1, 2),
    (13, '英语阅读', 1, 3);

-- 初始化测试数据：二级目录（计算机下的子分类）
INSERT INTO category (id, name, parent_id, sort_order)
VALUES
    (21, '编程语言', 2, 1),
    (22, '数据库', 2, 2),
    (23, '操作系统', 2, 3);

-- 初始化测试图书数据
INSERT INTO book (
    title,
    author,
    isbn,
    publisher,
    category_id,
    price,
    cost_price,
    stock,
    cover_image,
    description,
    detail_html,
    difficulty_level
)
VALUES
    (
        'Java编程思想',
        'Bruce Eckel',
        '9787111213826',
        '机械工业出版社',
        21,
        108.00,
        70.00,
        50,
        '/images/java-think.jpg',
        'Java经典著作',
        '<h3>内容简介</h3><p>本书赢得了全球程序员的广泛赞誉...</p>',
        3
    ),
    (
        'MySQL必知必会',
        'Ben Forta',
        '9787115316882',
        '人民邮电出版社',
        22,
        39.00,
        25.00,
        30,
        '/images/mysql.jpg',
        'MySQL入门经典',
        '<h3>内容简介</h3><p>书中从介绍简单的数据检索开始...</p>',
        1
    ),
    (
        '新概念英语2',
        '亚历山大',
        '9787560013473',
        '外语教学与研究出版社',
        13,
        49.90,
        32.00,
        100,
        '/images/nce2.jpg',
        '实践与进步',
        '<h3>内容简介</h3><p>构建完美的英语学习体系...</p>',
        1
    );