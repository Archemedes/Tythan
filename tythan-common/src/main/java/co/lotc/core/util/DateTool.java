/*
 * Copyright © Maxim Chipeev 2016- All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package co.lotc.core.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.ChatColor;

public final class DateTool {
	private DateTool() { throw new AssertionError(); }

	// TODO switch to TimeUnit instead of TimeAmount
	
	public static long getTimeLongMillis() {
		return new Date().getTime();
	}
	
	public static long getTimeLongHours() {
		return getTimeLongMillis() / (1000 * 60 * 24);
	}
	
	public static long getTimeLong() {
		return getTimeLongMillis() / 1000;
	}
	
	public static Map<TimeAmount, Integer> splitTime(long time) {
		Map<TimeAmount, Integer> list = new LinkedHashMap<>();
		TimeAmount[] values = TimeAmount.values();
		for(int i = values.length-1; i >= 0; i--){
			long seconds = values[i].seconds;
			if(time >= seconds) {
				long times = time / seconds;
				list.put(values[i], (int)times);
				time = time % seconds;
			}
		}
		return list;
	}

	public static String getStringFromTime(long time, boolean shorthand, TimeAmount exclude) {
		StringBuilder msg = new StringBuilder();
		DateTool.splitTime(time).forEach((a, n) -> {
			if(exclude == null || exclude.ordinal() < a.ordinal()) {
				msg.append(ChatColor.WHITE).append(n).append(ChatColor.GRAY);
				if(shorthand)
					msg.append(a.getShorthand());
				else
					msg.append(' ').append(a.getName()).append(' ');
			}
		});
		return msg.toString().trim();
	}

	public static String getStringFromTime(long time, boolean shorthand) {
		return getStringFromTime(time, shorthand, null);
	}

	public static StringDateFormatter getStringFromTime(long time) {
		return new StringDateFormatter(time);
	}

	public static class StringDateFormatter {
		TimeAmount exclude;
		boolean shorthand;
		String numberFormat;
		String wordFormat;
		long time;

		StringDateFormatter(long time) {
			this.time = time;
			this.shorthand = false;
			this.exclude = null;
			setNumberFormat(ChatColor.WHITE);
			setWordFormat(ChatColor.GRAY);
		}

		@Override
		public String toString() {
			StringBuilder msg = new StringBuilder();
			DateTool.splitTime(time).forEach((a, n) -> {
				if (exclude == null || exclude.ordinal() < a.ordinal() || msg.length() == 0) {
					//if(msg.length() == 0 && exclude != null && exclude.ordinal() >= a.ordinal())
					//	exclude = null;
					msg.append(numberFormat).append(n).append(wordFormat);
					if (shorthand)
						msg.append(a.getShorthand());
					else {
						msg.append(' ').append(a.getName());
						if (n > 1)
							msg.append('s');
						msg.append(' ');
					}
				}
			});
			if (msg.length() == 0) {
				return shorthand ? numberFormat + "0" + wordFormat + "s" : numberFormat + "0" + wordFormat + " seconds";
			}
			return msg.toString().trim();
		}

		public StringDateFormatter excludeFrom(TimeAmount amount) {
			this.exclude = amount;
			if(this.exclude == TimeAmount.YEAR)
				this.exclude = null;
			return this;
		}

		public StringDateFormatter setWordFormat(ChatColor color) {
			wordFormat = color.toString();
			return this;
		}

		public StringDateFormatter setWordFormat(String wordFormat) {
			this.wordFormat = wordFormat;
			return this;
		}

		public StringDateFormatter setNumberFormat(ChatColor color) {
			this.numberFormat = color.toString();
			return this;
		}

		public StringDateFormatter setNumberFormat(String numberFormat) {
			this.numberFormat = numberFormat;
			return this;
		}

		public StringDateFormatter setDisplayShorthand() {
			shorthand = true;
			return this;
		}
	}
	
	/**
	 * Expects a dd/mm/yyyy string.
	 * @param s The date string
	 * @return The LocalDate
	 */
	public static LocalDate dateFromString(String s) {
		return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy"));	
	}
	
	public static LocalDate currentDate() {
		return Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	public enum TimeAmount {
		SECOND(1L, "second", "s"),
		MINUTE(60L, "minute", "m"),
		HOUR(60*60L, "hour", "h"),
		DAY(60*60*24L, "day", "d"),
		YEAR(60*60*24*365L, "year", "y");
		
		String name;
		String shorthand;
		long seconds;
		
		TimeAmount(long s, String n, String t) {
			this.seconds = s;
			this.shorthand = t;
			this.name = n;
		}
		
		public String getName() {
			return name;
		}
		
		public String getShorthand() {
			return shorthand;
		}
		
		public long seconds(long count) {
			return getSeconds() * count;
		}
		
		public long ticks(long count) {
			return getTicks() * count;
		}
		
		public long getSeconds() {
			return seconds;
		}
		
		public long getTicks() {
			return seconds * 20;
		}
	}
}
