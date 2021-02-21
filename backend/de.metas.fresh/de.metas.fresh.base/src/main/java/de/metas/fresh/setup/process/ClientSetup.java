package de.metas.fresh.setup.process;

import de.metas.acct.api.AcctSchemaId;
import de.metas.acct.api.IAcctSchemaDAO;
import de.metas.adempiere.model.I_AD_User;
import de.metas.banking.api.IBPBankAccountDAO;
import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.service.IBPartnerBL;
import de.metas.bpartner.service.IBPartnerDAO;
import de.metas.bpartner.service.IBPartnerOrgBL;
import de.metas.cache.interceptor.CacheInterceptor;
import de.metas.currency.CurrencyCode;
import de.metas.currency.ICurrencyDAO;
import de.metas.location.ILocationBL;
import de.metas.money.CurrencyId;
import de.metas.organization.IOrgDAO;
import de.metas.organization.OrgId;
import de.metas.organization.OrgInfo;
import de.metas.organization.OrgInfoUpdateRequest;
import de.metas.pricing.service.IPriceListDAO;
import de.metas.util.Check;
import de.metas.util.Services;
import de.metas.util.StringUtils;
import lombok.NonNull;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.ad.trx.api.OnTrxMissingPolicy;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.service.ClientId;
import org.adempiere.service.IClientDAO;
import org.adempiere.util.lang.IAutoCloseable;
import org.compiere.model.I_AD_Client;
import org.compiere.model.I_AD_ClientInfo;
import org.compiere.model.I_AD_Image;
import org.compiere.model.I_AD_Org;
import org.compiere.model.I_C_AcctSchema;
import org.compiere.model.I_C_BP_BankAccount;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_Location;
import org.compiere.model.I_M_PriceList;
import org.compiere.model.X_C_BP_BankAccount;
import org.compiere.util.Env;

import javax.annotation.Nullable;
import java.util.OptionalInt;
import java.util.Properties;

/*
 * #%L
 * de.metas.fresh.base
 * %%
 * Copyright (C) 2015 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

/**
 * AD_Client setup adapter which will provide following functionalities:
 * <ul>
 * <li>when created it will load all related database records
 * <li>when a setter is called it will set the value to all needed database records (but won't save)
 * <li>save everything: {@link #save()}
 * <li>when a getter is called, it will fetch the value directly from the loaded database record
 * </ul>
 * <p>
 * This shall be a short living object.
 *
 * @author metas-dev <dev@metasfresh.com>
 */
@SuppressWarnings({ "UnusedReturnValue", "FieldCanBeLocal" })
public class ClientSetup
{
	public static ClientSetup newInstance() { return new ClientSetup(Env.getCtx()); }

	public static ClientSetup newInstance(final Properties ctx) { return new ClientSetup(ctx); }

	// services
	private final ITrxManager trxManager = Services.get(ITrxManager.class);
	private final IClientDAO clientDAO = Services.get(IClientDAO.class);
	private final IOrgDAO orgDAO = Services.get(IOrgDAO.class);
	private final IBPartnerOrgBL partnerOrgBL = Services.get(IBPartnerOrgBL.class);
	private final IBPartnerBL bpartnerBL = Services.get(IBPartnerBL.class);
	private final IBPartnerDAO bpartnerDAO = Services.get(IBPartnerDAO.class);
	private final IBPBankAccountDAO bankAccountDAO = Services.get(IBPBankAccountDAO.class);
	private final ILocationBL locationBL = Services.get(ILocationBL.class);
	private final IAcctSchemaDAO acctSchemaDAO = Services.get(IAcctSchemaDAO.class);
	private final ICurrencyDAO currencyDAO = Services.get(ICurrencyDAO.class);

	private static final OrgId AD_Org_ID_Main = OrgId.ofRepoId(1000000);

	// Parameters
	private final Properties _ctx;
	private final I_AD_Client adClient;
	private final I_AD_ClientInfo adClientInfo;
	private final I_AD_Org adOrg;
	private final OrgInfoUpdateRequest.OrgInfoUpdateRequestBuilder adOrgInfoChangeRequest;
	private final I_C_BPartner orgBPartner;
	private final I_C_BPartner_Location orgBPartnerLocation;
	private final I_AD_User orgContact;
	private final I_C_BP_BankAccount orgBankAccount;
	private final I_C_AcctSchema acctSchema;
	private final I_M_PriceList priceList_None;

