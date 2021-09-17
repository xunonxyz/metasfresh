-- 2021-09-17T06:46:07.756Z
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO AD_Process (AccessLevel,AD_Client_ID,AD_Org_ID,AD_Process_ID,AllowProcessReRun,Classname,CopyFromProcess,Created,CreatedBy,Description,EntityType,Help,IsActive,IsApplySecuritySettings,IsBetaFunctionality,IsDirectPrint,IsFormatExcelFile,IsNotifyUserAfterExecution,IsOneInstanceOnly,IsReport,IsTranslateExcelHeaders,IsUseBPartnerLanguage,LockWaitTimeout,Name,PostgrestResponseFormat,RefreshAllAfterExecution,ShowHelp,Type,Updated,UpdatedBy,Value) VALUES ('3',0,0,584907,'Y','de.metas.bpartner.process.C_BPartner_AddRemoveCreditStopStatus_Example','N',TO_TIMESTAMP('2021-09-17 08:46:07','YYYY-MM-DD HH24:MI:SS'),100,'','U','If parameter IsSetCreditStop is on Y, the process will set CreditStatus to CreditStop.
If parameter IsSetCreditStop is on N, the process will remove CreditStop status, but only if the credit limit allows it.','Y','N','N','N','Y','N','N','N','Y','Y',0,'ExampleProcess','json','N','Y','Java',TO_TIMESTAMP('2021-09-17 08:46:07','YYYY-MM-DD HH24:MI:SS'),100,'C_BPartner_AddRemoveCreditStopStatus_Example')
;

-- 2021-09-17T06:46:07.776Z
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO AD_Process_Trl (AD_Language,AD_Process_ID, Description,Help,Name, IsTranslated,AD_Client_ID,AD_Org_ID,Created,Createdby,Updated,UpdatedBy,IsActive) SELECT l.AD_Language, t.AD_Process_ID, t.Description,t.Help,t.Name, 'N',t.AD_Client_ID,t.AD_Org_ID,t.Created,t.Createdby,t.Updated,t.UpdatedBy,'Y' FROM AD_Language l, AD_Process t WHERE l.IsActive='Y'AND (l.IsSystemLanguage='Y') AND t.AD_Process_ID=584907 AND NOT EXISTS (SELECT 1 FROM AD_Process_Trl tt WHERE tt.AD_Language=l.AD_Language AND tt.AD_Process_ID=t.AD_Process_ID)
;

-- 2021-09-17T06:48:58.030Z
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO AD_Process_Para (AD_Client_ID,AD_Org_ID,AD_Process_ID,AD_Process_Para_ID,AD_Reference_ID,AD_Reference_Value_ID,ColumnName,Created,CreatedBy,Description,EntityType,FieldLength,IsActive,IsAutocomplete,IsCentrallyMaintained,IsEncrypted,IsMandatory,IsRange,Name,SeqNo,Updated,UpdatedBy) VALUES (0,0,584907,542096,17,541325,'setCreditStatus',TO_TIMESTAMP('2021-09-17 08:48:57','YYYY-MM-DD HH24:MI:SS'),100,'Lorem Ipsum','U',0,'Y','N','Y','N','N','N','neuer ExampleProcess',10,TO_TIMESTAMP('2021-09-17 08:48:57','YYYY-MM-DD HH24:MI:SS'),100)
;

-- 2021-09-17T06:48:58.048Z
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO AD_Process_Para_Trl (AD_Language,AD_Process_Para_ID, Description,Help,Name, IsTranslated,AD_Client_ID,AD_Org_ID,Created,Createdby,Updated,UpdatedBy,IsActive) SELECT l.AD_Language, t.AD_Process_Para_ID, t.Description,t.Help,t.Name, 'N',t.AD_Client_ID,t.AD_Org_ID,t.Created,t.Createdby,t.Updated,t.UpdatedBy,'Y' FROM AD_Language l, AD_Process_Para t WHERE l.IsActive='Y'AND (l.IsSystemLanguage='Y') AND t.AD_Process_Para_ID=542096 AND NOT EXISTS (SELECT 1 FROM AD_Process_Para_Trl tt WHERE tt.AD_Language=l.AD_Language AND tt.AD_Process_Para_ID=t.AD_Process_Para_ID)
;

-- 2021-09-17T06:51:43.578Z
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO AD_Table_Process (AD_Client_ID,AD_Org_ID,AD_Process_ID,AD_Table_ID,AD_Table_Process_ID,Created,CreatedBy,EntityType,IsActive,Updated,UpdatedBy,WEBUI_DocumentAction,WEBUI_IncludedTabTopAction,WEBUI_ViewAction,WEBUI_ViewQuickAction,WEBUI_ViewQuickAction_Default) VALUES (0,0,584907,291,540998,TO_TIMESTAMP('2021-09-17 08:51:43','YYYY-MM-DD HH24:MI:SS'),100,'D','Y',TO_TIMESTAMP('2021-09-17 08:51:43','YYYY-MM-DD HH24:MI:SS'),100,'Y','N','Y','N','N')
;

