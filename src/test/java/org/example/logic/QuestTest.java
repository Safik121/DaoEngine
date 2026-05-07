package org.example.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QuestTest {

    @Test
    public void testAddProgressCompletion() {
        Quest quest = new Quest("q1", "Test", "Desc", Quest.ObjectiveType.KILL, "target", 3);
        
        // Add 1 out of 3
        boolean completed = quest.addProgress(1);
        assertFalse(completed);
        assertEquals(1, quest.getCurrentAmount());
        assertFalse(quest.isCompleted());
        
        // Finish it up to 3
        completed = quest.addProgress(2);
        assertTrue(completed);
        assertEquals(3, quest.getCurrentAmount());
        assertTrue(quest.isCompleted());
    }

    @Test
    public void testAddProgressOverflow() {
        Quest quest = new Quest("q1", "Test", "Desc", Quest.ObjectiveType.KILL, "target", 5);
        
        // Add more than needed -> it should stop at max
        quest.addProgress(10);
        assertEquals(5, quest.getCurrentAmount());
        assertTrue(quest.isCompleted());
    }

    @Test
    public void testAddProgressWhenAlreadyCompleted() {
        Quest quest = new Quest("q1", "Test", "Desc", Quest.ObjectiveType.KILL, "target", 1);
        quest.addProgress(1);
        
        // Trying to add to a finished quest
        boolean result = quest.addProgress(1);
        assertFalse(result);
    }
}
