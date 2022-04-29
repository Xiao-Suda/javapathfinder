import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import gov.nasa.jpf.vm.Verify;
import java.lang.Math;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Code by @author Wonsun Ahn
 * 
 * <p>Uses the Java Path Finder model checking tool to check BeanCounterLogic in
 * various modes of operation. It checks BeanCounterLogic in both "luck" and
 * "skill" modes for various numbers of slots and beans. It also goes down all
 * the possible random path taken by the beans during operation.
 */

public class BeanCounterLogicTest {
	private static BeanCounterLogic logic; // The core logic of the program
	private static Bean[] beans; // The beans in the machine
	private static String failString; // A descriptive fail string for assertions

	private static int slotCount; // The number of slots in the machine we want to test
	private static int beanCount; // The number of beans in the machine we want to test
	private static boolean isLuck; // Whether the machine we want to test is in "luck" or "skill" mode

	private int getInFlightBeanCount(BeanCounterLogic logic, int slotCount) {
		int inFlight = 0;
		for (int yPos = 0; yPos < slotCount; yPos++) {
			int xPos = logic.getInFlightBeanXPos(yPos);
			if (xPos != BeanCounterLogic.NO_BEAN_IN_YPOS) {
				inFlight++;
			}
		}
		return inFlight;
	}

	private int getInSlotsBeanCount(BeanCounterLogic logic, int slotCount) {
		int inSlots = 0;
		for (int i = 0; i < slotCount; i++) {
			inSlots += logic.getSlotBeanCount(i);
		}
		return inSlots;
	}

	/**
	 * Sets up the test fixture.
	 */
	@BeforeClass
	public static void setUp() {
		if (Config.getTestType() == TestType.JUNIT) {
			slotCount = 5;
			beanCount = 3;
			isLuck = true;
		} else if (Config.getTestType() == TestType.JPF_ON_JUNIT) {
			/*
			 * TODO: Use the Java Path Finder Verify API to generate choices for slotCount,
			 * beanCount, and isLuck: slotCount should take values 1-5, beanCount should
			 * take values 0-3, and isLucky should be either true or false. For reference on
			 * how to use the Verify API, look at:
			 * https://github.com/javapathfinder/jpf-core/wiki/Verify-API-of-JPF
			 */

			isLuck = Verify.getBoolean();
			beanCount = Verify.getInt(0, 3);
			slotCount = Verify.getInt(1, 5);

		} else {
			assert (false);
		}

		// Create the internal logic
		logic = BeanCounterLogic.createInstance(slotCount);
		// Create the beans
		beans = new Bean[beanCount];
		for (int i = 0; i < beanCount; i++) {
			beans[i] = Bean.createInstance(slotCount, isLuck, new Random(42));
		}

		// A failstring useful to pass to assertions to get a more descriptive error.
		failString = "Failure in (slotCount=" + slotCount
				+ ", beanCount=" + beanCount + ", isLucky=" + isLuck + "):";
	}

	/**
	 * Tears down the test fixture.
	 */
	@AfterClass
	public static void tearDown() {

		logic = null;

	}

