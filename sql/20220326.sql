create table `mq_message`
(
    `id`             bigint        not null,
    `message_id`     varchar(32)   not null default '' COMMENT '消息id',
    `content`        varchar(3000) not null default '' COMMENT '消息内容',
    `to_exchange`    varchar(255)  not null default '' COMMENT '目标交换机',
    `routing_key`    varchar(255)  not null default '' COMMENT '路由键',
    `class_type`     varchar(255)  not null default '' COMMENT 'class的类型',
    `message_status` tyniint(1) not null default '0' COMMENT '0，新建；1，已发送；2，错误抵达；3，已抵达',
    `create_time`    datetime               default NULL COMMENT '创建时间',
    `update_time`    datetime               default NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;