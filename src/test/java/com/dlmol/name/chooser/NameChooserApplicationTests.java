package com.dlmol.name.chooser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NameChooserApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void getEligibleIndices(){
		List<Name> names = new ArrayList<>(3);
		Name name1 = new Name("Adam");
		names.add(name1);
		Name name2 = new Name("Bob");
		name2.incrementRejectCount();
		names.add(name2);
		Name name3 = new Name("Carl");
		names.add(name3);

		IntStream.range(0, 100).forEach(i -> {
			List<Integer> eligibleIndices = NameChooserApplication.getEligibleIndices(names, 2);
			assertFalse("Iteration #" + i + " returned name with index 2 (already rejected)!", eligibleIndices.contains(1));
		});
	}

	@Test
	public void getEligibleIndicesAll0(){
		List<Name> names = new ArrayList<>(3);
		Name name1 = new Name("Adam");
		names.add(name1);
		Name name2 = new Name("Bob");
		names.add(name2);
		Name name3 = new Name("Carl");
		names.add(name3);

		IntStream.range(0, 100).forEach(i -> {
			List<Integer> eligibleIndices = NameChooserApplication.getEligibleIndices(names, 2);
			assertTrue("Expected size 2 but received: " + eligibleIndices.size(), eligibleIndices.size() == 2);
		});
	}
}