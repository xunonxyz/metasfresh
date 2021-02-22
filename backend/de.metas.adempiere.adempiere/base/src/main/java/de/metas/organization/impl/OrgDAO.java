package de.metas.organization.impl;

import de.metas.bpartner.BPartnerLocationId;
import de.metas.cache.CCache;
import de.metas.cache.annotation.CacheCtx;
import de.metas.calendar.CalendarId;
import de.metas.common.util.time.SystemTime;
import de.metas.organization.IOrgDAO;
import de.metas.organization.OrgId;
import de.metas.organization.OrgIdNotFoundException;
import de.metas.organization.OrgInfo;
import de.metas.organization.OrgInfoUpdateRequest;
import de.metas.organization.OrgQuery;
import de.metas.organization.OrgTypeId;
import de.metas.organization.StoreCreditCardNumberMode;
import de.metas.pricing.PricingSystemId;
import de.metas.security.permissions.Access;
import de.metas.user.UserId;
import de.metas.util.Check;
import de.metas.util.Services;
import de.metas.util.StringUtils;
import de.metas.workflow.WFResponsibleId;
import lombok.NonNull;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.service.ClientId;
import org.adempiere.util.proxy.Cached;
import org.adempiere.warehouse.WarehouseId;
import org.compiere.model.I_AD_Org;
import org.compiere.model.I_AD_OrgInfo;
import org.compiere.util.Env;

import javax.annotation.Nullable;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.saveRecord;

public class OrgDAO implements IOrgDAO
{
	private final IQueryBL queryBL = Services.get(IQueryBL.class);

	private final CCache<OrgId, OrgInfo> orgInfosCache = CCache.<OrgId, OrgInfo>builder()
			.tableName(I_AD_OrgInfo.Table_Name)
			.build();

	@Override
	public ClientId getClientIdByOrgId(@NonNull final OrgId orgId)
	{
		final I_AD_Org orgRecord = getById(orgId);
		return ClientId.ofRepoId(orgRecord.getAD_Client_ID());
	}

	@Override
	public void save(@NonNull final I_AD_Org orgRecord)
	{
		saveRecord(orgRecord);
	}

	@Override
	@Cached(cacheName = I_AD_Org.Table_Name + "#by#" + I_AD_Org.COLUMNNAME_AD_Client_ID)
	public List<I_AD_Org> retrieveClientOrgs(@CacheCtx final Properties ctx, final int adClientId)
	{
		return queryBL
				.createQueryBuilder(I_AD_Org.class, ctx, ITrx.TRXNAME_None)
				.addEqualsFilter(I_AD_Org.COLUMNNAME_AD_Client_ID, adClientId)
				.create()
				.list(I_AD_Org.class);
	}

	@Override
	public I_AD_Org retrieveOrgInTrx(@NonNull final OrgId adOrgId)
	{
		return queryBL
				.createQueryBuilder(I_AD_Org.class)
				.addEqualsFilter(I_AD_Org.COLUMNNAME_AD_Org_ID, adOrgId)
				.create()
				.firstOnlyNotNull(I_AD_Org.class);
	}

	@SuppressWarnings("OptionalAssignedToNull")
	@Override
	public OrgInfo createOrUpdateOrgInfo(@NonNull final OrgInfoUpdateRequest request)
	{
		I_AD_OrgInfo record = retrieveOrgInfoRecordOrNull(request.getOrgId(), ITrx.TRXNAME_ThreadInherited);
		if (record == null)
		{
			record = newInstance(I_AD_OrgInfo.class);
			record.setAD_Org_ID(request.getOrgId().getRepoId());
			record.setStoreCreditCardData(StoreCreditCardNumberMode.LAST_4_DIGITS.getCode());
		}

		if (request.getOrgTypeId() != null)
		{
			record.setAD_OrgType_ID(request.getOrgTypeId().map(OrgTypeId::getRepoId).orElse(-1));
		}

		if (request.getOrgBPartnerLocationId() != null)
		{
			final BPartnerLocationId bpartnerLocationId = request.getOrgBPartnerLocationId().orElse(null);
			record.setOrg_BPartner_ID(bpartnerLocationId != null ? bpartnerLocationId.getBpartnerId().getRepoId() : -1);
			record.setOrgBP_Location_ID(bpartnerLocationId != null ? bpartnerLocationId.getRepoId() : -1);
		}

		if (request.getWarehouseId() != null)
		{
			record.setM_Warehouse_ID(request.getWarehouseId().map(WarehouseId::getRepoId).orElse(-1));
		}

		if (request.getLogoImageId() != null)
		{
			record.setLogo_ID(request.getLogoImageId().orElse(-1));
		}

		if (request.getTimeZone() != null)
		{
			final ZoneId timeZone = request.getTimeZone().orElse(null);
			record.setTimeZone(timeZone != null ? timeZone.getId() : null);
		}

		saveRecord(record);

		return toOrgInfo(record);
	}

	@Override
	public OrgInfo getOrgInfoById(@NonNull final OrgId adOrgId)
	{
		return orgInfosCache.getOrLoad(adOrgId, k -> retrieveOrgInfo(adOrgId, ITrx.TRXNAME_None));
	}

	@Override
	public OrgInfo getOrgInfoByIdInTrx(final OrgId adOrgId)
	{
		return retrieveOrgInfo(adOrgId, ITrx.TRXNAME_ThreadInherited);
	}

