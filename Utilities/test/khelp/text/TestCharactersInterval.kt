package khelp.text

import org.junit.Assert
import org.junit.Test

class TestCharactersInterval
{
    @Test
    fun testEMPTY_CHARACTERS_INTERVAL()
    {
        Assert.assertTrue(EMPTY_CHARACTERS_INTERVAL.empty)
        Assert.assertEquals('B', EMPTY_CHARACTERS_INTERVAL.minimum)
        Assert.assertEquals('A', EMPTY_CHARACTERS_INTERVAL.maximum)
        Assert.assertEquals("[]", EMPTY_CHARACTERS_INTERVAL.toString())
        val interval = EMPTY_CHARACTERS_INTERVAL.toCharactersInterval()
        Assert.assertTrue(interval.empty)
        Assert.assertEquals("[]", interval.toString())
    }

    @Test
    fun testCreateBasicCharactersInterval()
    {
        var interval = createBasicCharactersInterval('Z', 'A')
        Assert.assertTrue(interval.empty)
        Assert.assertTrue(interval === EMPTY_CHARACTERS_INTERVAL)
        Assert.assertEquals("[]", interval.toString())

        interval = createBasicCharactersInterval('A', 'Z')
        Assert.assertFalse(interval.empty)
        Assert.assertTrue('G' in interval)
        Assert.assertFalse('a' in interval)
        Assert.assertEquals("[A, Z]", interval.toString())

        interval = createBasicCharactersInterval('G', 'G')
        Assert.assertFalse(interval.empty)
        Assert.assertTrue('G' in interval)
        Assert.assertFalse('a' in interval)
        Assert.assertEquals("{G}", interval.toString())
    }

    @Test
    fun testEqualsBasicCharactersInterval()
    {
        val interval = createBasicCharactersInterval('A', 'Z')
        var interval2 = createBasicCharactersInterval('A', 'Z')
        Assert.assertTrue(interval == interval2)
        Assert.assertTrue(interval.hashCode() == interval2.hashCode())
        interval2 = createBasicCharactersInterval('A', 'Y')
        Assert.assertFalse(interval == interval2)
        Assert.assertFalse(interval.hashCode() == interval2.hashCode())
    }

    @Test
    fun testIntersectionBasicCharactersInterval()
    {
        val interval = createBasicCharactersInterval('D', 'H')
        var interval2 = createBasicCharactersInterval('A', 'Z')
        Assert.assertTrue(interval.intersects(interval2))
        Assert.assertEquals(interval, interval * interval2)

        interval2 = createBasicCharactersInterval('A', 'F')
        Assert.assertTrue(interval.intersects(interval2))
        Assert.assertEquals(createBasicCharactersInterval('D', 'F'), interval * interval2)

        interval2 = createBasicCharactersInterval('F', 'Z')
        Assert.assertTrue(interval.intersects(interval2))
        Assert.assertEquals(createBasicCharactersInterval('F', 'H'), interval * interval2)

        interval2 = createBasicCharactersInterval('E', 'G')
        Assert.assertTrue(interval.intersects(interval2))
        Assert.assertEquals(interval2, interval * interval2)

        interval2 = createBasicCharactersInterval('A', 'C')
        Assert.assertFalse(interval.intersects(interval2))
        Assert.assertEquals(EMPTY_CHARACTERS_INTERVAL, interval * interval2)

        interval2 = createBasicCharactersInterval('L', 'P')
        Assert.assertFalse(interval.intersects(interval2))
        Assert.assertEquals(EMPTY_CHARACTERS_INTERVAL, interval * interval2)

        Assert.assertFalse(interval.intersects(EMPTY_CHARACTERS_INTERVAL))
        Assert.assertEquals(EMPTY_CHARACTERS_INTERVAL, interval * EMPTY_CHARACTERS_INTERVAL)
    }

