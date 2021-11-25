package de.metas.distribution.ddorder.movement.schedule.plan;

import de.metas.handlingunits.HuId;
import de.metas.handlingunits.IHandlingUnitsBL;
import de.metas.handlingunits.model.I_M_HU;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.AlternativePickFromKey;
import de.metas.handlingunits.storage.IHUStorageFactory;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import lombok.Getter;
import lombok.NonNull;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.warehouse.LocatorId;

class AllocableHU
{
	private final IHUStorageFactory storageFactory;

	@Getter
	private final I_M_HU hu;
	private final ProductId productId;

	private LocatorId _locatorId; // lazy
	private Quantity _storageQty; // lazy
	private AlternativePickFromKey _alternativeKey; // lazy

	private Quantity qtyAllocated;

	public AllocableHU(
			final IHUStorageFactory storageFactory,
			final I_M_HU hu,
			final ProductId productId)
	{
		this.storageFactory = storageFactory;
		this.hu = hu;
		this.productId = productId;
	}

	public boolean hasQtyAvailableToAllocate()
	{
		return getQtyAvailableToAllocate().isPositive();
	}

	public Quantity getQtyAvailableToAllocate()
	{
		final Quantity qtyStorage = getStorageQty();
		return qtyAllocated != null
				? qtyStorage.subtract(qtyAllocated)
				: qtyStorage;
	}

	private Quantity getStorageQty()
	{
		Quantity storageQty = this._storageQty;
		if (storageQty == null)
		{
			storageQty = this._storageQty = storageFactory.getStorage(hu).getProductStorage(productId).getQty();
		}
		return storageQty;
	}

	public void addQtyAllocated(@NonNull final Quantity qtyAllocatedToAdd)
	{
		final Quantity newQtyAllocated = this.qtyAllocated != null
				? this.qtyAllocated.add(qtyAllocatedToAdd)
				: qtyAllocatedToAdd;

		final Quantity storageQty = getStorageQty();
		if (newQtyAllocated.isGreaterThan(storageQty))
		{
			throw new AdempiereException("Over-allocating is not allowed")
					.appendParametersToMessage()
					.setParameter("this.qtyAllocated", this.qtyAllocated)
					.setParameter("newQtyAllocated", newQtyAllocated)
					.setParameter("storageQty", storageQty);
		}

		this.qtyAllocated = newQtyAllocated;
	}

	private HuId getHuId()
	{
		return HuId.ofRepoId(hu.getM_HU_ID());
	}

	private LocatorId getLocatorId()
	{
		LocatorId locatorId = this._locatorId;
		if (locatorId == null)
		{
			locatorId = this._locatorId = IHandlingUnitsBL.extractLocatorId(hu);
		}
		return locatorId;
	}

	public AlternativePickFromKey toAlternativePickFromKey()
	{
		AlternativePickFromKey alternativeKey = _alternativeKey;
		if (alternativeKey == null)
		{
			alternativeKey = this._alternativeKey = AlternativePickFromKey.of(getLocatorId(), getHuId(), productId);
		}
		return alternativeKey;
	}

	public boolean isMatching(@NonNull final AlternativePickFromKey key)
	{
		return toAlternativePickFromKey().equals(key);
	}
}
