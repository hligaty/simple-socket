package io.github.hligaty.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Get the member list of the group, which is used to facilitate the broadcast of messages
 */
public final class GroupCache {
    private static final Map<Object, List<Object>> GROUPS = new ConcurrentHashMap<>();

    public static List<Object> getGroup(Object groupKey) {
        List<Object> list = GROUPS.get(groupKey);
        return list != null ? list : Collections.emptyList();
    }

    public static void addGroup(Object groupKey, Object... groupMembers) {
        List<Object> group = GROUPS.get(groupKey);
        if (group != null) {
            group.addAll(Arrays.asList(groupMembers));
        } else {
            synchronized (GROUPS) {
                group = GROUPS.get(groupKey);
                if (group == null) {
                    GROUPS.put(groupKey, new CopyOnWriteArrayList<>(groupMembers));
                } else {
                    group.addAll(Arrays.asList(groupMembers));
                }
            }
        }
    }
}
