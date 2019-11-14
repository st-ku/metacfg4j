SELECT `C`.`ID`, `C`.`NAME`, `C`.`DESCRIPTION`, `C`.`VERSION`, `C`.`UPDATED`, `A`.`KEY`, `A`.`VALUE` FROM
 `CONFIGS` AS `C` LEFT JOIN `CONFIG_ATTRIBUTES` AS `A` ON `C`.`ID` = `A`.`CONFIG_ID` WHERE `C`.`NAME` = 'Simple Config';

SELECT `C`.`ID`, `C`.`NAME`, `C`.`DESCRIPTION`, `C`.`VERSION`, `C`.`UPDATED`, `P`.`NAME` FROM
  `CONFIGS` AS `C` LEFT JOIN `PROPERTIES` AS `P` ON `C`.`ID` = `P`.`CONFIG_ID` WHERE `C`.`NAME` = 'Simple Config';

SELECT `C`.`ID`, `C`.`NAME`, `C`.`DESCRIPTION`, `C`.`VERSION`, `C`.`UPDATED`, `CA`.`KEY`, `CA`.`VALUE`, `P`.`ID`, `P`.`PROPERTY_ID`, `P`.`NAME` , `P`.`CAPTION`, `P`.`DESCRIPTION`, `P`.`TYPE`, `P`.`VALUE` , `P`.`VERSION` FROM
  `CONFIGS` AS `C` LEFT JOIN `PROPERTIES` AS `P` ON `C`.`ID` = `P`.`CONFIG_ID` LEFT JOIN `CONFIG_ATTRIBUTES` AS `CA` ON `C`.`ID` = `CA`.`CONFIG_ID` WHERE `C`.`NAME` = 'Simple Config';