package com.robin.rapidexcel.elements;

public class CellAddress {
    private static final char ABSOLUTE_REFERENCE_MARKER = '$';
    private static final int COL_RADIX = 'Z' - 'A' + 1;

    private final int row;
    private final int col;
    public CellAddress(int row, int col){
        this.row=row;
        this.col=col;
    }
    public CellAddress(String address){
        final int length = address.length();
        if (length == 0) {
            this.row = 0;
            this.col = 0;
        } else {
            int offset = address.charAt(0) == ABSOLUTE_REFERENCE_MARKER ? 1 : 0;
            int col = 0;
            for (; offset < length; offset++) {
                final char c = address.charAt(offset);
                if (c == ABSOLUTE_REFERENCE_MARKER) {
                    offset++;
                    break; //next there must be digits
                }
                if (isAsciiDigit(c)) {
                    break;
                }
                col = col * COL_RADIX + toUpperCase(c) - (int) 'A' + 1;
            }
            this.col = col - 1;
            this.row = Integer.parseUnsignedInt(address.substring(offset)) - 1;
        }
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return col;
    }

    private static final boolean isAsciiDigit(char c) {
        return '0' <= c && c <= '9';
    }

    private final static char toUpperCase(char c) {
        if (isAsciiUpperCase(c)) {
            return c;
        }
        if (isAsciiLowerCase(c)) {
            return (char) (c + ('A' - 'a'));
        }
        throw new IllegalArgumentException("Unexpected char: " + c);
    }
    private static final boolean isAsciiLowerCase(char c) {
        return 'a' <= c && c <= 'z';
    }

    private static final boolean isAsciiUpperCase(char c) {
        return 'A' <= c && c <= 'Z';
    }
}
