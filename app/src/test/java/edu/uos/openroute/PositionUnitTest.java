package edu.uos.openroute;

import edu.uos.openroute.util.Position;
import org.junit.Assert;
import org.junit.Test;

public class PositionUnitTest {
    @Test
    public void testFormat() {
        String degreesMinutesSeconds = Position.toDegreeMinutesSeconds(40.34722);
        Assert.assertEquals("40° 20' 50''", degreesMinutesSeconds);
    }
}
