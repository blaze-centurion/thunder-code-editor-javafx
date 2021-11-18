package sample.editor.Highlighter;

import org.fxmisc.richtext.StyleClassedTextArea;
import sample.editor.controller.Utils;

public class SyntaxHighlighter {
    private StyleClassedTextArea codeArea;

    public SyntaxHighlighter(StyleClassedTextArea codeArea) {
        this.codeArea = codeArea;
    }

    public void start(String fileName) {
        String ext = new Utils().getExtension(fileName);
        if (ext.equals("java")) {
            new JavaKeywordHighlighter().start(codeArea);
        } else if (ext.equals("py")) {
            new PythonKeywordHighlighter().start(codeArea);
        }
    }
}
