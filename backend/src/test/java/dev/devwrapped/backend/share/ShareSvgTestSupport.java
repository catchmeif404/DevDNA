package dev.devwrapped.backend.share;

import static org.assertj.core.api.Assertions.assertThat;

import dev.devwrapped.backend.analysis.AnalysisResult;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

final class ShareSvgTestSupport {
    private ShareSvgTestSupport() {}

    static AnalysisResult result() {
        AnalysisResult result = new AnalysisResult();
        result.setGithubUsername("example-dev");
        result.setDeveloperType("BUG_SLAYER");
        result.setTotalCommits(1842);
        result.setTotalRepositories(24);
        result.setTotalPullRequests(89);
        result.setTopLanguage("Java");
        return result;
    }

    static Document parse(String svg) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(svg)));
        assertThat(document.getDocumentElement().getLocalName()).isEqualTo("svg");
        assertThat(document.getDocumentElement().getNamespaceURI()).isEqualTo("http://www.w3.org/2000/svg");
        assertThat(document.getDocumentElement().getAttribute("role")).isEqualTo("img");
        return document;
    }

    static void assertTextWithinPage(Document document) {
        double width = Double.parseDouble(document.getDocumentElement().getAttribute("width"));
        double height = Double.parseDouble(document.getDocumentElement().getAttribute("height"));
        var texts = document.getElementsByTagName("text");
        for (int i = 0; i < texts.getLength(); i++) {
            Element text = (Element) texts.item(i);
            double x = Double.parseDouble(text.getAttribute("x"));
            double y = Double.parseDouble(text.getAttribute("y"));
            double length = Double.parseDouble(text.getAttribute("textLength"));
            double size = Double.parseDouble(text.getAttribute("font-size"));
            assertThat(x).isGreaterThanOrEqualTo(0);
            assertThat(x + length).as(text.getTextContent()).isLessThanOrEqualTo(width - 16);
            assertThat(y).isBetween(size, height - 16);
            assertThat(size).isGreaterThanOrEqualTo(8);
            assertThat(text.getAttribute("lengthAdjust")).isEqualTo("spacingAndGlyphs");
        }
    }
}
