package com.velocitypowered.proxy.util;

import net.kyori.adventure.text.Component;

/**
 * Utility class providing text formatting helpers such as
 * mini font conversion and centered gradient text.
 */
public class UtilsManager {

    /**
     * Converts text inside custom gradient tags to mini font characters.
     * <p>
     * Matches patterns like:
     * {@code <#FFFFFF>Text<#000000>}
     * and converts the inner text to mini font while preserving the tags.
     *
     * @param text the input text containing gradient tags
     * @return the processed text with mini font conversion applied
     */
    public static String MiniFontConvert(String text) {
        String regex = "<#([A-Fa-f0-9]{6})>(.*?)<#([A-Fa-f0-9]{6})>";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(text);

        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String startColorHex = matcher.group(1);
            String gradientText = matcher.group(2);
            String endColorHex = matcher.group(3);

            String convertedText = convertText(gradientText);
            String replacement = "<#" + startColorHex + ">" + convertedText + "<#" + endColorHex + ">";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Converts standard lowercase characters to their mini font equivalents.
     *
     * @param text the input text
     * @return the converted mini font text
     */
    public static String convertText(String text) {
        String mini = text.toLowerCase();

        return mini
                .replace("a", "ᴀ")
                .replace("b", "ʙ")
                .replace("c", "ᴄ")
                .replace("d", "ᴅ")
                .replace("e", "ᴇ")
                .replace("f", "ꜰ")
                .replace("g", "ɢ")
                .replace("h", "ʜ")
                .replace("i", "ɪ")
                .replace("j", "ᴊ")
                .replace("k", "ᴋ")
                .replace("l", "ʟ")
                .replace("m", "ᴍ")
                .replace("n", "ɴ")
                .replace("o", "ᴏ")
                .replace("p", "ᴘ")
                .replace("q", "ǫ")
                .replace("r", "ʀ")
                .replace("s", "ꜱ")
                .replace("t", "ᴛ")
                .replace("u", "ᴜ")
                .replace("v", "ᴠ")
                .replace("w", "ᴡ")
                .replace("y", "ʏ")
                .replace("z", "ᴢ");
    }

    public static Component centerComponent(Component component) {
        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component);

        int maxLineLength = 60;
        String[] lines = plain.split("\n");

        Component result = Component.empty();

        int index = 0;

        for (String line : lines) {
            int padding = (maxLineLength - line.length()) / 2;
            String spaces = " ".repeat(Math.max(0, padding));

            Component originalLine = component.children().get(index);

            result = result.append(Component.text(spaces)).append(originalLine);

            if (index < lines.length - 1) {
                result = result.append(Component.newline());
            }

            index++;
        }

        return result;
    }
}