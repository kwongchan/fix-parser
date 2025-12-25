package io.github.kwongchan.fixparser.benchmark;

import io.github.kwongchan.fixparser.FIXParser;
import io.github.kwongchan.fixparser.FIXParserFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import quickfix.DefaultMessageFactory;
import quickfix.FieldNotFound;
import quickfix.InvalidMessage;
import quickfix.field.ApplVerID;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Benchmark)
public class FixParserBenchmark {

    private static final int MESSAGES_COUNT = 1_000_000;

    private byte[][] fixMessages;
    private DateTimeFormatter dateTimeFormatter;
    private FIXParser fixParser;

    @Setup(Level.Trial)
    public void setup() {
        // Generate 1 million sample FIX messages
        fixMessages = new byte[MESSAGES_COUNT][];
        var fixStringTemplate = "8=FIX.4.4\u00019=129\u000135=8\u000134=2\u000149=SELLER\u000156=BUYER\u000111=ORDERID\u000117=EXECID\u000120=0\u0001150=2\u000139=2\u000155=SYM\u000154=1\u000138=100\u000140=2\u000144=99.99\u000159=1\u000160=20230101-00:00:00.123\u000110=012\u0001";

        for (int i = 0; i < MESSAGES_COUNT; i++) {
            // Vary the MsgSeqNum (tag 34) to make each message unique
            var fixString = fixStringTemplate.replace("34=2", "34=" + (i % 10000 + 2));
            fixMessages[i] = fixString.getBytes(StandardCharsets.UTF_8);
        }
        System.out.println("Setup complete: " + MESSAGES_COUNT + " messages prepared.");
        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss[.SSS]");

        fixParser = new FIXParserFactory().newParser();
    }

    @Benchmark
    public void parseOneMillionMessagesWithStringSplit(Blackhole blackhole) {
        for (int i = 0; i < MESSAGES_COUNT; i++) {
            // Simple manual parsing: split on SOH and extract a field
            var msg = new String(fixMessages[i]);
            var fields = msg.split("\u0001");
            int orderQty = 0;
            double orderPrice = 0.0;
            LocalDateTime transactTime = null;
            for (String field : fields) {
                if (field.startsWith("38=")) {
                    orderQty = Integer.parseInt(field.substring(3));
                }
                if (field.startsWith("44=")) {
                    orderPrice = Double.parseDouble(field.substring(3));
                }
                if (field.startsWith("60=")) {
                    transactTime = LocalDateTime.parse(field.substring(3), dateTimeFormatter);
                }
            }
            // Consume the result to prevent dead-code elimination
            blackhole.consume(orderQty);
            blackhole.consume(orderPrice);
            blackhole.consume(transactTime);
        }
    }

    @Benchmark
    public void parseOneMillionMessagesWithFIXParser(Blackhole blackhole) {
        for (int i = 0; i < MESSAGES_COUNT; i++) {
            var fixMessage = fixParser.parse(fixMessages[i]);
            var orderQty = fixMessage.getInt(38);
            var orderPrice = fixMessage.getDouble(44);
            var transactTimeMillis = fixMessage.getDateTimeEpochMillis(60);
            // Consume the result to prevent dead-code elimination
            blackhole.consume(orderQty);
            blackhole.consume(orderPrice);
            blackhole.consume(transactTimeMillis);
        }
    }

    @Benchmark
    public void parseOneMillionMessagesWithQuickFixJ(Blackhole blackhole) throws FieldNotFound, InvalidMessage {
        for (int i = 0; i < MESSAGES_COUNT; i++) {
            var messageFactory = new DefaultMessageFactory(ApplVerID.FIX44);
            var message = quickfix.MessageUtils.parse(messageFactory, null, new String(fixMessages[i]));
            var orderQty = message.getInt(38);
            var orderPrice = message.getDouble(44);
            var transactTime = message.getUtcTimeStamp(60);
            // Consume the result to prevent dead-code elimination
            blackhole.consume(orderQty);
            blackhole.consume(orderPrice);
            blackhole.consume(transactTime);
        }
    }
}
