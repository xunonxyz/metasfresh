/*
 * #%L
 * de.metas.util
 * %%
 * Copyright (C) 2021 metas GmbH
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

package de.metas.util;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Borrowed from: https://mkyong.com/java/java-password-generator-example/
 */
@EqualsAndHashCode(exclude = "random")
@ToString(exclude = "random")
@Immutable
public final class PasswordGenerator
{
	public static PasswordGeneratorBuilder builder(final int totalLength)
	{
		return new PasswordGeneratorBuilder()
				.totalLength(totalLength);
	}

	@SuppressWarnings("SpellCheckingInspection")
	private static final String CHAR_LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
	private static final String CHAR_UPPERCASE = CHAR_LOWERCASE.toUpperCase();
	private static final String DIGIT = "0123456789";
	private static final String PUNCTUATION = "+.,?!()=";

	private final transient Random random = new Random();

	private final int totalLength;
	private final boolean useLowercase;
	private final boolean useUppercase;
	private final boolean useDigit;
	private final boolean usePunctuation;
	private final String groupSeparator;
	private final int groupSize;

	@Builder
	private PasswordGenerator(
			final int totalLength,
			final boolean useLowercase,
			final boolean useUppercase,
			final boolean useDigit,
			final boolean usePunctuation,
			final String groupSeparator,
			final int groupSize)
	{
		if (totalLength < 1)
		{
			throw Check.mkEx("Password length must be > 0");
		}

		if (!useLowercase && !useUppercase && !useDigit && !usePunctuation)
		{
			throw Check.mkEx("At least one of useLowercase, useUppercase, useDigit, usePunctuation shall be set");
		}

		if (groupSize > 0 && Check.isEmpty(groupSeparator))
		{
			throw Check.mkEx("groupSeparator shall be provided when groupSize=" + groupSize);
		}

		this.totalLength = totalLength;
		this.useLowercase = useLowercase;
		this.useUppercase = useUppercase;
		this.useDigit = useDigit;
		this.usePunctuation = usePunctuation;
		this.groupSeparator = groupSeparator;
		this.groupSize = groupSize;
	}

	public static class PasswordGeneratorBuilder
	{
		public String generate()
		{
			return build().generate();
		}
	}

	@NonNull
	public String generate()
	{
		String result = generatePasswordSeed();
		result = shuffleString(result);

		if (groupSize > 0)
		{
			result = StringUtils.insertSeparatorEveryNCharacters(result, groupSeparator, groupSize);
		}

		if (result.length() > totalLength)
		{
			result = result.substring(0, totalLength);
		}

		return result;
	}

	private String generatePasswordSeed()
	{
		String fillCharacters = "";
		final StringBuilder result = new StringBuilder(totalLength);

		if (useLowercase)
		{
			// guaranteed 2 chars (lowercase)
			result.append(generateRandomString(CHAR_LOWERCASE, 2));

			fillCharacters += CHAR_LOWERCASE;
		}

		if (useUppercase)
		{
			// guaranteed 2 chars (uppercase)
			result.append(generateRandomString(CHAR_UPPERCASE, 2));

			fillCharacters += CHAR_UPPERCASE;
		}

		if (useDigit)
		{
			// guaranteed 2 digits
			result.append(generateRandomString(DIGIT, 2));

			fillCharacters += DIGIT;
		}

		if (usePunctuation)
		{
			// guaranteed 2 punctuation
			result.append(generateRandomString(PUNCTUATION, 2));

			// don't fill with punctuation
			// fillCharacters += PUNCTUATION;
		}

		{
			// remaining until length: random
			result.append(generateRandomString(fillCharacters, totalLength - result.length()));
		}

		return result.toString();
	}

	@NonNull
	private String generateRandomString(@NonNull final String input, final int size)
	{
		if (size < 1)
		{
			return "";
		}

		final StringBuilder result = new StringBuilder(size);
		for (int i = 0; i < size; i++)
		{
			// produce a random order
			final int index = random.nextInt(input.length());
			result.append(input.charAt(index));
		}
		return result.toString();
	}

	@NonNull
	private static String shuffleString(@NonNull final String input)
	{
		final List<String> result = Arrays.asList(input.split(""));
		Collections.shuffle(result);

		return String.join("", result);
	}
}