    @Test
    fun testUnionBasicCharactersInterval()
    {
        val interval = createBasicCharactersInterval('D', 'H')
        var interval2 = createBasicCharactersInterval('K', 'P')
        var union = interval + interval2
        val collect = ArrayList<BasicCharactersInterval>()
        union.forEach { collect.add(it) }
        Assert.assertEquals(2, collect.size)
        Assert.assertEquals(interval, collect[0])
        Assert.assertEquals(interval2, collect[1])

        interval2 = createBasicCharactersInterval('I', 'P')
        union = interval + interval2
        collect.clear()
        union.forEach { collect.add(it) }
        Assert.assertEquals(1, collect.size)
        Assert.assertEquals(createBasicCharactersInterval('D', 'P'), collect[0])

        interval2 = createBasicCharactersInterval('E', 'P')
        union = interval + interval2
        collect.clear()
        union.forEach { collect.add(it) }
        Assert.assertEquals(1, collect.size)
        Assert.assertEquals(createBasicCharactersInterval('D', 'P'), collect[0])

        interval2 = createBasicCharactersInterval('A', 'P')
        union = interval + interval2
        collect.clear()
        union.forEach { collect.add(it) }
        Assert.assertEquals(1, collect.size)
        Assert.assertEquals(interval2, collect[0])

        interval2 = createBasicCharactersInterval('A', 'F')
        union = interval + interval2
        collect.clear()
        union.forEach { collect.add(it) }
        Assert.assertEquals(1, collect.size)
        Assert.assertEquals(createBasicCharactersInterval('A', 'H'), collect[0])

        interval2 = createBasicCharactersInterval('A', 'C')
        union = interval + interval2
        collect.clear()
        union.forEach { collect.add(it) }
        Assert.assertEquals(1, collect.size)
        Assert.assertEquals(createBasicCharactersInterval('A', 'H'), collect[0])

        interval2 = createBasicCharactersInterval('A', 'B')
        union = interval + interval2
        collect.clear()
        union.forEach { collect.add(it) }
        Assert.assertEquals(2, collect.size)
        Assert.assertEquals(interval2, collect[0])
        Assert.assertEquals(interval, collect[1])
    }

    @Test
    fun testExclusionBasicCharactersInterval()
    {
        val interval = createBasicCharactersInterval('H', 'T')
        var interval2 = createBasicCharactersInterval('W', 'Z')
        var exclude = interval - interval2
        val collect = ArrayList<BasicCharactersInterval>()
        exclude.forEach { collect.add(it) }
        Assert.assertEquals(1, collect.size)
        Assert.assertEquals(interval, collect[0])

        interval2 = createBasicCharactersInterval('R', 'Z')
        exclude = interval - interval2
        collect.clear()
        exclude.forEach { collect.add(it) }
        Assert.assertEquals(1, collect.size)
        Assert.assertEquals(createBasicCharactersInterval('H', 'Q'), collect[0])

        interval2 = createBasicCharactersInterval('A', 'Z')
        exclude = interval - interval2
        collect.clear()
        exclude.forEach { collect.add(it) }
        Assert.assertEquals(0, collect.size)

        interval2 = createBasicCharactersInterval('L', 'N')
        exclude = interval - interval2
        collect.clear()
        exclude.forEach { collect.add(it) }
        Assert.assertEquals(2, collect.size)
        Assert.assertEquals(createBasicCharactersInterval('H', 'K'), collect[0])
        Assert.assertEquals(createBasicCharactersInterval('O', 'T'), collect[1])

        interval2 = createBasicCharactersInterval('A', 'K')
        exclude = interval - interval2
        collect.clear()
        exclude.forEach { collect.add(it) }
        Assert.assertEquals(1, collect.size)
        Assert.assertEquals(createBasicCharactersInterval('L', 'T'), collect[0])

        interval2 = createBasicCharactersInterval('A', 'C')
        exclude = interval - interval2
        collect.clear()
        exclude.forEach { collect.add(it) }
        Assert.assertEquals(1, collect.size)
        Assert.assertEquals(interval, collect[0])
    }