	private ClientSetup(@NonNull final Properties ctx)
	{
		_ctx = ctx;

		//
		// Load
		try (final IAutoCloseable ignored = CacheInterceptor.temporaryDisableCaching())
		{
			adClient = clientDAO.getByIdInTrx(ClientId.METASFRESH);
			adClientInfo = clientDAO.retrieveClientInfoInTrx(ClientId.METASFRESH);
			adOrg = orgDAO.retrieveOrgInTrx(AD_Org_ID_Main);
			final OrgInfo adOrgInfo = orgDAO.getOrgInfoByIdInTrx(AD_Org_ID_Main);
			adOrgInfoChangeRequest = OrgInfoUpdateRequest.builder()
					.orgId(OrgId.ofRepoId(adOrg.getAD_Org_ID()));
			//
			orgBPartner = partnerOrgBL.retrieveLinkedBPartner(adOrg);
			orgBPartnerLocation = bpartnerDAO.getBPartnerLocationById(adOrgInfo.getOrgBPartnerLocationId());
			orgContact = bpartnerDAO.retrieveDefaultContactOrNull(orgBPartner, I_AD_User.class);
			if (orgContact == null)
			{
				// shall not happen
				// TODO: create if does not exist
				throw new AdempiereException("No organization contact defined");
			}

			final BPartnerId orgBPartnerId = BPartnerId.ofRepoId(orgBPartner.getC_BPartner_ID());
			orgBankAccount = bankAccountDAO.retrieveDefaultBankAccountInTrx(orgBPartnerId).orElse(null);
			if (orgBankAccount == null)
			{
				// shall not happen
				// TODO create one if does not exists
				throw new AdempiereException("No organization bank account defined");
			}

			//
			final AcctSchemaId primaryAcctSchemaId = AcctSchemaId.ofRepoId(adClientInfo.getC_AcctSchema1_ID());
			acctSchema = acctSchemaDAO.getRecordById(primaryAcctSchemaId);

			priceList_None = InterfaceWrapperHelper.create(getCtx(), IPriceListDAO.M_PriceList_ID_None, I_M_PriceList.class, ITrx.TRXNAME_ThreadInherited);
		}
	}

	public void save()
	{
		final String trxName = trxManager.getThreadInheritedTrxName(OnTrxMissingPolicy.ReturnTrxNone);
		trxManager.run(trxName, this::saveInTrx);
	}

	private void saveInTrx()
	{
		setOtherDefaults();

		InterfaceWrapperHelper.save(adClient, ITrx.TRXNAME_ThreadInherited);
		InterfaceWrapperHelper.save(adClientInfo, ITrx.TRXNAME_ThreadInherited);
		InterfaceWrapperHelper.save(adOrg, ITrx.TRXNAME_ThreadInherited);
		orgDAO.createOrUpdateOrgInfo(adOrgInfoChangeRequest.build());

		InterfaceWrapperHelper.save(orgBPartner, ITrx.TRXNAME_ThreadInherited);

		InterfaceWrapperHelper.disableReadOnlyColumnCheck(orgBPartnerLocation); // disable it because AD_Org_ID is not updateable
		orgBPartnerLocation.setAD_Org_ID(adOrg.getAD_Org_ID()); // FRESH-211
		InterfaceWrapperHelper.save(orgBPartnerLocation, ITrx.TRXNAME_ThreadInherited);

		InterfaceWrapperHelper.save(orgContact, ITrx.TRXNAME_ThreadInherited);

		InterfaceWrapperHelper.save(orgBankAccount, ITrx.TRXNAME_ThreadInherited);

		InterfaceWrapperHelper.save(acctSchema, ITrx.TRXNAME_ThreadInherited);

		InterfaceWrapperHelper.save(priceList_None, ITrx.TRXNAME_ThreadInherited);
	}

	private void setOtherDefaults()
	{
		//
		// AD_Org
		adOrg.setIsSummary(false);

		//
		// AD_Org Linked BPartner:
		{
			orgBPartner.setIsCustomer(false);
			orgBPartner.setIsVendor(false);
			orgBPartner.setIsEmployee(false);
		}

		//
		// AD_Org Linked BPartner Location:
		{
			// C_BPartner_Location - EDI
			{
				final de.metas.edi.model.I_C_BPartner_Location ediBPartnerLocation = InterfaceWrapperHelper.create(orgBPartnerLocation, de.metas.edi.model.I_C_BPartner_Location.class);
				ediBPartnerLocation.setGLN(null); // TODO: how to set the GLN?!?

			}
		}

		//
		// AD_Org Linked Bank Account:
		{
			orgBankAccount.setA_Name("-"); // FIXME: shall we add here the contact name?!
			orgBankAccount.setIsDefault(true);
			orgBankAccount.setBPBankAcctUse(X_C_BP_BankAccount.BPBANKACCTUSE_Both);
			orgBankAccount.setBankAccountType(X_C_BP_BankAccount.BANKACCOUNTTYPE_Girokonto);
		}

		// task FRESH-129
		// Make sure the org contact is both sales and purchase contact
		orgContact.setIsSalesContact(true);
		orgContact.setIsPurchaseContact(true);
	}

