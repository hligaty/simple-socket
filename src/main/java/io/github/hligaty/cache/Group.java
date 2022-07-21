package io.github.hligaty.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 简单的线程安全的分组缓存
 *
 * @author hligaty
 */
public final class Group {
    private static final Map<Object, Set<Object>> GROUPS = new ConcurrentHashMap<>();

    public static Collection<Object> get(Object groupKey) {
        Collection<Object> group = GROUPS.get(groupKey);
        return group != null ? group : Collections.emptySet();
    }

    public static void add(Object groupKey, Object groupMember) {
        Set<Object> group = GROUPS.get(groupKey);
        if (group != null) {
            group.add(groupMember);
        } else {
            synchronized (GROUPS) {
                group = GROUPS.get(groupKey);
                if (group == null) {
                    GROUPS.put(groupKey, group = new CopyOnWriteArraySet<>());
                }
                group.add(groupMember);
            }
        }
    }

    public static void remove(Object groupKey, Object groupMember) {
        Set<Object> group = GROUPS.get(groupKey);
        if (group != null) {
            group.remove(groupMember);
        }
    }

    public static void clearEmpty() {
        synchronized (GROUPS) {
            GROUPS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
    }

    public static void clear(Object groupKey) {
        synchronized (GROUPS) {
            GROUPS.remove(groupKey);
        }
    }

    public static void addAll(Object groupKey, Collection<Object> groupMembers) {
        Set<Object> group = GROUPS.get(groupKey);
        if (group != null) {
            group.addAll(groupMembers);
        } else {
            synchronized (GROUPS) {
                group = GROUPS.get(groupKey);
                if (group == null) {
                    GROUPS.put(groupKey, new CopyOnWriteArraySet<>(groupMembers));
                } else {
                    group.addAll(groupMembers);
                }
            }
        }
    }

    public static void removeAll(Object groupKey, Collection<Object> groupMembers) {
        Set<Object> group = GROUPS.get(groupKey);
        if (group != null) {
            groupMembers.forEach(group::remove);
        }
    }
}
