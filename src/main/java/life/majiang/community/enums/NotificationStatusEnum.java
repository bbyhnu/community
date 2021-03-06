package life.majiang.community.enums;

/**
 * Created by codedrinker on 2019/6/14.
 *  通知状态的枚举：0是未读，1是已读
 */
public enum NotificationStatusEnum {
    UNREAD(0), READ(1);
    private int status;

    public int getStatus() {
        return status;
    }

    NotificationStatusEnum(int status) {
        this.status = status;
    }
}
