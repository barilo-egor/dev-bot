package tgb.cryptoexchange.devbot.handler;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import tgb.cryptoexchange.tgcommon.handler.SlashCommandHandler;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

@Service
public class StartHandler implements SlashCommandHandler {

    private final ResponseSender responseSender;

    public StartHandler(ResponseSender responseSender) {
        this.responseSender = responseSender;
    }

    @Override
    public void handle(Message message) {
        responseSender.to(message.getChatId())
                .message("ok")
                .send();
    }

    @Override
    public String getSlashCommand() {
        return "/start";
    }

    @Override
    public boolean hasAccess(Long chatId) {
        return true;
    }
}
