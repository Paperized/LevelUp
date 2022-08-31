package it.ilogreco.levelup.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;
import java.util.List;

import it.ilogreco.levelup.R;

/**
 * Adapter used with a Slider, takes a List of Uris that will load the images in background.
 * All Uris required permissions if taken from the gallery via Intent, otherwise an error is thrown
 */
public class SliderImageAdapter extends SliderViewAdapter<SliderImageAdapter.SliderAdapterVH> {
    private List<Uri> mSliderItems = new ArrayList<>();

    public void deleteItem(int position) {
        if(position >= this.mSliderItems.size()) return;
        this.mSliderItems.remove(position);
        notifyDataSetChanged();
    }

    public void addItems(List<Uri> sliderItem) {
        if(sliderItem == null || sliderItem.size() == 0) return;
        this.mSliderItems.addAll(sliderItem);
        notifyDataSetChanged();
    }

    public void setItems(List<Uri> sliderItem) {
        if(sliderItem == null) sliderItem = new ArrayList<>();
        this.mSliderItems = sliderItem;
        notifyDataSetChanged();
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {
        Uri sliderItem = mSliderItems.get(position);

        try {
            viewHolder.imageViewBackground.setImageURI(sliderItem);
        } catch (Exception exception) {
            viewHolder.imageViewBackground.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
    }

    public List<Uri> getItems() {
        return new ArrayList<>(mSliderItems);
    }

    @Override
    public int getCount() {
        return mSliderItems.size();
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
