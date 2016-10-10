package com.example.calendartest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

public class EventListAdapter extends ArrayAdapter<MyEvent> {

    private Context context;
    private List<MyEvent> events;

    public EventListAdapter(Context context, List<MyEvent> events) {
        super(context, R.layout.events_calendar_layout, events);
        this.context = context;
        this.events = events;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.events_calendar_layout, parent, false);
        }

        MyEvent event = events.get(position);
        TextView textViewEventSummary = (TextView) convertView.findViewById(R.id.textViewEventSummary);
        TextView textViewEventTime = (TextView) convertView.findViewById(R.id.textViewEventTime);
        FrameLayout frameLayoutEventColor = (FrameLayout) convertView.findViewById(R.id.frameLayoutEventColor);

        GradientDrawable shape = (GradientDrawable) frameLayoutEventColor.getBackground();
        shape.setColor(Color.parseColor(Tools.getEventColor(event)));

        textViewEventSummary.setText(event.getSummary());
        if (event.getEnd().isEmpty()) {
            textViewEventTime.setText("All day event");
        } else {
            textViewEventTime.setText(DateUtils.formatEventDateTimeToTime(event.getStart()) + " - " + DateUtils.formatEventDateTimeToTime(event.getEnd()));
        }

        return convertView;
    }
}
