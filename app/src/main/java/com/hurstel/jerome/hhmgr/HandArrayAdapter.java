package com.hurstel.jerome.hhmgr;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class HandArrayAdapter  extends RecyclerView.Adapter<HandArrayAdapter.ViewHolder>  {

    private List<Hand> mDataSet;

    private OnHandItemClickListener mOnHandItemClickListener;
    public void setOnHandItemClickListener(OnHandItemClickListener listener) { mOnHandItemClickListener = listener; }

    public interface OnHandItemClickListener {
        void onFavoriteButtonClick(View v, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final ImageView ivIcon;
        private final TextView tvName;
        private final TextView tvBlinds;
        private final ImageButton ibFavorite;

        //private final String MY_URL = "http://www.poker-academie.com/communaute-poker-academie/replayer-poker-academie/inserer-une-main-sur-le-replayer-poker-academie.html";
        private final String MY_URL = "https://www.weaktight.com/hand";

        public ViewHolder(View v) {
            super(v);

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);

            ivIcon = (ImageView) v.findViewById(R.id.imageViewType);
            tvName = (TextView) v.findViewById(R.id.textViewName);
            tvBlinds = (TextView) v.findViewById(R.id.textViewBlinds);
            ibFavorite = (ImageButton) v.findViewById(R.id.imageButtonFavorite);
        }

        public void bind(Hand hand, final OnHandItemClickListener listener){
            ivIcon.setImageResource(R.drawable.ic_icon_suits);
            tvName.setText(String.format(Locale.FRENCH, "Hand #%d", hand.getId()));
            if (hand.getAnte() > 0) {
                tvBlinds.setText(String.format(Locale.FRENCH, "%s/%s/%s", hand.getAnte(), hand.getSB(), hand.getBB()));
            } else {
                tvBlinds.setText(String.format(Locale.FRENCH, "%s/%s", hand.getSB(), hand.getBB()));
            }
            ibFavorite.setTag(hand);
            if (hand.isFavorite()) {
                ibFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                ibFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            }
            ibFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onFavoriteButtonClick(v, getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Summary")
                    .setIcon(R.drawable.ic_icon_suits)
                    .setMessage(Hand.getHands().get(getAdapterPosition()).getSummary())
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }

        @Override
        public boolean onLongClick(View v) {
            // Copy to clipboard
            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("summary", Hand.getHands().get(getAdapterPosition()).getSummary());
            clipboard.setPrimaryClip(clip);

            // Start web browser
            Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( MY_URL ) );
            v.getContext().startActivity(intent);

            return false;
        }
    }

    public HandArrayAdapter(List<Hand> dataSet) {
        mDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.hand_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.bind(mDataSet.get(position), mOnHandItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
