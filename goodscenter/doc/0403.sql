alter table goods_item add column conversion int unsigned not null default 1 COMMENT '商品换算比例';
alter table goods_item add index item_code(item_code);
alter table goods_item add index conversion(conversion);