    @Test
    fun testSymmetricDifferenceBasicCharactersInterval()
    {
        val interval = createBasicCharactersInterval('H', 'T')
        var interval2 = createBasicCharactersInterval('W', 'Z')
        var symmetricDifference = interval % interval2
        val collect = ArrayList<BasicCharactersInterval>()
        symmetricDifference.forEach { collect.add(it) }
        Assert.assertEquals(2, collect.size)
        Assert.assertEquals(interval, collect[0])
        Assert.assertEquals(interval2, collect[1])

        interval2 = createBasicCharactersInterval('R', 'Z')
        symmetricDifference = interval % interval2
        collect.clear()
        symmetricDifference.forEach { collect.add(it) }
        Assert.assertEquals(2, collect.size)
        Assert.assertEquals(createBasicCharactersInterval('H', 'Q'), collect[0])
        Assert.assertEquals(createBasicCharactersInterval('U', 'Z'), collect[1])

        interval2 = createBasicCharactersInterval('K', 'Q')
        symmetricDifference = interval % interval2
        collect.clear()
        symmetricDifference.forEach { collect.add(it) }
        Assert.assertEquals(2, collect.size)
        Assert.assertEquals(createBasicCharactersInterval('H', 'J'), collect[0])
        Assert.assertEquals(createBasicCharactersInterval('R', 'T'), collect[1])

        interval2 = createBasicCharactersInterval('A', 'Q')
        symmetricDifference = interval % interval2
        collect.clear()
        symmetricDifference.forEach { collect.add(it) }
        Assert.assertEquals(2, collect.size)
        Assert.assertEquals(createBasicCharactersInterval('A', 'G'), collect[0])
        Assert.assertEquals(createBasicCharactersInterval('R', 'T'), collect[1])

        interval2 = createBasicCharactersInterval('A', 'Z')
        symmetricDifference = interval % interval2
        collect.clear()
        symmetricDifference.forEach { collect.add(it) }
        Assert.assertEquals(2, collect.size)
        Assert.assertEquals(createBasicCharactersInterval('A', 'G'), collect[0])
        Assert.assertEquals(createBasicCharactersInterval('U', 'Z'), collect[1])

        interval2 = createBasicCharactersInterval('A', 'F')
        symmetricDifference = interval % interval2
        collect.clear()
        symmetricDifference.forEach { collect.add(it) }
        Assert.assertEquals(2, collect.size)
        Assert.assertEquals(interval2, collect[0])
        Assert.assertEquals(interval, collect[1])
    }

    @Test
    fun testEqualsCharactersInterval()
    {
        val charactersInterval = CharactersInterval()
        Assert.assertEquals("[]", charactersInterval.toString())
        Assert.assertTrue(charactersInterval.empty)
        Assert.assertTrue(charactersInterval.equals(EMPTY_CHARACTERS_INTERVAL))
        charactersInterval += 'A'
        Assert.assertEquals("{A}", charactersInterval.toString())
        charactersInterval += 'C'
        Assert.assertEquals("{A} U {C}", charactersInterval.toString())
        charactersInterval += 'B'
        Assert.assertEquals("[A, C]", charactersInterval.toString())
        val charactersInterval2 = CharactersInterval()
        charactersInterval2 += createBasicCharactersInterval('A', 'C')
        Assert.assertEquals("[A, C]", charactersInterval2.toString())
        Assert.assertTrue(charactersInterval.equals(charactersInterval2))
    }

    @Test
    fun testCopyCharactersInterval()
    {
        val charactersInterval = CharactersInterval()
        charactersInterval += createBasicCharactersInterval('A', 'D')
        charactersInterval += createBasicCharactersInterval('R', 'U')
        val charactersInterval2 = charactersInterval.copy()
        Assert.assertTrue(charactersInterval.equals(charactersInterval2))
    }

    @Test
    fun testForEachCharactersInterval()
    {
        val charactersInterval = CharactersInterval()
        charactersInterval += createBasicCharactersInterval('A', 'D')
        charactersInterval += createBasicCharactersInterval('Y', 'Z')
        charactersInterval += createBasicCharactersInterval('R', 'U')
        val collect = ArrayList<BasicCharactersInterval>()
        charactersInterval.forEach { collect.add(it) }
        Assert.assertEquals(3, collect.size)
        Assert.assertEquals(createBasicCharactersInterval('A', 'D'), collect[0])
        Assert.assertEquals(createBasicCharactersInterval('R', 'U'), collect[1])
        Assert.assertEquals(createBasicCharactersInterval('Y', 'Z'), collect[2])
    }

