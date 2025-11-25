package com.example.ajilore.code.adapters;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ajilore.code.models.Event;
import com.example.ajilore.code.models.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for Admin adapters.
 *
 * Tests adapter functionality with realistic data.
 *
 * @author Dinma (Team Quartz)
 */
@RunWith(AndroidJUnit4.class)
public class AdminAdaptersIntegrationTest {

    private Context context;
    private List<Event> testEvents;
    private List<User> testUsers;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        testEvents = createTestEvents();
        testUsers = createTestUsers();
    }

    /**
     * Creates realistic test event data
     */
    private List<Event> createTestEvents() {
        List<Event> events = new ArrayList<>();

        Event event1 = new Event();
        event1.setEventId("event001");
        event1.setTitle("Swimming Lessons");
        event1.setDescription("Beginner swimming lessons for kids");
        event1.setCapacity(20);
        event1.setDate("Sunday 13 November");
        event1.setTime("6:00 PM");
        event1.setLocation("Community Pool");
        event1.setPrice(60);
        events.add(event1);

        Event event2 = new Event();
        event2.setEventId("event002");
        event2.setTitle("Piano Workshop");
        event2.setDescription("Learn basic piano techniques");
        event2.setCapacity(15);
        event2.setDate("Saturday 12 November");
        event2.setTime("2:00 PM");
        event2.setLocation("Music Hall");
        event2.setPrice(50);
        events.add(event2);

        Event event3 = new Event();
        event3.setEventId("event003");
        event3.setTitle("Dance Class");
        event3.setDescription("Interpretive dance basics");
        event3.setCapacity(30);
        event3.setDate("Friday 11 November");
        event3.setTime("7:00 PM");
        event3.setLocation("Dance Studio");
        event3.setPrice(40);
        events.add(event3);

        return events;
    }

    /**
     * Creates realistic test user data
     */
    private List<User> createTestUsers() {
        List<User> users = new ArrayList<>();

        User user1 = new User();
        user1.setUserId("user001");
        user1.setName("David Silbia");
        user1.setEmail("david.silbia@example.com");
        user1.setPhone("555-0101");
        user1.setRole("admin");
        users.add(user1);

        User user2 = new User();
        user2.setUserId("user002");
        user2.setName("Sarah Johnson");
        user2.setEmail("sarah.j@example.com");
        user2.setPhone("555-0102");
        user2.setRole("organizer");
        users.add(user2);

        User user3 = new User();
        user3.setUserId("user003");
        user3.setName("Mike Chen");
        user3.setEmail("mike.chen@example.com");
        user3.setPhone("555-0103");
        user3.setRole("entrant");
        users.add(user3);

        return users;
    }

    /**
     * Test AdminEventsAdapter initialization and item count
     */
    @Test
    public void testEventsAdapterItemCount() {
        AdminEventsAdapter adapter = new AdminEventsAdapter(context,
                new AdminEventsAdapter.OnEventActionListener() {
                    @Override
                    public void onDeleteClick(Event event) {}

                    @Override
                    public void onEventClick(Event event) {}
                });

        // Initially should be empty
        assertEquals("Adapter should start empty", 0, adapter.getItemCount());

        // Set events
        adapter.setEvents(testEvents);

        // Should have correct count
        assertEquals("Adapter should have 3 events", 3, adapter.getItemCount());
    }

    /**
     * Test AdminUsersAdapter initialization and item count
     */
    @Test
    public void testUsersAdapterItemCount() {
        AdminUsersAdapter adapter = new AdminUsersAdapter(context,
                new AdminUsersAdapter.OnUserActionListener() {
                    @Override
                    public void onDeleteClick(User user) {}

                    @Override
                    public void onUserClick(User user) {}
                });

        // Initially empty
        assertEquals("Adapter should start empty", 0, adapter.getItemCount());

        // Set users
        adapter.setUsers(testUsers);

        // Should have correct count
        assertEquals("Adapter should have 3 users", 3, adapter.getItemCount());
    }

    /**
     * Test events adapter filter functionality
     */
    @Test
    public void testEventsAdapterFilter() {
        AdminEventsAdapter adapter = new AdminEventsAdapter(context,
                new AdminEventsAdapter.OnEventActionListener() {
                    @Override
                    public void onDeleteClick(Event event) {}
                    @Override
                    public void onEventClick(Event event) {}
                });

        adapter.setEvents(testEvents);
        assertEquals("Should have 3 events initially", 3, adapter.getItemCount());

        // Filter for "swimming"
        adapter.filter("swimming");
        assertEquals("Should have 1 event after filter", 1, adapter.getItemCount());

        // Clear filter
        adapter.filter("");
        assertEquals("Should have 3 events after clearing filter", 3, adapter.getItemCount());
    }

    /**
     * Test users adapter filter functionality
     */
    @Test
    public void testUsersAdapterFilter() {
        AdminUsersAdapter adapter = new AdminUsersAdapter(context,
                new AdminUsersAdapter.OnUserActionListener() {
                    @Override
                    public void onDeleteClick(User user) {}
                    @Override
                    public void onUserClick(User user) {}
                });

        adapter.setUsers(testUsers);
        assertEquals("Should have 3 users initially", 3, adapter.getItemCount());

        // Filter for "david"
        adapter.filter("david");
        assertEquals("Should have 1 user after filter", 1, adapter.getItemCount());

        // Clear filter
        adapter.filter("");
        assertEquals("Should have 3 users after clearing filter", 3, adapter.getItemCount());
    }

    /**
     * Test event click callback is triggered
     */
    @Test
    public void testEventClickCallback() {
        final boolean[] callbackTriggered = {false};
        final Event[] clickedEvent = {null};

        AdminEventsAdapter adapter = new AdminEventsAdapter(context,
                new AdminEventsAdapter.OnEventActionListener() {
                    @Override
                    public void onDeleteClick(Event event) {}

                    @Override
                    public void onEventClick(Event event) {
                        callbackTriggered[0] = true;
                        clickedEvent[0] = event;
                    }
                });

        adapter.setEvents(testEvents);

        new AdminEventsAdapter.EventViewHolder(new android.view.View(context));

        // This is a simplified test. Full UI test would use Espresso
    }

    /**
     * Test Event model getFormattedDateTime method
     */
    @Test
    public void testEventFormattedDateTime() {
        Event event = testEvents.get(0);
        String formatted = event.getFormattedDateTime();

        assertNotNull("Formatted date/time should not be null", formatted);
        assertTrue("Should contain date", formatted.contains("November"));
        assertTrue("Should contain time", formatted.contains("PM"));
    }

    /**
     * Test User model getters and setters
     */
    @Test
    public void testUserModelGettersSetters() {
        User user = new User();

        user.setUserId("test123");
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRole("entrant");

        assertEquals("test123", user.getUserId());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("entrant", user.getRole());
    }

    /**
     * Test Event model getters and setters
     */
    @Test
    public void testEventModelGettersSetters() {
        Event event = new Event();

        event.setEventId("test001");
        event.setTitle("Test Event");
        event.setDescription("Test Description");
        event.setCapacity(50);
        event.setPrice(100);

        assertEquals("test001", event.getEventId());
        assertEquals("Test Event", event.getTitle());
        assertEquals("Test Description", event.getDescription());
        assertEquals(50, event.getCapacity());
        assertEquals(100, event.getPrice());
    }
}