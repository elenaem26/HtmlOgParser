package ru.parser.html.og;

import java.util.Map;
import java.util.Optional;

import static ru.parser.html.og.HtmlParser.*;

public class Main {

    public static void main(String[] args) {
        HttpHeaderReader httpHeaderReader = new HttpHeaderReader();
        String header = httpHeaderReader
                .readHeader("https://www.youtube.com/watch?v=JvPTCvUnNQA")
                .join();
        Map<String, String> ogData = extractOgDataFromHtmlHeader(header);
        System.out.println(ogData);

        Optional<String> titleO = extractTitleO(header);
        System.out.println(titleO.orElse("<NOTHING>"));

        Optional<String> faviconLinkO = extractFaviconLinkFromHtml(header);
        System.out.println(faviconLinkO.orElse("<NOTHING>"));
    }
}
