package tgb.cryptoexchange.devbot.filter;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import tgb.cryptoexchange.devbot.handler.StartHandler;
import tgb.cryptoexchange.tgcommon.handler.UpdateFilter;
import tgb.cryptoexchange.tgcommon.service.RedisUserStateService;

@Service
public class StartFilter implements UpdateFilter {

    private final RedisUserStateService redisUserStateService;

    private final StartHandler startHandler;

    public StartFilter(RedisUserStateService redisUserStateService, StartHandler startHandler) {
        this.redisUserStateService = redisUserStateService;
        this.startHandler = startHandler;
    }

    @Override
    public void handle(Update update) {
        redisUserStateService.delete(update.getMessage().getChatId());
        startHandler.handle(update.getMessage());
    }

    @Override
    public boolean match(Update update) {
        return update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().equals("/start");
    }
}
