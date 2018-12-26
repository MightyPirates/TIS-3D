package li.cil.tis3d.util;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class SimpleConfig {
    private static final String COMMENT_PREFIX = "#";
    private static final String KEY_VALUE_ASSIGNMENT = "=";

    private final List<String> segments = new ArrayList<>();
    private final Object2IntMap<String> entryMap = new Object2IntArrayMap<>();

    public String getString(final String key, final String defaultValue, @Nullable final String comment) {
        int index = entryMap.getOrDefault(key, -1);
        if (index < 0) {
            if (comment != null) {
                if (segments.size() > 0) segments.add(System.lineSeparator());
                segments.add(COMMENT_PREFIX);
                segments.add(" ");
                segments.add(comment);
            }

            if (segments.size() > 0) segments.add(System.lineSeparator());
            segments.add(key);
            segments.add(KEY_VALUE_ASSIGNMENT);
            segments.add(defaultValue);
            index = segments.size() - 1;
            segments.add(System.lineSeparator());
        }

        return segments.get(index);
    }

    public int getInt(final String key, final int defaultValue, @Nullable final String comment) {
        final String value = getString(key, String.valueOf(defaultValue), comment);
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException ignored) {
            LogManager.getLogger().error("Failed parsing config value [{}] as integer.", value);
            return defaultValue;
        }
    }

    public int getInt(final String key, final int defaultValue, final int minValue, final int maxValue, @Nullable final String comment) {
        int value = getInt(key, defaultValue, String.format("%s [%d, %d]", comment, minValue, maxValue));
        if (value > maxValue) value = maxValue;
        if (value < minValue) value = minValue;
        return value;
    }

    public boolean getBoolean(final String key, final boolean defaultValue, @Nullable final String comment) {
        final String value = getString(key, String.valueOf(defaultValue), comment);
        return Boolean.parseBoolean(value);
    }

    public static SimpleConfig create(final File file) {
        final SimpleConfig config = new SimpleConfig();
        config.load(file);
        return config;
    }

    public void load(final File file) {
        segments.clear();
        entryMap.clear();

        if (!file.exists()) {
            return;
        }

        if (!file.canRead()) {
            LogManager.getLogger().error("Cannot read config file [{}].", file);
            return;
        }

        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                final String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith(COMMENT_PREFIX)) {
                    segments.add(line);
                    segments.add(System.lineSeparator());
                    continue;
                }

                final int splitIndex = line.indexOf(KEY_VALUE_ASSIGNMENT);
                if (splitIndex < 0) {
                    segments.add(line);
                    segments.add(System.lineSeparator());
                    LogManager.getLogger().warn("Failed parsing config line [{}] in file [{}].", lineNumber, file);
                    continue;
                }

                final String key = line.substring(0, splitIndex);
                final String value = line.substring(splitIndex + 1);
                if (key.trim().isEmpty()) {
                    segments.add(line);
                    segments.add(System.lineSeparator());
                    LogManager.getLogger().warn("Empty key in config line [{}] in file [{}].", lineNumber, file);
                    continue;
                }

                segments.add(key);
                segments.add(KEY_VALUE_ASSIGNMENT);
                segments.add(value);
                entryMap.put(key.trim(), segments.size() - 1);
                segments.add(System.lineSeparator());
            }
        } catch (final IOException e) {
            LogManager.getLogger().error(e);
        }
    }

    public void save(final File file) {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            LogManager.getLogger().error("Failed creating directory for config file [{}].", file);
            return;
        }

        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (final String segment : segments) {
                writer.write(segment);
            }
        } catch (final IOException e) {
            LogManager.getLogger().error(e);
        }
    }
}
