package com.jgross.xbot.eventsystem;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EventSystem {

    public EventSystem() {
    }

    public void callEvent( Event event ) {
        EventList events = event.getEvents();
        RegisteredListener[] listeners = events.getRegisteredListeners();
        for ( RegisteredListener listen : listeners ) {
            try {
                listen.execute( event );
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
    public void registerEvents( Listener l ) {
        for (Map.Entry< Class< ? extends Event >, Set< RegisteredListener > > entry : addMuffins( l ).entrySet()) {
            try {
                getEventListeners( getRegistrationClass( entry.getKey() ) ).registerAll( entry.getValue() );
            } catch ( IllegalAccessException | NullPointerException e ) {
                e.printStackTrace();
            }
        }
    }

    private EventList getEventListeners( Class< ? extends Event > type ) {
        try {
            Method method = getRegistrationClass( type ).getDeclaredMethod( "getEventList" );
            method.setAccessible( true );
            return ( EventList ) method.invoke( null );
        } catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    private Class< ? extends Event > getRegistrationClass( Class< ? extends Event > clazz ) throws IllegalAccessException {
        try {
            clazz.getDeclaredMethod( "getEventList" );
            return clazz;
        } catch ( NoSuchMethodException e ) {
            if ( clazz.getSuperclass() != null && !clazz.getSuperclass().equals( Event.class ) && Event.class.isAssignableFrom( clazz.getSuperclass() ) ) {
                return getRegistrationClass( clazz.getSuperclass().asSubclass( Event.class ) );
            } else {
                throw new IllegalAccessException( "Unable to find event list for event " + clazz.getName() );
            }
        }
    }

    public Map<Class< ? extends Event >, Set< RegisteredListener > > addMuffins( Listener listen ) {
        Map< Class< ? extends Event >, Set< RegisteredListener > > ret = new HashMap< Class< ? extends Event >, Set< RegisteredListener > >();
        Method[] methods;
        try {
            methods = listen.getClass().getDeclaredMethods();
        } catch ( NoClassDefFoundError e ) {
            return null;
        }
        for ( final Method m : methods ) {
            if ( m.getAnnotation(EventHandler.class) == null )
                continue;
            if ( !Event.class.isAssignableFrom( m.getParameterTypes()[0] ) || m.getParameterTypes().length > 1 )
                continue;
            final Class< ? extends Event > eventClass = m.getParameterTypes()[0].asSubclass( Event.class );
            m.setAccessible( true );
            Set<RegisteredListener> events = ret.get( eventClass );
            if ( events == null ) {
                events = new HashSet<>();
                ret.put( eventClass, events );
            }
            Executor exe = (listener, event) -> {
                try {
                    if ( !eventClass.isAssignableFrom( event.getClass() ) )
                        return;
                    m.invoke(listener, event );
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            };
            events.add( new RegisteredListener( listen, exe, m.getAnnotation( EventHandler.class ).priority() ) );
        }
        return ret;
    }
}

