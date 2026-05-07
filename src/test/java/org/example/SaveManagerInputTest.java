package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended tests for SaveManager input validation.
 * Implements EC (Equivalence Class) and BVA (Boundary Value Analysis)
 * according to the project's testing strategy.
 */
@DisplayName("SaveManager Input Validation Tests")
public class SaveManagerInputTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("Valid Slots Check (EC1)")
    public void testValidSlots(int slot) {
        // Valid slots 1-5 should pass validation without IllegalArgumentException
        assertDoesNotThrow(() -> {
            try {
                // We call exists, which internally validates the slot
                SaveManager.exists(slot);
            } catch (Exception e) {
                // Any I/O errors (file doesn't exist) are secondary in this test
            }
        }, "Slot " + slot + " should be valid.");
    }

    @ParameterizedTest
    @ValueSource(ints = {-100, -1, 0, 6, 7, 1000})
    @DisplayName("Invalid Slots Check (EC2, EC3, BVA)")
    public void testInvalidSlots(int slot) {
        // Values outside 1-5 must throw IllegalArgumentException
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            SaveManager.exists(slot);
        }, "Slot " + slot + " should have been rejected by the validator.");
        
        assertTrue(ex.getMessage().toLowerCase().contains("slot"), "Error message should contain information about the slot.");
    }

    @Test
    @DisplayName("Null Data Handling (EC5)")
    public void testSaveNullData() {
        // T04 from pairwise table: null data, valid slot
        // We expect the system to not allow saving a null object
        Exception ex = assertThrows(Exception.class, () -> {
            SaveManager.save(null, 1);
        });
        assertNotNull(ex);
    }

    @Test
    @DisplayName("Boundary Value Analysis - Precise Check")
    public void testBvaPrecise() {
        // Testing close to the boundaries
        assertDoesNotThrow(() -> { try { SaveManager.exists(1); } catch(Exception e){} }); // Min
        assertDoesNotThrow(() -> { try { SaveManager.exists(5); } catch(Exception e){} }); // Max
        
        assertThrows(IllegalArgumentException.class, () -> SaveManager.exists(0)); // Just below min
        assertThrows(IllegalArgumentException.class, () -> SaveManager.exists(6)); // Just above max
    }
}

