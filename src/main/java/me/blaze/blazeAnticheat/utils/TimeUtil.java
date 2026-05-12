package me.blaze.blazeAnticheat.utils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    public static long parse(String s) {
        if (s == null || s.isEmpty() || s.equalsIgnoreCase("perm") || s.equalsIgnoreCase("permanent")) return -1;
        long t = 0;
        Matcher m = Pattern.compile("(\\d+)([wdhms])").matcher(s.toLowerCase());
        boolean f = false;
        while (m.find()) {
            f = true;
            long v = Long.parseLong(m.group(1));
            switch (m.group(2)) {
                case "w": t += TimeUnit.DAYS.toMillis(v * 7); break;
                case "d": t += TimeUnit.DAYS.toMillis(v); break;
                case "h": t += TimeUnit.HOURS.toMillis(v); break;
                case "m": t += TimeUnit.MINUTES.toMillis(v); break;
                case "s": t += TimeUnit.SECONDS.toMillis(v); break;
            }
        }
        return f ? t : -2;
    }

    public static String format(long ms) {
        if (ms == -1) return "Permanent";
        if (ms <= 0) return "0s";
        long d = TimeUnit.MILLISECONDS.toDays(ms); ms -= TimeUnit.DAYS.toMillis(d);
        long h = TimeUnit.MILLISECONDS.toHours(ms); ms -= TimeUnit.HOURS.toMillis(h);
        long m = TimeUnit.MILLISECONDS.toMinutes(ms); ms -= TimeUnit.MINUTES.toMillis(m);
        long s = TimeUnit.MILLISECONDS.toSeconds(ms);
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("d ");
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        if (s > 0 || sb.length() == 0) sb.append(s).append("s");
        return sb.toString().trim();
    }
}
