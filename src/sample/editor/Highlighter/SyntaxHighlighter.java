package sample.editor.Highlighter;

import org.fxmisc.richtext.StyleClassedTextArea;
import sample.editor.controller.Utils;

public class SyntaxHighlighter {
    private final StyleClassedTextArea codeArea;

    public SyntaxHighlighter(StyleClassedTextArea codeArea) {
        this.codeArea = codeArea;
    }

    public void start(String fileName) {
        String ext = new Utils().getExtension(fileName);
        switch (ext) {
            case "java" -> new JavaKeywordHighlighter().start(codeArea);
            case "py" -> new PythonKeywordHighlighter().start(codeArea);
            case "c" -> new CKeywordHighlighter().start(codeArea);
            case "cpp" -> new CppKeywordHighlighter().start(codeArea);
        }
    }
}
