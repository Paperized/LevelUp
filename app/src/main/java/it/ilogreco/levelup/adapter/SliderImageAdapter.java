package it.ilogreco.levelup.adapter;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smarteist.autoimageslider.SliderViewAdapter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import it.ilogreco.levelup.R;
import it.ilogreco.levelup.utils.Image;

/**
 * Adapter used with a Slider, takes a List of Uris that will load the images in background.
 * All Uris required permissions if taken from the gallery via Intent, otherwise an error is thrown
 */
public class SliderImageAdapter extends SliderViewAdapter<SliderImageAdapter.SliderAdapterVH> {
    private final List<Image> mSliderItems = new ArrayList<>();
    private final WeakReference<ContentResolver> contentResolverWeakReference;

    public SliderImageAdapter(ContentResolver contentResolver) {
        contentResolverWeakReference = new WeakReference<>(contentResolver);
    }

    public void deleteItem(int position) {
        if(position >= this.mSliderItems.size()) return;
        this.mSliderItems.remove(position);
        notifyDataSetChanged();
    }

    public void addItems(List<Uri> sliderItem) {
        if(sliderItem == null || sliderItem.size() == 0) return;
        for (Uri uri : sliderItem) {
            Bitmap prevBitmap = loadBitmap(uri);
            Image image = new Image(uri, prevBitmap);
            mSliderItems.add(image);
        }

        notifyDataSetChanged();
    }

    public void setItems(List<Uri> sliderItem) {
        if(sliderItem == null) sliderItem = new ArrayList<>();
        mSliderItems.clear();

        for (Uri uri : sliderItem) {
            Bitmap prevBitmap = loadBitmap(uri);
            Image image = new Image(uri, prevBitmap);
            mSliderItems.add(image);
        }

        notifyDataSetChanged();
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {
        Image sliderItem = mSliderItems.get(position);
        if(sliderItem.bitmap == null)
            sliderItem.bitmap = loadBitmap(sliderItem.uri);

        try {
            viewHolder.imageViewBackground.setImageBitmap(sliderItem.bitmap);
        } catch (Exception exception) {
            viewHolder.imageViewBackground.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
    }

    public List<Image> getItems() {
        return mSliderItems;
    }

    public List<Uri> getItemsUri() {
        List<Uri> uris = new ArrayList<>();
        for(Image image : mSliderItems) {
            if(image.uri != null)
                uris.add(image.uri);
        }

        return uris;
    }

    private Bitmap loadBitmap(Uri uri) {
        ContentResolver contentResolver = contentResolverWeakReference.get();
        Bitmap prevBitmap = getBitmap(uri);
        if(prevBitmap == null && contentResolver != null) {
            try {
                prevBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
            } catch (IOException e) { }
        }

        return prevBitmap;
    }

    @Override
    public int getCount() {
        return mSliderItems.size();
    }

    private Bitmap getBitmap(Uri uri) {
        for (Image image : mSliderItems)
            if(image.uri.equals(uri))
                return image.bitmap;

        return null;
    }

    static class SliderAdapterVH extends SliderViewAdapter.ViewHolder {
        View itemView;
        ImageView imageViewBackground;
        ImageView imageGifContainer;
        TextView textViewDescription;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            imageGifContainer = itemView.findViewById(R.id.iv_gif_container);
            textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider);
            this.itemView = itemView;
        }
    }

}
