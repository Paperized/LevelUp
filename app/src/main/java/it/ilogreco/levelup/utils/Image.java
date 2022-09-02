package it.ilogreco.levelup.utils;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.Objects;

public class Image {
    public Uri uri;
    public Bitmap bitmap;

    public Image() { }
    public Image(Uri uri, Bitmap bitmap) {
        this.uri = uri;
        this.bitmap = bitmap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return Objects.equals(uri, image.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }
}
