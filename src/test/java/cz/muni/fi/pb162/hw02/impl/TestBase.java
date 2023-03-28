package cz.muni.fi.pb162.hw02.impl;

import cz.muni.fi.pb162.hw02.mesaging.broker.Message;
import cz.muni.fi.pb162.hw02.mesaging.broker.Broker;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@ExtendWith(SoftAssertionsExtension.class)
public class TestBase {

    protected static final String TOPIC_HOUSE = "house";
    protected static final String TOPIC_GARDEN = "garden";

    /**
     * This allows multiple asserts to be executed in single test even when earlier asserts fail.
     */
    @InjectSoftAssertions
    protected SoftAssertions softly;


    /**
     * Finds the message with the latest id in given topic
     *
     * @param messages collection of messages
     * @param topic filtered topic
     * @return message with the latest id in given topic
     */
    protected Message last(Collection<Message> messages, String topic) {

        return messages.stream()
                .filter(m -> Objects.equals(topic, m.topic()))
                .max(Comparator.comparing(Message::id))
                .orElseThrow();
    }


    /**
     * Factory for creating a message with {@code null} ID
     *
     * @param topic message's topic
     * @param data message's data
     * @return message object
     */
    protected static Message msg(String topic, Map<String, Object> data) {
        return msg(null, topic, data);
    }


    /**
     * Factory for creating a message with ID
     *
     * @param id message's id
     * @param topic message's topic
     * @param data message's data
     * @return message object
     */
    protected static Message msg(Long id, String topic, Map<String, Object> data) {
        return new Message() {
            @Override
            public Long id() {
                return id;
            }

            @Override
            public String topic() {
                return topic;
            }

            @Override
            public Map<String, Object> data() {
                return data;
            }
        };
    }

    /**
     * A Test double implementation of {@link Broker} interface
     * This implementation provides control over return values
     * which is useful in tests.
     */
    protected static class TestBroker implements Broker {

        private final List<Message> stored = new ArrayList<>();

        private List<Long> ids = List.of();

        private List<String> topics = List.of();

        private List<Message> batch = List.of();

        private Map<String, Long> lastPollOffsets = Map.of();
        private int lastPollNum = 0;
        private Collection<String> lastPollTopics = List.of();

        public List<Message> messages() {
            return stored;
        }

        public Map<String, Long> lastPollOffsets() {
            return lastPollOffsets;
        }

        public int lastPollNum() {
            return lastPollNum;
        }

        public Collection<String> lastPollTopics() {
            return lastPollTopics;
        }

        public void setTopics(String... topics) {
            this.topics = Stream.of(topics).toList();
        }

        public void setIDs(long... ids) {
            this.ids = LongStream.of(ids).boxed().toList();
        }

        public void setBatch(Message... messages) {
            this.batch = Stream.of(messages).toList();
        }

        @Override
        public Collection<String> listTopics() {
            return topics;
        }

        @Override
        public Collection<Message> push(Collection<Message> messages) {
            var list = new ArrayList<>(messages);
            var batch = IntStream.range(0, messages.size())
                    .mapToObj(i -> msg(ids.get(i), list.get(i).topic(), list.get(i).data()))
                    .toList();

            this.stored.addAll(batch);
            return batch;
        }

        @Override
        public Collection<Message> poll(Map<String, Long> offsets, int num, Collection<String> topics) {
            this.lastPollOffsets = offsets;
            this.lastPollNum = num;
            this.lastPollTopics = topics;
            return batch;
        }
    }
}
