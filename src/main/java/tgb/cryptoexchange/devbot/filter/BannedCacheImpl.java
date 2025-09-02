package tgb.cryptoexchange.devbot.filter;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.tgcommon.handler.BannedCache;

@Service
public class BannedCacheImpl implements BannedCache {
    @Override
    public boolean get(Long chatId) {
        return false;
    }
}
