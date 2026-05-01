CREATE TABLE `auditoria` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(100) NOT NULL,
    `date_hour_login` DATETIME,
    `date_hour_action` DATETIME NOT NULL,
    `type_action` VARCHAR(20) NOT NULL,
    `affected_entity` VARCHAR(100) NOT NULL,
    `id_affected_record` VARCHAR(50),
    `details` TEXT,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;