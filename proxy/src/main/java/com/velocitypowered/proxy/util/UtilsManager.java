package com.velocitypowered.proxy.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class providing text formatting helpers such as
 * mini font conversion and centered gradient text based on pixel width.
 */
public class UtilsManager {

    private static final Map<Character, Integer> WIDTHS = new HashMap<>();
    private static final int MAX_MOTD_WIDTH = 270; // Gesamtpixelbreite der MOTD-Zeile
    private static final int SPACE_WIDTH = 4;      // Breite eines Leerzeichens in Pixeln

    static {
        // Pixelbreiten für Small-Caps (inkl. 1px Abstand)
        WIDTHS.put('ᴀ', 6); WIDTHS.put('ʙ', 6); WIDTHS.put('ᴄ', 6);
        WIDTHS.put('ᴅ', 6); WIDTHS.put('ᴇ', 6); WIDTHS.put('ꜰ', 6);
        WIDTHS.put('ɢ', 6); WIDTHS.put('ʜ', 6); WIDTHS.put('ɪ', 4);
        WIDTHS.put('ᴊ', 6); WIDTHS.put('ᴋ', 6); WIDTHS.put('ʟ', 6);
        WIDTHS.put('ᴍ', 6); WIDTHS.put('ɴ', 6); WIDTHS.put('ᴏ', 6);
        WIDTHS.put('ᴘ', 6); WIDTHS.put('ǫ', 6); WIDTHS.put('ʀ', 6);
        WIDTHS.put('ꜱ', 6); WIDTHS.put('ᴛ', 6); WIDTHS.put('ᴜ', 6);
        WIDTHS.put('ᴠ', 6); WIDTHS.put('ᴡ', 8); WIDTHS.put('ʏ', 6);
        WIDTHS.put('ᴢ', 6);

        // Standard-Zeichen (für normale Großbuchstaben wie "BITCROW")
        WIDTHS.put('A', 6); WIDTHS.put('B', 6); WIDTHS.put('C', 6);
        WIDTHS.put('D', 6); WIDTHS.put('E', 6); WIDTHS.put('F', 6);
        WIDTHS.put('G', 6); WIDTHS.put('H', 6); WIDTHS.put('I', 4);
        WIDTHS.put('J', 6); WIDTHS.put('K', 6); WIDTHS.put('L', 6);
        WIDTHS.put('M', 6); WIDTHS.put('N', 6); WIDTHS.put('O', 6);
        WIDTHS.put('P', 6); WIDTHS.put('Q', 6); WIDTHS.put('R', 6);
        WIDTHS.put('S', 6); WIDTHS.put('T', 6); WIDTHS.put('U', 6);
        WIDTHS.put('V', 6); WIDTHS.put('W', 8); WIDTHS.put('X', 6);
        WIDTHS.put('Y', 6); WIDTHS.put('Z', 6);

        // Kleinbuchstaben (falls mal was durchrutscht)
        WIDTHS.put('a', 6); WIDTHS.put('b', 6); WIDTHS.put('c', 6);
        WIDTHS.put('d', 6); WIDTHS.put('e', 6); WIDTHS.put('f', 5);
        WIDTHS.put('g', 6); WIDTHS.put('h', 6); WIDTHS.put('i', 2);
        WIDTHS.put('j', 6); WIDTHS.put('k', 5); WIDTHS.put('l', 3);
        WIDTHS.put('m', 6); WIDTHS.put('n', 6); WIDTHS.put('o', 6);
        WIDTHS.put('p', 6); WIDTHS.put('q', 6); WIDTHS.put('r', 6);
        WIDTHS.put('s', 6); WIDTHS.put('t', 4); WIDTHS.put('u', 6);
        WIDTHS.put('v', 6); WIDTHS.put('w', 6); WIDTHS.put('x', 6);
        WIDTHS.put('y', 6); WIDTHS.put('z', 6);

        // Zahlen und Sonderzeichen
        WIDTHS.put(' ', 4); WIDTHS.put('!', 2); WIDTHS.put('.', 2);
        WIDTHS.put('-', 6); WIDTHS.put('_', 6); WIDTHS.put(':', 2);
    }

    public static String MiniFontConvert(String text) {
        if (text == null || text.isEmpty()) return "";

        String regex = "<#([A-Fa-f0-9]{6})>(.*?)<#([A-Fa-f0-9]{6})>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String startColorHex = matcher.group(1);
            String content = matcher.group(2);
            String endColorHex = matcher.group(3);

            String converted = convertText(content);
            String replacement = "<#" + startColorHex + ">" + converted + "<#" + endColorHex + ">";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static String convertText(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replace("a", "ᴀ").replace("b", "ʙ").replace("c", "ᴄ")
                .replace("d", "ᴅ").replace("e", "ᴇ").replace("f", "ꜰ")
                .replace("g", "ɢ").replace("h", "ʜ").replace("i", "ɪ")
                .replace("j", "ᴊ").replace("k", "ᴋ").replace("l", "ʟ")
                .replace("m", "ᴍ").replace("n", "ɴ").replace("o", "ᴏ")
                .replace("p", "ᴘ").replace("q", "ǫ").replace("r", "ʀ")
                .replace("s", "ꜱ").replace("t", "ᴛ").replace("u", "ᴜ")
                .replace("v", "ᴠ").replace("w", "ᴡ").replace("y", "ʏ")
                .replace("z", "ᴢ");
    }

    public static Component centerComponent(Component component) {
        if (component == null) return Component.empty();

        String plain = PlainTextComponentSerializer.plainText().serialize(component);
        int totalPixelWidth = 0;

        for (char c : plain.toCharArray()) {
            totalPixelWidth += WIDTHS.getOrDefault(c, 6);
        }

        // Berechne benötigte Leerzeichen für die Zentrierung
        int paddingPixels = (MAX_MOTD_WIDTH - totalPixelWidth) / 2;
        if (paddingPixels <= 0) return component;

        int spacesNeeded = paddingPixels / SPACE_WIDTH;
        return Component.text(" ".repeat(spacesNeeded)).append(component);
    }
}