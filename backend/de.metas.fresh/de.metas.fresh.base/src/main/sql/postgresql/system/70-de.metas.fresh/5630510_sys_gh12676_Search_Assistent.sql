-- 2022-03-17T13:52:55.351Z
-- URL zum Konzept
UPDATE AD_Tab SET AD_Table_ID=541588,Updated=TO_TIMESTAMP('2022-03-17 14:52:55','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Tab_ID=543530
;

-- 2022-03-17T13:52:56.193Z
-- URL zum Konzept
UPDATE AD_Field SET AD_Column_ID=573241, Description='Suchschlüssel für den Eintrag im erforderlichen Format - muss eindeutig sein', Help='A search key allows you a fast method of finding a particular record.
If you leave the search key empty, the system automatically creates a numeric number.  The document sequence used for this fallback number is defined in the "Maintain Sequence" window with the name "DocumentNo_<TableName>", where TableName is the actual name of the table (e.g. C_Order).', Name='Suchschlüssel',Updated=TO_TIMESTAMP('2022-03-17 14:52:56','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Field_ID=639411
;

-- 2022-03-17T13:52:56.428Z
-- URL zum Konzept
/* DDL */  select update_FieldTranslation_From_AD_Name_Element(620) 
;

-- 2022-03-17T13:52:56.587Z
-- URL zum Konzept
DELETE FROM AD_Element_Link WHERE AD_Field_ID=639411
;

-- 2022-03-17T13:52:56.662Z
-- URL zum Konzept
/* DDL */ select AD_Element_Link_Create_Missing_Field(639411)
;

-- 2022-03-17T13:52:57.459Z
-- URL zum Konzept
UPDATE AD_Field SET AD_Column_ID=573242, Description=NULL, Help=NULL, Name='Firma',Updated=TO_TIMESTAMP('2022-03-17 14:52:57','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Field_ID=639412
;

-- 2022-03-17T13:52:57.597Z
-- URL zum Konzept
/* DDL */  select update_FieldTranslation_From_AD_Name_Element(540400) 
;

-- 2022-03-17T13:52:57.668Z
-- URL zum Konzept
DELETE FROM AD_Element_Link WHERE AD_Field_ID=639412
;

-- 2022-03-17T13:52:57.734Z
-- URL zum Konzept
/* DDL */ select AD_Element_Link_Create_Missing_Field(639412)
;

-- 2022-03-17T13:52:58.516Z
-- URL zum Konzept
UPDATE AD_Field SET AD_Column_ID=573243, Description='', Help='', Name='Name',Updated=TO_TIMESTAMP('2022-03-17 14:52:58','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Field_ID=639410
;

-- 2022-03-17T13:52:58.650Z
-- URL zum Konzept
/* DDL */  select update_FieldTranslation_From_AD_Name_Element(469) 
;

-- 2022-03-17T13:52:58.892Z
-- URL zum Konzept
DELETE FROM AD_Element_Link WHERE AD_Field_ID=639410
;

-- 2022-03-17T13:52:58.956Z
-- URL zum Konzept
/* DDL */ select AD_Element_Link_Create_Missing_Field(639410)
;

-- 2022-03-17T13:52:59.690Z
-- URL zum Konzept
UPDATE AD_Field SET AD_Column_ID=573058, Description=NULL, Help=NULL, Name='Nachname',Updated=TO_TIMESTAMP('2022-03-17 14:52:59','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Field_ID=635057
;

-- 2022-03-17T13:52:59.823Z
-- URL zum Konzept
/* DDL */  select update_FieldTranslation_From_AD_Name_Element(540399) 
;

-- 2022-03-17T13:52:59.896Z
-- URL zum Konzept
DELETE FROM AD_Element_Link WHERE AD_Field_ID=635057
;

-- 2022-03-17T13:52:59.961Z
-- URL zum Konzept
/* DDL */ select AD_Element_Link_Create_Missing_Field(635057)
;

-- 2022-03-17T13:53:00.693Z
-- URL zum Konzept
UPDATE AD_Field SET AD_Column_ID=573057, Description='Vorname', Help=NULL, Name='Vorname',Updated=TO_TIMESTAMP('2022-03-17 14:53:00','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Field_ID=635058
;

-- 2022-03-17T13:53:00.824Z
-- URL zum Konzept
/* DDL */  select update_FieldTranslation_From_AD_Name_Element(540398) 
;

-- 2022-03-17T13:53:00.909Z
-- URL zum Konzept
DELETE FROM AD_Element_Link WHERE AD_Field_ID=635058
;

-- 2022-03-17T13:53:00.974Z
-- URL zum Konzept
/* DDL */ select AD_Element_Link_Create_Missing_Field(635058)
;

-- 2022-03-17T13:53:01.743Z
-- URL zum Konzept
UPDATE AD_Field SET AD_Column_ID=573059, Description='Name des Ortes', Help='Bezeichnet einen einzelnen Ort in diesem Land oder dieser Region.', Name='Ort',Updated=TO_TIMESTAMP('2022-03-17 14:53:01','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Field_ID=635059
;

-- 2022-03-17T13:53:01.879Z
-- URL zum Konzept
/* DDL */  select update_FieldTranslation_From_AD_Name_Element(225) 
;

-- 2022-03-17T13:53:01.950Z
-- URL zum Konzept
DELETE FROM AD_Element_Link WHERE AD_Field_ID=635059
;

-- 2022-03-17T13:53:02.013Z
-- URL zum Konzept
/* DDL */ select AD_Element_Link_Create_Missing_Field(635059)
;

-- 2022-03-17T13:53:02.754Z
-- URL zum Konzept
UPDATE AD_Field SET AD_Column_ID=573244, Description='Organisatorische Einheit des Mandanten', Help='Eine Organisation ist ein Bereich ihres Mandanten - z.B. Laden oder Abteilung. Sie können Daten über Organisationen hinweg gemeinsam verwenden.', Name='Organisation',Updated=TO_TIMESTAMP('2022-03-17 14:53:02','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Field_ID=639413
;

-- 2022-03-17T13:53:02.887Z
-- URL zum Konzept
/* DDL */  select update_FieldTranslation_From_AD_Name_Element(113) 
;

-- 2022-03-17T13:53:03.198Z
-- URL zum Konzept
DELETE FROM AD_Element_Link WHERE AD_Field_ID=639413
;

-- 2022-03-17T13:53:03.263Z
-- URL zum Konzept
/* DDL */ select AD_Element_Link_Create_Missing_Field(639413)
;