	/**
	 * Test case for void reset(Bean[] beans).
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 * Invariants: If beanCount is greater than 0,
	 *             remaining bean count is beanCount - 1
	 *             in-flight bean count is 1 (the bean initially at the top)
	 *             in-slot bean count is 0.
	 *             If beanCount is 0,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is 0.
	 */
	@Test
	public void testReset() {

		logic.reset(beans);

		if (beanCount > 0) {

			assertEquals(failString, beanCount - 1, logic.getRemainingBeanCount());
			assertEquals(failString, 1, getInFlightBeanCount(logic, slotCount));
			assertEquals(failString, 0, getInSlotsBeanCount(logic, slotCount));

		} else if (beanCount == 0) {

			assertEquals(failString, 0, logic.getRemainingBeanCount());
			assertEquals(failString, 0, getInFlightBeanCount(logic, slotCount));
			assertEquals(failString, 0, getInSlotsBeanCount(logic, slotCount));

		}
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After each advanceStep(),
	 *             all positions of in-flight beans are legal positions in the logical coordinate system.
	 */
	@Test
	public void testAdvanceStepCoordinates() {


		logic.reset(beans);
		while (logic.advanceStep()) {

			for (int yPos = 0; yPos < slotCount; yPos++) {
				int xPos = logic.getInFlightBeanXPos(yPos);
				if (xPos != BeanCounterLogic.NO_BEAN_IN_YPOS) {
					assertTrue(xPos < slotCount && xPos > -1);
				}
			}
		}	
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After each advanceStep(),
	 *             the sum of remaining, in-flight, and in-slot beans is equal to beanCount.
	 */
	@Test
	public void testAdvanceStepBeanCount() {
		// TODO: Implement
		logic.reset(beans);
		while (logic.advanceStep()) {
			int sum = 0;
			sum += logic.getRemainingBeanCount();
			sum += getInFlightBeanCount(logic, slotCount);
			sum += getInSlotsBeanCount(logic, slotCount);

			assertEquals(beanCount, sum);
		}
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 */
	@Test
	public void testAdvanceStepPostCondition() {
		// TODO: Implement
		logic.reset(beans);
		while (logic.advanceStep()) {

		}
		assertEquals(failString, 0, logic.getRemainingBeanCount());
		assertEquals(failString, 0, getInFlightBeanCount(logic, slotCount));
		assertEquals("InSLot count:", beanCount, getInSlotsBeanCount(logic, slotCount));
	}
	
	/**
	 * Test case for void lowerHalf()().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.lowerHalf().
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 *             After calling logic.lowerHalf(),
	 *             slots in the machine contain only the lower half of the original beans.
	 *             Remember, if there were an odd number of beans, (N+1)/2 beans should remain.
	 *             Check each slot for the expected number of beans after having called logic.lowerHalf().
	 */
	@Test
	public void testLowerHalf() {
		// TODO: Implement
		logic.reset(beans);

		while (logic.advanceStep()) {

		}

		int half = 0;

		if (beanCount % 2 == 0) { //is even
			half = (beanCount) / 2;
		} else {
			half = (beanCount + 1) / 2;
		}

		assertEquals(0, logic.getRemainingBeanCount());
		assertEquals(0, getInFlightBeanCount(logic, slotCount));
		assertEquals(beanCount, getInSlotsBeanCount(logic, slotCount));

		int[] expected = new int[slotCount];

		int currSum = 0;
		int start = 0;
		boolean emptySlots = false;
		for (int i = 0; i < slotCount; i++) {
			currSum += logic.getSlotBeanCount(i);

			if (currSum < half) {
				expected[i] = logic.getSlotBeanCount(i);
			} else if (currSum == half) {
				expected[i] = logic.getSlotBeanCount(i);
				if (i < slotCount - 1) {
					emptySlots = true;
					start = i + 1;
				}
				break;
			} else if (currSum > half) {
				int remainder = currSum - half;
				expected[i] = logic.getSlotBeanCount(i) - remainder;
				if (i < slotCount - 1) {
					emptySlots = true;
					start = i + 1;
				}
				break;
			}
		}

		if (emptySlots == true) {
			for (int i = start; i < slotCount; i++) {
				expected[i] = 0;
			}
		}

		logic.lowerHalf();


		int[] observed = new int[slotCount];
		for (int i = 0; i < slotCount; i++) {
			observed[i] = logic.getSlotBeanCount(i);
		}
		for (int i = 0; i < slotCount; i++) {
			assertEquals(expected[i], observed[i]);
		}

	}
	
	/**
	 * Test case for void upperHalf().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.upperHalf().
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 *             After calling logic.upperHalf(),
	 *             slots in the machine contain only the upper half of the original beans.
	 *             Remember, if there were an odd number of beans, (N+1)/2 beans should remain.
	 *             Check each slot for the expected number of beans after having called logic.upperHalf().
	 */
	@Test
	public void testUpperHalf() {
		// TODO: Implement
		logic.reset(beans);

		while (logic.advanceStep()) {

		}

		int half = 0;

		if (beanCount % 2 == 0) {
			half = (beanCount) / 2;
		} else {
			half = (beanCount + 1) / 2;
		}

		assertEquals(0, logic.getRemainingBeanCount());
		assertEquals(0, getInFlightBeanCount(logic, slotCount));
		assertEquals(beanCount, getInSlotsBeanCount(logic, slotCount));

		int[] expected = new int[slotCount];
		int currSum = 0;
		int start = 0;
		boolean emptySlots = false;
		for (int i = slotCount - 1; i >= 0; i--) {
			currSum += logic.getSlotBeanCount(i);

			if (currSum < half) {
				expected[i] = logic.getSlotBeanCount(i);
			} else if (currSum == half) {
				expected[i] = logic.getSlotBeanCount(i);
				if (i < slotCount - 1) {
					emptySlots = true;
					start = i - 1;
				}
				break;
			} else if (currSum > half) {
				int remainder = currSum - half;
				expected[i] = logic.getSlotBeanCount(i) - remainder;
				if (i < slotCount - 1) {
					emptySlots = true;
					start = i - 1;
				}
				break;
			}
		}

		if (emptySlots == true) {
			for (int i = start; i >= 0; i--) {
				expected[i] = 0;
			}
		}

		logic.upperHalf();

		int[] observed = new int[slotCount];
		for (int i = 0; i < slotCount; i++) {
			observed[i] = logic.getSlotBeanCount(i);
		}
		for (int i = 0; i < slotCount; i++) {
			assertEquals(expected[i], observed[i]);
		}
	}
	
	/**
	 * Test case for void repeat().
	 * Preconditions: The machine is operating in skill mode.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.repeat();
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: Bean count in each slot is identical after the first run and second run of the machine. 
	 */
	@Test
	public void testRepeat() {

		if (!isLuck) {	
			logic.reset(beans);
			while (logic.advanceStep()) {

			}

			int[] firstTime = new int[slotCount];
			for (int i = 0; i < firstTime.length; i++) {
				firstTime[i] = logic.getSlotBeanCount(i);
			}

			logic.repeat();
			while (logic.advanceStep()) {

			}

			int[] secondTime = new int[slotCount];
			for (int i = 0; i < secondTime.length; i++) {
				secondTime[i] = logic.getSlotBeanCount(i);
			}
			

			for (int i = 0; i < secondTime.length; i++) {
				assertEquals(failString, firstTime[i], secondTime[i]);
			}
		}

		
	}

	@Test
	public void testRemainingBeanCount() {

		int startingCount = beanCount - 1;

		logic.advanceStep();

		assertEquals(failString, startingCount - 2, 
			logic.getRemainingBeanCount()); //test after one advanceStep()
		
		while (logic.advanceStep()) {
			startingCount--;
			assertEquals(failString, startingCount, logic.getRemainingBeanCount());
		}

	}




}
