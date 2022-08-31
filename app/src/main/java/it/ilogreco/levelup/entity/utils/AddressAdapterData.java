package it.ilogreco.levelup.entity.utils;

import android.location.Address;

import java.util.ArrayList;
import java.util.List;

/**
 * A simplified version of Address, it holds a full address name, latitude and longitude
 */
public class AddressAdapterData {
    private String addressName;
    private double latitude;
    private double longitude;

    public static AddressAdapterData fromAddress(Address address) {
        if(address == null) return null;
        AddressAdapterData addressAdapterData = new AddressAdapterData();
        addressAdapterData.setAddressName(address.getAddressLine(0));
        addressAdapterData.setLatitude(address.getLatitude());
        addressAdapterData.setLongitude(address.getLongitude());
        return addressAdapterData;
    }

    public static List<AddressAdapterData> fromAddresses(List<Address> addresses) {
        if(addresses == null || addresses.size() == 0) return List.of();
        List<AddressAdapterData> addressAdapterData = new ArrayList<>();
        for(Address address : addresses) {
            AddressAdapterData current = fromAddress(address);
            if(current != null)
                addressAdapterData.add(current);
        }

        return addressAdapterData;
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

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
}
