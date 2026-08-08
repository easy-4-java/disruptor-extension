package com.lmax.disruptor.config;

import com.lmax.disruptor.exception.EventHandleException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IniTests {

    @Test
    void shouldCreateEmptyIni() {
        Ini ini = new Ini();
        assertTrue(ini.isEmpty());
        assertEquals(0, ini.size());
    }

    @Test
    void shouldCreateFromDefaults() {
        Ini defaults = new Ini();
        defaults.setSectionProperty("section1", "key1", "value1");
        Ini copy = new Ini(defaults);
        assertEquals("value1", copy.getSectionProperty("section1", "key1"));
    }

    @Test
    void shouldThrowOnNullDefaults() {
        assertThrows(NullPointerException.class, () -> new Ini(null));
    }

    @Test
    void shouldLoadFromString() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[main]\nkey1=value1\nkey2=value2\n");
        assertEquals("value1", ini.getSectionProperty("main", "key1"));
        assertEquals("value2", ini.getSectionProperty("main", "key2"));
    }

    @Test
    void shouldLoadFromInputStream() throws IOException {
        String content = "[section]\nfoo=bar\n";
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        Ini ini = new Ini();
        ini.load(is);
        assertEquals("bar", ini.getSectionProperty("section", "foo"));
    }

    @Test
    void shouldLoadFromReader() {
        String content = "[sec]\na=b\n";
        Ini ini = new Ini();
        ini.load(new java.io.StringReader(content));
        assertEquals("b", ini.getSectionProperty("sec", "a"));
    }

    @Test
    void shouldLoadFromScanner() {
        Ini ini = new Ini();
        ini.load(new Scanner("[s]\nk=v\n"));
        assertEquals("v", ini.getSectionProperty("s", "k"));
    }

    @Test
    void shouldSkipComments() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("# comment\n; another comment\n[key]\nval=1\n");
        assertEquals("1", ini.getSectionProperty("key", "val"));
    }

    @Test
    void shouldHandleMultipleSections() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[a]\nkey=1\n[b]\nkey=2\n");
        assertEquals("1", ini.getSectionProperty("a", "key"));
        assertEquals("2", ini.getSectionProperty("b", "key"));
    }

    @Test
    void shouldGetSectionNames() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[sec1]\nk=v\n[sec2]\nk=v\n");
        Set<String> names = ini.getSectionNames();
        assertTrue(names.contains("sec1"));
        assertTrue(names.contains("sec2"));
    }

    @Test
    void shouldGetSections() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[sec]\nk=v\n");
        Collection<Ini.Section> sections = ini.getSections();
        assertFalse(sections.isEmpty());
    }

    @Test
    void shouldGetSection() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[mysec]\nk=v\n");
        Ini.Section section = ini.getSection("mysec");
        assertNotNull(section);
        assertEquals("v", section.get("k"));
    }

    @Test
    void shouldReturnNullForMissingSection() {
        Ini ini = new Ini();
        assertNull(ini.getSection("nonexistent"));
    }

    @Test
    void shouldAddSection() {
        Ini ini = new Ini();
        Ini.Section section = ini.addSection("newsec");
        assertNotNull(section);
        assertEquals("newsec", section.getName());
    }

    @Test
    void shouldReturnExistingSectionOnAdd() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[existing]\nk=v\n");
        Ini.Section section = ini.addSection("existing");
        assertNotNull(section);
    }

    @Test
    void shouldRemoveSection() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[toremove]\nk=v\n");
        Ini.Section removed = ini.removeSection("toremove");
        assertNotNull(removed);
        assertNull(ini.getSection("toremove"));
    }

    @Test
    void shouldSetAndGetSectionProperty() {
        Ini ini = new Ini();
        ini.setSectionProperty("sec", "key", "val");
        assertEquals("val", ini.getSectionProperty("sec", "key"));
    }

    @Test
    void shouldReturnNullForMissingProperty() {
        Ini ini = new Ini();
        assertNull(ini.getSectionProperty("sec", "missing"));
    }

    @Test
    void shouldReturnDefaultForMissingProperty() {
        Ini ini = new Ini();
        assertEquals("default", ini.getSectionProperty("sec", "missing", "default"));
    }

    @Test
    void shouldImplementMapMethods() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[s]\nk=v\n");
        assertTrue(ini.containsKey("s"));
        assertFalse(ini.isEmpty());
        assertEquals(1, ini.size());
        assertNotNull(ini.get("s"));
    }

    @Test
    void shouldSupportPut() {
        Ini ini = new Ini();
        Ini.Section section = ini.addSection("test");
        ini.put("test", section);
        assertTrue(ini.containsKey("test"));
    }

    @Test
    void shouldSupportRemove() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[s]\nk=v\n");
        ini.remove("s");
        assertFalse(ini.containsKey("s"));
    }

    @Test
    void shouldSupportClear() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[s]\nk=v\n");
        ini.clear();
        assertEquals(0, ini.size());
    }

    @Test
    void shouldSupportKeySet() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[s]\nk=v\n");
        Set<String> keys = ini.keySet();
        assertTrue(keys.contains("s"));
    }

    @Test
    void shouldSupportValues() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[s]\nk=v\n");
        assertFalse(ini.values().isEmpty());
    }

    @Test
    void shouldSupportEntrySet() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[s]\nk=v\n");
        assertFalse(ini.entrySet().isEmpty());
    }

    @Test
    void shouldImplementEquals() {
        Ini ini1 = new Ini();
        Ini ini2 = new Ini();
        assertEquals(ini1, ini2);
    }

    @Test
    void shouldNotEqualNonIni() {
        Ini ini = new Ini();
        assertFalse(ini.equals("not an ini"));
    }

    @Test
    void shouldImplementHashCode() {
        Ini ini = new Ini();
        assertEquals(ini.hashCode(), new Ini().hashCode());
    }

    @Test
    void shouldImplementToString() {
        Ini ini = new Ini();
        assertNotNull(ini.toString());
    }

    @Test
    void shouldImplementToStringWithSections() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[s]\nk=v\n");
        String str = ini.toString();
        assertNotNull(str);
        assertTrue(str.contains("s"));
    }

    @Test
    void shouldReturnEmptyForNoSections() {
        Ini ini = new Ini();
        assertEquals("<empty INI>", ini.toString());
    }

    // Section tests - use addSection to get Section instances since constructors are private

    @Test
    void shouldCreateSectionViaAddSection() {
        Ini ini = new Ini();
        Ini.Section section = ini.addSection("test");
        assertEquals("test", section.getName());
        assertTrue(section.isEmpty());
    }

    @Test
    void shouldCreateSectionWithContentViaLoad() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[test]\nkey=value\n");
        Ini.Section section = ini.getSection("test");
        assertNotNull(section);
        assertEquals("value", section.get("key"));
    }

    @Test
    void shouldSupportSectionMapOperations() {
        Ini ini = new Ini();
        Ini.Section section = ini.addSection("test");
        section.put("key", "value");
        assertEquals("value", section.get("key"));
        assertEquals(1, section.size());
        assertFalse(section.isEmpty());
        assertTrue(section.containsKey("key"));
        assertTrue(section.containsValue("value"));
    }

    @Test
    void shouldSupportSectionRemove() {
        Ini ini = new Ini();
        Ini.Section section = ini.addSection("test");
        section.put("key", "value");
        section.remove("key");
        assertTrue(section.isEmpty());
    }

    @Test
    void shouldSupportSectionClear() {
        Ini ini = new Ini();
        Ini.Section section = ini.addSection("test");
        section.put("key", "value");
        section.clear();
        assertTrue(section.isEmpty());
    }

    @Test
    void shouldSupportSectionKeySet() {
        Ini ini = new Ini();
        Ini.Section section = ini.addSection("test");
        section.put("k1", "v1");
        section.put("k2", "v2");
        assertEquals(2, section.keySet().size());
    }

    @Test
    void shouldSupportSectionValues() {
        Ini ini = new Ini();
        Ini.Section section = ini.addSection("test");
        section.put("k", "v");
        assertEquals(1, section.values().size());
    }

    @Test
    void shouldSupportSectionEntrySet() {
        Ini ini = new Ini();
        Ini.Section section = ini.addSection("test");
        section.put("k", "v");
        assertEquals(1, section.entrySet().size());
    }

    @Test
    void shouldSupportSectionPutAll() {
        Ini ini = new Ini();
        Ini.Section section = ini.addSection("test");
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        section.putAll(map);
        assertEquals(2, section.size());
    }

    @Test
    void shouldImplementSectionEquals() {
        Ini ini = new Ini();
        Ini.Section s1 = ini.addSection("test");
        Ini ini2 = new Ini();
        Ini.Section s2 = ini2.addSection("test");
        assertEquals(s1, s2);
    }

    @Test
    void shouldNotEqualDifferentSections() {
        Ini ini1 = new Ini();
        Ini.Section s1 = ini1.addSection("a");
        Ini ini2 = new Ini();
        Ini.Section s2 = ini2.addSection("b");
        assertNotEquals(s1, s2);
    }

    @Test
    void shouldImplementSectionHashCode() {
        Ini ini1 = new Ini();
        Ini.Section s1 = ini1.addSection("test");
        Ini ini2 = new Ini();
        Ini.Section s2 = ini2.addSection("test");
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void shouldImplementSectionToString() {
        Ini ini = new Ini();
        Ini.Section section = ini.addSection("test");
        assertEquals("test", section.toString());
    }

    @Test
    void shouldImplementSectionToStringDefault() {
        Ini ini = new Ini();
        Ini.Section section = ini.addSection("");
        assertEquals("<default>", section.toString());
    }

    @Test
    void shouldHandleContinuedLines() {
        assertTrue(Ini.Section.isContinued("line\\"));
        assertFalse(Ini.Section.isContinued("line"));
        assertFalse(Ini.Section.isContinued(null));
    }

    @Test
    void shouldSplitKeyValue() {
        String[] kv = Ini.Section.splitKeyValue("key=value");
        assertNotNull(kv);
        assertEquals("key", kv[0]);
        assertEquals("value", kv[1]);
    }

    @Test
    void shouldSplitKeyValueWithColon() {
        String[] kv = Ini.Section.splitKeyValue("key: value");
        assertNotNull(kv);
        assertEquals("key", kv[0]);
        assertEquals("value", kv[1]);
    }

    @Test
    void shouldThrowOnInvalidKeyValueLine() {
        assertThrows(IllegalArgumentException.class, () -> Ini.Section.splitKeyValue("noequals"));
    }

    @Test
    void shouldReturnNullForBlankKeyValue() {
        assertNull(Ini.Section.splitKeyValue(null));
        assertNull(Ini.Section.splitKeyValue("  "));
    }

    @Test
    void shouldHandleSectionHeaderParsing() {
        assertTrue(Ini.isSectionHeader("[section]"));
        assertFalse(Ini.isSectionHeader("key=value"));
        assertFalse(Ini.isSectionHeader(null));
    }

    @Test
    void shouldGetSectionName() {
        assertEquals("section", Ini.getSectionName("[section]"));
        assertNull(Ini.getSectionName("key=value"));
    }

    @Test
    void shouldSupportContainsValue() throws EventHandleException {
        Ini ini = new Ini();
        ini.load("[s]\nk=v\n");
        assertTrue(ini.containsValue(ini.get("s")));
    }

    @Test
    void shouldSupportPutAll() {
        Ini ini = new Ini();
        Ini ini2 = new Ini();
        ini2.addSection("test");
        ini.putAll(ini2);
        assertTrue(ini.containsKey("test"));
    }
}
