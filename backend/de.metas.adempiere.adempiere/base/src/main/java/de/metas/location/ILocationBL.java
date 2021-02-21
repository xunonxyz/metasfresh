package de.metas.location;

import de.metas.util.ISingletonService;
import lombok.NonNull;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_Location;

import javax.annotation.Nullable;

public interface ILocationBL extends ISingletonService
{

	I_C_Location getById(@NonNull LocationId locationId);

	/**
	 * Validate given Address.
	 */
	void validatePostal(I_C_Location location) throws AdempiereException;

	/**
	 * Build address string based on given locationId and bpartner and user blocks
	 * <p>
	 * task FRESH-119: transaction no longer needed in this method. Provide the location as object, not by its ID. This way we avoid unnecessary extra loading of the object based on id.
	 *
	 * @param bpartner - optional parameter; we need this for language
	 * @return address string
	 */
	String mkAddress(
			I_C_Location location,
			@Nullable I_C_BPartner bpartner,
			@Nullable String bPartnerBlock,
			@Nullable String userBlock);

	/**
	 * Same as {@link #mkAddress(I_C_Location, I_C_BPartner, String, String)} but bpartner and user blocks are empty
	 */
	String mkAddress(I_C_Location location);

	/**
	 * location to string
	 */
	String toString(I_C_Location location);

	/**
	 * Duplicate given location.
	 *
	 * @return new duplicated location
	 */
	I_C_Location duplicate(I_C_Location location);

	LocationId createLocation(LocationCreateRequest request);
}
