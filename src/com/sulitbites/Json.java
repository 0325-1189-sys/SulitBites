package com.sulitbites;

import java.util.*;

/** A tiny JSON reader and writer (no external libraries). Objects become Map, arrays List, numbers Double. */
final class Json {
    private final String s; private int i;
    private Json(String s) { this.s = s; }
    static Object parse(String text) { Json j = new Json(text); Object v = j.value(); j.skip(); if (j.i != text.length()) throw j.fail("Extra data"); return v; }

    private IllegalArgumentException fail(String m) { return new IllegalArgumentException("Bad JSON at " + i + ": " + m); }
    private void skip() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
    private Object value() {
        skip(); if (i >= s.length()) throw fail("Unexpected end");
        char c = s.charAt(i);
        if (c == '{') return object();
        if (c == '[') return array();
        if (c == '"') return string();
        if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
        if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
        if (s.startsWith("null", i)) { i += 4; return null; }
        int st = i; while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
        if (st == i) throw fail("Unexpected character");
        return Double.parseDouble(s.substring(st, i));
    }
    private Map<String, Object> object() {
        Map<String, Object> m = new LinkedHashMap<>(); i++; skip();
        if (s.charAt(i) == '}') { i++; return m; }
        while (true) {
            skip(); String k = string(); skip();
            if (s.charAt(i++) != ':') throw fail("Expected ':'");
            m.put(k, value()); skip(); char c = s.charAt(i++);
            if (c == '}') return m;
            if (c != ',') throw fail("Expected ',' or '}'");
        }
    }
    private List<Object> array() {
        List<Object> l = new ArrayList<>(); i++; skip();
        if (s.charAt(i) == ']') { i++; return l; }
        while (true) {
            l.add(value()); skip(); char c = s.charAt(i++);
            if (c == ']') return l;
            if (c != ',') throw fail("Expected ',' or ']'");
        }
    }
    private String string() {
        if (s.charAt(i) != '"') throw fail("Expected string"); i++;
        StringBuilder b = new StringBuilder();
        while (true) {
            char c = s.charAt(i++);
            if (c == '"') return b.toString();
            if (c != '\\') { b.append(c); continue; }
            char e = s.charAt(i++);
            switch (e) {
                case 'n': b.append('\n'); break; case 't': b.append('\t'); break; case 'r': b.append('\r'); break;
                case 'b': b.append('\b'); break; case 'f': b.append('\f'); break;
                case 'u': b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                default: b.append(e);
            }
        }
    }

    /** Writes JSON with object keys sorted, so equal data always gives equal text. */
    static String write(Object o) { StringBuilder b = new StringBuilder(); put(b, o); return b.toString(); }
    @SuppressWarnings("unchecked")
    private static void put(StringBuilder b, Object o) {
        if (o == null) b.append("null");
        else if (o instanceof Boolean) b.append(o);
        else if (o instanceof Number) { double d = ((Number) o).doubleValue(); b.append(d == Math.rint(d) && Math.abs(d) < 1e15 ? String.valueOf((long) d) : String.valueOf(d)); }
        else if (o instanceof Map) {
            b.append('{'); boolean first = true;
            for (Map.Entry<String, Object> e : new TreeMap<>((Map<String, Object>) o).entrySet()) { if (!first) b.append(','); first = false; quote(b, e.getKey()); b.append(':'); put(b, e.getValue()); }
            b.append('}');
        } else if (o instanceof List) {
            b.append('['); boolean first = true;
            for (Object x : (List<Object>) o) { if (!first) b.append(','); first = false; put(b, x); }
            b.append(']');
        } else quote(b, o.toString());
    }
    private static void quote(StringBuilder b, String s) {
        b.append('"');
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': b.append("\\\""); break; case '\\': b.append("\\\\"); break; case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break; case '\t': b.append("\\t"); break;
                default: if (c < 0x20) b.append(String.format("\\u%04x", (int) c)); else b.append(c);
            }
        }
        b.append('"');
    }
}
