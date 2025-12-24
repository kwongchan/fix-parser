package io.github.kwongchan.fixparser.utils;

public class ByteArrayCharSequence implements Poolable, CharSequence {

    private byte[] bytes;
    private int start;
    private int end;

    @Override
    public void reset() {
        bytes = null;
        start = 0;
        end = 0;
    }

    public void init(byte[] byteArray, int start, int end) {
        this.bytes = byteArray;
        this.start = start;
        this.end = end;
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index > length() || length() == 0) {
            throw new IndexOutOfBoundsException(index);
        }
        return (char) (bytes[start + index] & 0XFF);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        var result = new ByteArrayCharSequence();
        result.init(bytes, this.start + start, this.start + end);
        return result;
    }

    @Override
    public String toString() {
        if (length() == 0) {
            return "";
        }
        var bytes = new byte[length()];
        System.arraycopy(this.bytes, start, bytes, 0, end - start);
        return new String(bytes);
    }
}
