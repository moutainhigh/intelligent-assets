INSERT INTO `biz_number_rule` (`name`, `type`, `prefix`, `date_format`, `length`, `range`, `create_time`, `update_time`, `is_enable`, `step`) VALUES ('杭水公寓租赁退款单', 'hzsc_lodgingLease_refundOrder', 'HZSCTKGY', 'yyyyMMdd', 4, '1', '2020-11-25 15:39:54', '2020-11-25 15:39:54', 1, 50);
INSERT INTO `biz_number_rule` (`name`, `type`, `prefix`, `date_format`, `length`, `range`, `create_time`, `update_time`, `is_enable`, `step`) VALUES ('杭水公寓租赁缴费单号', 'hzsc_lodgingLease_paymentOrder', 'HZSCPOGY', 'yyyyMMdd', 5, '1', '2020-11-25 15:38:46', '2020-11-25 15:38:46', 1, 50);
INSERT INTO `biz_number_rule` (`name`, `type`, `prefix`, `date_format`, `length`, `range`, `create_time`, `update_time`, `is_enable`, `step`) VALUES ('杭水公寓租赁订单号', 'hzsc_lodgingLease_leaseOrder', 'HZSCGY', 'yyyyMMdd', 4, '1', '2020-11-25 15:37:46', '2020-11-25 15:37:46', 1, 50);
INSERT INTO `biz_number_rule` (`name`, `type`, `prefix`, `date_format`, `length`, `range`, `create_time`, `update_time`, `is_enable`, `step`) VALUES ('杭水冷库租赁退款单', 'hzsc_locationLease_refundOrder', 'HZSCTKLK', 'yyyyMMdd', 4, '1', '2020-11-25 15:34:20', '2020-11-25 15:34:20', 1, 50);
INSERT INTO `biz_number_rule` (`name`, `type`, `prefix`, `date_format`, `length`, `range`, `create_time`, `update_time`, `is_enable`, `step`) VALUES ('杭水冷库租赁缴费单号', 'hzsc_locationLease_paymentOrder', 'HZSCPOLK', 'yyyyMMdd', 5, '1', '2020-11-25 15:32:57', '2020-11-25 15:32:57', 1, 50);
INSERT INTO `biz_number_rule` (`name`, `type`, `prefix`, `date_format`, `length`, `range`, `create_time`, `update_time`, `is_enable`, `step`) VALUES ('杭水冷库租赁订单号', 'hzsc_locationLease_leaseOrder', 'HZSCLK', 'yyyyMMdd', 4, '1', '2020-11-25 15:31:24', '2020-11-25 15:35:34', 1, 50);

ALTER TABLE `dili_ia`.`assets_lease_order`
ADD COLUMN `biz_type` varchar(120) NULL COMMENT '业务类型（数据字典值编码 1：摊位租赁 4：冷库租赁 5：公寓租赁）' AFTER `assets_type`;
ALTER TABLE `dili_ia`.`assets_lease_order_item`
ADD COLUMN `biz_type` varchar(120) NULL COMMENT '业务类型（数据字典值编码 1：摊位租赁 4：冷库租赁 5：公寓租赁）' AFTER `assets_type`;
update `dili_ia`.`assets_lease_order` set biz_type = '1';
update `dili_ia`.`assets_lease_order_item` set biz_type = '1';