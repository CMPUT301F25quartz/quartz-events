package com.example.ajilore.code.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ajilore.code.R;
/**
 * {@code EventsScreenFragment} is a minimal concrete fragment that simply inflates
 * the {@code fragment_events} layout.
 *
 * <p>This class exists because {@link EventsFragment} is an abstract class and
 * cannot be instantiated directly. Some parts of the application (e.g., XML
 * navigation graphs or placeholder screens) may require a non-abstract fragment
 * that renders the base event list layout without providing custom logic.</p>
 *
 * <p><b>Important:</b> This fragment does not load or display any events by itself.
 * It simply inflates the layout file. Fragments such as
 * {@link GeneralEventsFragment}, {@link OrganizerEventsFragment}, or other subclasses
 * of {@link EventsFragment} handle data loading and interactive functionality.</p>
 *
 * <p>Use this fragment only when you need the static UI of the event list screen
 * without the underlying event-fetching behavior.</p>
 *
 */
//for the fragment_events screen. because EventsFragment is abstract
public class EventsScreenFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events, container, false);
    }
}
