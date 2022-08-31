package sample.editor.AutoCompletion;

import sample.editor.controller.Utils;

import java.util.List;

public class AutoCompletionWords {
    private List<String> words;

    public List<String> getWords(String fileName) {
        String ext = new Utils().getExtension(fileName);
        switch (ext) {
            case "java" -> words = List.of(
                    "abstract", "assert", "boolean", "break", "byte",
                    "case", "catch", "char", "class", "const",
                    "continue", "default", "do", "double", "else",
                    "enum", "extends", "final", "finally", "float",
                    "for", "goto", "if", "implements", "import",
                    "instanceof", "int", "interface", "long", "native",
                    "new", "package", "private", "protected", "public",
                    "return", "short", "static", "strictfp", "super",
                    "switch", "synchronized", "this", "throw", "throws",
                    "transient", "try", "void", "volatile", "while",
                    "true", "false", "String"
            );
            case "py" -> words = List.of(
                    "False", "None", "True", "and", "as", "assert", "async", "await",
                    "break", "class", "continue", "def", "del", "elif", "else", "except",
                    "finally", "for", "from", "global", "if", "import", "in", "is", "lambda",
                    "nonlocal", "not", "or", "pass", "raise", "return", "try", "while", "with",
                    "yield","print"
            );
            case "c" -> words = List.of(
                    "auto", "break", "case", "char", "const", "continue",
                    "default", "do", "double", "else", "enum", "extern",
                    "float", "for", "goto", "if", "int", "long", "register",
                    "return", "short", "signed", "sizeof", "static", "struct",
                    "switch", "typedef", "union", "unsigned", "void", "volatile",
                    "while", "#include"
            );
            case "cpp" -> words = List.of(
                    "asm", "double", "new", "switch", "auto", "else", "operator", "template",
                    "break", "enum", "private", "this", "case", "extern", "protected", "throw",
                    "catch", "float", "public", "try", "char", "for", "register", "typedef",
                    "class", "friend", "return", "union", "const", "goto", "short", "unsigned",
                    "continue", "if", "signed", "virtual", "default", "inline", "sizedof", "void",
                    "delete", "int", "static", "volatile", "do", "long", "struct", "while", "#include"
            );
        }

        return words;
    }
}