	private OrgInfo retrieveOrgInfo(@NonNull final OrgId orgId, @Nullable final String trxName)
	{
		final I_AD_OrgInfo record = retrieveOrgInfoRecordOrNull(orgId, trxName);
		if (record == null)
		{
			throw new AdempiereException("@NotFound@ @AD_OrgInfo@: " + orgId);
		}

		return toOrgInfo(record);
	}

	@Nullable
	private I_AD_OrgInfo retrieveOrgInfoRecordOrNull(@NonNull final OrgId adOrgId, @Nullable final String trxName)
	{
		return queryBL
				.createQueryBuilder(I_AD_OrgInfo.class, Env.getCtx(), trxName)
				.addEqualsFilter(I_AD_OrgInfo.COLUMNNAME_AD_Org_ID, adOrgId)
				.create()
				.firstOnly(I_AD_OrgInfo.class);
	}

	public static OrgInfo toOrgInfo(final I_AD_OrgInfo record)
	{
		final OrgId parentOrgId = record.getParent_Org_ID() > 0
				? OrgId.ofRepoId(record.getParent_Org_ID())
				: null;

		final UserId supervisorId = InterfaceWrapperHelper.isNull(record, I_AD_OrgInfo.COLUMNNAME_Supervisor_ID) ? null : UserId.ofRepoId(record.getSupervisor_ID());
		final String timeZoneCode = record.getTimeZone();
		final ZoneId timeZone = timeZoneCode != null && !Check.isEmpty(timeZoneCode)
				? ZoneId.of(timeZoneCode.trim())
				: null;

		return OrgInfo.builder()
				.clientId(ClientId.ofRepoIdOrSystem(record.getAD_Client_ID()))
				.orgId(OrgId.ofRepoId(record.getAD_Org_ID()))
				//
				.parentOrgId(parentOrgId)
				//
				.supervisorId(supervisorId)
				.calendarId(CalendarId.ofRepoIdOrNull(record.getC_Calendar_ID()))
				.pricingSystemId(PricingSystemId.ofRepoIdOrNull(record.getM_PricingSystem_ID()))
				//
				.warehouseId(WarehouseId.ofRepoIdOrNull(record.getM_Warehouse_ID()))
				.purchaseWarehouseId(WarehouseId.ofRepoIdOrNull(record.getM_WarehousePO_ID()))
				.dropShipWarehouseId(WarehouseId.ofRepoIdOrNull(record.getDropShip_Warehouse_ID()))
				//
				.storeCreditCardNumberMode(StoreCreditCardNumberMode.ofCode(record.getStoreCreditCardData()))
				//
				.logoImageId(record.getLogo_ID())
				.workflowResponsibleId(WFResponsibleId.ofRepoIdOrNull(record.getAD_WF_Responsible_ID()))
				.orgBPartnerLocationId(BPartnerLocationId.ofRepoIdOrNull(record.getOrg_BPartner_ID(), record.getOrgBP_Location_ID()))
				.reportsPathPrefix(record.getReportPrefix())
				.timeZone(timeZone)
				//
				.build();
	}

	@Override
	public WarehouseId getOrgWarehouseId(@NonNull final OrgId orgId)
	{
		return getOrgInfoById(orgId).getWarehouseId();
	}

	@Override
	public WarehouseId getOrgPOWarehouseId(@NonNull final OrgId orgId)
	{
		return getOrgInfoById(orgId).getPurchaseWarehouseId();
	}

	@Override
	public WarehouseId getOrgDropshipWarehouseId(@NonNull final OrgId orgId)
	{
		return getOrgInfoById(orgId).getDropShipWarehouseId();
	}

	@Override
	public I_AD_Org retrieveOrganizationByValue(final Properties ctx, final String value)
	{
		if (value == null)
		{
			return null;
		}

		final String valueFixed = value.trim();

		return queryBL
				.createQueryBuilder(I_AD_Org.class, ctx, ITrx.TRXNAME_None)
				.addEqualsFilter(I_AD_Org.COLUMNNAME_Value, valueFixed)
				.create()
				.setClient_ID()
				.firstOnly(I_AD_Org.class);
	}

	@Override
	public Optional<OrgId> retrieveOrgIdBy(@NonNull final OrgQuery orgQuery)
	{
		final int orgId = queryBL.createQueryBuilderOutOfTrx(I_AD_Org.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_AD_Org.COLUMNNAME_Value, orgQuery.getOrgValue())
				.create()
				.setRequiredAccess(Access.READ)
				.firstIdOnly();

		if (orgId < 0 && orgQuery.isFailIfNotExists())
		{
			final String msg = StringUtils.formatMessage("Found no existing Org; Searched via value='{}'", orgQuery.getOrgValue());
			throw new OrgIdNotFoundException(msg);
		}

		return Optional.ofNullable(OrgId.ofRepoIdOrNull(orgId));
	}

	@Override
	public ZoneId getTimeZone(@NonNull final OrgId orgId)
	{
		if (!orgId.isRegular())
		{
			return SystemTime.zoneId();
		}
		final ZoneId timeZone = getOrgInfoById(orgId).getTimeZone();
		return timeZone != null ? timeZone : SystemTime.zoneId();
	}

}
