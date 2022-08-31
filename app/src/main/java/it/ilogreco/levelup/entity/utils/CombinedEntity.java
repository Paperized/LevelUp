package it.ilogreco.levelup.entity.utils;

/**
 *  Used for entities with two primary keys
 */
public abstract class CombinedEntity implements Entity {
    private long firstId;
    private long secondId;

    public long getFirstId() {
        return firstId;
    }

    public void setFirstId(long firstId) {
        this.firstId = firstId;
    }

    public long getSecondId() {
        return secondId;
    }

    public void setSecondId(long secondId) {
        this.secondId = secondId;
    }

    @Override
    public boolean isIdValid() {
        return firstId > 0 && secondId > 0;
    }
}
