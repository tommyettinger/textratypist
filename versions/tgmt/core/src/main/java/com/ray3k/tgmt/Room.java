package com.ray3k.tgmt;

import com.badlogic.gdx.utils.Array;

public class Room {
    public String name;
    public final Array<Element> elements = new Array<>();
    public final Array<Action> actions = new Array<>();
    
    public static class Element {
        public final Array<Key> requiredKeys = new Array<>();
        public final Array<Key> bannedKeys = new Array<>();
    }
    
    public static class TextElement extends Element {
        public String text;
    
        @Override
        public String toString() {
            return text;
        }
    }
    
    public static class ImageElement extends Element {
        public String image;
    
        @Override
        public String toString() {
            return image;
        }
    }
    
    public static class MusicElement extends Element {
        public String music;
    
        @Override
        public String toString() {
            return music;
        }
    }
    
    public static class SoundElement extends Element {
        public String sound;
    
        @Override
        public String toString() {
            return sound;
        }
    }
    
    public static class Action {
        public String name;
        public String targetRoom;
        public final Array<Key> requiredKeys = new Array<>();
        public final Array<Key> bannedKeys = new Array<>();
        public final Array<Key> giveKeys = new Array<>();
        public final Array<Key> removeKeys = new Array<>();
        public String sound;
        public boolean removeAllKeys;
    }
    
    public static class Key {
        public String name;
    
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Key) return ((Key)obj).name.equals(name);
            return false;
        }
    }
}
