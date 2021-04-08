package mas.bezkoder.compare;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class CompareHTML {
    private String path;
    private Document file1;
    private Document file2;

    public CompareHTML(String file1, String file2) {
        this.file1 = Jsoup.parse(file1);
        this.file2 = Jsoup.parse(file2);
    }

    public void runCompare() {
        List<Element> elementsHold = new ArrayList<>();
        Elements elements1 = this.file1.body().getAllElements();
        Elements elements2 = this.file2.body().getAllElements();
        for (Element e: elements1) {
            System.out.println(e);
        }

        for (Element e: elements2) {
            System.out.println(e);
        }

        System.out.println(elements2);
        for (Element e: elements1) {
            if (e.children().size() > 0) {
                continue;
            }
            if (e.tagName().equals("script")) continue;
            if (!elements2.contains(e)) {
                elementsHold.add(e);
            }
        }

        for (Element e: elementsHold) {
            String a = e.attr("style");
            if (a.length() == 0) {
                e.attr("style", "background-color: #0000FF");
            } else {
                e.attr("style", a + "; background-color: #0000FF");
            }
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Document getFile1() {
        return file1;
    }

    public void setFile1(Document file1) {
        this.file1 = file1;
    }

    public Document getFile2() {
        return file2;
    }

    public void setFile2(Document file2) {
        this.file2 = file2;
    }
}