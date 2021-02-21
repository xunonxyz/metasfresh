package org.compiere.model;

import de.metas.i18n.Language;
import de.metas.location.ICountryDAO;
import de.metas.util.Services;
import org.adempiere.util.LegacyAdapters;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

/**
 *	Location Country Model (Value Object)
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: MCountry.java,v 1.3 2006/07/30 00:58:18 jjanke Exp $
 *   
 *   * @author Michael Judd (Akuna Ltd)
 * 				<li>BF [ 2695078 ] Country is not translated on invoice
 */
public final class MCountry extends X_C_Country
{
	@Nullable
	@Deprecated
	public static MCountry get(final Properties ctx, final int C_Country_ID)
	{
		final I_C_Country country = Services.get(ICountryDAO.class).get(ctx, C_Country_ID);
		if (country == null)
		{
			return null;
		}
		return LegacyAdapters.convertToPO(country);
	}

	/**
	 * Return Countries as Array
	 * 
	 * @param ctx
	 *            context
	 * @return MCountry Array
	 * @deprecated Please use {@link ICountryDAO#getCountries(Properties)}
	 */
	@Deprecated
	public static MCountry[] getCountries(final Properties ctx)
	{
		final List<I_C_Country> list = Services.get(ICountryDAO.class).getCountries(ctx);
		return LegacyAdapters.convertToPOArray(list, MCountry.class);
	} // getCountries

	/**
	 * Get Default Country
	 * 
	 * @param ctx
	 *            context
	 * @return Country
	 */
	@Deprecated
	public static MCountry getDefault(final Properties ctx)
	{
		final I_C_Country country = Services.get(ICountryDAO.class).getDefault(ctx);
		return LegacyAdapters.convertToPO(country);
	}

	/**
	 * 	Set the Language for Display (toString)
	 *	@param AD_Language language or null
	 */
	public static void setDisplayLanguage (final String AD_Language)
	{
		s_AD_Language = AD_Language;
		if (Language.isBaseLanguage(AD_Language))
			s_AD_Language = null;
	}	//	setDisplayLanguage
	
	/**	Display Language				*/
	@Nullable private static String		s_AD_Language = null;
	//	Default DisplaySequence	*/
	private static final String		DISPLAYSEQUENCE = "@C@, @P@";

	
	public MCountry (final Properties ctx, final int C_Country_ID, @Nullable final String trxName)
	{
		super (ctx, C_Country_ID, trxName);
		if (is_new())
		{
		//	setName (null);
		//	setCountryCode (null);
			setDisplaySequence(DISPLAYSEQUENCE);
			setHasRegion(false);
			setHasPostal_Add(false);
			setIsAddressLinesLocalReverse (false);
			setIsAddressLinesReverse (false);
		}
	}   //  MCountry

	@SuppressWarnings("unused")
	public MCountry (final Properties ctx, final ResultSet rs, final String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MCountry

	/**	Translated Name			*/
	private String	m_trlName = null;
	
	
	/**
	 *	Return Name - translated if DisplayLanguage is set.
	 *  @return Name
	 */
	@Override
	public String toString()
	{
		if (s_AD_Language != null)
		{
			final String nn = getTrlName();
			if (nn != null)
				return nn;
		}
		return getName();
	}   //  toString

	/**
	 * 	Get Translated Name
	 *	@return name
	 */
	@Nullable
	private String getTrlName()
	{
		if (m_trlName != null && s_AD_Language != null)
		{
			m_trlName = get_Translation(COLUMNNAME_Name, s_AD_Language);
			if (m_trlName == null)
				s_AD_Language = null;	//	assume that there is no translation
		}
		return m_trlName;
	}	//	getTrlName
	
	String getTrlName(final String language)
	{
		if ( language != null)
		{
			m_trlName = get_Translation(COLUMNNAME_Name, language);
		}
		return m_trlName;
	}	//	getTrlName
	
	
	/**
	 * 	Get Display Sequence
	 *	@return display sequence
	 */
	@Override
	public String getDisplaySequence ()
	{
		String ds = super.getDisplaySequence ();
		if (ds == null || ds.length() == 0)
			ds = DISPLAYSEQUENCE;
		return ds;
	}	//	getDisplaySequence

	/**
	 * 	Get Local Display Sequence.
	 * 	If not defined get Display Sequence
	 *	@return local display sequence
	 */
	@Override
	public String getDisplaySequenceLocal ()
	{
		String ds = super.getDisplaySequenceLocal();
		if (ds == null || ds.length() == 0)
			ds = getDisplaySequence();
		return ds;
	} // getDisplaySequenceLocal

	/**
	 * 	Is the region valid in the country
	 *	@param C_Region_ID region
	 *	@return true if valid
	 */
	public boolean isValidRegion(final int C_Region_ID)
	{
		if (C_Region_ID == 0 
			|| getC_Country_ID() == 0
			|| !isHasRegion())
			return false;
		final MRegion[] regions = MRegion.getRegions(getCtx(), getC_Country_ID());
		for (final MRegion region : regions)
		{
			if (C_Region_ID == region.getC_Region_ID())
				return true;
		}
		return false;
	}	//	isValidRegion

	/**
	 * Compare based on Name
	 * 
	 * @param o1
	 *            object 1
	 * @param o2
	 *            object 2
	 * @return -1,0, 1
	 */
	@Override
	public int compare(final Object o1, final Object o2)
	{
		String s1 = o1.toString();
		if (s1 == null)
			s1 = "";
		String s2 = o2.toString();
		if (s2 == null)
			s2 = "";
		return s1.compareTo(s2);
	}
}
