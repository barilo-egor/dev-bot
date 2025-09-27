package tgb.cryptoexchange.devbot.filter;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.devbot.config.DevBotConfig;
import tgb.cryptoexchange.tgcommon.handler.BannedCache;

import java.util.List;

@Service
public class BannedCacheImpl implements BannedCache {

    private final List<Long> adminsChatIds;

    public BannedCacheImpl(DevBotConfig devBotConfig) {
        this.adminsChatIds = devBotConfig.getAdminsChatIds();
    }

    @Override
    public boolean get(Long chatId) {
        return !adminsChatIds.contains(chatId);
    }
}
