package com.hurstel.jerome.hhmgr;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class SessionArrayAdapter extends ArrayAdapter<Session> {

    private final Context context;
    private final int resource;
    private final List<Session> values;

    public SessionArrayAdapter(Context context, int resource, List<Session> values) {
        super(context, resource, values);
        this.context = context;
        this.resource = resource;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(resource, parent, false);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageViewType);
        String[] type = context.getResources().getStringArray(R.array.session_type_array);
        if (values.get(position).getType().equals(type[0])) { // "Tournoi MTT"
            imageView.setImageResource(R.drawable.ic_icon_mtt);
        } else if (values.get(position).getType().equals(type[1])) { // "Cash Game"
            imageView.setImageResource(R.drawable.ic_icon_cg);
        }

        TextView textViewName = (TextView) rowView.findViewById(R.id.textViewName);
        textViewName.setText(values.get(position).getName());

        TextView textViewBlinds = (TextView) rowView.findViewById(R.id.textViewBlinds);
        if (values.get(position).getAnte() > 0) {
            textViewBlinds.setText(String.format("Blinds %s/%s/%s", values.get(position).getAnte(), values.get(position).getSB(), values.get(position).getBB()));
        } else {
            textViewBlinds.setText(String.format("Blinds %s/%s", values.get(position).getSB(), values.get(position).getBB()));
        }

        HandManager m = new HandManager(getContext());
        m.open();
        Cursor c = m.getHands(values.get(position).getName());
        TextView textViewHands = (TextView) rowView.findViewById(R.id.textViewHands);
        textViewHands.setText(String.format(Locale.FRENCH, "Contains %d hand%s", c.getCount(), c.getCount() > 1 ? "s" : ""));

        TextView textViewDate = (TextView) rowView.findViewById(R.id.textViewDate);
        textViewDate.setText(String.format(Locale.FRENCH, "Created %s", values.get(position).getDate()));

        return rowView;
    }
}