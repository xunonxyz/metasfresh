package org.compiere.model;

import de.metas.organization.IOrgDAO;
import de.metas.organization.OrgId;
import de.metas.organization.OrgInfoUpdateRequest;
import de.metas.util.Services;
import org.adempiere.util.LegacyAdapters;
import org.compiere.util.DB;
import org.compiere.util.Env;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

/**
 *	Organization Model
 *
 *  @author Jorg Janke
 *  @version $Id: MOrg.java,v 1.3 2006/07/30 00:58:04 jjanke Exp $
 */
public class MOrg extends X_AD_Org
{
	// metas
	public static MOrg[] getOfClient(final Properties ctx, final int AD_Client_ID)
	{
		final List<I_AD_Org> clientOrgs = Services.get(IOrgDAO.class).retrieveClientOrgs(ctx, AD_Client_ID);
		return LegacyAdapters.convertToPOArray(clientOrgs, MOrg.class);
	}	//	getOfClient

	@SuppressWarnings("unused")
	@Nullable
	@Deprecated
	public static MOrg get (final Properties ctx_NOTUSED, final int orgRepoId)
	{
		final OrgId orgId = OrgId.ofRepoIdOrNull(orgRepoId);
		if (orgId == null)
		{
			return null;
		}

		final I_AD_Org org = Services.get(IOrgDAO.class).getById(orgId);
		return LegacyAdapters.convertToPO(org);
	}	//	get

	public MOrg (final Properties ctx, final int AD_Org_ID, final String trxName)
	{
		super(ctx, AD_Org_ID, trxName);
		if (is_new())
		{
		//	setValue (null);
		//	setName (null);
			setIsSummary (false);
		}
	}	//	MOrg

	public MOrg (final Properties ctx, final ResultSet rs, final String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MOrg

	public MOrg (final MClient client, final String name)
	{
		this (client.getCtx(), -1, client.get_TrxName());
		setAD_Client_ID (client.getAD_Client_ID());
		setValue (name);
		setName (name);
	}	//	MOrg

	/**	Linked Business Partner			*/
	private Integer 	m_linkedBPartner = null;

	@Override
	protected boolean afterSave (final boolean newRecord, final boolean success)
	{
		if (!success)
		{
			return success;
		}
		if (newRecord)
		{
			//	Info
			Services.get(IOrgDAO.class).createOrUpdateOrgInfo(OrgInfoUpdateRequest.builder()
					.orgId(OrgId.ofRepoId(getAD_Org_ID()))
					.build());

			//	TreeNode
			// insert_Tree(MTree_Base.TREETYPE_Organization);
		}
		//	Value/Name change
		if (!newRecord && (is_ValueChanged("Value") || is_ValueChanged("Name")))
		{
			MAccount.updateValueDescription(getCtx(), "AD_Org_ID=" + getAD_Org_ID(), get_TrxName());

			final String elementOrgTrx = Env.CTXNAME_AcctSchemaElementPrefix + X_C_AcctSchema_Element.ELEMENTTYPE_OrgTrx;
			if ("Y".equals(Env.getContext(getCtx(), elementOrgTrx)))
			{
				MAccount.updateValueDescription(getCtx(), "AD_OrgTrx_ID=" + getAD_Org_ID(), get_TrxName());
			}
		}

		return true;
	}	//	afterSave

	/**
	 * 	Get Linked BPartner
	 *	@return C_BPartner_ID
	 */
	public int getLinkedC_BPartner_ID(final String trxName)
	{
		if (m_linkedBPartner == null)
		{
			int C_BPartner_ID = DB.getSQLValue(trxName,
				"SELECT C_BPartner_ID FROM C_BPartner WHERE AD_OrgBP_ID=?",
				getAD_Org_ID());
			if (C_BPartner_ID < 0)
			{
				C_BPartner_ID = 0;
			}
			m_linkedBPartner = C_BPartner_ID;
		}
		return m_linkedBPartner;
	}	//	getLinkedC_BPartner_ID

}	//	MOrg
