package io.github.lightman314.lightmanscurrency.common.notifications;

import io.github.lightman314.lightmanscurrency.LCConfig;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;

public class NotificationData {

    List<Notification> notifications = new ArrayList<>();
    public List<Notification> getNotifications() { return this.notifications; }
    public List<Notification> getNotifications(NotificationCategory category) {
        if(category == NotificationCategory.GENERAL)
            return this.notifications;
        List<Notification> result = new ArrayList<>();
        for(Notification not : notifications)
        {
            if(category.matches(not.getCategory()))
                result.add(not);
        }
        return result;
    }

    public boolean unseenNotification() { return this.unseenNotification(NotificationCategory.GENERAL); }
    public boolean unseenNotification(NotificationCategory category) {
        for(Notification n : this.getNotifications(category))
        {
            if(!n.wasSeen())
                return true;
        }
        return false;
    }

    public List<NotificationCategory> getCategories() {
        List<NotificationCategory> result = new ArrayList<>();
        for(Notification not : this.notifications)
        {
            NotificationCategory category = not.getCategory();
            if(category != null && result.stream().noneMatch(cat -> cat.matches(category)))
                result.add(category);
        }
        return result;
    }

    public void addNotification(Notification newNotification) {
        boolean shouldAdd = true;
        if(this.notifications.size() > 0)
        {
            Notification mostRecent = this.notifications.get(0);
            if(mostRecent.onNewNotification(newNotification))
                shouldAdd = false;
        }
        if(shouldAdd)
            this.notifications.add(0, newNotification);

        this.validateListSize();

    }

    private void validateListSize()
    {
        int limit = LCConfig.SERVER.notificationLimit.get();
        while(this.notifications.size() > limit)
            this.notifications.remove(this.notifications.get(this.notifications.size() - 1));
    }

    public NbtCompound save() {
        NbtCompound compound = new NbtCompound();
        NbtList notificationList = new NbtList();
        for(int i = 0; i < notifications.size(); ++i)
        {
            notificationList.add(notifications.get(i).save());
        }
        compound.put("Notifications", notificationList);
        return compound;
    }

    public static NotificationData loadFrom(NbtCompound compound) {
        NotificationData data = new NotificationData();
        data.load(compound);
        return data;
    }

    public void load(NbtCompound compound) {
        if(compound.contains("Notifications", NbtElement.LIST_TYPE))
        {
            this.notifications = new ArrayList<>();
            NbtList notificationList = compound.getList("Notifications", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < notificationList.size(); ++i)
            {
                NbtCompound notTag = notificationList.getCompound(i);
                Notification not = Notification.deserialize(notTag);
                if(not != null)
                    this.notifications.add(not);
            }
            this.validateListSize();
        }
    }

}