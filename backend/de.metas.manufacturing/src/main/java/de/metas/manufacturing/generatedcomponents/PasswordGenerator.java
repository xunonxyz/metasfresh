/*
 * #%L
 * de.metas.manufacturing
 * %%
 * Copyright (C) 2020 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package de.metas.manufacturing.generatedcomponents;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import de.metas.util.Check;
import de.metas.util.StringUtils;
import lombok.NonNull;
import org.adempiere.mm.attributes.AttributeCode;
import org.adempiere.mm.attributes.api.AttributeConstants;
import org.adempiere.mm.attributes.api.ImmutableAttributeSet;

public class PasswordGenerator implements IComponentGenerator
{
	@VisibleForTesting
	static final String PARAM_TOTAL_LENGTH = "totalLength";
	@VisibleForTesting
	static final String PARAM_USE_LOWERCASE = "useLowercase";
	@VisibleForTesting
	static final String PARAM_USE_UPPERCASE = "useUppercase";
	@VisibleForTesting
	static final String PARAM_USE_DIGIT = "useDigit";
	@VisibleForTesting
	static final String PARAM_USE_PUNCTUATION = "usePunctuation";
	@VisibleForTesting
	static final String PARAM_GROUP_SEPARATOR = "groupSeparator";
	@VisibleForTesting
	static final String PARAM_GROUP_SIZE = "groupSize";

	private static final ComponentGeneratorParams DEFAULT_PARAMETERS = ComponentGeneratorParams.builder()
			.parameter(PARAM_TOTAL_LENGTH, "14")
			.parameter(PARAM_USE_LOWERCASE, StringUtils.ofBoolean(true))
			.parameter(PARAM_USE_UPPERCASE, StringUtils.ofBoolean(true))
			.parameter(PARAM_USE_DIGIT, StringUtils.ofBoolean(true))
			.parameter(PARAM_USE_PUNCTUATION, StringUtils.ofBoolean(true))
			.parameter(PARAM_GROUP_SEPARATOR, "-")
			.parameter(PARAM_GROUP_SIZE, "4")
			.build();

	@Override
	public ImmutableAttributeSet generate(@NonNull final ComponentGeneratorContext context)
	{
		final int qty = context.getQty();
		Check.errorIf(qty != 1, "Only 1 Router Password Attribute exists, so 1 password should be generated. Requested qty: {}", qty);

		final ImmutableList<AttributeCode> attributesToGenerate = context.computeRemainingAttributesToGenerate(AttributeConstants.RouterPassword);
		if (attributesToGenerate.isEmpty())
		{
			return ImmutableAttributeSet.EMPTY;
		}

		final ComponentGeneratorParams parameters = context.getParameters();
		final String password = generatePassword(
				StringUtils.toIntegerOrZero(parameters.getValue(PARAM_TOTAL_LENGTH)),
				StringUtils.toBoolean(parameters.getValue(PARAM_USE_LOWERCASE)),
				StringUtils.toBoolean(parameters.getValue(PARAM_USE_UPPERCASE)),
				StringUtils.toBoolean(parameters.getValue(PARAM_USE_DIGIT)),
				StringUtils.toBoolean(parameters.getValue(PARAM_USE_PUNCTUATION)),
				parameters.getValue(PARAM_GROUP_SEPARATOR),
				StringUtils.toIntegerOrZero(parameters.getValue(PARAM_GROUP_SIZE)));

		return ImmutableAttributeSet.builder()
				.attributeValue(AttributeConstants.RouterPassword, password)
				.build();
	}

	@Override
	public ComponentGeneratorParams getDefaultParameters()
	{
		return DEFAULT_PARAMETERS;
	}

	@NonNull
	@VisibleForTesting
	String generatePassword(
			final int totalLength,
			final boolean useLowercase,
			final boolean useUppercase,
			final boolean useDigit,
			final boolean usePunctuation,
			final String groupSeparator,
			final int groupSize)
	{
		return StringUtils.newPasswordGenerator(totalLength)
				.useLowercase(useLowercase)
				.useUppercase(useUppercase)
				.useDigit(useDigit)
				.usePunctuation(usePunctuation)
				.groupSize(groupSize)
				.groupSeparator(groupSeparator)
				.generate();
	}
}
