package io.github.kwongchan.fixparser;

import java.util.function.Supplier;

class FIXParserImpl implements FIXParser {

    private final Supplier<FIXMessageImpl> messageFactory;

    FIXParserImpl(Supplier<FIXMessageImpl> messageFactory) {
        this.messageFactory = messageFactory;
    }

    @Override
    public FIXMessage parse(byte[] data, int start, int end) {
        var message = messageFactory.get();
        message.init(data);

        int index = start;
        while (index < end) {
            int tagStart = index;
            while (index < end && data[index] != '=') index++;
            if (index >= end) {
                throw new IllegalArgumentException("Missing '=' after tag");
            }
            int tag = parseTag(data, tagStart, index);

            index++; // skip '='
            int valueStart = index;
            // find SOH (0x01)
            while (index < end && data[index] != 0x01) {
                index++;
            }
            if (index >= end) {
                throw new IllegalArgumentException("Missing SOH after value");
            }

            message.addFields(tag, valueStart, index);

            if (tag == 10) {
                // checksum is the last field
                break;
            }

            index++; // skip SOH
        }

        return message;
    }

    private int parseTag(byte[] data, int start, int end) {
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