	private Properties getCtx()
	{
		return _ctx;
	}

	public ClientSetup setCompanyName(@Nullable final String companyNameParam)
	{
		final String companyNameNorm = StringUtils.trimBlankToNull(companyNameParam);
		if (companyNameNorm == null)
		{
			return this;
		}

		adClient.setValue(companyNameNorm);
		adClient.setName(companyNameNorm);

		adOrg.setValue(companyNameNorm);
		adOrg.setName(companyNameNorm);

		orgBPartner.setValue(companyNameNorm);
		orgBPartner.setName(companyNameNorm);
		orgBPartner.setCompanyName(companyNameNorm);
		orgBPartner.setIsCompany(true);

		return this;
	}

	@Nullable
	public String getCompanyName()
	{
		return orgBPartner.getCompanyName();
	}

	public ClientSetup setCurrencyId(final CurrencyId currencyId)
	{
		if (currencyId == null)
		{
			return this;
		}

		final CurrencyId acctCurrencyId = CurrencyId.ofRepoId(acctSchema.getC_Currency_ID());
		final CurrencyCode acctCurrencyCode = currencyDAO.getCurrencyCodeById(acctCurrencyId);

		orgBankAccount.setC_Currency_ID(currencyId.getRepoId());
		acctSchema.setC_Currency_ID(currencyId.getRepoId());
		acctSchema.setName(acctSchema.getGAAP() + " / " + acctCurrencyCode.toThreeLetterCode());

		priceList_None.setC_Currency_ID(currencyId.getRepoId());

		return this;
	}

	public int getC_Currency_ID()
	{
		return acctSchema.getC_Currency_ID();
	}

	public ClientSetup setAD_Language(@Nullable final String adLanguage)
	{
		if (adLanguage == null || Check.isBlank(adLanguage))
		{
			return this;
		}

		adClient.setAD_Language(adLanguage);
		orgBPartner.setAD_Language(adLanguage); // i.e. Org Language

		return this;
	}

	@Nullable
	public String getAD_Language()
	{
		return orgBPartner.getAD_Language();
	}

	@Nullable
	public final I_C_Location getCompanyAddress()
	{
		final I_C_Location orgLocation = orgBPartnerLocation.getC_Location();
		if (orgLocation == null || orgLocation.getC_Location_ID() <= 0)
		{
			return null;
		}

		//
		// Return a copy of org location, to make sure nobody is changing it
		// NOTE: C_Location shall be handled as a value object!
		return copy(orgLocation);
	}

	@Nullable
	private I_C_Location copy(final I_C_Location location)
	{
		if (location == null)
		{
			return null;
		}

		return locationBL.duplicate(location);
	}

	public final int getCompanyAddressLocationId()
	{
		final I_C_Location companyAddress = getCompanyAddress();
		if (companyAddress == null)
		{
			return 0;
		}
		final int locationId = companyAddress.getC_Location_ID();
		return Math.max(locationId, 0);
	}

	public ClientSetup setCompanyAddress(final I_C_Location companyAddress)
	{
		if (companyAddress == null)
		{
			return this;
		}

		// C_Location
		final I_C_Location orgLocation = orgBPartnerLocation.getC_Location();
		InterfaceWrapperHelper.copyValues(companyAddress, orgLocation);
		InterfaceWrapperHelper.save(orgLocation);

		// C_BPartner_Location
		orgBPartnerLocation.setName(orgLocation.getCity()); // To be updated on save...
		bpartnerBL.setAddress(orgBPartnerLocation); // update Address string

		return this;
	}

	public ClientSetup setCompanyAddressByLocationId(final int locationId)
	{
		if (locationId <= 0)
		{
			return this;
		}

		final I_C_Location companyAddress = InterfaceWrapperHelper.create(getCtx(), locationId, I_C_Location.class, ITrx.TRXNAME_ThreadInherited);
		return setCompanyAddress(companyAddress);
	}

	public ClientSetup setCompanyLogo(final I_AD_Image companyLogo)
	{
		if (companyLogo == null || companyLogo.getAD_Image_ID() <= 0)
		{
			return this;
		}

		adClientInfo.setLogo_ID(companyLogo.getAD_Image_ID());
		adClientInfo.setLogoReport_ID(companyLogo.getAD_Image_ID());
		adClientInfo.setLogoWeb_ID(companyLogo.getAD_Image_ID());

		adOrgInfoChangeRequest.logoImageId(OptionalInt.of(companyLogo.getAD_Image_ID()));

		orgBPartner.setLogo_ID(companyLogo.getAD_Image_ID());

		return this;
	}

