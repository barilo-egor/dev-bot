package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.devbot.constants.DevBotUserState;
import tgb.cryptoexchange.tgcommon.handler.CallbackQueryHandler;
import tgb.cryptoexchange.tgcommon.keyboard.InlineButton;
import tgb.cryptoexchange.tgcommon.keyboard.KeyboardBuilder;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.RedisUserStateService;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

import java.util.List;

@Service
public class NewUserCallbackHandler implements CallbackQueryHandler {

    private final ResponseSender responseSender;

    private final RedisUserStateService redisUserStateService;

    private final KeyboardBuilder keyboardBuilder;

    public NewUserCallbackHandler(ResponseSender responseSender, RedisUserStateService redisUserStateService,
                                  KeyboardBuilder keyboardBuilder) {
        this.responseSender = responseSender;
        this.redisUserStateService = redisUserStateService;
        this.keyboardBuilder = keyboardBuilder;
    }

    @Override
    public void handle(PressedInlineButton button) {
        redisUserStateService.save(button.getChatId(), DevBotUserState.NEW_USER.getState());
        responseSender.to(button.getChatId())
                .editText(button.getMessage().getMessageId(), "Введите идентификатор пользователя.")
                .replyKeyboard(keyboardBuilder.buildInline(List.of(
                        new InlineButton(CallbackQueryId.BACK_TO_AUTH_MENU.name(), "Назад")))
                )
                .send();
    }

    @Override
    public String getId() {
        return CallbackQueryId.NEW_USER.name();
    }

    @Override
    public boolean hasAccess(Long chatId) {
        return true;
    }
}
