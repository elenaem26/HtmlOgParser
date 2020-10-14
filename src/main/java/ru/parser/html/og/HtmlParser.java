package ru.parser.html.og;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

public class HtmlParser {

    private static final Pattern OG_PREFIX_PATTERN =
            Pattern.compile("((?<ogPrefix>[A-Za-z0-9_]+):\\s+(http://ogp.me/ns(/\\w+)*#))\\s*");
    private static final List<String> HTML_LINK_FAVICON_RELS = Arrays.asList("icon", "shortcut icon");
    private static final String REL = "rel";
    private static final String HREF = "href";
    private static final String TYPE = "type";
    private static final String IMAGE = "image";
    private static final String LINK = "link";
    private static final String TITLE = "title";
    private static final String CONTENT = "content";
    private static final String PROPERTY = "property";
    private static final String NAME = "name";
    private static final String META = "meta";
    private static final String OG_PREFIX = "ogPrefix";
    private static final String OG = "og";
    private static final String PREFIX = "prefix";
    private static final HtmlCleaner cleaner = new HtmlCleaner();

    /**
     * Extract open graph data from header.
     * @param htmlHeader Html header
     * @return Map that contains parsed og data without prefix
     */
    public static Map<String, String> extractOgDataFromHtmlHeader(String htmlHeader) {
        TagNode pageData = cleaner.clean(htmlHeader);
        List<String> prefixes = new ArrayList<>();
        if (pageData.hasAttribute(PREFIX)) {
            String namespaceData = pageData.getAttributeByName(PREFIX);
            Matcher matcher = OG_PREFIX_PATTERN.matcher(namespaceData);
            while (matcher.find()) {
                String prefix = matcher.group(OG_PREFIX);
                prefixes.add(prefix);
            }
        }
        if (prefixes.isEmpty()) {
            prefixes.add(OG);
        }

        Map<String, String> result  = new HashMap<>();
        TagNode[] metaData = pageData.getElementsByName(META, true);
        for (TagNode metaElement : metaData) {
            String target = null;
            if (metaElement.hasAttribute(PROPERTY)) {
                target = PROPERTY;
            } else if (metaElement.hasAttribute(NAME)) {
                target = NAME;
            }

            for (String prefix : prefixes) {
                if (target != null && metaElement.getAttributeByName(target).startsWith(prefix + ":")) {
                    String property = metaElement.getAttributeByName(target).replaceAll(prefix + ":", "")
                            .trim().toLowerCase();
                    String content = metaElement.getAttributeByName(CONTENT).trim();
                    result.putIfAbsent(property, content);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Extract title from html header
     * @param htmlHeader Html header
     * @return title
     */
    public static Optional<String> extractTitleO(String htmlHeader) {
        TagNode pageData = cleaner.clean(htmlHeader);
        TagNode titleElement = pageData.findElementByName(TITLE, true);
        if (titleElement != null) {
            return Optional.ofNullable(titleElement.getText().toString());
        }
        return Optional.empty();
    }

    /**
     * Extract absolute favicon link from html header
     * @param htmlHeader html header
     * @return Link if present
     */
    public static Optional<String> extractFaviconLinkFromHtml(String htmlHeader) {
        TagNode pageData = cleaner.clean(htmlHeader);
        TagNode[] links = pageData.getElementsByName(LINK, true);
        for (TagNode link : links) {
            Optional<String> imageLinkO = imageLinkFromLinkO(link);
            if (imageLinkO.isPresent()) {
                return imageLinkO;
            }
        }
        return Optional.empty();
    }

    private static Optional<String> imageLinkFromLinkO(TagNode link) {
        if (isImageLink(link)) {
            String linkStr = link.getAttributeByName(HREF);
            return Optional.of(linkStr);
        } else {
            return Optional.empty();
        }
    }

    private static boolean isImageLink(TagNode link) {
        return link.hasAttribute(REL)
                && link.hasAttribute(TYPE)
                && link.hasAttribute(HREF)
                && HTML_LINK_FAVICON_RELS.contains(link.getAttributeByName(REL))
                && link.getAttributeByName(TYPE).toLowerCase().contains(IMAGE);
    }
}
