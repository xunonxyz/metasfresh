package de.metas.camel.externalsystems.amazon;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import lombok.NonNull;

public class AmazonUtils
{
	
	@NonNull
	public static LocalDate toLocalDate(@NonNull final String in)
	{
		Instant instant = Instant.parse(in);
		LocalDate localDate = LocalDate.ofInstant(instant, ZoneOffset.UTC);
		return localDate;
	}

}
