package com.arcabank.core_finance.model.util;

import java.util.HashMap;
import java.util.Map;

public class TransliterationUtil {

    private static final Map<Character, String> DICT = new HashMap<>();

    static {
        DICT.put('А', "A"); DICT.put('а', "A");
        DICT.put('Б', "B"); DICT.put('б', "B");
        DICT.put('В', "V"); DICT.put('в', "V");
        DICT.put('Г', "H"); DICT.put('г', "H");
        DICT.put('Ґ', "G"); DICT.put('ґ', "G");
        DICT.put('Д', "D"); DICT.put('д', "D");
        DICT.put('Е', "E"); DICT.put('е', "E");
        DICT.put('Є', "YE"); DICT.put('є', "YE");
        DICT.put('Ж', "ZH"); DICT.put('ж', "ZH");
        DICT.put('З', "Z"); DICT.put('з', "Z");
        DICT.put('И', "Y"); DICT.put('и', "Y");
        DICT.put('І', "I"); DICT.put('і', "I");
        DICT.put('Ї', "YI"); DICT.put('ї', "YI");
        DICT.put('Й', "Y"); DICT.put('й', "Y");
        DICT.put('К', "K"); DICT.put('к', "K");
        DICT.put('Л', "L"); DICT.put('л', "L");
        DICT.put('М', "M"); DICT.put('м', "M");
        DICT.put('Н', "N"); DICT.put('н', "N");
        DICT.put('О', "O"); DICT.put('о', "O");
        DICT.put('П', "P"); DICT.put('п', "P");
        DICT.put('Р', "R"); DICT.put('р', "R");
        DICT.put('С', "S"); DICT.put('с', "S");
        DICT.put('Т', "T"); DICT.put('т', "T");
        DICT.put('У', "U"); DICT.put('у', "U");
        DICT.put('Ф', "F"); DICT.put('ф', "F");
        DICT.put('Х', "KH"); DICT.put('х', "KH");
        DICT.put('Ц', "TS"); DICT.put('ц', "TS");
        DICT.put('Ч', "CH"); DICT.put('ч', "CH");
        DICT.put('Ш', "SH"); DICT.put('ш', "SH");
        DICT.put('Щ', "SHCH"); DICT.put('щ', "SHCH");
        DICT.put('Ь', ""); DICT.put('ь', "");
        DICT.put('Ю', "IU"); DICT.put('ю', "IU");
        DICT.put('Я', "IA"); DICT.put('я', "IA");
        DICT.put('\'', ""); DICT.put('’', "");
    }

    public static String formatCardHolderName(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            return "UNKNOWN HOLDER";
        }

        String fullName = firstName.trim() + " " + lastName.trim();
        StringBuilder result = new StringBuilder();

        for (char ch : fullName.toCharArray()) {
            if (DICT.containsKey(ch)) {
                result.append(DICT.get(ch));
            } else {
                result.append(Character.toUpperCase(ch));
            }
        }
        return result.toString();
    }
}
