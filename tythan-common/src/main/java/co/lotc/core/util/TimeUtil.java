package co.lotc.core.util;

import static java.util.concurrent.TimeUnit.*;
import static net.md_5.bungee.api.ChatColor.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.primitives.Longs;

import co.lotc.core.Tythan;
import co.lotc.core.agnostic.AbstractChatBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public final class TimeUtil {

	private TimeUtil() {}
	
	public static Instant parseEager(String input) {
		Long simple = Longs.tryParse(input);
		if(simple != null) return Instant.ofEpochMilli(simple);

		Duration duration = parseDuration(input);
		if(duration != null) return Instant.now().minus(duration);
		
		LocalDateTime t = tryParseTime(input);
		if(t == null) t = tryParseDate(input);
		if(t == null) t = tryParseDateTime(input);
		if(t == null) return null;
		return t.toInstant(ZoneOffset.UTC);
	}

	public static LocalDateTime tryParseTime(String input) {
		try {
			LocalTime t = LocalTime.parse(input);
			return t.atDate(LocalDate.now());
		}catch(DateTimeParseException e) {
			return null;
		}
	}

	public static LocalDateTime tryParseDate(String input) {
		try {
			LocalDate t = LocalDate.parse(input);
			return t.atStartOfDay();
		}catch(DateTimeParseException e) {
			return null;
		}
	}

	public static LocalDateTime tryParseDateTime(String input) {
		try {
			return LocalDateTime.parse(input);
		}catch(DateTimeParseException e) {
			return null;
		}
	}


	public static Duration parseDuration(String parsable) {
		Duration duration = Duration.ZERO;
		boolean anything = false;
		Pattern pat = Pattern.compile("(\\d+)([wdhms])");
		Matcher matcher = pat.matcher(parsable);
		while(matcher.find()) {
			anything = true;
			int quantity = Integer.parseInt(matcher.group(1));
			String timescale = matcher.group(2);

			TemporalUnit unit = null;
			switch (timescale) {
			case "w":
				quantity *= 7*24;
				unit = ChronoUnit.HOURS;
				break;
			case "d":
				quantity *=24;
				unit = ChronoUnit.HOURS;
				break;
			case "h":
				unit = ChronoUnit.HOURS;
				break;
			case "m":
				unit = ChronoUnit.MINUTES;
				break;
			case "s":
				unit = ChronoUnit.SECONDS;
				break;
			default:
				return null;
			}
			duration = duration.plus(quantity, unit);
		}
		if(anything) return duration;
		else return null;
	}

	
	public static BaseComponent printTicks(long ticks) {
		return printMillis(ticks * 50l);
	}
	
	public static BaseComponent printTicksRaw(long ticks) {
		return printMillisRaw(ticks * 50l);
	}
	
	public static BaseComponent printMillis(long millis) {
		return print(millis, false, WHITE, GRAY);
	}
	
	public static BaseComponent printMillisRaw(long millis) {
		return print(millis, false, null, null);
	}
	
	public static BaseComponent printBrief(long millis) {
		return print(millis, true, null, null);
	}
	
	public static BaseComponent print(long ms, boolean brief, ChatColor numColor, ChatColor unitColor) {
		AbstractChatBuilder<?> sb = Tythan.get().chatBuilder();
		
		if(ms == 0) {
			sb.append("unknown");
			if(numColor != null) sb.color(numColor);
			return sb.build();
		}
		
		long days = MILLISECONDS.toDays(ms);
		long hours = MILLISECONDS.toHours(ms) - days*24;
		long minutes = MILLISECONDS.toMinutes(ms) - DAYS.toMinutes(days) - hours*60;
		long seconds = MILLISECONDS.toSeconds(ms) - DAYS.toSeconds(days) - hours*3600 - minutes*60;
		
		boolean space = false;
		space = append(sb, days, brief, "days", "d", numColor, unitColor, space);
		space = append(sb, hours, brief, "hours", "h", numColor, unitColor, space);
		space = append(sb, minutes, brief, "minutes", "m", numColor, unitColor, space);
		append(sb, seconds, brief, "seconds", "s", numColor, unitColor, space);
		return sb.build();
	}

	private static boolean append(AbstractChatBuilder<?> sb, long val, boolean brief, String big, String small, ChatColor c1, ChatColor c2, boolean space) {
		if(val == 0) return false;
		
		if(space) sb.append(' ');
		sb.append(val);
		if(c1 != null) sb.color(c1);
		
		if(brief) sb.append(small);
		else sb.append(' ' + big);
		if(c2!=null) sb.color(c2);
		
		return true;
	}
}
