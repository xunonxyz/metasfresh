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

package org.adempiere.util.time;

import de.metas.util.time.DurationUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class DurationUtilsTest
{
	public static final Percentage ACCURACY_PERCENTAGE = Percentage.withPercentage(0.0001);

	@Test
	public void testRounding1Month()
	{
		Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> DurationUtils.toWorkDuration(BigDecimal.valueOf(1), ChronoUnit.MONTHS));
	}

	@Test
	public void testNegative()
	{
		Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> DurationUtils.toWorkDuration(BigDecimal.valueOf(-0.5), ChronoUnit.HOURS));
	}

	@Test
	public void testRoundingHalfDay()
	{
		final Duration duration = DurationUtils.toWorkDuration(BigDecimal.valueOf(0.5), ChronoUnit.DAYS);
		Assertions.assertThat(duration.getSeconds()).isCloseTo((4 * 60 * 60), ACCURACY_PERCENTAGE);
	}

	@Test
	public void testRoundingHalfHour()
	{
		final Duration duration = DurationUtils.toWorkDuration(BigDecimal.valueOf(0.5), ChronoUnit.HOURS);
		Assertions.assertThat(duration.getSeconds()).isCloseTo((long)(0.5 * 60 * 60), ACCURACY_PERCENTAGE);
	}

	@Test
	public void testRounding014Hour()
	{
		final Duration duration = DurationUtils.toWorkDuration(BigDecimal.valueOf(0.14), ChronoUnit.HOURS);
		Assertions.assertThat(duration.getSeconds()).isCloseTo((long)(0.14 * 60 * 60), ACCURACY_PERCENTAGE);
	}

	@Test
	public void testRounding00001Seconds()
	{
		Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> DurationUtils.toWorkDuration(BigDecimal.valueOf(0.0001), ChronoUnit.SECONDS));
	}

	@Test
	public void testRoundingUpHalfHour()
	{
		final Duration duration = DurationUtils.toWorkDurationRoundUp(BigDecimal.valueOf(0.5), ChronoUnit.HOURS);
		Assertions.assertThat(duration.getSeconds()).isCloseTo((60 * 60), ACCURACY_PERCENTAGE);
	}

	@Test
	public void testTemporalUnitSeconds()
	{
		final TemporalUnit temporalUnit = DurationUtils.getTemporalUnitForWorkDuration(Duration.ofSeconds(3601));
		Assertions.assertThat(temporalUnit).isEqualTo(ChronoUnit.SECONDS);
	}

	@Test
	public void testTemporalUnitHours()
	{
		final TemporalUnit temporalUnit = DurationUtils.getTemporalUnitForWorkDuration(Duration.ofSeconds(3600));
		Assertions.assertThat(temporalUnit).isEqualTo(ChronoUnit.HOURS);
	}

	@Test
	public void testTemporalUnitDays()
	{
		final TemporalUnit temporalUnit = DurationUtils.getTemporalUnitForWorkDuration(Duration.ofSeconds(3 * 8 * 3600));
		Assertions.assertThat(temporalUnit).isEqualTo(ChronoUnit.DAYS);
	}
}
