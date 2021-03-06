package fi.otavanopisto.muikku.plugins.schooldatapyramus.rest.cache;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.otavanopisto.muikku.plugins.schooldatapyramus.webhook.WebhookNotificationEvent;

@ApplicationScoped
public class EntityCacheEvictor {
  
  @Inject
  private Logger logger;
  
  @Inject
  private CacheConfigs cacheConfigs;

  @PostConstruct
  public void init() {
    caches = Collections.synchronizedSet(new HashSet<AbstractEntityCache>());
  }
  
  public void addCache(AbstractEntityCache cache) {
    try {
      caches.add(cache);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed to add cache to entity cache evictor", e);
    }
  }
  
  public void removeCache(AbstractEntityCache cache) {
    try {
      caches.remove(cache);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed to remove cache from entity cache evictor", e);
    }
  }

  public void evictPath(String path) {
    try {
      for (AbstractEntityCache cache : caches) {
        cache.remove(path);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, String.format("Failed to evict path %s", path));
    }
  }

  public void flushAll() {
    try {
      for (AbstractEntityCache cache : caches) {
        cache.clear();
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, String.format("Failed to flush all caches"));
    }
  }
  
  public void onWebhookNotificationEvent(@Observes WebhookNotificationEvent event) {
    try {
      List<String> evictTypePaths = cacheConfigs.getEvictTypePaths(event.getType());
  
      Map<String, String> data = null;
      try {
        data = new ObjectMapper().readValue(event.getData(), new TypeReference<Map<String, String>>() { });
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Could not parse webhook notification data", e);
        return;
      }
  
      for (AbstractEntityCache cache : caches) {
        try {
          Pattern pattern = Pattern.compile("\\{[a-zA-Z]*\\}");
          
          for (String evictTypePath : evictTypePaths) {
            String path = evictTypePath;
            
            Matcher matcher = pattern.matcher(path);
            while (matcher.find()) {
              String variable = StringUtils.substring(matcher.group(0), 1, -1);
              String value = data.get(variable);
              
              if (value != null) {
                path = matcher.replaceFirst(value);
              } else {
                logger.log(Level.SEVERE, String.format("Failed to parse path %s", evictTypePath));              
                break;
              }
              
              matcher = pattern.matcher(path);
            }
            
            cache.remove(path);
          }
        } catch (Exception e) {
          logger.log(Level.SEVERE, "Failed to evict caches", e);
          return;
        }
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Cache evict crashed", e);
    }
  }
  
  private Set<AbstractEntityCache> caches;

}
