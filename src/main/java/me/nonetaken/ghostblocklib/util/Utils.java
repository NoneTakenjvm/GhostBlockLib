package me.nonetaken.ghostblocklib.util;

/*
 * Project: me.nonetaken.ghostblocklib.util | Author: NoneTaken#0001
 * Created: 26/07/2023 at 20:17
 */
public class Utils {

    public static String removeBeginningCharacters(String input, char character) {
        int index = 0;
        while (index < input.length() && input.charAt(index) == character) {
            index++;
        }
        return input.substring(index);
    }
}
