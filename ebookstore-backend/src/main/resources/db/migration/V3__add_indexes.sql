-- 补充列表排序/状态过滤/联表查询常用索引
CREATE INDEX idx_order_created_at ON `order` (created_at);
CREATE INDEX idx_order_status ON `order` (status);
CREATE INDEX idx_order_item_book_id ON order_item (book_id);
CREATE INDEX idx_message_created_at ON message (created_at);
CREATE INDEX idx_purchase_created_at ON purchase (created_at);
