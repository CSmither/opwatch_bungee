# OpWatch Bungee

## Creating Database
To use this plugin you now *need* an sql database.
Use the SQL commands below to create the database and table needed.
```
CREATE DATABASE `opwatch`;
USE `opwatch`;
CREATE TABLE `signs` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `line0` varchar(32) DEFAULT NULL,
  `line1` varchar(32) DEFAULT NULL,
  `line2` varchar(32) DEFAULT NULL,
  `line3` varchar(32) DEFAULT NULL,
  `server` varchar(32) NOT NULL,
  `world` varchar(32) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `z` int(11) NOT NULL,
  `player` varchar(16) NOT NULL,
  `wiped` int(1) unsigned DEFAULT '0',
  `attemptWipe` int(1) unsigned DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
```
