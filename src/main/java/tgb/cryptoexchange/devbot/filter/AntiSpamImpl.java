package tgb.cryptoexchange.devbot.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.tgcommon.handler.AntiSpam;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AntiSpamImpl implements AntiSpam {

    private final Map<Long, UserUpdateActivity> userActivityMap = new ConcurrentHashMap<>();

    private final int messageLimit;

    private static final long TIME_WINDOW_MS = 10_000;

    public AntiSpamImpl(@Value("${antiSpam.allowedCount:10}") Integer allowedCount) {
        this.messageLimit = allowedCount;
    }

    @Override
    public boolean isSpam(Long chatId) {
        long currentTime = System.currentTimeMillis();
        UserUpdateActivity activity = userActivityMap.computeIfAbsent(chatId, id -> new UserUpdateActivity());
        activity.cleanOldMessages(currentTime, TIME_WINDOW_MS);
        if (activity.getMessageCount() >= messageLimit) {
            return true;
        }
        activity.addMessage(currentTime);
        return false;
    }

    public static class UserUpdateActivity {
        private final Queue<Long> messageTimestamps = new LinkedList<>();

        public void addMessage(long timestamp) {
            messageTimestamps.add(timestamp);
        }

        public int getMessageCount() {
            return messageTimestamps.size();
        }

        public void cleanOldMessages(long currentTime, long timeWindowMs) {
            Long timestamp;
            while ((timestamp = messageTimestamps.peek()) != null && currentTime - timestamp > timeWindowMs) {
                messageTimestamps.poll();
            }
        }
    }
}
