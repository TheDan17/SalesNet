package com.thedan17.salesnet.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Класс для хранения и последующего обновления кэша.
 *
 * <p>Изначально спроектирован для хранения результатов поиска, где каждому сочетанию параметров
 * соответствует список подходящих объектов. Предполагается использование в одном потоке
 *
 * <p>Перед работой необходимо задать функции через метод {@link CacheIdManager#setFunctionality},
 * если планируется использовать методы {@link CacheIdManager#doAction} или {@link
 * CacheIdManager#hardUpdateCache} (неявно используется функцией {@link CacheIdManager#updateCache})
 *
 * @param <KeyT> параметр поиска
 * @param <EntityT> объект результатов поиска
 * @param <IdT> тип id, получаемого из объекта
 */
public class CacheIdManager<KeyT, EntityT, IdT> {
  private final Long cacheMaxSize;
  private final Short clearPercentage;
  private final HashMap<KeyT, Set<IdT>> cache = new HashMap<>();
  private final Queue<KeyT> history = new LinkedList<>();
  private final HashMap<IdT, Set<KeyT>> linkRepository = new HashMap<>();
  private final HashMap<IdT, EntityT> entityRepository = new HashMap<>();

  private final Function<EntityT, IdT> getId;
  private Function<KeyT, Set<EntityT>> cacheableAction;
  private BiPredicate<KeyT, EntityT> isPairValid;

  // TODO remove debug objects
  private static final Logger logger = LoggerFactory.getLogger(CacheIdManager.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  /** Перечисление для указания причины {@link #updateCache}. */
  public enum UpdateReason {
    ENTITY_ADDED,
    ENTITY_EDITED,
    ENTITY_DELETED
  }

  /** Конструктор для задания настроек класса. */
  public CacheIdManager(Function<EntityT, IdT> getIdFunc, long sizeOfCache, short clearPercentage) {
    if (clearPercentage < 1 || clearPercentage > 100) {
      throw new RuntimeException("Clear percentage in CacheIdManager must be between 1 and 100");
    }
    this.cacheMaxSize = sizeOfCache;
    this.clearPercentage = clearPercentage;
    this.getId = getIdFunc;
    logger.debug("Object created");
  }

  /** Задание опциональных полей класса. */
  public void setFunctionality(
      Function<KeyT, Set<EntityT>> cacheableAction,
      BiPredicate<KeyT, EntityT> isPairValid) {
    this.cacheableAction = cacheableAction;
    this.isPairValid = isPairValid;
    logger.debug("Functionality set");
  }

  /**
   * Метод получения результатов действия; если таковые имеются в кэше, то получаются оттуда.
   *
   * @param isResultCaching влияет на то, сохранятся ли полученные данные в кэш при их отсутствии
   */
  public Set<EntityT> doAction(KeyT argument, boolean isResultCaching) {
    logger.debug("Entry doAction");
    logger.debug("Key argument: {}", argument.toString());
    Optional<Set<EntityT>> cacheResult = getCachedAction(argument);
    if (cacheResult.isPresent()) {
      logger.debug("Cache hit");
      return cacheResult.get();
    }
    logger.debug("Cache miss");
    Set<EntityT> actionResult = cacheableAction.apply(argument);
    if (isResultCaching) {
      logger.debug("Add cache");
      addCache(argument, actionResult);
    }
    return actionResult;
  }

  /**
   * Перегрузка для {@link #doAction(Object, boolean)}, когда кэширование отсутствующего результата
   * подразумевается по умолчанию.
   */
  public Set<EntityT> doAction(KeyT argument) {
    return doAction(argument, true);
  }

  /**
   * Прямое извлечение результата из имеющегося кэша.
   *
   * @return значение из кэша по параметру поиска, иначе {@code Optional.empty()}
   */
  public Optional<Set<EntityT>> getCachedAction(KeyT argument) {
    Set<IdT> resultInner = cache.get(argument);
    Set<EntityT> result = new HashSet<>();
    if (resultInner == null) {
      return Optional.empty();
    }
    for (var resItem : resultInner) {
      result.add(entityRepository.get(resItem));
    }
    return Optional.of(result);
  }

  /**
   * Добавление кэша с проверкой на заполненность.
   *
   * <ul>
   *   <li>Если при добавлении кэш заполнен, то сначала очищает часть записей
   *   <li>Размер кэша и процент очищаемых записей заданы при создании объекта класса
   *   <li>Помимо самих записей добавляет быстрые ссылки на ключи, которые будут содержать значения
   * </ul>
   */
  public void addCache(KeyT key, Set<EntityT> results) {
    trimCacheIfFull();
    Set<IdT> resultsCopy = new HashSet<>();
    for (EntityT resItem : results) {
      IdT id = getId.apply(resItem);
      resultsCopy.add(id);
      entityRepository.put(id, resItem);
      linkRepository.computeIfAbsent(id, k -> new HashSet<>()).add(key);
    }
    cache.put(key, resultsCopy);
    history.add(key);
  }

  /**
   * Актуализация кэша при изменении сущностей, которые могут находиться в результатах ключей.
   *
   * <p>Значения из {@link UpdateReason} соответствуют операциям в БД.
   *
   * @param includeKeysWithoutEntity флаг, отвечающий за жёсткость актуализации кэша
   */
  public void updateCache(
      EntityT entity, UpdateReason updateReason, boolean includeKeysWithoutEntity) {
    logger.debug("Entry to updating cache, reason: {}", updateReason.toString());
    logger.debug("Entity - {}", entity.toString());
    logger.debug(
        "Data: \nCache - {}\nHistory - {}\nLinks - {}\nEntities - {}",
        cache,
        history,
        linkRepository,
        entityRepository);
    // init
    IdT entityId = getId.apply(entity);
    Set<KeyT> baseKeys = new HashSet<>(cache.keySet());
    if (linkRepository.containsKey(entityId)) {
      logger.debug("Reverse cache with such id found");
    } else {
      logger.debug("Reverse cache with such id NOT found");
    }
    Set<KeyT> possibleConflictKeys =
        new HashSet<>(linkRepository.computeIfAbsent(entityId, k -> new HashSet<>())); // TODO wrong
    // keys with entity
    for (KeyT key : possibleConflictKeys) {
      logger.debug("Entry to current key (possible conflict): {}", key.toString());
      boolean shouldKeepPair =
          (updateReason != UpdateReason.ENTITY_DELETED && isPairValid.test(key, entity));
      if (!shouldKeepPair) {
        logger.debug("Remove entity");
        cache.get(key).remove(entityId);
        linkRepository.get(entityId).remove(key);
      }
      baseKeys.remove(key);
    }
    // keys without entity
    if (updateReason == UpdateReason.ENTITY_DELETED) {
      linkRepository.remove(entityId);
      entityRepository.remove(entityId);
    } else if (includeKeysWithoutEntity) {
      for (KeyT key : baseKeys) {
        logger.debug("Entry to current key (without entity): {}", key.toString());
        if (isPairValid.test(key, entity)) {
          logger.debug("Add entity");
          cache.get(key).add(entityId);
          linkRepository.computeIfAbsent(entityId, en -> new HashSet<>()).add(key);
        }
      }
    }
    // final
    switch (updateReason) {
      case ENTITY_ADDED, ENTITY_EDITED:
        entityRepository.put(entityId, entity);
        break;
      case ENTITY_DELETED:
        entityRepository.remove(entityId);
        break;
      default:
    }
  }

  /** Перегрузка для значения по умолчанию у {@link #updateCache(Object, UpdateReason, boolean)}. */
  public void softUpdateCache(EntityT group, UpdateReason updateReason) {
    updateCache(group, updateReason, false);
  }

  /** Перегрузка для значения по умолчанию у {@link #updateCache(Object, UpdateReason, boolean)}. */
  public void hardUpdateCache(EntityT group, UpdateReason updateReason) {
    updateCache(group, updateReason, true);
  }

  /** Очищает весь кэш, связанный с объектом класса. */
  public void clearAllCache() {
    cache.clear();
    history.clear();
    linkRepository.clear();
  }

  /** Метод, который удаляет самые старые записи из кэша. */
  private void trimCache() {
    long targetCacheSize = (long) (cacheMaxSize * ((100.0 - clearPercentage) / 100.0));
    while (cache.size() > targetCacheSize) {
      if (history.isEmpty()) {
        throw new RuntimeException("Cache overflow clear is running despite cache is empty");
      }
      KeyT cleaningKey = history.poll();
      if (cleaningKey == null) {
        throw new NullPointerException("Cache element in history is null");
      }
      Set<IdT> updatedLinks = cache.get(cleaningKey);
      for (IdT linkKey : updatedLinks) {
        linkRepository.get(linkKey).remove(cleaningKey);
      }
      cache.remove(cleaningKey);
    }
  }

  /**
   * Проверка пула кэша на заполненность.
   *
   * <p>Вызывает очистку кэша и выбрасывает исключение, если очистка не сработала, как ожидалось
   */
  private void trimCacheIfFull() {
    if (cache.size() >= cacheMaxSize) {
      trimCache();
      if (cache.size() >= cacheMaxSize) {
        throw new RuntimeException("Cache clearance failed when handling overflow");
      }
    }
  }
}
