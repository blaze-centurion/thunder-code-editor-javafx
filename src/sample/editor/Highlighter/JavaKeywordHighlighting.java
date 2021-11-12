package sample.editor.Highlighter;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;

import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.collection.ListModification;

public class JavaKeywordHighlighting {

    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "break",
            "case", "catch", "class", "const",
            "continue", "default", "do", "else",
            "enum", "extends", "final", "finally",
            "for", "goto", "if", "implements", "import",
            "instanceof", "interface", "native",
            "new", "package", "private", "protected", "public",
            "return", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while",
            "goto", "const"
    };
    private static final String[] DATA_TYPES = new String[] {
            "int", "String", "float", "double",
            "long", "char", "short", "boolean",
            "byte"
    };
    private static final String[] LITERALS = new String[] {"true", "false", "null"};

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String DATA_TYPE_PATTERN = "\\b(" + String.join("|", DATA_TYPES) + ")\\b";
    private static final String LITERALS_PATTERN = "\\b(" + String.join("|", LITERALS) + ")\\b";
    private static final String PAREN_PATTERN = "[()]";
    private static final String BRACE_PATTERN = "[{}]";
    private static final String BRACKET_PATTERN = "[\\[\\]]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/"   // for whole text processing (text blocks)
            + "|" + "/\\*[^\\v]*" + "|" + "^\\h*\\*([^\\v]*|/)";  // for visible paragraph processing (line by line)

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
//                    + "(?<DATATYPE>" + DATA_TYPE_PATTERN + ")"
//                    + "(?<LITERALS>" + LITERALS_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    public void start(StyleClassedTextArea codeArea) {
        codeArea.getVisibleParagraphs().addModificationObserver(new VisibleParagraphStyler<>( codeArea, this::computeHighlighting ));
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
//                            matcher.group("DATATYPE") != null ? "data-types" :
//                                matcher.group("LITERALS") != null ? "literals" :
                                    matcher.group("PAREN") != null ? "paren" :
                                            matcher.group("BRACE") != null ? "brace" :
                                                    matcher.group("BRACKET") != null ? "bracket" :
                                                            matcher.group("SEMICOLON") != null ? "semicolon" :
                                                                    matcher.group("STRING") != null ? "string" :
                                                                            matcher.group("COMMENT") != null ? "comment" :
                                                                                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private class VisibleParagraphStyler<PS, SEG, S> implements Consumer<ListModification<? extends Paragraph<PS, SEG, S>>>
    {
        private final GenericStyledArea<PS, SEG, S> area;
        private final Function<String,StyleSpans<S>> computeStyles;
        private int prevParagraph, prevTextLength;

        public VisibleParagraphStyler( GenericStyledArea<PS, SEG, S> area, Function<String,StyleSpans<S>> computeStyles )
        {
            this.computeStyles = computeStyles;
            this.area = area;
        }

        @Override
        public void accept( ListModification<? extends Paragraph<PS, SEG, S>> lm )
        {
            if ( lm.getAddedSize() > 0 )
            {
                int paragraph = Math.min( area.firstVisibleParToAllParIndex() + lm.getFrom(), area.getParagraphs().size()-1 );
                String text = area.getText( paragraph, 0, paragraph, area.getParagraphLength( paragraph ) );

                if ( paragraph != prevParagraph || text.length() != prevTextLength )
                {
                    int startPos = area.getAbsolutePosition( paragraph, 0 );
                    Platform.runLater( () -> area.setStyleSpans( startPos, computeStyles.apply( text ) ) );
                    prevTextLength = text.length();
                    prevParagraph = paragraph;
                }
            }
        }
    }
}




//import java.util.Collection;
//import java.util.Collections;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.fxmisc.richtext.CodeArea;
//import org.fxmisc.richtext.LineNumberFactory;
//import org.fxmisc.richtext.StyleClassedTextArea;
//import org.fxmisc.richtext.model.StyleSpans;
//import org.fxmisc.richtext.model.StyleSpansBuilder;
//import org.fxmisc.wellbehaved.event.EventPattern;
//import org.fxmisc.wellbehaved.event.InputMap;
//import org.fxmisc.wellbehaved.event.Nodes;
//
//import javafx.scene.input.KeyEvent;
//
//public class JavaKeywordHighlighting {
//
//    private static final String[] KEYWORDS = new String[] {
//        "abstract", "assert", "boolean", "break", "byte",
//        "case", "catch", "char", "class", "const",
//        "continue", "default", "do", "double", "else",
//        "enum", "extends", "final", "finally", "float",
//        "for", "goto", "if", "implements", "import",
//        "instanceof", "int", "interface", "long", "native",
//        "new", "package", "private", "protected", "public",
//        "return", "short", "static", "strictfp", "super",
//        "switch", "synchronized", "this", "throw", "throws",
//        "transient", "try", "void", "volatile", "while"
//    };
//
//    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
//    private static final String PAREN_PATTERN = "\\(|\\)";
//    private static final String BRACE_PATTERN = "\\{|\\}";
//    private static final String BRACKET_PATTERN = "\\[|\\]";
//    private static final String SEMICOLON_PATTERN = "\\;";
//    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
//    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
//
//    private static final Pattern PATTERN = Pattern.compile(
//        "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
//            + "|(?<PAREN>" + PAREN_PATTERN + ")"
//            + "|(?<BRACE>" + BRACE_PATTERN + ")"
//            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
//            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
//            + "|(?<STRING>" + STRING_PATTERN + ")"
//            + "|(?<COMMENT>" + COMMENT_PATTERN + ")");
////
////    private CodeArea codeArea;
////
////    public CodeArea getComponent() {
////        if (codeArea != null) return codeArea;
////
////        codeArea = new CodeArea();
////        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
////
////        codeArea.richChanges()
////            .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
////            .subscribe(change -> {
////                codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
////            });
////
////        InputMap<KeyEvent> im = InputMap.consume(
////            EventPattern.keyTyped("\t"),
////            e -> codeArea.replaceSelection("    "));
////        Nodes.addInputMap(codeArea, im);
////
////        return codeArea;
////    }
//
//    public void start(StyleClassedTextArea codeArea) {
//        codeArea.richChanges()
//            .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
//            .subscribe(change -> {
//                codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
//            });
//
////        InputMap<KeyEvent> im = InputMap.consume(EventPattern.keyTyped("\t"), e -> {
//////            e.consume();
////            codeArea.replaceSelection("    ");
////        });
////        Nodes.addInputMap(codeArea, im);
//
//    }
//
////    public void setText(String text) {
////        getComponent().replaceText(0, getComponent().getText().length(), text);
////    }
////
////    public String getText() {
////        return getComponent().getText();
////    }
//
//    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
//        Matcher matcher = PATTERN.matcher(text);
//        int lastKwEnd = 0;
//        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
//        while (matcher.find()) {
//            String styleClass =
//                matcher.group("KEYWORD") != null ? "keyword" : matcher.group("PAREN") != null ? "paren" : matcher.group("BRACE") != null ? "brace" : matcher.group("BRACKET") != null ? "bracket" : matcher.group("SEMICOLON") != null ? "semicolon" : matcher.group("STRING") != null ? "string" : matcher.group("COMMENT") != null ? "comment" : null;
//            // never happens
//            assert styleClass != null;
//            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
//            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
//            lastKwEnd = matcher.end();
//        }
//        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
//        return spansBuilder.create();
//    }
//}