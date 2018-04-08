package com.sqless.sqlessmobile.utils;

import java.util.ArrayList;
import java.util.List;

public class HTMLDoc {

    private String HTML;
    private String assetsFolder;
    private List<String> css;
    private List<String> js;
    private String onDocReady;
    private String head;

    public String getHTML() {
        return HTML;
    }

    public String getAssetsFolder() {
        return assetsFolder;
    }

    public List<String> getCss() {
        return css;
    }

    public List<String> getJs() {
        return js;
    }

    public String getOnDocReady() {
        return onDocReady;
    }

    public String getHead() {
        return head;
    }

    public static final class HTMLDocBuilder {
        private String onDocReady;
        private String assetsFolder;
        private List<String> css;
        private List<String> js;
        private StringBuilder body;

        public HTMLDocBuilder(String assetsFolder) {
            body = new StringBuilder("<body>");
            this.assetsFolder = "file:///android_asset/" + assetsFolder + "/";
        }

        public HTMLDocBuilder addHTML(Object HTML) {
            body.append(HTML);
            return this;
        }

        public HTMLDocBuilder withCss(String... cssFiles) {
            this.css = new ArrayList<>();
            for (String fileName : cssFiles) {
                this.css.add("<link rel=\"stylesheet\" href=\"css/" + fileName + ".css\" />");
            }
            return this;
        }

        public HTMLDocBuilder withJs(String... jsFiles) {
            this.js = new ArrayList<>();
            for (String fileName : jsFiles) {
                this.js.add("<script src=\"js/" + fileName + ".js\"></script>");
            }
            return this;
        }

        public HTMLDocBuilder onDocReady(String onDocReady) {
            this.onDocReady = onDocReady;
            return this;
        }

        private StringBuilder buildHead() {
            StringBuilder sbHead = new StringBuilder();
            sbHead.append("<head>");
            sbHead.append("<meta charset=\"utf-8\">");
            if (css != null && !css.isEmpty()) {
                for (String cssTag : css) {
                    sbHead.append(cssTag);
                }
            }
            sbHead.append("</head>");
            return sbHead;
        }

        private StringBuilder buildScripts() {
            StringBuilder sbScripts = new StringBuilder();
            if (js == null || js.isEmpty()) {
                return sbScripts;
            }
            for (String scriptTag : js) {
                sbScripts.append(scriptTag);
            }
            sbScripts.append("<script type=\"text/javascript\">").append(onDocReady).append("</script>");
            return sbScripts;
        }

        private String assembleHTML() {
            StringBuilder htmlSb = new StringBuilder();
            htmlSb.append("<!DOCTYPE html>")
                    .append("<html lang=\"en\">")
                    .append(buildHead())
                    .append(body)
                    .append(buildScripts())
                    .append("</body>")
                    .append("</html>");
            return htmlSb.toString();
        }

        public HTMLDoc build() {
            HTMLDoc htmlDoc = new HTMLDoc();
            htmlDoc.css = this.css;
            htmlDoc.js = this.js;
            htmlDoc.onDocReady = this.onDocReady;
            htmlDoc.assetsFolder = this.assetsFolder;
            htmlDoc.HTML = assembleHTML();
            return htmlDoc;
        }
    }
}
