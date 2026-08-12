-- 初始 schema:所有表均使用 IF NOT EXISTS,可安全在已存在的库上执行(幂等)
-- 图书分类表(两级目录支持)
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

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    address VARCHAR(255) COMMENT '地址',
    role TINYINT DEFAULT 1 COMMENT '1普通会员 2店主',
    status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 购物车表
CREATE TABLE IF NOT EXISTS cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_book (user_id, book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表(order 为保留字,使用反引号)
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0待付款 1待汇款确认 2已收款待配送 3已配送 4已完成 5已取消',
    receiver_name VARCHAR(50),
    receiver_phone VARCHAR(20),
    receiver_address VARCHAR(255),
    remark VARCHAR(255),
    payment_deadline DATE,
    delivery_deadline DATE,
    paid_at DATETIME,
    delivered_at DATETIME,
    completed_at DATETIME,
    receipt_signature VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单明细表
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    book_title VARCHAR(200),
    book_author VARCHAR(100),
    book_price DECIMAL(10,2),
    quantity INT,
    subtotal DECIMAL(10,2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 进货单表
CREATE TABLE IF NOT EXISTS purchase (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    purchase_no VARCHAR(32) NOT NULL UNIQUE,
    supplier VARCHAR(100),
    total_cost DECIMAL(10,2),
    status TINYINT DEFAULT 1,
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 进货明细表
CREATE TABLE IF NOT EXISTS purchase_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    purchase_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    book_title VARCHAR(200),
    quantity INT,
    cost_price DECIMAL(10,2),
    subtotal DECIMAL(10,2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_purchase (purchase_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 公告表
CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    is_top TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '1发布 0下架',
    view_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 留言表
CREATE TABLE IF NOT EXISTS message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '可为空(匿名留言)',
    username VARCHAR(50),
    content TEXT NOT NULL,
    reply TEXT,
    replied_at DATETIME,
    status TINYINT DEFAULT 1 COMMENT '1展示 0隐藏',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 日结表
CREATE TABLE IF NOT EXISTS daily_settlement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    settle_date DATE NOT NULL UNIQUE,
    total_sales DECIMAL(10,2) DEFAULT 0,
    total_cost DECIMAL(10,2) DEFAULT 0,
    total_profit DECIMAL(10,2) DEFAULT 0,
    order_count INT DEFAULT 0,
    paid_order_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
