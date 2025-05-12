package com.thedan17.salesnet.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

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
 * @param <K> Key, параметр поиска
 * @param <O> Object, объект результатов поиска
 * @param <I> Id, тип id, получаемого из объекта
 */
public class CacheIdManager<K, O, I> {
  private final Long cacheMaxSize;
  private final Short clearPercentage;
  private final HashMap<K, Set<I>> cache = new HashMap<>();
  private final Queue<K> history = new LinkedList<>();
  private final HashMap<I, Set<K>> linkRepository = new HashMap<>();
  private final HashMap<I, O> entityRepository = new HashMap<>();

  private final Function<O, I> getId;
  private Function<K, Set<O>> cacheableAction;
  private BiPredicate<K, O> isPairValid;

  private final AppLoggerCore logger = new AppLoggerCore();

  /** Перечисление для указания причины {@link #updateCache}. */
  public enum UpdateReason {
    ENTITY_ADDED,
    ENTITY_EDITED,
    ENTITY_DELETED
  }

  /** Конструктор для задания настроек класса. */
  public CacheIdManager(Function<O, I> getIdFunc, long sizeOfCache, short clearPercentage) {
    if (clearPercentage < 1 || clearPercentage > 100) {
      throw new IllegalArgumentException("Clear percentage in CacheIdManager must be between 1 and 100");
    }
    this.cacheMaxSize = sizeOfCache;
    this.clearPercentage = clearPercentage;
    this.getId = getIdFunc;
    this.logger.debug("Object created");
  }

  /** Задание опциональных полей класса. */
  public void setFunctionality(
      Function<K, Set<O>> cacheableAction,
      BiPredicate<K, O> isPairValid) {
    this.cacheableAction = cacheableAction;
    this.isPairValid = isPairValid;
    logger.debug("Functionality set");
  }

  /**
   * Метод получения результатов действия; если таковые имеются в кэше, то получаются оттуда.
   *
   * @param isResultCaching влияет на то, сохранятся ли полученные данные в кэш при их отсутствии
   */
  public Set<O> doAction(K argument, boolean isResultCaching) {
    logger.debug("Entry doAction");
    logger.debug("Key argument: {}", argument.toString());
    Optional<Set<O>> cacheResult = getCachedAction(argument);
    if (cacheResult.isPresent()) {
      logger.debug("Cache hit");
      return cacheResult.get();
    }
    logger.debug("Cache miss");
    Set<O> actionResult = cacheableAction.apply(argument);
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
  public Set<O> doAction(K argument) {
    return doAction(argument, true);
  }

  /**
   * Прямое извлечение результата из имеющегося кэша.
   *
   * @return значение из кэша по параметру поиска, иначе {@code Optional.empty()}
   */
  public Optional<Set<O>> getCachedAction(K argument) {
    Set<I> resultInner = cache.get(argument);
    Set<O> result = new HashSet<>();
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
  public void addCache(K key, Set<O> results) {
    trimCacheIfFull();
    Set<I> resultsCopy = new HashSet<>();
    for (O resItem : results) {
      I id = getId.apply(resItem);
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
  public void updateCache(O entity, UpdateReason updateReason, boolean includeKeysWithoutEntity) {
    logger.debug("Entry to updating cache, reason: {}", updateReason.toString());
    logger.debug("Entity - {}", entity.toString());
    logger.debug(
        "Data: \nCache - {}\nHistory - {}\nLinks - {}\nEntities - {}",
        cache,
        history,
        linkRepository,
        entityRepository);
    // init
    I entityId = getId.apply(entity);
    Set<K> baseKeys = new HashSet<>(cache.keySet());
    if (linkRepository.containsKey(entityId)) {
      logger.debug("Reverse cache with such id found");
    } else {
      logger.debug("Reverse cache with such id NOT found");
    }
    Set<K> possibleConflictKeys =
        new HashSet<>(linkRepository.computeIfAbsent(entityId, k -> new HashSet<>()));
    // keys with entity
    for (K key : possibleConflictKeys) {
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
      for (K key : baseKeys) {
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
  public void softUpdateCache(O group, UpdateReason updateReason) {
    updateCache(group, updateReason, false);
  }

  /** Перегрузка для значения по умолчанию у {@link #updateCache(Object, UpdateReason, boolean)}. */
  public void hardUpdateCache(O group, UpdateReason updateReason) {
    updateCache(group, updateReason, true);
  }

  /** Очищает весь кэш, связанный с объектом класса. */
  public void clearAllCache() {
    cache.clear();
    history.clear();
    linkRepository.clear();
  }

  /** Метод, который удаляет самые старые записи из кэша, определённый процент от размера. */
  private void trimCache() {
    long targetCacheSize = (long) (cacheMaxSize * ((100.0 - clearPercentage) / 100.0));
    while (cache.size() > targetCacheSize) {
      if (history.isEmpty()) {
        throw new IllegalStateException("Cache overflow clear is running despite cache is empty");
      }
      K cleaningKey = history.poll();
      if (cleaningKey == null) {
        throw new NullPointerException("Cache element in history is null");
      }
      Set<I> updatedLinks = cache.get(cleaningKey);
      for (I linkKey : updatedLinks) {
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
        throw new IllegalStateException("Cache clearance failed when handling overflow");
      }
    }
  }
}
