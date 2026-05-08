package com.velocitypowered.proxy.util.gradient;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradientComponentFormatter {

    public static Component applyGradient(String text) {
        String regex = "<#([A-Fa-f0-9]{6})>(.*?)<#([A-Fa-f0-9]{6})>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        Component finalComponent = Component.empty();
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String nonGradientText = text.substring(lastEnd, matcher.start());
                finalComponent = finalComponent.append(Component.text(nonGradientText));
            }

            String startColorHex = matcher.group(1);
            String gradientText = matcher.group(2);
            String endColorHex = matcher.group(3);

            Component gradientComponent = createGradient(gradientText, startColorHex, endColorHex);
            finalComponent = finalComponent.append(gradientComponent);

            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            String nonGradientText = text.substring(lastEnd);
            finalComponent = finalComponent.append(Component.text(nonGradientText));
        }

        return finalComponent;
    }

    private static Component createGradient(String text, String startColorHex, String endColorHex) {
        TextColor startColor = TextColor.color(parseColor(startColorHex));
        TextColor endColor = TextColor.color(parseColor(endColorHex));

        int length = text.length();
        Component gradientComponent = Component.empty();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (float) (length - 1);
            TextColor interpolatedColor = interpolateColor(startColor, endColor, ratio);

            gradientComponent = gradientComponent.append(
                    Component.text(String.valueOf(text.charAt(i)))
                            .color(interpolatedColor)
            );
        }

        return gradientComponent;
    }

    private static int parseColor(String hex) {
        return Integer.parseInt(hex, 16);
    }

    private static TextColor interpolateColor(TextColor start, TextColor end, float ratio) {
        int red = interpolateChannel(start.red(), end.red(), ratio);
        int green = interpolateChannel(start.green(), end.green(), ratio);
        int blue = interpolateChannel(start.blue(), end.blue(), ratio);

        return TextColor.color(red, green, blue);
    }

    private static int interpolateChannel(int start, int end, float ratio) {
        return (int) (start + (end - start) * ratio);
    }
}
