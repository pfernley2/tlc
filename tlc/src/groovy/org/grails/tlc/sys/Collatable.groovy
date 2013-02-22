package org.grails.tlc.sys

class Collatable implements Comparable {
    def object
    def key

    public Collatable(collator, obj, closure) {
        if (obj != null) {
            object = obj
            key = closure ? closure(obj) : obj.toString()
            if (key != null) key = collator.getCollationKey(key.toString())
        }
    }

    public int compareTo(other) {
        if (key != null && other.key != null) return key.compareTo(other.key)
        if (key == null && other.key == null) return 0
        return (key == null) ? -1 : 1
    }
}
