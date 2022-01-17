
-- 2022-01-13T10:08:41.427Z
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO M_Attribute (AD_Client_ID,AD_Org_ID,AttributeValueType,Created,CreatedBy,IsActive,IsAlwaysUpdateable,IsAttrDocumentRelevant,IsHighVolume,IsInstanceAttribute,IsMandatory,IsPricingRelevant,IsReadOnlyValues,IsStorageRelevant,IsTransferWhenNull,M_Attribute_ID,Name,Updated,UpdatedBy,Value) VALUES (0,0,'S',TO_TIMESTAMP('2022-01-13 12:08:41','YYYY-MM-DD HH24:MI:SS'),100,'Y','N','N','N','N','N','N','N','N','N',540101,'LockNotice',TO_TIMESTAMP('2022-01-13 12:08:41','YYYY-MM-DD HH24:MI:SS'),100,'LockNotice')
;

-- 2022-01-13T10:08:41.583Z
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE M_Attribute SET IsInstanceAttribute='Y',Updated=TO_TIMESTAMP('2022-01-13 12:08:41','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE M_Attribute_ID=540101
;

-- 2022-01-13T10:08:41.585Z
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE M_AttributeSet mas SET IsInstanceAttribute='Y' WHERE IsInstanceAttribute='N' AND EXISTS (SELECT * FROM M_AttributeUse mau WHERE mas.M_AttributeSet_ID=mau.M_AttributeSet_ID AND mau.M_Attribute_ID=540101)
;

-- 2022-01-13T10:10:13.956Z
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO M_HU_PI_Attribute (AD_Client_ID,AD_Org_ID,Created,CreatedBy,HU_TansferStrategy_JavaClass_ID,IsActive,IsDisplayed,IsInstanceAttribute,IsMandatory,IsOnlyIfInProductAttributeSet,IsReadOnly,M_Attribute_ID,M_HU_PI_Attribute_ID,M_HU_PI_Version_ID,PropagationType,SeqNo,SplitterStrategy_JavaClass_ID,Updated,UpdatedBy,UseInASI) VALUES (0,0,TO_TIMESTAMP('2022-01-13 12:10:13','YYYY-MM-DD HH24:MI:SS'),100,540027,'Y','Y','N','N','N','Y',540101,540081,100,'TOPD',9110,540017,TO_TIMESTAMP('2022-01-13 12:10:13','YYYY-MM-DD HH24:MI:SS'),100,'Y')
;

-- 2022-01-13T10:10:34.242Z
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE M_HU_PI_Attribute SET UseInASI='N',Updated=TO_TIMESTAMP('2022-01-13 12:10:34','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE M_HU_PI_Attribute_ID=540081
;

-- 2022-01-13T10:10:34.723Z
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE M_HU_PI_Attribute SET IsInstanceAttribute='Y',Updated=TO_TIMESTAMP('2022-01-13 12:10:34','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE M_HU_PI_Attribute_ID=540081
;