	public ClientSetup setCompanyLogoByImageId(final int adImageId)
	{
		if (adImageId <= 0)
		{
			return this;
		}

		final I_AD_Image companyLogo = InterfaceWrapperHelper.create(getCtx(), adImageId, I_AD_Image.class, ITrx.TRXNAME_ThreadInherited);
		return setCompanyLogo(companyLogo);
	}

	@Nullable
	public final I_AD_Image getCompanyLogo()
	{
		final I_AD_Image logo = orgBPartner.getLogo();
		if (logo == null || logo.getAD_Image_ID() <= 0)
		{
			return null;
		}

		// NOTE: even if the AD_Image is a value object, it's safe to return it as-is,
		// because the VImageDialog knows to create a new record instead of changing an existing image

		return logo;
	}

	public int getCompanyLogoImageId()
	{
		final I_AD_Image logo = getCompanyLogo();
		if (logo == null || logo.getAD_Image_ID() <= 0)
		{
			return -1;
		}
		return logo.getAD_Image_ID();
	}

	public ClientSetup setCompanyTaxID(@Nullable final String companyTaxIDParam)
	{
		final String companyTaxID = StringUtils.trimBlankToNull(companyTaxIDParam);
		if (companyTaxID != null)
		{
			orgBPartner.setVATaxID(companyTaxID);
		}

		return this;
	}

	@Nullable
	public final String getCompanyTaxID()
	{
		return orgBPartner.getVATaxID();
	}

	public String getContactFirstName()
	{
		return orgContact.getFirstname();
	}

	public ClientSetup setContactFirstName(@Nullable final String contactFirstName)
	{
		if (contactFirstName != null && !Check.isBlank(contactFirstName))
		{
			orgContact.setFirstname(contactFirstName.trim());
		}
		return this;
	}

	public String getContactLastName()
	{
		return orgContact.getLastname();
	}

	public ClientSetup setContactLastName(@Nullable final String contactLastName)
	{
		if (contactLastName != null && !Check.isBlank(contactLastName))
		{
			orgContact.setLastname(contactLastName.trim());
		}
		return this;
	}

	public ClientSetup setAccountNo(@Nullable final String accountNo)
	{
		if (accountNo != null && !Check.isBlank(accountNo))
		{
			orgBankAccount.setAccountNo(accountNo.trim());
		}
		return this;
	}

	public final String getAccountNo()
	{
		return orgBankAccount.getAccountNo();
	}

	public String getIBAN()
	{
		return orgBankAccount.getIBAN();
	}

	public ClientSetup setIBAN(@Nullable final String iban)
	{
		if (iban != null && !Check.isBlank(iban))
		{
			orgBankAccount.setIBAN(iban.trim());
		}
		return this;
	}

	public ClientSetup setC_Bank_ID(final int bankId)
	{
		if (bankId > 0)
		{
			orgBankAccount.setC_Bank_ID(bankId);
		}
		return this;
	}

	public final int getC_Bank_ID()
	{
		return orgBankAccount.getC_Bank_ID();
	}

	public ClientSetup setPhone(@Nullable final String phone)
	{
		if (phone != null && !Check.isBlank(phone))
		{
			// NOTE: we are not setting the Phone, Fax, EMail on C_BPartner_Location because those fields are hidden in BPartner window
			orgContact.setPhone(phone.trim());
		}

		return this;
	}

	public final String getPhone()
	{
		return orgContact.getPhone();
	}

	public ClientSetup setFax(@Nullable final String fax)
	{
		if (fax != null && !Check.isBlank(fax))
		{
			// NOTE: we are not setting the Phone, Fax, EMail on C_BPartner_Location because those fields are hidden in BPartner window
			orgContact.setFax(fax.trim());
		}
		return this;
	}

	public final String getFax()
	{
		return orgContact.getFax();
	}

	public ClientSetup setEMail(@Nullable final String email)
	{
		if (email != null && !Check.isBlank(email))
		{
			// NOTE: we are not setting the Phone, Fax, EMail on C_BPartner_Location because those fields are hidden in BPartner window
			orgContact.setEMail(email.trim());
		}
		return this;
	}

	public final String getEMail()
	{
		return orgContact.getEMail();
	}

	public ClientSetup setBPartnerDescription(@Nullable final String bpartnerDescription)
	{
		if (bpartnerDescription == null || Check.isBlank(bpartnerDescription))
		{
			return this;
		}

		orgBPartner.setDescription(bpartnerDescription.trim());
		return this;
	}

	@Nullable
	public String getBPartnerDescription()
	{
		return orgBPartner.getDescription();
	}
}
