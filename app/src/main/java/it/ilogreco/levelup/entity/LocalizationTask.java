package it.ilogreco.levelup.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import it.ilogreco.levelup.entity.utils.AddressAdapterData;
import it.ilogreco.levelup.entity.utils.BaseEntity;

/**
 *  LocalizationTask is a specialized task, it contains an address name, latitude, longitude and a completion radius (if the user is inside this radius with center at (lat, lon))
 */
@Entity(tableName = "LocalizationTask", foreignKeys = {@ForeignKey(entity = UserTask.class, parentColumns = "id", childColumns = "taskId", onDelete = CASCADE)},
        indices = {@Index("taskId")})
public class LocalizationTask extends BaseEntity {
    private double latitude;
    private double longitude;
    private String addressName;

    private int completionRadius;
    private long taskId;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public int getCompletionRadius() {
        return completionRadius;
    }

    public void setCompletionRadius(int completionRadius) {
        this.completionRadius = completionRadius;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public boolean isPlaceValid() {
        return addressName != null && !addressName.equals("") && latitude != 0 && longitude != 0;
    }

    public AddressAdapterData getAsAddressAdapterData() {
        AddressAdapterData addressAdapterData = new AddressAdapterData();
        addressAdapterData.setAddressName(addressName);
        addressAdapterData.setLatitude(latitude);
        addressAdapterData.setLongitude(longitude);
        return addressAdapterData;
    }
}
