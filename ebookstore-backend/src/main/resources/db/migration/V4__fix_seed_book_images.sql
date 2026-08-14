-- 种子图书封面路径与真实文件(.png)不一致,修正(按 ISBN 精确匹配,不影响其他图书)
UPDATE book SET cover_image = '/images/java-think.png' WHERE isbn = '9787111213826' AND cover_image = '/images/java-think.jpg';
UPDATE book SET cover_image = '/images/mysql.png' WHERE isbn = '9787115316882' AND cover_image = '/images/mysql.jpg';
UPDATE book SET cover_image = '/images/nce2.png' WHERE isbn = '9787560013473' AND cover_image = '/images/nce2.jpg';
