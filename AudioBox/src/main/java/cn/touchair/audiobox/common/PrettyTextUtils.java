package cn.touchair.audiobox.common;

import androidx.annotation.NonNull;

public abstract class PrettyTextUtils {

    private static final int TABLE_PADDING_HORIZONTAL = 8;

    public static String table(@NonNull String title, @NonNull Object[][] rows) {
        int width = title.length() + TABLE_PADDING_HORIZONTAL;
        StringBuilder temp = new StringBuilder();
        String[] lines = new String[rows.length];
        for (int i = 0; i < rows.length; i++) {
            temp.append("- ");
            for (int j = 0; j < rows[i].length; j++) {
                temp.append(rows[i][j]);
                if (j < rows[i].length - 1) {
                    temp.append(": ");
                }
            }
            lines[i] = temp.toString();
            if (width < temp.length() + TABLE_PADDING_HORIZONTAL) {
                width = temp.length() + TABLE_PADDING_HORIZONTAL;
            }
            temp.delete(0, temp.length());
        }
        width = (width + 4) / 4 * 4;
        for (int i = 0; i < width + 1; i++) {
            temp.append("*");
        }
        temp.append("\n");
        int k = (int) Math.ceil((float) (width - title.length()) / 2);
        temp.append("*");
        for (int i = 0; i < k - 1; ++i) {
            temp.append(" ");
        }
        temp.append(title);
        for (int i = 0; i < k - 1; ++i) {
            temp.append(" ");
        }
        temp.append("*\n");
        for (int i = 0; i < width + 1; i++) {
            temp.append("*");
        }
        temp.append("\n");
        int c0 = (int) Math.ceil((float) width / 4);
        for (String line : lines) {
            temp.append(line);
            k = c0 - (line.length() / 4);
            for (int i = 0; i < k; i++) {
                temp.append("\t");
            }
            temp.append("-\n");
        }
        for (int i = 0; i < width + 1; i++) {
            temp.append("*");
        }
        temp.append("\n");
        return temp.toString();
    }
}
