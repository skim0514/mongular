package org.outerj.daisy.diff;

import org.outerj.daisy.diff.html.HTMLDiffer;
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.DomTreeBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Locale;

public class Main {
  static boolean quietMode = false;

  public static void main(String[] args) throws URISyntaxException, IOException {

      InputStream is1 = new FileInputStream("/Users/skim/s2w/mongular/bezkoder/firstFile.html");
      InputStream is2 = new FileInputStream("/Users/skim/s2w/mongular/bezkoder/secondFile.html");
      System.out.println(main2(is1, is2));
  }

    public static String main2(InputStream is1, InputStream is2) throws URISyntaxException, IOException {
        File outputFile = null;

        boolean htmlDiff = true;
        boolean htmlOut = true;
        String outputFileName = "daisydiff.htm";
        String[] css = new String[]{};

        InputStream oldStream = null;
        InputStream newStream = null;

        try {
            outputFile= File.createTempFile("prefix", "htm");
            if (!quietMode){
              System.out.println("Daisy Diff https://github.com/DaisyDiff/DaisyDiff");
              System.out.println( "Diff type: " +(
                htmlDiff
                ? "html"
                : "tag"
              ) );
            }

            if (!quietMode){
              System.out.println("");
              System.out.print(".");
            }
            SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
                    .newInstance();

            TransformerHandler result = tf.newTransformerHandler();
            // If the file path were malformed, then the following
            result.setResult(new StreamResult(outputFile));


            oldStream = is1;
            newStream = is2;

            XslFilter filter = new XslFilter();

            if (htmlDiff) {

                ContentHandler postProcess = htmlOut? filter.xsl(result,
                        "xslfilter/htmlheader.xsl"):result;

                Locale locale = Locale.getDefault();
                String prefix = "diff";

                HtmlCleaner cleaner = new HtmlCleaner();

                InputSource oldSource = new InputSource(oldStream);
                InputSource newSource = new InputSource(newStream);

                DomTreeBuilder oldHandler = new DomTreeBuilder();
                cleaner.cleanAndParse(oldSource, oldHandler);
                System.out.print(".");
                TextNodeComparator leftComparator = new TextNodeComparator(
                        oldHandler, locale);

                DomTreeBuilder newHandler = new DomTreeBuilder();
                cleaner.cleanAndParse(newSource, newHandler);
                System.out.print(".");
                TextNodeComparator rightComparator = new TextNodeComparator(
                        newHandler, locale);

                postProcess.startDocument();
                postProcess.startElement("", "diffreport", "diffreport",
                        new AttributesImpl());
                doCSS(css, postProcess);
                postProcess.startElement("", "diff", "diff",
                        new AttributesImpl());
                HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(postProcess,
                        prefix);

                HTMLDiffer differ = new HTMLDiffer(output);
                differ.diff(leftComparator, rightComparator);
                System.out.print(".");
                postProcess.endElement("", "diff", "diff");
                postProcess.endElement("", "diffreport", "diffreport");
                postProcess.endDocument();

            } else {

                ContentHandler postProcess = htmlOut? filter.xsl(result,
                        "xslfilter/tagheader.xsl"):result;
                postProcess.startDocument();
                postProcess.startElement("", "diffreport", "diffreport",
                        new AttributesImpl());
                postProcess.startElement("", "diff", "diff",
                        new AttributesImpl());
                System.out.print(".");


                InputStreamReader oldReader = null;
                BufferedReader oldBuffer = null;

                InputStreamReader newISReader = null;
                BufferedReader newBuffer = null;
                try {
                    oldReader = new InputStreamReader(oldStream);
                    oldBuffer = new BufferedReader(oldReader);

                    newISReader = new InputStreamReader(newStream);
                    newBuffer = new BufferedReader(newISReader);
                    DaisyDiff.diffTag(oldBuffer, newBuffer, postProcess);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    oldBuffer.close();
                    newBuffer.close();
                    oldReader.close();
                    newISReader.close();
                }


                System.out.print(".");
                postProcess.endElement("", "diff", "diff");
                postProcess.endElement("", "diffreport", "diffreport");
                postProcess.endDocument();
            }

        } catch (Throwable e) {
          if (quietMode){
            System.out.println(e);
          } else {
            e.printStackTrace();
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            if (e instanceof SAXException) {
                ((SAXException) e).getException().printStackTrace();
            }
            help();
          }
        } finally {
            try {
                if(oldStream != null) oldStream.close();
            } catch (IOException e) {
                //ignore this exception
            }
            try {
                if(newStream != null) newStream.close();
            } catch (IOException e) {
                //ignore this exception
            }
        }
        if (quietMode)
          System.out.println();
        else {
            return Files.readString(outputFile.toPath());
        }
        return outputFileName;
    }

    private static void doCSS(String[] css, ContentHandler handler) throws SAXException {
        handler.startElement("", "css", "css",
                new AttributesImpl());
        for(String cssLink : css){
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute("", "href", "href", "CDATA", cssLink);
            attr.addAttribute("", "type", "type", "CDATA", "text/css");
            attr.addAttribute("", "rel", "rel", "CDATA", "stylesheet");
            handler.startElement("", "link", "link",
                    attr);
            handler.endElement("", "link", "link");
        }

        handler.endElement("", "css", "css");

    }

    private static void help() {
        System.out.println("==========================");
        System.out.println("DAISY DIFF HELP:");
        System.out.println("java -jar daisydiff.jar [oldHTML] [newHTML]");
        System.out
                .println("--file=[filename] - Write output to the specified file.");
        System.out
                .println("--type=[html/tag] - Use the html (default) diff algorithm or the tag diff.");
        System.out.println("--css=[cssfile1;cssfile2;cssfile3] - Add external CSS files.");
        System.out.println("--output=[html/xml] - Write html (default) or xml output.");
        System.out.println("--q  - Generate less console output.");
        System.out.println("");
        System.out.println("EXAMPLES: ");
        System.out.println("(1)");
        System.out
                .println("java -jar daisydiff.jar http://web.archive.org/web/20070107145418/http://news.bbc.co.uk/ http://web.archive.org/web/20070107182640/http://news.bbc.co.uk/ --css=http://web.archive.org/web/20070107145418/http://news.bbc.co.uk/nol/shared/css/news_r5.css");
        System.out.println("(2)");
        System.out.println("java -jar daisydiff.jar http://cocoondev.org/wiki/291-cd/version/15/part/SimpleDocumentContent/data http://cocoondev.org/wiki/291-cd/version/17/part/SimpleDocumentContent/data --css=http://cocoondev.org/resources/skins/daisysite/css/daisy.css --output=xml --file=daisysite.htm");
        System.out.println("==========================");
        System.exit(0);
    }

}