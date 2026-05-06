package com.velocitypowered.proxy.util.gradient;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

@SuppressWarnings({"unused", "DataFlowIssue"})
public class GradientTextFormatter {

    public static Component applyGradient(String text) {
        String regex = "<#([A-Fa-f0-9]{6})>(.*?)<#([A-Fa-f0-9]{6})>";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(text);

        Component result = Component.empty();

        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                result = result.append(Component.text(text.substring(lastEnd, matcher.start())));
            }

            String startColorHex = matcher.group(1);
            String gradientText = matcher.group(2);
            String endColorHex = matcher.group(3);

            result = result.append(createGradient(gradientText, startColorHex, endColorHex));

            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            result = result.append(Component.text(text.substring(lastEnd)));
        }

        return result;
    }

    private static Component createGradient(String text, String startHex, String endHex) {
        TextColor start = TextColor.fromHexString("#" + startHex);
        TextColor end = TextColor.fromHexString("#" + endHex);

        int length = text.length();
        Component result = Component.empty();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (float) (length - 1);

            int r = (int) (start.red() + ratio * (end.red() - start.red()));
            int g = (int) (start.green() + ratio * (end.green() - start.green()));
            int b = (int) (start.blue() + ratio * (end.blue() - start.blue()));

            TextColor color = TextColor.color(r, g, b);

            result = result.append(Component.text(String.valueOf(text.charAt(i)), color));
        }

        return result;
    }
}