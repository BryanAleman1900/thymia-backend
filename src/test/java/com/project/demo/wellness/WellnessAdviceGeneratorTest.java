package com.project.demo.wellness;

import com.project.demo.logic.entity.wellness.WellnessAdviceGenerator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WellnessAdviceGeneratorTest {

    @Test
    void returns_null_when_text_blank() {
        WellnessAdviceGenerator gen = new WellnessAdviceGenerator();
        assertNull(gen.generate(null, " ", null, null));
    }

    @Test
    void returns_advice_for_negative() {
        WellnessAdviceGenerator gen = new WellnessAdviceGenerator();
        var a = gen.generate(null, "me siento mal", "NEGATIVE", 0.9);
        assertNotNull(a);
        assertNotNull(a.title());
        assertNotNull(a.content());
    }
}

