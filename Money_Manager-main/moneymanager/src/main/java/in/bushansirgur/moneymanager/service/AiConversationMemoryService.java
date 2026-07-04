package in.bushansirgur.moneymanager.service;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiConversationMemoryService {

    private static final int MAX_TURNS_PER_PROFILE = 8;

    private final Map<Long, Deque<ConversationTurn>> conversations = new ConcurrentHashMap<>();

    public String recentConversationForProfile(Long profileId) {
        Deque<ConversationTurn> turns = conversations.get(profileId);
        if (turns == null || turns.isEmpty()) {
            return "No previous conversation in this session.";
        }

        List<ConversationTurn> snapshot;
        synchronized (turns) {
            snapshot = new ArrayList<>(turns);
        }

        StringBuilder sb = new StringBuilder();
        for (ConversationTurn turn : snapshot) {
            sb.append("User: ").append(turn.userQuestion()).append("\n");
            sb.append("Assistant: ").append(turn.assistantAnswer()).append("\n\n");
        }
        return sb.toString().trim();
    }

    public void remember(Long profileId, String userQuestion, String assistantAnswer) {
        Deque<ConversationTurn> turns = conversations.computeIfAbsent(profileId, ignored -> new ArrayDeque<>());
        synchronized (turns) {
            turns.addLast(new ConversationTurn(trim(userQuestion), trim(assistantAnswer)));
            while (turns.size() > MAX_TURNS_PER_PROFILE) {
                turns.removeFirst();
            }
        }
    }

    public void clear(Long profileId) {
        conversations.remove(profileId);
    }

    private String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= 800 ? value : value.substring(0, 800);
    }

    private record ConversationTurn(String userQuestion, String assistantAnswer) {
    }
}