    @Test
    fun testAddCharactersInterval()
    {
        val charactersInterval = CharactersInterval()
        charactersInterval += 'A'
        charactersInterval.add('E', 'K')
        charactersInterval += createBasicCharactersInterval('R', 'U')
        val charactersInterval2 = CharactersInterval()
        charactersInterval2 += createBasicCharactersInterval('X', 'Z')
        charactersInterval += charactersInterval2
        charactersInterval += CharactersInterval()
        charactersInterval += EMPTY_CHARACTERS_INTERVAL
        Assert.assertEquals("{A} U [E, K] U [R, U] U [X, Z]", charactersInterval.toString())
        val collect = ArrayList<BasicCharactersInterval>()
        charactersInterval.forEach { collect.add(it) }
        Assert.assertEquals(4, collect.size)
        Assert.assertEquals(createBasicCharactersInterval('A'), collect[0])
        Assert.assertEquals(createBasicCharactersInterval('E', 'K'), collect[1])
        Assert.assertEquals(createBasicCharactersInterval('R', 'U'), collect[2])
        Assert.assertEquals(createBasicCharactersInterval('X', 'Z'), collect[3])

        Assert.assertTrue((charactersInterval + createBasicCharactersInterval('A', 'Z')) ==
                                  createBasicCharactersInterval('A', 'Z').toCharactersInterval())
    }

    @Test
    fun testRemoveCharactersInterval()
    {
        val charactersInterval = CharactersInterval()
        charactersInterval.add('A', 'Z')
        Assert.assertEquals("[A, Z]", charactersInterval.toString())
        charactersInterval.remove('F', 'K')
        Assert.assertEquals("[A, E] U [L, Z]", charactersInterval.toString())
        charactersInterval -= 'C'
        Assert.assertEquals("[A, B] U [D, E] U [L, Z]", charactersInterval.toString())
        charactersInterval -= createBasicCharactersInterval('O', 'Q')
        Assert.assertEquals("[A, B] U [D, E] U [L, N] U [R, Z]", charactersInterval.toString())
        charactersInterval -= 'B'
        Assert.assertEquals("{A} U [D, E] U [L, N] U [R, Z]", charactersInterval.toString())
        charactersInterval -= createBasicCharactersInterval('U', 'W').toCharactersInterval()
        Assert.assertEquals("{A} U [D, E] U [L, N] U [R, T] U [X, Z]", charactersInterval.toString())
    }

    @Test
    fun testContainsCharactersInterval()
    {
        val charactersInterval = CharactersInterval()
        charactersInterval.add('A', 'Z')
        Assert.assertEquals("[A, Z]", charactersInterval.toString())
        charactersInterval.remove('F', 'K')
        Assert.assertEquals("[A, E] U [L, Z]", charactersInterval.toString())
        charactersInterval -= 'C'
        Assert.assertEquals("[A, B] U [D, E] U [L, Z]", charactersInterval.toString())
        charactersInterval -= createBasicCharactersInterval('O', 'Q')
        Assert.assertEquals("[A, B] U [D, E] U [L, N] U [R, Z]", charactersInterval.toString())
        charactersInterval -= 'B'
        Assert.assertEquals("{A} U [D, E] U [L, N] U [R, Z]", charactersInterval.toString())
        charactersInterval -= createBasicCharactersInterval('U', 'W').toCharactersInterval()
        Assert.assertEquals("{A} U [D, E] U [L, N] U [R, T] U [X, Z]", charactersInterval.toString())
        Assert.assertTrue('A' in charactersInterval)
        Assert.assertFalse('B' in charactersInterval)
        Assert.assertTrue('M' in charactersInterval)
        Assert.assertFalse('U' in charactersInterval)
    }

    @Test
    fun testIntersectionCharactersInterval()
    {
        val charactersInterval = CharactersInterval()
        charactersInterval.add('H', 'Q')
        Assert.assertTrue(charactersInterval.intersects(createBasicCharactersInterval('A', 'J')))
        Assert.assertEquals(createBasicCharactersInterval('H', 'J').toCharactersInterval(),
                            charactersInterval * createBasicCharactersInterval('A', 'J'))
        Assert.assertEquals(createBasicCharactersInterval('H', 'J').toCharactersInterval(),
                            charactersInterval * createBasicCharactersInterval('A', 'J').toCharactersInterval())
        Assert.assertFalse(charactersInterval.intersects(createBasicCharactersInterval('A', 'E')))
        Assert.assertEquals(CharactersInterval(),
                            charactersInterval * createBasicCharactersInterval('A', 'E'))
        Assert.assertEquals(CharactersInterval(),
                            charactersInterval * createBasicCharactersInterval('A', 'E').toCharactersInterval())
    }

    @Test
    fun testSymmetricDifference()
    {
        val charactersInterval = CharactersInterval()
        charactersInterval.add('H', 'Q')
        charactersInterval %= createBasicCharactersInterval('A', 'J')
        Assert.assertEquals("[A, G] U [K, Q]", charactersInterval.toString())
    }
}