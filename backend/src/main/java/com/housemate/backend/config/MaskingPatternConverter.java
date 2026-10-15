package com.housemate.backend.config;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaskingPatternConverter extends MessageConverter {

    private static final Pattern MASK_PATTERN = Pattern.compile(
        "(?i)(\"?(?:password|token|iban|invite[-_]?code)\"?\\s*[:=]\\s*\"?|bearer\\s+)([^\\s,;\"'}{]+)",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        if (message == null || message.isEmpty()) {
            return message;
        }
        return mask(message);
    }

    public static String mask(String message) {
        if (message == null) {
            return null;
        }
        Matcher matcher = MASK_PATTERN.matcher(message);
        return matcher.replaceAll("$1***");
    }
}
