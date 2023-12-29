package top.hcode.hoj.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MossGetLanguage {

    public static final Map<String, String> languageToExtension;

    static {
        List<String> supportedLanguages = Arrays.asList("c", "cc", "java",
                "ml", "pascal", "ada", "lisp", "schema", "haskell", "fortran",
                "ascii", "vhdl", "perl", "matlab", "python", "mips", "prolog",
                "spice", "vb", "csharp", "modula2", "a8086", "javascript", "plsql");

        languageToExtension = supportedLanguages.stream()
                .collect(Collectors.toMap(MossGetLanguage::getExtension, language -> language));
    }

    private static String getExtension(String language) {
        switch (language) {
            case "cc":
                return "cpp";
            case "a8086":
                return "asm";
            case "python":
                return "py";
            case "javascript":
                return "js";
            case "pascal":
                return "pas";
            case "csharp":
                return "cs";
            case "haskell":
                return "hs";
            case "ascii":
                return "php";
            case "fortran":
                return "f";
            case "perl":
                return "pl";
            case "matlab":
                return "m";
            case "pl":
                return "prolog";
            case "modula2":
                return "mod";
            default:
                return language;
        }
    }

    // public static void main(String[] args) {
    //     // languageToExtension.forEach((key, value) -> System.out.println("Language: " +
    //     //         key + ", Extension: " + value));
    //     System.out.println(languageToExtension.get("cpp"));
    // }
}
