package tgb.cryptoexchange.devbot.filter;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.devbot.config.DevBotConfigurationProperties;
import tgb.cryptoexchange.tgcommon.handler.BannedCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class BannedCacheImpl implements BannedCache {

    private final List<Long> adminsChatIds;

    public BannedCacheImpl(DevBotConfigurationProperties devBotConfigurationProperties) {
        if (Objects.nonNull(devBotConfigurationProperties.getAdminsChatIds())) {
            this.adminsChatIds = devBotConfigurationProperties.getAdminsChatIds();
        } else {
            this.adminsChatIds = new ArrayList<>();
        }
    }

    @Override
    public boolean get(Long chatId) {
        return !adminsChatIds.contains(chatId);
    }
}
