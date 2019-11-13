-- Create the Config table
CREATE TABLE IF NOT EXISTS `CONFIGS`
(`ID` IDENTITY NOT NULL,
 `NAME` VARCHAR(255) NOT NULL,
 `DESCRIPTION` VARCHAR(1024),
 `VERSION` INT NOT NULL,
 `UPDATED` BIGINT NOT NULL);
-- Create the Config Attributes table
CREATE TABLE IF NOT EXISTS `CONFIG_ATTRIBUTES`
(`ID` IDENTITY NOT NULL,
 `CONFIG_ID` BIGINT NOT NULL,
 `KEY` VARCHAR(255) NOT NULL,
 `VALUE` VARCHAR(1024),
 FOREIGN KEY(CONFIG_ID) REFERENCES CONFIGS(ID) ON DELETE CASCADE);
-- Create the Properties table
CREATE TABLE IF NOT EXISTS `PROPERTIES`
(`ID` IDENTITY NOT NULL,
 `PROPERTY_ID` BIGINT NOT NULL,
 `CONFIG_ID` BIGINT NOT NULL,
 `NAME` VARCHAR(255) NOT NULL,
 `CAPTION` VARCHAR(255),
 `DESCRIPTION` VARCHAR(1024),
 `TYPE` ENUM ('BOOL', 'DOUBLE', 'LONG', 'STRING', 'STRING_ARRAY') NOT NULL,
 `VALUE` VARCHAR(4096) NOT NULL,
 `VERSION` INT NOT NULL,
 FOREIGN KEY(CONFIG_ID) REFERENCES CONFIGS(ID) ON DELETE CASCADE,
 FOREIGN KEY(PROPERTY_ID) REFERENCES PROPERTIES(ID) ON DELETE CASCADE);
-- Create the Property Attributes table
CREATE TABLE IF NOT EXISTS `PROPERTY_ATTRIBUTES`
(`ID` IDENTITY NOT NULL,
 `PROPERTY_ID` BIGINT NOT NULL,
 `KEY` VARCHAR(255) NOT NULL,
 `VALUE` VARCHAR(1024),
 FOREIGN KEY(PROPERTY_ID) REFERENCES PROPERTIES(ID) ON DELETE CASCADE);