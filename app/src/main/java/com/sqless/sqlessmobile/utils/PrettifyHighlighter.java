package com.sqless.sqlessmobile.utils;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import prettify.PrettifyParser;
import syntaxhighlight.ParseResult;
import syntaxhighlight.Parser;

public class PrettifyHighlighter {
    private static final Map<String, String> COLORS = buildColorsMap();

    private static final String FONT_PATTERN = "<span style=\"color:#%s\">%s</span>";

    private final Parser parser = new PrettifyParser();

    public String highlight(String fileExtension, String sourceCode) {
        StringBuilder highlighted = new StringBuilder();
        List<ParseResult> results = parser.parse(fileExtension, sourceCode);
        for (ParseResult result : results) {
            String type = result.getStyleKeys().get(0);
            String content = sourceCode.substring(result.getOffset(), result.getOffset() + result.getLength());
            highlighted.append(String.format(FONT_PATTERN, getColor(type), content));
        }
        return highlighted.toString().replaceAll("\n", "<br>");
    }

    private String getColor(String type) {
        return COLORS.containsKey(type) ? COLORS.get(type) : COLORS.get("pln");
    }

    private static Map<String, String> buildColorsMap() {
        Map<String, String> map = new HashMap<>();
        map.put("typ", "87cefa");
        map.put("kwd", "1500DD");
        map.put("lit", "C60C00");
        map.put("com", "4C4C4C");
        map.put("str", "ff4500");
        map.put("pun", "444444");
        map.put("pln", "000000");
        return map;
    }
}
