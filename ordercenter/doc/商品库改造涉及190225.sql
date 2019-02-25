alter table order_base add column handle tinyint(3) not null default 0 comment '是否人工介入0否；1是';
alter table order_goods add column specs_tp_id varchar(45) not null comment '商品规格ID';
ALTER TABLE order_base ADD INDEX idx_handle (handle);
drop index idx_push_user_id on order_base;
drop index idx_combinationId on order_base;
drop index idx_regionalCenterId on order_base;
drop index tag_fun on order_base;
drop index idx_orderFlag on order_base;