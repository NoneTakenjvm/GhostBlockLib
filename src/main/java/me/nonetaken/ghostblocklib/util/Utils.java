package me.nonetaken.ghostblocklib.util;

import lombok.experimental.UtilityClass;

/*
 * Project: me.nonetaken.ghostblocklib.util | Author: NoneTaken#0001
 * Created: 26/07/2023 at 20:17
 */
@UtilityClass
public class Utils {

    /**
     * Remove all subsequent characters from the beginning of a string
     *
     * @param input the string to remove characters from
     * @param character the character to remove
     *
     * @return the string, with the provided occurence of character removed from the beginning of it
     */
    public static String removeBeginningCharacters(String input, char character) {
        int index = 0;
        while (index < input.length() && input.charAt(index) == character) {
            index++;
        }
        return input.substring(index);
    }
}
