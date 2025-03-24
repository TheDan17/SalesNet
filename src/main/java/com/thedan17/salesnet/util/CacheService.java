package com.thedan17.salesnet.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Класс для хранения и последующего обновления кэша.
 *
 * <p>Изначально спроектирован для хранения результатов поиска, где каждому сочетанию параметров
 * соответствует список подходящих объектов. Предполагается использование в одном потоке
 *
 * <p>Перед работой необходимо задать функции через метод {@link CacheService#setFunctionality},
 * если планируется использовать методы {@link CacheService#doAction} или {@link
 * CacheService#hardUpdateCache}
 *
 * @param <KeyT> параметр поиска
 * @param <EntityT> объект результатов поиска
 */
public class CacheService<KeyT, EntityT> {
  private final Long cacheMaxSize;
  private final Short clearPercentage;
  private final HashMap<KeyT, Set<EntityT>> cache = new HashMap<>();
  private final Queue<KeyT> history = new LinkedList<>();
  private final HashMap<EntityT, Set<KeyT>> linkRepository = new HashMap<>();

  private Function<KeyT, Set<EntityT>> cacheableAction;
  private BiFunction<KeyT, EntityT, Boolean> isPairValid;

  /** Перечисление для указания причины {@link #updateCache}. */
  public enum UpdateReason {
    ENTITY_EDITED,
    ENTITY_DELETED
  }

  /** Конструктор для задания настроек класса. */
  public CacheService(long sizeOfCache, short clearPercentage) {
    if (clearPercentage < 1 || clearPercentage > 100) {
      throw new RuntimeException("Clear percentage in CacheService must be between 1 and 100");
    }
    this.cacheMaxSize = sizeOfCache;
    this.clearPercentage = clearPercentage;
  }

  /** Задание опциональных полей класса. */
  public void setFunctionality(
      Function<KeyT, Set<EntityT>> cacheableAction,
      BiFunction<KeyT, EntityT, Boolean> isPairValid) {
    this.cacheableAction = cacheableAction;
    this.isPairValid = isPairValid;
  }

  /**
   * Метод получения результатов действия; если таковые имеются в кэше, то получаются оттуда.
   *
   * @param isResultCaching влияет на то, сохранятся ли полученные данные в кэш при их отсутствии
   */
  public Set<EntityT> doAction(KeyT argument, boolean isResultCaching) {
    Optional<Set<EntityT>> cacheResult = getCachedAction(argument);
    if (cacheResult.isPresent()) {
      return cacheResult.get();
    }
    Set<EntityT> actionResult = cacheableAction.apply(argument);
    if (isResultCaching) {
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
    Set<EntityT> result = cache.get(argument);
    if (result == null) {
      return Optional.empty();
    } else {
      return Optional.of(result);
    }
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
    Set<EntityT> resultsCopy = new HashSet<>(results);
    trimCacheIfFull();
    cache.put(key, resultsCopy);
    history.add(key);
    addCacheLinks(key, resultsCopy);
  }

  /** Добавление ссылок от объектов к ключам, результат которых их содержит. */
  private void addCacheLinks(KeyT key, Set<EntityT> results) {
    for (EntityT result : results) {
      if (!linkRepository.containsKey(result)) {
        linkRepository.put(result, new HashSet<>());
      }
      linkRepository.get(result).add(key);
    }
  }

  /**
   * Актуализация кэша при изменении сущностей, которые могут находиться в результатах ключей.
   *
   * <p>Значения из {@link UpdateReason} соответствуют операциям в БД.
   *
   * @param includeKeysWithoutEntity флаг, отвечающий за соотношение жёсткости и скорости
   *     актуализации кэша
   */
  public void updateCache(
      EntityT group, UpdateReason updateReason, boolean includeKeysWithoutEntity) {
    Set<KeyT> baseKeys = new HashSet<>(cache.keySet());
    Set<KeyT> possibleConflictKeys = new HashSet<>();
    if (linkRepository.containsKey(group)) {
      possibleConflictKeys = linkRepository.get(group);
    }
    for (KeyT key : possibleConflictKeys) {
      boolean shouldKeep =
          (updateReason == UpdateReason.ENTITY_EDITED && isPairValid.apply(key, group));
      if (!shouldKeep) {
        cache.get(key).remove(group);
      }
      baseKeys.remove(key);
    }
    if (includeKeysWithoutEntity && updateReason != UpdateReason.ENTITY_DELETED) {
      for (KeyT key : baseKeys) {
        if (isPairValid.apply(key, group)) {
          cache.get(key).add(group);
        }
      }
    }
    if (updateReason == UpdateReason.ENTITY_DELETED) {
      linkRepository.remove(group);
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
      Set<EntityT> updatedLinks = cache.get(cleaningKey);
      for (EntityT linkKey : updatedLinks) {
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
