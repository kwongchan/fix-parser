package io.github.kwongchan.fixparser;

class FIXParserImpl implements FIXParser {

    private final FIXMessageImpl reusableMessage;

    FIXParserImpl(FIXMessageImpl reusableMessage) {
        this.reusableMessage = reusableMessage;
    }

    @Override
    public FIXMessage parse(byte[] data, int start, int end) {
        reusableMessage.reset();
        reusableMessage.init(data);

        int i = start;
        while (i < end) {
            int tagStart = i;
            while (i < end && data[i] != '=') i++;
            if (i >= end) {
                throw new IllegalArgumentException("Missing '=' after tag");
            }
            int tag = parseTag(data, tagStart, i);

            i++; // skip '='
            int valueStart = i;
            // find SOH (0x01)
            while (i < end && data[i] != 0x01) {
                i++;
            }
            if (i >= end) {
                throw new IllegalArgumentException("Missing SOH after value");
            }

            reusableMessage.addFields(tag, valueStart, i);

            if (tag == 10) {
                // checksum is the last field
                break;
            }

            i++; // skip SOH
        }

        return reusableMessage;
    }

    private static int parseTag(byte[] data, int start, int end) {
        if (start >= end) {
            throw new NumberFormatException("Empty integer");
        }

        int i = start;
        int result = 0;
        while (i < end) {
            int d = data[i] - '0';
            if (d < 0 || d > 9) {
                throw new NumberFormatException("Invalid digit");
            }
            result = result * 10 + d;
            i++;
        }
        return result;
    }
}
